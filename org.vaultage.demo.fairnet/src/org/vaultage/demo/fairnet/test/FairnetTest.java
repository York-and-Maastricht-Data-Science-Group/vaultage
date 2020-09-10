package org.vaultage.demo.fairnet.test;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Scanner;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vaultage.core.Vaultage;
import org.vaultage.core.VaultageServer;
import org.vaultage.demo.fairnet.AddFriendResponseHandler;
import org.vaultage.demo.fairnet.FairnetBroker;
import org.vaultage.demo.fairnet.FairnetVault;
import org.vaultage.demo.fairnet.Friend;
import org.vaultage.demo.fairnet.GetPostResponseHandler;
import org.vaultage.demo.fairnet.GetPostsResponseHandler;
import org.vaultage.demo.fairnet.Post;
import org.vaultage.demo.fairnet.RemoteFairnetVault;
import org.vaultage.util.VaultageEncryption;

public class FairnetTest {

	// change this to a bigger value if your machine if slower than the machine I
	// used for testing
	private static final int SLEEP_TIME = 50;
	private static final String BROKER_ADDRESS = "tcp://localhost:61616";
//	private static final String BROKER_ADDRESS = "tcp://178.79.178.61:61616"; test using Linode
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
	public void testRegistration() throws Exception {
		System.out.println("---" + new Object() {}.getClass().getEnclosingMethod().getName() + "---");
		
		VaultageServer fairnet = new VaultageServer(BROKER_ADDRESS);

		/*** User ***/
		FairnetVault user1 = new FairnetVault();
		user1.setId("bob[at]publickey.net");
		user1.setName("Bob");

		boolean isSuccess = user1.register(fairnet);
		Thread.sleep(SLEEP_TIME);

		assertEquals(true, isSuccess);

		user1.unregister();
	}

//
	@Test
	public void testCreatePost() throws Exception {
		System.out.println("---" + new Object() {}.getClass().getEnclosingMethod().getName() + "---");
		
		FairnetVault user1 = new FairnetVault();
		user1.setId("bob[at]publickey.net");
		user1.setName("Bob");

		Post post1 = user1.createPost("Hello World!", true);
		Post post2 = user1.getPostById(post1.getId());

		assertEquals(post1.getId(), post2.getId());
	}

	@Test
	public void testAddFriendLocally() throws Exception {
		System.out.println("---" + new Object() {}.getClass().getEnclosingMethod().getName() + "---");

		VaultageServer fairnet = new VaultageServer(BROKER_ADDRESS);

		// user 1
		FairnetVault user1 = createVault("bob[at]publickey.net", "Bob", fairnet);
		user1.register(fairnet);
		Thread.sleep(SLEEP_TIME);

		// user 2
		FairnetVault user2 = createVault("alice[at]publickey.com", "Alice", fairnet);
		user2.register(fairnet);
		Thread.sleep(SLEEP_TIME);

		// exchanging public keys
		exchangePublicKeys(user1, user2);

		assertEquals(true, user1.getFriends().stream().anyMatch(f -> f.getPublicKey().equals(user2.getPublicKey())));
		assertEquals(true, user2.getFriends().stream().anyMatch(f -> f.getPublicKey().equals(user1.getPublicKey())));

		user1.unregister();
		user2.unregister();
	}

	@Test
	public void testDoubleEncryption() throws Exception {

		System.out.println("---" + new Object() {}.getClass().getEnclosingMethod().getName() + "---");

		VaultageServer fairnet = new VaultageServer(BROKER_ADDRESS);

		// user 1
		FairnetVault user1 = createVault("bob[at]publickey.net", "Bob", fairnet);
		user1.register(fairnet);
		Thread.sleep(SLEEP_TIME);

		// user 2
		FairnetVault user2 = createVault("alice[at]publickey.com", "Alice", fairnet);
		user2.register(fairnet);
		Thread.sleep(SLEEP_TIME);

		// exchanging public keys
		exchangePublicKeys(user1, user2);

		String message = "ABC";
		String encryptedMessage = VaultageEncryption.doubleEncrypt(message, user1.getPublicKey(),
				user2.getPrivateKey());
		String decryptedMessage = VaultageEncryption.doubleDecrypt(encryptedMessage, user2.getPublicKey(),
				user1.getPrivateKey());
		assertEquals(message, decryptedMessage);

		user1.unregister();
		user2.unregister();
	}

