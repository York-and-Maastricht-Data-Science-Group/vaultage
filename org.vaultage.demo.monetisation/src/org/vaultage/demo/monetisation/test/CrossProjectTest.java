package org.vaultage.demo.monetisation.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vaultage.core.Vault;
import org.vaultage.core.VaultageServer;
import org.vaultage.demo.fairnet.AddFriendResponseHandler;
import org.vaultage.demo.fairnet.FairnetBroker;
import org.vaultage.demo.fairnet.FairnetVault;
import org.vaultage.demo.fairnet.GetPostResponseHandler;
import org.vaultage.demo.fairnet.GetPostsResponseHandler;
import org.vaultage.demo.fairnet.Post;
import org.vaultage.demo.fairnet.RemoteFairnetVault;
import org.vaultage.demo.fairnet.test.UnitTestAddFriendResponseHandler;
import org.vaultage.demo.fairnet.test.UnitTestGetPostResponseHandler;
import org.vaultage.demo.fairnet.test.UnitTestGetPostsResponseHandler;
import org.vaultage.demo.monetisation.CityCouncil;

public class CrossProjectTest {

	private static final String BROKER_ADDRESS = "tcp://localhost:61616";
	private static FairnetBroker BROKER = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		BROKER = new FairnetBroker();
		BROKER.start(BROKER_ADDRESS);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		BROKER.stop();
	}

	/***
	 * In this scenario, the City Council retrieves all Bob's public posts. Both
	 * vaults are developed in different projects and are in different packages.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCrossProjectRequest() throws Exception {

		VaultageServer fairnet = new VaultageServer(BROKER_ADDRESS);

		// user 1
		FairnetVault bobsFairnetVault = new FairnetVault();
		bobsFairnetVault.setId("FairnetVault-Bob");
		bobsFairnetVault.setName("Bob");
		bobsFairnetVault.register(fairnet);

		bobsFairnetVault.createPost("Hello world!!!", true);
		bobsFairnetVault.createPost("Stay at home, protect the NHS!", false);
		bobsFairnetVault.createPost("Don't worry, be happy!", true);

		// user 2
		CityCouncil cityCouncil = new CityCouncil();
		cityCouncil.setId("CityCouncil");
		cityCouncil.register(fairnet);

		//create and add handlers
		CityCouncilAddFriendResponseHandler addFriendResponseHandler = new CityCouncilAddFriendResponseHandler();
		CityCouncilGetPostResponseHandler getPostResponseHandler = new CityCouncilGetPostResponseHandler();
		CityCouncilGetPostsResponseHandler getPostsResponseHandler = new CityCouncilGetPostsResponseHandler();
		cityCouncil.addResponseHandler(addFriendResponseHandler);
		cityCouncil.addResponseHandler(getPostResponseHandler);
		cityCouncil.addResponseHandler(getPostsResponseHandler);
	
		// simulate the City Council retrieves Bob's public posts 
		RemoteFairnetVault remoteBobsFairnetVault = new RemoteFairnetVault(cityCouncil,
				bobsFairnetVault.getPublicKey());

		synchronized (cityCouncil.getResponseHandler(AddFriendResponseHandler.class)) {
			remoteBobsFairnetVault.addFriend(cityCouncil.getId());
			cityCouncil.getResponseHandler(AddFriendResponseHandler.class).wait();
		}
		assertEquals(true, addFriendResponseHandler.getResult());
		
		synchronized (cityCouncil.getResponseHandler(GetPostsResponseHandler.class)) {
			remoteBobsFairnetVault.getPosts();
			cityCouncil.getResponseHandler(GetPostsResponseHandler.class).wait();
		}
		List<String> bobsPostIds = getPostsResponseHandler.getPostIds(); 

		// simulate request user1's post contents per post
		for (String postId : bobsPostIds) {
			Post declaredPost = bobsFairnetVault.getPostById(postId);

			synchronized (cityCouncil.getResponseHandler(GetPostResponseHandler.class)) {
				remoteBobsFairnetVault.getPost(postId);
				cityCouncil.getResponseHandler(GetPostResponseHandler.class).wait();
			}
			Post retrievedPost = getPostResponseHandler.getPost();
			assertEquals(declaredPost.getContent(), retrievedPost.getContent());
		}

		bobsFairnetVault.unregister();
		cityCouncil.unregister();

	}

	// define the add friend handler for city council
	class CityCouncilAddFriendResponseHandler implements AddFriendResponseHandler {
		private boolean result;
		@Override
		public void run(Vault localVault, RemoteFairnetVault remoteVault, String responseToken, Boolean result)
				throws Exception {
			this.result = result;
			synchronized (this) {
				this.notify();
			}
		}
		@Override
		public void run(FairnetVault localVault, RemoteFairnetVault remoteVault, String responseToken, Boolean result)
				throws Exception {
		}
		public boolean getResult() {
			return result;
		}
	}
	
	// define the get post handler for city council
	class CityCouncilGetPostResponseHandler implements GetPostResponseHandler {
		private Post post;
		@Override
		public void run(FairnetVault localVault, RemoteFairnetVault remoteVault, String responseToken, Post result)
				throws Exception {
		}
		@Override
		public void run(Vault localVault, RemoteFairnetVault remoteVault, String responseToken, Post result)
				throws Exception {
			post = result;
			synchronized (this) {
				this.notify();
			}
		}
		public Post getPost() {
			return post;
		}
	}

	// define the get post ids handler for city council
	class CityCouncilGetPostsResponseHandler implements GetPostsResponseHandler {
		private List<String>postIds; 
		@Override
		public void run(FairnetVault localVault, RemoteFairnetVault remoteVault, String responseToken,
				List<String> result) throws Exception {
		}
		@Override
		public void run(Vault localVault, RemoteFairnetVault remoteVault, String responseToken, List<String> result)
				throws Exception {
			postIds = result;
			synchronized (this) {
				this.notify();
			}
		}
		public List<String> getPostIds() {
			return postIds;
		}
	}

}
