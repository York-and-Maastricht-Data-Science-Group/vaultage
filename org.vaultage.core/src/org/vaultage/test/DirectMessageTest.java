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

public class DirectMessageTest {

	@Test
	public void testLocalSocketSever() throws IOException, InterruptedException {

		DirectMessageServer server = new SocketDirectMessageServer(9999);
		server.start();
		Thread.sleep(2000);

		DirectMessageClient client1 = new SocketDirectMessageClient(server.getLocalAddress(), server.getLocalPort());
		DirectMessageClient client2 = new SocketDirectMessageClient(server.getLocalAddress(), server.getLocalPort());
		
		client1.connect();
		client2.connect();
		
		client2.sendMessage("B");
		client1.sendMessage("A");
		client1.sendMessage("A");
		client2.sendMessage("B");
		client1.sendMessage("A");
	
		assertEquals(true, true);

		Thread.sleep(2000);
		client1.disconnect();
		client2.disconnect();
		server.shutdown();
	}

	@Test
	public void testLocalNettyServer() throws InterruptedException, UnknownHostException, IOException {

		DirectMessageServer server = new NettyDirectMessageServer(9999);
		server.start();
		Thread.sleep(2000);

		System.out.println(server.getLocalAddress() +":" + server.getLocalPort());
	
		DirectMessageClient client1 = new NettyDirectMessageClient(server.getLocalAddress(), server.getLocalPort());
		DirectMessageClient client2 = new NettyDirectMessageClient(server.getLocalAddress(), server.getLocalPort());
		
		client1.connect();
		client2.connect();
		
		client2.sendMessage("B");
		client1.sendMessage("A");
		client1.sendMessage("A");
		client2.sendMessage("B");
		client1.sendMessage("A");
		
		assertEquals(true, true);
		
		Thread.sleep(2000);
		client1.shutdown();
		client2.shutdown();
		server.shutdown();
	}
}