	@Test
	public void testIsFriend() throws Exception {

		System.out.println("---" + new Object() {}.getClass().getEnclosingMethod().getName() + "---");

		VaultageServer fairnet = new VaultageServer(BROKER_ADDRESS);

		// user 1
		FairnetVault user1 = createVault("bob[at]publickey.net", "Bob", fairnet);
		user1.register(fairnet);
		Thread.sleep(SLEEP_TIME);

		// user 2
		FairnetVault user2 = createVault("alice[at]publickey.com", "Alice", fairnet);
		user2.register(fairnet);
		Thread.sleep(SLEEP_TIME);

		// exchanging public keys
		exchangePublicKeys(user1, user2);

		boolean result = user1.isFriend(user2.getPublicKey());
		assertEquals(true, result);

		user1.unregister();
		user2.unregister();
	}

	@Test
	public void testGetMyPosts() throws Exception {

		System.out.println("---" + new Object() {}.getClass().getEnclosingMethod().getName() + "---");

		VaultageServer fairnet = new VaultageServer(BROKER_ADDRESS);

		// user 1
		FairnetVault user1 = createVault("bob[at]publickey.net", "Bob", fairnet);
		user1.register(fairnet);
		Thread.sleep(SLEEP_TIME);

		Post post1 = user1.createPost("Hello world!!!", true);
		Post post2 = user1.createPost("Don't worry, be happy!", true);

		List<Post> posts = user1.getPosts();
		String post1id = posts.stream().filter(p -> p.getId().equals(post1.getId())).findFirst().orElse(null).getId();
		String post2id = posts.stream().filter(p -> p.getId().equals(post2.getId())).findFirst().orElse(null).getId();

//		System.out.println(post1.getId());
		assertEquals(post1.getId(), post1id);
//		System.out.println(post2.getId());
		assertEquals(post2.getId(), post2id);

	}

	@Test
	public void testAddFriend() throws Exception {

		VaultageServer fairnet = new VaultageServer(BROKER_ADDRESS);

		// user 1
		FairnetVault user1 = createVault("bob[at]publickey.net", "Bob", fairnet);
		user1.register(fairnet);
		Thread.sleep(SLEEP_TIME);

		// user 2
		FairnetVault user2 = createVault("alice[at]publickey.com", "Alice", fairnet);
		user2.register(fairnet);
		Thread.sleep(SLEEP_TIME);

		user2.setAddFriendResponseHandler(new UnitTestAddFriendResponseHandler());

		// user 2 adds user 1 as a friend
		RemoteFairnetVault remoteRequester = new RemoteFairnetVault(user2, user1.getPublicKey());

		// simulate add user 2 by user 1
		synchronized (user2.getAddFriendResponseHandler()) {
			remoteRequester.addFriend(user2.getName());
			user2.getAddFriendResponseHandler().wait();
		}

		assertEquals(true, user1.getFriends().stream().anyMatch(f -> f.getPublicKey().equals(user2.getPublicKey())));
		assertEquals(true, user2.getFriends().stream().anyMatch(f -> f.getPublicKey().equals(user1.getPublicKey())));

		user1.unregister();
		user2.unregister();
	}

