package org.vaultage.core;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.UUID;

/***
 * The Java Socket implementation of direct message client. TODO The class
 * should be extending class Thread so that it doesn't block other processes and
 * runs in background.
 * 
 * @author Alfa Yohannis
 *
 */
public class SocketDirectMessageClient implements DirectMessageClient {

	Socket clientSocket;
	SocketAddress socketAddress;

	/***
	 * For testing or dummy of this Socket direct message client.
	 * 
	 * @param args
	 */
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

	/***
	 * The constructor of the direct message client with the direct message server's
	 * op/hostname and port parameters.
	 * 
	 * @param serverName the ip/hostname of the direct message server
	 * @param port       the port of the direct message server
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public SocketDirectMessageClient(String serverName, int port) throws UnknownHostException, IOException {
		clientSocket = new Socket();
		socketAddress = new InetSocketAddress(serverName, port);
		clientSocket.setKeepAlive(true);
	}

	/***
	 * To connect to a direct message server using its ip/hostname and port.
	 * 
	 * @param serverName the direct message server's ip or hostname
	 * @param port       the direct message server's port
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public boolean connect(String serverName, int port) throws IOException {
		try {
			socketAddress = new InetSocketAddress(serverName, port);
			clientSocket.connect(socketAddress);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/***
	 * To connect to a direct message server using its ip/hostname and port if the
	 * ip/hostname and port have been already defined.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public boolean connect() throws IOException {
		try {
			clientSocket.connect(socketAddress);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/***
	 * To connect to a direct message server using its ip/hostname and port.
	 * 
	 * @param serverName the direct message server's ip or hostname
	 * @param port       the direct message server's port
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void reconnect(String serverName, int port) throws IOException {
		if (clientSocket.isConnected())
			this.disconnect();
		socketAddress = new InetSocketAddress(serverName, port);
		clientSocket.connect(socketAddress);
	}

	/***
	 * To re-connect to a direct message server with the ip/hostname and port if the
	 * ip/hostname and port have been already defined.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void reconnect() throws IOException {
		if (clientSocket.isConnected())
			this.disconnect();
		clientSocket.connect(socketAddress);
	}

	/***
	 * To disconnect from a direct message server.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void disconnect() throws IOException {
		clientSocket.close();
	}

	/***
	 * To send a message to the direct message server
	 * 
	 * @param message the message
	 * @throws IOException
	 */
	public void sendMessage(String message) throws IOException {
		OutputStream outputStream = clientSocket.getOutputStream();
		outputStream.write((message + System.lineSeparator()).getBytes());
	}

	/***
	 * To check if the client has successfully connected to the direct message
	 * server.
	 * 
	 * @return true if the connection is active, false if it's inactive
	 */
	@Override
	public void shutdown() throws IOException, InterruptedException {
		this.disconnect();
	}

	/***
	 * To check if the client has successfully connected to the direct message
	 * server.
	 * 
	 * @return true if the connection is active, false if it's inactive
	 */
	public boolean isActive() {
		return clientSocket.isConnected();
	}

	/***
	 * To get the address of the direct message server
	 * 
	 * @return the ip/hostname and port
	 */
	public InetSocketAddress getRemoteAddress() {
		return (InetSocketAddress) this.clientSocket.getRemoteSocketAddress();
	}

	/***
	 * To get the local address of the client
	 * 
	 * @return the ip/hostname and port
	 */
	public InetSocketAddress getLocalAddress() {
		return (InetSocketAddress) this.clientSocket.getLocalSocketAddress();
	}

}
