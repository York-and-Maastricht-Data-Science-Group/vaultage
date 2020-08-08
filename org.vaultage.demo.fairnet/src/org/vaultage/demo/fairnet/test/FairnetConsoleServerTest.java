package org.vaultage.demo.fairnet.test;

import org.vaultage.core.Vaultage;
import org.vaultage.core.VaultageServer;
import org.vaultage.demo.fairnet.FairnetBroker;
import org.vaultage.demo.fairnet.FairnetVault;

/***
 * This class is for testing direct messaging between Vaults. This class should
 * be run as server on a separate computer from the client. The addresses and
 * ports of the server (user1) and the broker should be adjusted according to
 * configuration of your network. This class should be run first before running
 * the client in FairnetConsoleClientTest.java. Once it has been started, this
 * class -- the server -- shows the public key of user1. Subsequently, start
 * running the client (user2). It will ask the public key of user1. Copy the
 * public key of user1 to user2 and press enter. User 2 will retrieve all public
 * posts of user1 and print their contents on the console.
 * 
 * @author Alfa Yohannis
 *
 */
public class FairnetConsoleServerTest {

	// define the address and the port of the broker server (Apache ActiveMQ)
	private static final String BROKER_ADDRESS = "tcp://localhost:61616";
	private static FairnetBroker BROKER = null;

	public static void main(String[] args) throws Exception {

		BROKER = new FairnetBroker();
		// start the broker server
		BROKER.start(BROKER_ADDRESS);

		// define the address of the broker server
		VaultageServer fairnet = new VaultageServer(BROKER_ADDRESS);

		// create user1 with id and name "Alice" with ip configured to the ip the of the
		// computer. The port is set to 50000, the default port for Vaultage.
		FairnetVault user1 = new FairnetVault("192.168.56.101", Vaultage.DEFAULT_SERVER_PORT);
		user1.setId("Alice");
		user1.setName("Alice");
		// connect user1 to the broker
		user1.register(fairnet);

		// display user1's public key
		System.out.println("Public Key:");
		// copy the public key to user2 when asked through command prompt
		System.out.println(user1.getPublicKey());

		// create the posts of user1
		// one post is set to private, so it will not be transferred to user2
		user1.createPost("Hello world!!!", true);
		user1.createPost("Stay at home, protect the NHS!", false);
		user1.createPost("Don't worry, be happy!", true);

		
		// this line to made this thread paused for 5 minutes
		// the server runs on separate thread, so it won't be paused
		Thread.sleep(300000);

		// disconnect user1 from broker
		user1.unregister();
		// shutdown the direct message server of user1
		user1.shutdown();
		// stop the broker server
		BROKER.stop();
	}
}