	@Test
	public void testGetFriendPosts() throws Exception {
		System.out.println("---" + new Object() {}.getClass().getEnclosingMethod().getName() + "---");

		VaultageServer fairnet = new VaultageServer(BROKER_ADDRESS);

		// user 1
		FairnetVault user1 = createVault("bob[at]publickey.net", "Bob", fairnet);
		user1.register(fairnet);
		Thread.sleep(SLEEP_TIME);

		Post post1 = user1.createPost("Hello world!!!", true);
		Post post2 = user1.createPost("Stay at home, protect the NHS!", false);
		Post post3 = user1.createPost("Don't worry, be happy!", true);

		// user 2
		FairnetVault user2 = createVault("alice[at]publickey.com", "Alice", fairnet);
		user2.register(fairnet);
		Thread.sleep(SLEEP_TIME);

		user2.setGetPostsResponseHandler(new UnitTestGetPostsResponseHandler());

		// exchange public keys
		exchangePublicKeys(user1, user2);

		// simulate request user1's post list by user 2
		RemoteFairnetVault remoteRequester = new RemoteFairnetVault(user2, user1.getPublicKey());

		synchronized (user2.getGetPostsResponseHandler()) {
			remoteRequester.getPosts();
			user2.getGetPostsResponseHandler().wait();
		}

		List<String> retrievedUser1posts = ((UnitTestGetPostsResponseHandler) user2.getGetPostsResponseHandler())
				.getResult();

		assertEquals(true, retrievedUser1posts.contains(post1.getId()));
		assertEquals(false, retrievedUser1posts.contains(post2.getId()));
		assertEquals(true, retrievedUser1posts.contains(post3.getId()));

		user1.unregister();
		user2.unregister();
	}

	@Test
	public void testGetMyFriendPost() throws Exception {

		VaultageServer fairnet = new VaultageServer(BROKER_ADDRESS);

		// user 1
		FairnetVault user1 = createVault("bob[at]publickey.net", "Bob", fairnet);
		user1.register(fairnet);
		Thread.sleep(SLEEP_TIME);

		user1.createPost("Hello world!!!", true);
		user1.createPost("Stay at home, protect the NHS!", false);
		user1.createPost("Don't worry, be happy!", true);

		// user 2
		FairnetVault user2 = createVault("alice[at]publickey.com", "Alice", fairnet);
		user2.register(fairnet);
		Thread.sleep(SLEEP_TIME);

		user2.setGetPostResponseHandler(new UnitTestGetPostResponseHandler());
		user2.setGetPostsResponseHandler(new UnitTestGetPostsResponseHandler());

		// exchange public keys
		exchangePublicKeys(user1, user2);

		// simulate request user1's post list by user 2
		RemoteFairnetVault remoteRequester = new RemoteFairnetVault(user2, user1.getPublicKey());

		synchronized (user2.getGetPostsResponseHandler()) {
			remoteRequester.getPosts();
			user2.getGetPostsResponseHandler().wait();
		}

		List<String> retrievedUser1posts = ((UnitTestGetPostsResponseHandler) user2.getGetPostsResponseHandler())
				.getResult();

		// simulate request user1's post contents per post
		for (String postId : retrievedUser1posts) {
			Post post = user1.getPostById(postId);

			synchronized (user2.getGetPostResponseHandler()) {
				remoteRequester.getPost(postId);
				user2.getGetPostResponseHandler().wait();
			}
			Post retrievedUser1post = ((UnitTestGetPostResponseHandler) user2.getGetPostResponseHandler()).getResult();
			assertEquals(post.getContent(), retrievedUser1post.getContent());
		}

		user1.unregister();
		user2.unregister();
	}

//	@Test
//	public void testAsycAddFriend() throws Exception {
//
//		VaultageServer fairnet = new VaultageServer(BROKER_ADDRESS);
//
//		// user 1
//		FairnetVault bob = createVault("bob[at]publickey.net", "Bob", fairnet);
//		bob.setAddFriendRequestBaseHandler(new AddFriendRequestHandler());
//		bob.setAddFriendResponseBaseHandler(new AddFriendResponseHandler());
//
//		// user 2
//		FairnetVault alice = createVault("alice[at]publickey.com", "Alice", fairnet);
//		alice.setAddFriendRequestBaseHandler(new AddFriendRequestHandler());
//		alice.setAddFriendResponseBaseHandler(new AddFriendResponseHandler());
//
//		// user 3
//		FairnetVault charlie = createVault("charlie[at]publickey.io", "Charlie", fairnet);
//		charlie.setAddFriendRequestBaseHandler(new AddFriendRequestHandler());
//		charlie.setAddFriendResponseBaseHandler(new AddFriendResponseHandler());
//		
//		
//		bob.getAddFriendRequestBaseHandler().isImmediatelyResponded(false);
//		charlie.getAddFriendRequestBaseHandler().isImmediatelyResponded(false);
//		
//		alice.register(fairnet);
//
//		bob.register(fairnet);
//		
//		charlie.register(fairnet);
//		
//		Thread.sleep(SLEEP_TIME);
//		
//		bob.unregister();
//		charlie.unregister();
//		Thread.sleep(SLEEP_TIME);
//		
//		alice.getRemoteRequester().requestAddFriend(bob.getPublicKey());
//		alice.getRemoteRequester().requestAddFriend(charlie.getPublicKey());
//				
//		Thread.sleep(SLEEP_TIME);
//		
//		BROKER.stop();
//		BROKER.start(BROKER_ADDRESS);
//		
//		Thread.sleep(SLEEP_TIME * 10);
//		
//		alice.register(fairnet);
//		bob.register(fairnet);
//		charlie.register(fairnet);
//		
//		
//		Thread.sleep(SLEEP_TIME);
//		assertEquals(true, bob.isFriend(alice.getPublicKey()));
//		assertEquals(false, alice.isFriend(bob.getPublicKey()));
//		
//		Thread.sleep(SLEEP_TIME);
//		assertEquals(true, charlie.isFriend(alice.getPublicKey()));
//		assertEquals(false, alice.isFriend(charlie.getPublicKey()));
//
//		
//		alice.unregister();
//		bob.unregister();
//		charlie.unregister();
//	}

