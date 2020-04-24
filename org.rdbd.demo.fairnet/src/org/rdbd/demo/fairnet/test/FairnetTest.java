package org.rdbd.demo.fairnet.test;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.rdbd.demo.fairnet.Fairnet;
import org.rdbd.demo.fairnet.Post;
import org.rdbd.demo.fairnet.User;
import org.rdbd.demo.fairnet.handler.AddFriendConfirmationHandler;
import org.rdbd.demo.fairnet.handler.AddFriendResponseHandler;
import org.rdbd.demo.fairnet.handler.GetPostsConfirmationHandler;
import org.rdbd.demo.fairnet.handler.GetPostsResponseHandler;


public class FairnetTest {

	// change this to a bigger value if your machine if slower than the machine I used for testing
	private static final int SLEEP_TIME = 30;

	@Test
	public void testRegistration() throws Exception {

		String address = "vm://localhost";
		Fairnet fairnet = new Fairnet(address);

		/*** User ***/

		// user1.db is a file/database containing all the user's friends and posts
		// as well as the user's name (as per the model above), id (public key)
		// and private key (which are not modelled as they are common in all
		// applications).
		// It can be an XMI document, a change-based (CBP) document etc.
		User user1 = new User("bob[at]publickey.net", "user1-private-key");
		user1.setName("Foo");

		boolean isSuccess = user1.register(fairnet);
		Thread.sleep(SLEEP_TIME);
		
//		System.out.println(isSuccess ? "connected!" : "failed!");
		assertEquals(true, isSuccess);
		
		user1.unregister();
		Thread.sleep(SLEEP_TIME);
	}

	@Test
	public void testCreatePost() throws Exception {
		User user1 = new User("bob[at]publickey.net", "user1-private-key");
		user1.setName("Foo");
		Post post1 = user1.createPost("20200421072728514T", "Hello world!");
		Post post2 = user1.getPost(post1.getId());

		assertEquals(post1.getId(), post2.getId());
	}

	@Test
	public void testAddFriend() throws Exception {
		
		System.out.println("\n---TestAddFriend---");
		
		Fairnet fairnet = new Fairnet("vm://localhost");

		User user1 = new User("bob[at]publickey.net", "user1-private-key");
		user1.setName("Bob");
		AddFriendResponseHandler h = new AddFriendResponseHandler();
		h.setName("testAddFriend-AddFriend");
		user1.setAddFriendResponseHandler(h);
		user1.register(fairnet);
		Thread.sleep(SLEEP_TIME);
		
		User user2 = new User("alice[at]publickey.com", "user2-private-key");
		user2.setName("Alice");
		AddFriendConfirmationHandler handler = new AddFriendConfirmationHandler();
		user2.setAddFriendConfirmationHandler(handler);
		user2.register(fairnet);
		Thread.sleep(SLEEP_TIME);
		
		RemoteUser remoteUser = new RemoteUser(fairnet, user2.getPublicKey(), user2);
		remoteUser.addFriend(user1.getPublicKey());
		Thread.sleep(SLEEP_TIME);
		
		boolean result = true;
		if (!"1".equals(handler.getMessage().getValue())){
			result = false;
		}
		assertEquals(true, result);
		
		user1.unregister();
		user2.unregister();
		Thread.sleep(SLEEP_TIME);
	}
	
	@Test
	public void testIsFriend() throws Exception {
		
		System.out.println("\n---TestIsFriend--");
		
		Fairnet fairnet = new Fairnet("vm://localhost");

		User user1 = new User("bob[at]publickey.net", "user1-private-key");
		user1.setName("Bob");
		AddFriendResponseHandler h = new AddFriendResponseHandler();
		h.setName("testIsFriend-AddFriend");
		user1.setAddFriendResponseHandler(h);
		user1.register(fairnet);
		Thread.sleep(SLEEP_TIME);
		
		User user2 = new User("alice[at]publickey.com", "user2-private-key");
		user2.setName("Alice");
		AddFriendConfirmationHandler handler = new AddFriendConfirmationHandler();
		user2.setAddFriendConfirmationHandler(handler);
		user2.register(fairnet);
		Thread.sleep(SLEEP_TIME);
		
		RemoteUser remoteUser = new RemoteUser(fairnet, user2.getPublicKey(), user2);
		remoteUser.addFriend(user1.getPublicKey());
		Thread.sleep(SLEEP_TIME);
	
		boolean result = user1.isFriend(user2);
		assertEquals(true, result);
		
		user1.unregister();
		user2.unregister();
		Thread.sleep(SLEEP_TIME);
	}
	
