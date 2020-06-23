package org.vaultage.demo.fairnet.test;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
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

		System.out.println("\n---Test Double Encryption---");

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

		System.out.println("\n---TestIsFriend--");

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

		System.out.println("\n---test Get My Posts--");

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

		System.out.println(post1.getId());
		assertEquals(post1.getId(), post1id);
		System.out.println(post2.getId());
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
			remoteRequester.addFriend();
			user2.getAddFriendResponseHandler().wait();
		}
		
		assertEquals(true, user1.getFriends().stream().anyMatch(f -> f.getPublicKey().equals(user2.getPublicKey())));
		assertEquals(true, user2.getFriends().stream().anyMatch(f -> f.getPublicKey().equals(user1.getPublicKey())));

		user1.unregister();
		user2.unregister();
	}

	@Test
	public void testGetFriendPosts() throws Exception {

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
		
		List<String> retrievedUser1posts = ((UnitTestGetPostsResponseHandler) user2.getGetPostsResponseHandler()).getResult(); 

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
		
		List<String> retrievedUser1posts = ((UnitTestGetPostsResponseHandler) user2.getGetPostsResponseHandler()).getResult();
		
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
