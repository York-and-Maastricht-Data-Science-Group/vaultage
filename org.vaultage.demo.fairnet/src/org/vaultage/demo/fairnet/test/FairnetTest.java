package org.vaultage.demo.fairnet.test;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.junit.Test;
import org.vaultage.core.VaultageServer;
import org.vaultage.demo.fairnet.AddFriendRequestHandler;
import org.vaultage.demo.fairnet.AddFriendResponseHandler;
import org.vaultage.demo.fairnet.FairnetVault;
import org.vaultage.demo.fairnet.GetPostRequestHandler;
import org.vaultage.demo.fairnet.GetPostResponseHandler;
import org.vaultage.demo.fairnet.GetPostsRequestHandler;
import org.vaultage.demo.fairnet.GetPostsResponseHandler;
import org.vaultage.demo.fairnet.Friend;
import org.vaultage.demo.fairnet.Post;
import org.vaultage.demo.fairnet.RemoteRequester;
import org.vaultage.util.VaultageEncryption;

public class FairnetTest {

	// change this to a bigger value if your machine if slower than the machine I
	// used for testing
	private static final int SLEEP_TIME = 50;

	@Test
	public void testRegistration() throws Exception {

		String address = "vm://localhost";
		VaultageServer fairnet = new VaultageServer(address);

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
		FairnetVault user1 = new FairnetVault();
		user1.setId("bob[at]publickey.net");
		user1.setName("Bob");

		Post post1 = user1.createPost("Hello World!", true);
		Post post2 = user1.getPostById(post1.getId());

		assertEquals(post1.getId(), post2.getId());
	}

	@Test
	public void testAddFriendLocally() throws Exception {

		System.out.println("\n---TestAddFriend---");

		VaultageServer fairnet = new VaultageServer("vm://localhost");

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

		System.out.println("\n---Test Double Encryption---");

		VaultageServer fairnet = new VaultageServer("vm://localhost");

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
	}

	@Test
	public void testIsFriend() throws Exception {

		System.out.println("\n---TestIsFriend--");

		VaultageServer fairnet = new VaultageServer("vm://localhost");

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

		System.out.println("\n---test Get My Posts--");

		VaultageServer fairnet = new VaultageServer("vm://localhost");

		// user 1
		FairnetVault user1 = createVault("bob[at]publickey.net", "Bob", fairnet);
		user1.register(fairnet);
		Thread.sleep(SLEEP_TIME);

		Post post1 = user1.createPost("Hello world!!!", true);
		Post post2 = user1.createPost("Don't worry, be happy!", true);

		List<Post> posts = user1.getPosts();
		String post1id = posts.stream().filter(p -> p.getId().equals(post1.getId())).findFirst().orElse(null).getId();
		String post2id = posts.stream().filter(p -> p.getId().equals(post2.getId())).findFirst().orElse(null).getId();

		System.out.println(post1.getId());
		assertEquals(post1.getId(), post1id);
		System.out.println(post2.getId());
		assertEquals(post2.getId(), post2id);

	}

	@Test
	public void testAddFriend() throws Exception {
 
		VaultageServer fairnet = new VaultageServer("vm://localhost");

		// user 1
		FairnetVault user1 = createVault("bob[at]publickey.net", "Bob", fairnet);
		user1.register(fairnet);
		Thread.sleep(SLEEP_TIME);
		
		user1.setAddFriendRequestBaseHandler(new AddFriendRequestHandler());

		// user 2
		FairnetVault user2 = createVault("alice[at]publickey.com", "Alice", fairnet);
		user2.register(fairnet);
		Thread.sleep(SLEEP_TIME);

		user2.setAddFriendResponseBaseHandler(new AddFriendResponseHandler());

		//add friend using remote requester
		RemoteRequester remoteRequester = new RemoteRequester(fairnet, user2);

		// simulate request user1's post list by user 2
		remoteRequester.addFriend(user1.getPublicKey());
		
		assertEquals(true, user1.getFriends().stream().anyMatch(f -> f.getPublicKey().equals(user2.getPublicKey())));
		assertEquals(true, user2.getFriends().stream().anyMatch(f -> f.getPublicKey().equals(user1.getPublicKey())));

		user1.unregister();
		user2.unregister();
	}
	
	@Test
	public void testGetFriendPosts() throws Exception {
 
		VaultageServer fairnet = new VaultageServer("vm://localhost");

		// user 1
		FairnetVault user1 = createVault("bob[at]publickey.net", "Bob", fairnet);
		user1.register(fairnet);
		Thread.sleep(SLEEP_TIME);

		Post post1 = user1.createPost("Hello world!!!", true);
		Post post2 = user1.createPost("Stay at home, protect the NHS!", false);
		Post post3 = user1.createPost("Don't worry, be happy!", true);

		user1.setGetPostsRequestBaseHandler(new GetPostsRequestHandler());

		// user 2
		FairnetVault user2 = createVault("alice[at]publickey.com", "Alice", fairnet);
		user2.register(fairnet);
		Thread.sleep(SLEEP_TIME);

		user2.setGetPostsResponseBaseHandler(new GetPostsResponseHandler());

		// exchange public keys
		exchangePublicKeys(user1, user2);

		// simulate request user1's post list by user 2
		RemoteRequester remoteRequester = new RemoteRequester(fairnet, user2);

		List<String> user1posts = remoteRequester.getPosts(user1.getPublicKey());

		assertEquals(true, user1posts.contains(post1.getId()));
		assertEquals(false, user1posts.contains(post2.getId()));
		assertEquals(true, user1posts.contains(post3.getId()));

		user1.unregister();
		user2.unregister();
	}

	@Test
	public void testGetMyFriendPost() throws Exception {

		VaultageServer fairnet = new VaultageServer("vm://localhost");

		// user 1
		FairnetVault user1 = createVault("bob[at]publickey.net", "Bob", fairnet);
		user1.register(fairnet);
		Thread.sleep(SLEEP_TIME);

		user1.createPost("Hello world!!!", true);
		user1.createPost("Stay at home, protect the NHS!", false);
		user1.createPost("Don't worry, be happy!", true);

		user1.setGetPostsRequestBaseHandler(new GetPostsRequestHandler());
		user1.setGetPostRequestBaseHandler(new GetPostRequestHandler());

		// user 2
		FairnetVault user2 = createVault("alice[at]publickey.com", "Alice", fairnet);
		user2.register(fairnet);
		Thread.sleep(SLEEP_TIME);

		user2.setGetPostsResponseBaseHandler(new GetPostsResponseHandler());
		user2.setGetPostResponseBaseHandler(new GetPostResponseHandler());

		// exchange public keys
		exchangePublicKeys(user1, user2);

		// simulate request user1's post list by user 2
		RemoteRequester remoteRequester = new RemoteRequester(fairnet, user2);

		List<String> user1posts = remoteRequester.getPosts(user1.getPublicKey());

		// simulate request user1's post contents per post
		for (String postId : user1posts) {
			Post user1Post = user1.getPostById(postId);
			Post postRemote = remoteRequester.getPost(user1.getPublicKey(), postId); //
			assertEquals(user1Post.getContent(), postRemote.getContent());
		}

		user1.unregister();
		user2.unregister();
	}

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
