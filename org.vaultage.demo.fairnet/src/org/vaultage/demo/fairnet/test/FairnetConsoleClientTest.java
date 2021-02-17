package org.vaultage.demo.fairnet.test;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Scanner;

import org.vaultage.core.Vaultage;
import org.vaultage.core.VaultageServer;
import org.vaultage.demo.fairnet.FairnetVault;
import org.vaultage.demo.fairnet.Post;
import org.vaultage.demo.fairnet.RemoteFairnetVault;

/***
 * This class is for testing direct messaging between Vaults. This class should
 * be run as client on a separate computer from the server. The address and port
 * of the client (user2) should be adjusted according to configuration of your
 * network. This class should be run after running the server in
 * FairnetConsoleServerTest.java. Once the server has been started, this class
 * shows the public key of user1. Subsequently, start running the client -- this
 * class -- (user2). It will ask the public key of user1. Copy the public key of
 * user1 to user2 and press enter. User 2 will retrieve all public posts of
 * user1 and print their contents on the console.
 * 
 * @author Alfa Yohannis
 *
 */
public class FairnetConsoleClientTest {

	// define the address of the broker server
	private static final String BROKER_ADDRESS = "tcp://192.168.56.101:61616";

	public static void main(String[] args) throws Exception {

		// define the address of the broker server
		VaultageServer fairnet = new VaultageServer(BROKER_ADDRESS);

		// create user1 with id and name "Bob" with ip configured to the ip the of the
		// computer. The port is set to 50000 + 1, the default port for Vaultage.
		FairnetVault user2 = new FairnetVault("192.168.56.1", Vaultage.DEFAULT_SERVER_PORT + 1);
		user2.setId("Bob");
		user2.setName("Bob");
		//connect user2 to the broker
		boolean success = user2.register(fairnet);
		System.out.println("Connected to " + BROKER_ADDRESS + ": " + success);

		// set handler for adding friend, getting post ids, and getting a post
		user2.addOperationResponseHandler(new UnitTestAddFriendResponseHandler());
		user2.addOperationResponseHandler(new UnitTestGetPostResponseHandler());
		user2.addOperationResponseHandler(new UnitTestGetPostsResponseHandler());

		// add user1's public key
		System.out.print("Input other user's public key: ");
		Scanner scanner = new Scanner(System.in);
		// user1's public should be copied to this prompt when asked
		String user1publicKey = scanner.nextLine();
		scanner.close();

		// add user1's network address to user2's public key to remote address mapping
		// this way, user2 knows the ip and port of user1 based on user1's public key
		user2.getVaultage().getPublicKeyToRemoteAddress().put("",
				new InetSocketAddress("192.168.56.101", Vaultage.DEFAULT_SERVER_PORT));

		// create to a remote requester from user2 to user1
		RemoteFairnetVault remoteRequester = new RemoteFairnetVault(user2, user1publicKey);

		// add user1 as user2's friend
		synchronized (user2.getOperationResponseHandler(UnitTestAddFriendResponseHandler.class)) {
			remoteRequester.addFriend(user2.getName());
			user2.getOperationResponseHandler(UnitTestAddFriendResponseHandler.class).wait();
		}

		// retrieve all post ids of user1's posts
		synchronized (user2.getOperationResponseHandler(UnitTestGetPostsResponseHandler.class)) {
			remoteRequester.getPosts();
			user2.getOperationResponseHandler(UnitTestGetPostsResponseHandler.class).wait();
		}
		List<String> retrievedUser1posts = ((UnitTestGetPostsResponseHandler) user2.getOperationResponseHandler(UnitTestGetPostsResponseHandler.class))
				.getResult();

		// simulate request to user1's post contents for each post
		for (String postId : retrievedUser1posts) {
			synchronized (user2.getOperationResponseHandler(UnitTestGetPostResponseHandler.class)) {
				remoteRequester.getPost(postId);
				user2.getOperationResponseHandler(UnitTestGetPostResponseHandler.class).wait();
			}
			Post retrievedUser1post = ((UnitTestGetPostResponseHandler) user2.getOperationResponseHandler(UnitTestGetPostResponseHandler.class)).getResult();
			System.out.println(retrievedUser1post.getContent());
		}

		// disconnect user2 from the broker 
		user2.unregister();
		/// shutdown direct message server of user2
		user2.shutdownServer();
		
		System.out.println("Finished!");
	}
}
