package org.vaultage.demo.fairnet.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vaultage.core.Vault;
import org.vaultage.core.VaultageServer;
import org.vaultage.demo.fairnet.FairnetBroker;
import org.vaultage.demo.fairnet.FairnetVault;
import org.vaultage.demo.fairnet.Friend;
import org.vaultage.demo.fairnet.Post;
import org.vaultage.demo.fairnet.RemoteFairnetVault;
import org.vaultage.demo.fairnet.StreamFileResponseHandler;

public class StreamingTest {

	private static final String BROKER_ADDRESS = "tcp://localhost:61616";
//	private static final String BROKER_ADDRESS = "tcp://139.162.228.32:61616"; ///// test using Linode
	private static FairnetBroker BROKER = null;

	@BeforeClass
	public static void startBroker() throws Exception {
		BROKER = new FairnetBroker();
		BROKER.start(BROKER_ADDRESS);
	}

	@AfterClass
	public static void stopBroker() throws Exception {
		BROKER.stop();
	}

	@Test
	public void testStreamFile() throws Exception {

		VaultageServer fairnet = new VaultageServer(BROKER_ADDRESS);

		// user 1
		FairnetVault bob = createVault("bob[at]publickey.net", "Bob", fairnet);
		bob.register(fairnet);

		bob.createPost("Hello world!!!", true);
		bob.createPost("Stay at home, protect the NHS!", false);
		bob.createPost("Don't worry, be happy!", true);
		bob.createPost("data.txt", true);

		// user 2
		FairnetVault alice = createVault("alice[at]publickey.com", "Alice", fairnet);
		alice.register(fairnet);

		alice.addOperationResponseHandler(new UnitTestGetPostResponseHandler());
		alice.addOperationResponseHandler(new UnitTestGetPostsResponseHandler());
		alice.addOperationResponseHandler(new StreamFileResponseHandler() {
			@Override
			public void run(Vault localVault, RemoteFairnetVault remoteVault, String responseToken,
					File result) throws Exception {
				System.out.println("BBBB");
			}
			@Override
			public void run(FairnetVault localVault, RemoteFairnetVault remoteVault, String responseToken, File result)
					throws Exception {
				System.out.println("CCCC");
			}
		});

		// exchange public keys
		exchangePublicKeys(bob, alice);

		// simulate request user1's post list by user 2
		RemoteFairnetVault bobRemoteRequester = new RemoteFairnetVault(alice, bob.getPublicKey());

		synchronized (alice.getOperationResponseHandler(UnitTestGetPostsResponseHandler.class)) {
			bobRemoteRequester.getPosts();
			alice.getOperationResponseHandler(UnitTestGetPostsResponseHandler.class).wait();
		}

		List<String> retrievedUser1posts = ((UnitTestGetPostsResponseHandler) alice
				.getOperationResponseHandler(UnitTestGetPostsResponseHandler.class)).getResult();

		// get post number 4 (or index 3 in the list since one post is not public) to get the audio id
		String postId = retrievedUser1posts.get(2);
		Post initialPost = bob.getPostById(postId);

		synchronized (alice.getOperationResponseHandler(UnitTestGetPostResponseHandler.class)) {
			bobRemoteRequester.getPost(postId);
			alice.getOperationResponseHandler(UnitTestGetPostResponseHandler.class).wait();
		}
		Post retrievedPost = ((UnitTestGetPostResponseHandler) alice
				.getOperationResponseHandler(UnitTestGetPostResponseHandler.class)).getResult();
		assertEquals(initialPost.getContent(), retrievedPost.getContent());
		
		// get the audio id and request the remote vault to stream the audio
		String audioId = retrievedPost.getContent();
		
		alice.downloadFile(bob, audioId);

		bob.unregister();
		alice.unregister();
	}

	/***
	 * Simulate exchanging network addresses, ips and ports, between user1 and
	 * user2. Public keys are used as the keys to retrieve the addresses.
	 * 
	 * @param user1
	 * @param user2
	 */
	protected void exchangeNetworkAddresses(FairnetVault user1, FairnetVault user2) {
		String user1publicKey = user1.getPublicKey();
		InetSocketAddress user1address = user1.getVaultage().getDirectMessageServerAddress();

		String user2publicKey = user2.getPublicKey();
		InetSocketAddress user2address = user2.getVaultage().getDirectMessageServerAddress();

		user1.getVaultage().getPublicKeyToRemoteAddress().put(user2publicKey, user2address);
		user2.getVaultage().getPublicKeyToRemoteAddress().put(user1publicKey, user1address);
	}

	/***
	 * Create FairnetVault and set userid and name
	 * 
	 * @param id
	 * @param name
	 * @param fairnet
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws Exception
	 * @throws InterruptedException
	 */
	private FairnetVault createVault(String id, String name, VaultageServer fairnet)
			throws FileNotFoundException, IOException, NoSuchAlgorithmException, Exception, InterruptedException {
		FairnetVault user = new FairnetVault();
		user.setId(id);
		user.setName(name);
		return user;
	}

	/***
	 * Simulate exchanging public keys between users. In the real world, this
	 * exchange could be done manually via emails or messengers.
	 * 
	 * @param user1
	 * @param user2
	 */
	private void exchangePublicKeys(FairnetVault user1, FairnetVault user2) {
		// add user 2 as a friend to user 1's vault
		Friend user1friend = new Friend();
		user1friend.setName(user2.getName());
		user1friend.setPublicKey(user2.getPublicKey());
		user1.getFriends().add(user1friend);

		// add user 2 as a friend to user 1's vault
		Friend user2friend = new Friend();
		user2friend.setName(user1.getName());
		user2friend.setPublicKey(user1.getPublicKey());
		user2.getFriends().add(user2friend);
	}
}
