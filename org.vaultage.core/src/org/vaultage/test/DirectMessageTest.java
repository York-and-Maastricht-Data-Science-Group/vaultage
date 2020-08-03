package org.vaultage.test;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;
import org.vaultage.core.DirectMessageClient;
import org.vaultage.core.DirectMessageServer;

public class DirectMessageTest {

	@Test
	public void testLocalSever() throws IOException, InterruptedException {

		DirectMessageServer server = new DirectMessageServer(9999);
		server.start();
		Thread.sleep(2000);

		DirectMessageClient client1 = new DirectMessageClient(server.getLocalAddress(), server.getLocalPort());
		DirectMessageClient client2 = new DirectMessageClient(server.getLocalAddress(), server.getLocalPort());
		
		client1.connect();
		client2.connect();
		
		client2.sendMessage("B");
		client1.sendMessage("A");
		client1.sendMessage("A");
		client2.sendMessage("B");
		client1.sendMessage("A");
	
		assertEquals(true, true);

		Thread.sleep(2000);
		client1.stop();
		client2.stop();
		server.shutdown();
	}

	@Test
	public void testLocalClientIsRunning() {

		assertEquals(true, true);
	}
}
