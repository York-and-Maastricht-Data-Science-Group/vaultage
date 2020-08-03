package org.vaultage.core;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.UUID;

public class DirectMessageClient {

	Socket clientSocket;
	SocketAddress socketAddress;

	public static void main(String[] args) {
		try {
			DirectMessageClient client = new DirectMessageClient("localhost", 9999);
			client.connect();
			client.sendMessage(UUID.randomUUID().toString());
			client.stop();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public DirectMessageClient(String serverName, int port) throws UnknownHostException, IOException {
		clientSocket = new Socket();
		socketAddress = new InetSocketAddress(serverName, port);
		clientSocket.setKeepAlive(true);
	}

	public void connect(String serverName, int port) throws IOException {
		socketAddress = new InetSocketAddress(serverName, port);
		clientSocket.connect(socketAddress);
	}

	public void connect() throws IOException {
		if (!clientSocket.isConnected())
			clientSocket.connect(socketAddress);
	}

	public void reconnect(String serverName, int port) throws IOException {
		if (clientSocket.isConnected())
			this.stop();
		socketAddress = new InetSocketAddress(serverName, port);
		clientSocket.connect(socketAddress);
	}

	public void reconnect() throws IOException {
		if (clientSocket.isConnected())
			this.stop();
		clientSocket.connect(socketAddress);
	}

	public void stop() throws IOException {
		clientSocket.close();
	}

	public void sendMessage(String message) throws IOException {
		OutputStream outputStream = clientSocket.getOutputStream();
		outputStream.write((message + System.lineSeparator()).getBytes());
	}

}