	@Test
	public void testGetMyPosts() throws Exception {

		/*** local ***/
		Fairnet fairnet = new Fairnet("vm://localhost");

		User user1 = new User("bob[at]publickey.net", "user1-private-key");
		user1.setName("Bob");
		
		Post post1 = user1.createPost("20200421072728514T", "Hello world!!!");
		Post post2 = user1.createPost("20200421072728515T", "Don't worry, be happy!");
		
		List<String> posts = user1.getPosts();
		String post1id = posts.stream().filter(p -> p.equals(post1.getId())).findFirst().orElse("");
		String post2id = posts.stream().filter(p -> p.equals(post2.getId())).findFirst().orElse("");
				
		assertEquals(post1.getId(), post1id);
		assertEquals(post2.getId(), post2id);
	}
	
	@Test
	public void testGetFriendPosts() throws Exception {

		Fairnet fairnet = new Fairnet("vm://localhost");

		User user1 = new User("bob[at]publickey.net", "user1-private-key");
		user1.setName("Bob");
		Post post1 = user1.createPost("20200421072728514T", "Hello world!!!");
		Post post2 = user1.createPost("20200421072728515F", "Stay at home, protect the NHS!");
		Post post3 = user1.createPost("20200421072728516T", "Don't worry, be happy!");
		
		user1.setAddFriendResponseHandler(new AddFriendResponseHandler());
		user1.setGetPostsResponseHandler(new GetPostsResponseHandler());
		user1.register(fairnet);
		Thread.sleep(SLEEP_TIME);
		
		User user2 = new User("alice[at]publickey.com", "user2-private-key");
		user2.setName("Alice");
		user2.setAddFriendConfirmationHandler(new AddFriendConfirmationHandler());
		user2.setGetPostsConfirmationHandler(new GetPostsConfirmationHandler());
		user2.register(fairnet);
		Thread.sleep(SLEEP_TIME);
		
		RemoteUser remoteUser = new RemoteUser(fairnet, user2.getPublicKey(), user2);
		remoteUser.addFriend(user1.getPublicKey());
		Thread.sleep(SLEEP_TIME);
		
		remoteUser.getPosts(user1);
		Thread.sleep(SLEEP_TIME);
		
		
		assertEquals(true, true);
		
		user1.unregister();
		user2.unregister();
		Thread.sleep(SLEEP_TIME);
	}

	
//	@Test
//	public void testGetMyFriendPost() throws Exception {
//
//		/*** local ***/
//		Fairnet fairnet = new Fairnet("vm://localhost");
//
//		User user1 = new User("bob[at]publickey.net", "user1-private-key");
//		user1.setName("Bob");
//		Post post1 = user1.createPost("20200421072728514T", "Hello world!");
//		user1.register(fairnet);
//		user1.setAddFriendResponseHandler(new AddFriendResponseHandler());
//		Thread.sleep(SLEEP_TIME);
//		
//		// create user2 as well
//		User user2 = new User("alice[at]publickey.com", "user2-private-key");
//		user2.setName("Alice");
//		AddFriendConfirmationHandler handler = new AddFriendConfirmationHandler();
//		user2.setAddFriendConfirmationHandler(handler);
//		user2.register(fairnet);
//		Thread.sleep(SLEEP_TIME);
//		
////		// Emulates a remote user, who is in fact just our local user2
////		// and who we will be querying on behalf of user1
//		RemoteUser remoteUser = new RemoteUser(fairnet, user2.getPublicKey(), user2);
//		remoteUser.addFriend(user1.getPublicKey());
//		Thread.sleep(SLEEP_TIME);
//		
//		Post user1Post = remoteUser.getPost(user1, post1.getId());
//		Thread.sleep(SLEEP_TIME);
//		
//				
//		assertEquals(post1.getId(), user1Post);
//		
//		user1.unregister();
//		user2.unregister();
//		Thread.sleep(SLEEP_TIME);
//	}
}
