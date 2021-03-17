package org.vaultage.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.stream.Collectors;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.vaultage.core.VaultageMessage.MessageType;
import org.vaultage.util.VaultageEncryption;

/***
 * The Socket implementation of direct message server. The class extends class
 * Thread so that it doesn't block other processes and runs in background.
 * 
 * @author Alfa Yohannis
 *
 */
public class SocketDirectMessageServer extends Thread implements DirectMessageServer {

	private static final int BACKLOG_NUMBER = 128;
	
	private ServerSocket serverSocket;
	private boolean isListening = false;
	private static long counter = 0; // for naming the threads

	private Vaultage vaultage;
	private String privateKey;

	/***
	 * For testing or dummy of this socket direct message server.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			System.out.println("Starting Socket Server ..");
			SocketDirectMessageServer server = new SocketDirectMessageServer(Vaultage.DEFAULT_SERVER_ADDRESS,
					Vaultage.DEFAULT_SERVER_PORT, null);
			server.start();
			System.out.println("Server started at " + server.getLocalAddress() + ":" + server.getLocalPort());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/***
	 * Constructor of this implementation of direct message server.
	 * 
	 * @param vaultage the Vaultage
	 * @param address  the ip/hostname of the direct message server.
	 * @param port     the port for the direct message server.
	 * @throws IOException
	 */
	public SocketDirectMessageServer(String address, int port, Vaultage vaultage) throws IOException {
		this.setName(this.getClass().getSimpleName() + "-" + counter);
		this.vaultage = vaultage;
		serverSocket = new ServerSocket(port, BACKLOG_NUMBER, (new InetSocketAddress(address, port)).getAddress());
		serverSocket.setReuseAddress(true);
	}
	
	/***
	 * Constructor of this implementation of direct message server.
	 * 
	 * @param address
	 * @param vaultage
	 * @throws IOException
	 */
	public SocketDirectMessageServer(InetSocketAddress address, Vaultage vaultage) throws IOException {
		this.setName(this.getClass().getSimpleName() + "-" + counter);
		this.vaultage = vaultage;
		serverSocket = new ServerSocket(address.getPort(), BACKLOG_NUMBER, address.getAddress());
		serverSocket.setReuseAddress(true);
	}

	/***
	 * Get the ip address or hostname of the direct message server.
	 * 
	 * @return ip or hostname
	 */
	public String getLocalAddress() {
		return serverSocket.getInetAddress().getHostAddress();
	}

	/***
	 * Get the port of the direct message server.
	 * 
	 * @return port number
	 */
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
				t.start();
				counter++;
			} catch (SocketException se) {
//				System.out.println(se.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/***
	 * To shutdown the direct message server.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void shutdown() throws IOException {
		isListening = false;
		serverSocket.close();
	}

	/***
	 * The class handler for a received message. This is where we define how to
	 * respond to a message received.
	 * 
	 * @author Alfa Yohannis
	 *
	 */
	public class SocketServerHandler extends Thread {

		Socket socket;
		int counter = 0;

		public SocketServerHandler(Socket socket) {
			this.setName(this.getClass().getSimpleName() + "-" + counter);
			this.socket = socket;
		}

		/***
		 * The process when this direct message server started is defined here.
		 */
		@Override
		public void run() {
			try {
				String mergedMessage = "";
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				 mergedMessage = in.lines().collect(Collectors.joining());
					 
//				while (true) {
//					String temp = in.readLine();
//					if (temp == null) break;
//					mergedMessage = mergedMessage + temp;
//				}
				in.close();
				
//				if (mergedMessage.equals("")) return;
				
//				System.out.println(socket.getRemoteSocketAddress() + ": " + incomingMessage);

				String encryptionFlag = mergedMessage.substring(0, 1);
				String senderPublicKey = mergedMessage.substring(1, 1 + VaultageEncryption.PUBLIC_KEY_LENGTH);
				String encryptedMessage = mergedMessage.substring(1 + VaultageEncryption.PUBLIC_KEY_LENGTH,
						mergedMessage.length());

//				System.out.println(senderPublicKey);
//				System.out.println(encryptedMessage);

				if (vaultage != null) {
					String content = (encryptionFlag.equals("1"))
							? VaultageEncryption.doubleDecrypt(encryptedMessage, senderPublicKey,
									SocketDirectMessageServer.this.privateKey)
							: encryptedMessage;

					// System.out.println("RECEIVED MESSAGE: " + topicId + "\n" + content);

					VaultageMessage vaultageMessage = Vaultage.deserialise(content, VaultageMessage.class);
					MessageType msgType = vaultageMessage.getMessageType();

					if (vaultageMessage.getSenderAddress() != null && vaultageMessage.getSenderPort() >= 0) {
					vaultage.getPublicKeyToRemoteAddress().put(senderPublicKey, new InetSocketAddress(
							vaultageMessage.getSenderAddress(), vaultageMessage.getSenderPort()));
					}

					switch (msgType) {
					case REQUEST:
						// calls the user vault method associated with the operation
						vaultage.getRequestMessageHandler().process(vaultageMessage, senderPublicKey,
								vaultage.getVault());
						break;
					case RESPONSE:
						// calls the registered handler of the operation
						vaultage.getResponseMessageHandler().process(vaultageMessage, senderPublicKey,
								vaultage.getVault());
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			} catch (IllegalBlockSizeException e) {
				e.printStackTrace();
			} catch (BadPaddingException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (NoSuchPaddingException e) {
				e.printStackTrace();
			} catch (InvalidKeySpecException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/***
	 * To set the private key of the direct message server. It is used to decrypt
	 * received messages.
	 * 
	 * @param privateKey
	 */
	@Override
	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	};
}
