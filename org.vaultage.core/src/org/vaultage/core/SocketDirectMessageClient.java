package org.vaultage.core;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.UUID;

public class SocketDirectMessageClient implements DirectMessageClient {

	Socket clientSocket;
	SocketAddress socketAddress;

	public static void main(String[] args) {
		try {
			SocketDirectMessageClient client = new SocketDirectMessageClient("192.168.56.101", 9998);
			client.connect();
			client.sendMessage(UUID.randomUUID().toString());
			client.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public SocketDirectMessageClient(String serverName, int port) throws UnknownHostException, IOException {
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
			this.disconnect();
		socketAddress = new InetSocketAddress(serverName, port);
		clientSocket.connect(socketAddress);
	}

	public void reconnect() throws IOException {
		if (clientSocket.isConnected())
			this.disconnect();
		clientSocket.connect(socketAddress);
	}

	public void disconnect() throws IOException {
		clientSocket.close();
	}

	public void sendMessage(String message) throws IOException {
		OutputStream outputStream = clientSocket.getOutputStream();
		outputStream.write((message + System.lineSeparator()).getBytes());
	}

	@Override
	public void shutdown() throws IOException, InterruptedException {
		this.disconnect();
	}

	public boolean isActive() {
		return clientSocket.isConnected();

	}

	public InetSocketAddress getRemoteAddress() {
		return (InetSocketAddress) this.clientSocket.getRemoteSocketAddress();
	}

	public InetSocketAddress getLocalAddress() {
		return (InetSocketAddress) this.clientSocket.getLocalSocketAddress();
	}

}
