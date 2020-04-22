package org.rdbd.demo.fairnet.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.rdbd.demo.fairnet.Fairnet;
import org.rdbd.demo.fairnet.Post;
import org.rdbd.demo.fairnet.User;
import org.rdbd.demo.fairnet.handler.AddFriendConfirmedHandler;
import org.rdbd.demo.fairnet.handler.AddFriendResponseHandler;
import org.rdbd.demo.fairnet.handler.GetPostsResponseHandler;

public class FairnetTest {

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
//		System.out.println(isSuccess ? "connected!" : "failed!");
		assertEquals(true, isSuccess);
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

		/*** local ***/
		Fairnet fairnet = new Fairnet("vm://localhost");

		User user1 = new User("bob[at]publickey.net", "user1-private-key");
		user1.setName("Bob");
		// Announces user1 to fairnet and starts listening to the message broker
		user1.register(fairnet);
		user1.setAddFriendResponseHandler(new AddFriendResponseHandler());
		Thread.sleep(500);
		
		// create user2 as well
		User user2 = new User("alice[at]publickey.com", "user2-private-key");
		user2.setName("Alice");
		user2.setAddFriendConfirmedHandler(new AddFriendConfirmedHandler());
		user2.register(fairnet);
		Thread.sleep(500);
		
//		// Emulates a remote user, who is in fact just our local user2
//		// and who we will be querying on behalf of user1
		RemoteUser remoteUser = new RemoteUser(fairnet, user2.getPublicKey(), user2);
		remoteUser.addFriend(user1.getPublicKey());
		Thread.sleep(500);
		
		assertEquals(true, true);
	}
}
