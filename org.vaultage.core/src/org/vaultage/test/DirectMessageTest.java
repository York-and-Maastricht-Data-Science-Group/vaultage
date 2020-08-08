package org.vaultage.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.UnknownHostException;

import org.junit.Test;
import org.vaultage.core.DirectMessageClient;
import org.vaultage.core.DirectMessageServer;
import org.vaultage.core.NettyDirectMessageClient;
import org.vaultage.core.NettyDirectMessageServer;
import org.vaultage.core.SocketDirectMessageClient;
import org.vaultage.core.SocketDirectMessageServer;
import org.vaultage.core.Vaultage;

/***
 * A class to test direct message server.
 * 
 * @author Alfa Yohannis
 *
 */
public class DirectMessageTest {

	static final long SLEEP_TIME = 500;

	/***
	 * Test multiple connections to Socket direct message server 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void testConnectToSocketSever() throws IOException, InterruptedException {

		DirectMessageServer server = new SocketDirectMessageServer(Vaultage.DEFAULT_SERVER_ADDRESS,
				Vaultage.DEFAULT_SERVER_PORT, null);
		server.start();
		Thread.sleep(SLEEP_TIME);

		DirectMessageClient client1 = new SocketDirectMessageClient(server.getLocalAddress(), server.getLocalPort());
		DirectMessageClient client2 = new SocketDirectMessageClient(server.getLocalAddress(), server.getLocalPort());

		boolean result1 = client1.connect();
		boolean result2 = client2.connect();

		assertEquals(true, result1);
		assertEquals(true, result2);

		Thread.sleep(SLEEP_TIME);
		client1.shutdown();
		client2.shutdown();
		server.shutdown();
	}

	/***
	 * Test multiple connections to Netty direct message server 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void testConnectToNettySever() throws InterruptedException, UnknownHostException, IOException {

		DirectMessageServer server = new NettyDirectMessageServer(Vaultage.DEFAULT_SERVER_ADDRESS,
				Vaultage.DEFAULT_SERVER_PORT, null);
		server.start();
		Thread.sleep(SLEEP_TIME);

		DirectMessageClient client1 = new NettyDirectMessageClient(server.getLocalAddress(), server.getLocalPort());
		DirectMessageClient client2 = new NettyDirectMessageClient(server.getLocalAddress(), server.getLocalPort());

		boolean result1 = client1.connect();
		boolean result2 = client2.connect();

		assertEquals(true, result1);
		assertEquals(true, result2);

		Thread.sleep(SLEEP_TIME);
		client1.shutdown();
		client2.shutdown();
		server.shutdown();
	}
}