	/***
	 * Test get my friend's posts one-by-one using direct messaging
	 * @throws Exception
	 */
	@Test
	public void testGetPostDirectMessage() throws Exception {

		VaultageServer fairnet = new VaultageServer(BROKER_ADDRESS);

		// user 1
		FairnetVault user1 = createVault("bob[at]publickey.net", "Bob", fairnet, "192.168.56.1",
				Vaultage.DEFAULT_SERVER_PORT);
		user1.register(fairnet);
		Thread.sleep(SLEEP_TIME);

		user1.createPost("Hello world!!!", true);
		user1.createPost("Stay at home, protect the NHS!", false);
		user1.createPost("Don't worry, be happy!", true);

		// user 2
		FairnetVault user2 = createVault("alice[at]publickey.com", "Alice", fairnet, "192.168.99.80",
				Vaultage.DEFAULT_SERVER_PORT + 1);
		user2.register(fairnet);
		Thread.sleep(SLEEP_TIME);

		// exchange addresses for direct messaging
		exchangeNetworkAddresses(user1, user2);

		// set handler
		user2.setGetPostResponseHandler(new UnitTestGetPostResponseHandler());
		user2.setGetPostsResponseHandler(new UnitTestGetPostsResponseHandler());

		// exchange public keys
		exchangePublicKeys(user1, user2);
		// user 2 adds user 1 as a friend
		RemoteFairnetVault remoteRequester = new RemoteFairnetVault(user2, user1.getPublicKey());

		synchronized (user2.getGetPostsResponseHandler()) {
			remoteRequester.getPosts();
			user2.getGetPostsResponseHandler().wait();
		}

		List<String> retrievedUser1posts = ((UnitTestGetPostsResponseHandler) user2.getGetPostsResponseHandler())
				.getResult();

		// simulate request user1's post contents per post
		for (String postId : retrievedUser1posts) {
			Post post = user1.getPostById(postId);

			synchronized (user2.getGetPostResponseHandler()) {
				remoteRequester.getPost(postId);
				user2.getGetPostResponseHandler().wait();
			}
			Post retrievedUser1post = ((UnitTestGetPostResponseHandler) user2.getGetPostResponseHandler()).getResult();
			assertEquals(post.getContent(), retrievedUser1post.getContent());
		}

		user1.unregister();
		user2.unregister();

		user1.shutdownServer();
		user2.shutdownServer();
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
	 * Create FairnetVault and set userid, name, direct message server's address and
	 * port
	 * 
	 * @param id
	 * @param name
	 * @param fairnet
	 * @param address
	 * @param port
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws Exception
	 * @throws InterruptedException
	 */
	private FairnetVault createVault(String id, String name, VaultageServer fairnet, String address, int port)
			throws FileNotFoundException, IOException, NoSuchAlgorithmException, Exception, InterruptedException {
		FairnetVault user = new FairnetVault(address, port);
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
