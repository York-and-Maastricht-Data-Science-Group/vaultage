package org.rdbd.demo.fairnet.test;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.rdbd.demo.fairnet.Fairnet;
import org.rdbd.demo.fairnet.Post;
import org.rdbd.demo.fairnet.User;
import org.rdbd.demo.fairnet.handler.AddFriendConfirmationHandler;
import org.rdbd.demo.fairnet.handler.AddFriendResponseHandler;
import org.rdbd.demo.fairnet.handler.GetPostConfirmationHandler;
import org.rdbd.demo.fairnet.handler.GetPostResponseHandler;
import org.rdbd.demo.fairnet.handler.GetPostsConfirmationHandler;
import org.rdbd.demo.fairnet.handler.GetPostsResponseHandler;

public class FairnetTest {

	// change this to a bigger value if your machine if slower than the machine I
	// used for testing
	private static final int SLEEP_TIME = 40;

	@Test
	public void testRegistration() throws Exception {

		String address = "vm://localhost";
		Fairnet fairnet = new Fairnet(address);

		/*** User ***/
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
		if (!"1".equals(handler.getMessage().getValue())) {
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
	public void testGetMyPost() throws Exception {

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

		List<String> postIds = user2.getGetPostsConfirmationHandler().getPostIds();
		assertEquals(true, postIds.contains(post1.getId()));
		assertEquals(true, postIds.contains(post2.getId()));
		assertEquals(true, postIds.contains(post3.getId()));

		user1.unregister();
		user2.unregister();
		Thread.sleep(SLEEP_TIME);
	}

	@Test
	public void testGetMyFriendPost() throws Exception {

		Fairnet fairnet = new Fairnet("vm://localhost");

		User user1 = new User("bob[at]publickey.net", "user1-private-key");
		user1.setName("Bob");
		user1.createPost("20200421072728514T", "Hello world!!!");
		user1.createPost("20200421072728515F", "Stay at home, protect the NHS!");
		user1.createPost("20200421072728516T", "Don't worry, be happy!");

		user1.setAddFriendResponseHandler(new AddFriendResponseHandler());
		user1.setGetPostsResponseHandler(new GetPostsResponseHandler());
		user1.setGetPostResponseHandler(new GetPostResponseHandler());
		user1.register(fairnet);
		Thread.sleep(SLEEP_TIME);

		User user2 = new User("alice[at]publickey.com", "user2-private-key");
		user2.setName("Alice");
		user2.setAddFriendConfirmationHandler(new AddFriendConfirmationHandler());
		user2.setGetPostsConfirmationHandler(new GetPostsConfirmationHandler());
		user2.setGetPostConfirmationHandler(new GetPostConfirmationHandler());
		user2.register(fairnet);
		Thread.sleep(SLEEP_TIME);

		RemoteUser remoteUser = new RemoteUser(fairnet, user2.getPublicKey(), user2);
		remoteUser.addFriend(user1.getPublicKey());
		Thread.sleep(SLEEP_TIME);

		remoteUser.getPosts(user1);
		Thread.sleep(SLEEP_TIME);

		List<String> postIds = user2.getGetPostsConfirmationHandler().getPostIds();
		for (String postId : postIds) {
			Post user1Post = user1.getPost(postId);
			Post postRemote = remoteUser.getPost(user1, postId);
			Thread.sleep(SLEEP_TIME);
			assertEquals(user1Post.getBody(), postRemote.getBody());
		}

		Thread.sleep(SLEEP_TIME);
		
		user1.unregister();
		user2.unregister();
		Thread.sleep(SLEEP_TIME);
	}
}
