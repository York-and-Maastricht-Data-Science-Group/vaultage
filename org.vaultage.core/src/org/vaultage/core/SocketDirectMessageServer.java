package org.vaultage.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class SocketDirectMessageServer extends Thread implements DirectMessageServer {

	private ServerSocket serverSocket;
	private boolean isListening = false;
	private long counter = 0;

	public static void main(String[] args) {
		try {
			System.out.println("Starting Socket Server ..");
			SocketDirectMessageServer server = new SocketDirectMessageServer(9999);
			server.start();
			System.out.println("Server started at " + server.getLocalAddress() + ":" + server.getLocalPort());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public SocketDirectMessageServer(int port) throws IOException {
		serverSocket = new ServerSocket(port);
	}

	public String getLocalAddress() {
		return serverSocket.getInetAddress().getHostAddress();
	}

	public int getLocalPort() {
		return serverSocket.getLocalPort();
	}

	@Override
	public void run() {
		isListening = true;
		while (isListening) {
			try {
				Socket socket = serverSocket.accept();
				SocketServerHandler t = new SocketServerHandler(socket);
				t.setName(this.getClass().getSimpleName() + "-" + counter);
				t.start();
				counter++;
			} catch (SocketException se) {
				System.out.println(se.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void shutdown() throws IOException {
		isListening = false;
		serverSocket.close();
	}

	public class SocketServerHandler extends Thread {

		Socket socket;

		public SocketServerHandler(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				String incomingMessage = null;
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				while (true) {
					incomingMessage = in.readLine();
					if (incomingMessage == null) {
						break;
					}
					System.out.println(socket.getRemoteSocketAddress() + ": " + incomingMessage);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};

}
