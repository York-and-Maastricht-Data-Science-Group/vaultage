package org.vaultage.core;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.vaultage.core.VaultageMessage.MessageType;
import org.vaultage.util.VaultageEncryption;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/***
 * The main class that is responsible to connect to, disconnect, listen, send,
 * and receive messages directly through from and to Direct Message Server and
 * Client or a broker server, Apache ActiveMQ server.
 * 
 * Vaultage will try to send the message directly to the receiver (another
 * vault). In other words, the message is sent from a direct message client
 * (local) to a direct message server (remote). However, if the connection
 * cannot be established, it will send it to an ActiveMQ message broker.
 * 
 * In receiving a message, if the message is from a direct message client than
 * the message will be handled by the implementation of the direct message
 * server since the message is sent without using any broker. If the message is
 * from a broker server, it will be handled by the MessageListener of ActiveMQ
 * broker.
 * 
 * @author Alfa Yohannis
 *
 */
public class Vaultage {

	public static final String DEFAULT_SERVER_ADDRESS = "localhost";
	public static final int DEFAULT_SERVER_PORT = 50000;

	public static Gson Gson = new GsonBuilder().setPrettyPrinting().create();
//	public static Gson Gson = new GsonBuilder().create();

	private Vault vault;
	private String brokerAddress;
	private ActiveMQConnectionFactory connectionFactory;
	private Connection connection;
	private Session session;
	private Set<String> expectedReplyTokens = new HashSet<>();

	private InetSocketAddress directMessageServerAddress;
	private Map<String, InetSocketAddress> publicKeyToRemoteAddress = new HashMap<>();

	private DirectMessageServer directMessageServer;

	private RequestMessageHandler requestMessageHandler;
	private ResponseMessageHandler responseMessageHandler;

	private boolean isForcedBrokeredMessaging = false;
	private boolean isEncrypted = true;

	/***
	 * Test or demo this Vaultage class. No encryption is performed. These routines
	 * are only to give some values to the parameters of the operations used in this
	 * section. So, plain text message is sent and received.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		// set sleep time to 0.05 seconds
		final int SLEEP_TIME = 500;

		// initialise and start a broker
		BrokerService broker = BrokerFactory.createBroker(new URI("broker:(tcp://localhost:61616)"));
		broker.start();

		/** setting up encryption **/
		// Actually, no encryption is performed. These routines are only to give some
		// values to the parameters of the operations used in this section. So, plain
		// text message is sent and received.
		KeyPair receiverKeyPair;
		KeyPair senderKeyPair;
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(VaultageEncryption.KEY_GENERATOR_ALGORITHM);
		keyPairGen.initialize(VaultageEncryption.KEY_LENGTH);

		receiverKeyPair = keyPairGen.generateKeyPair();
		senderKeyPair = keyPairGen.generateKeyPair();

		String receiverPublicKey = Base64.getEncoder().encodeToString(receiverKeyPair.getPublic().getEncoded());
		String receiverPrivateKey = Base64.getEncoder().encodeToString(receiverKeyPair.getPrivate().getEncoded());

		String senderPublicKey = Base64.getEncoder().encodeToString(senderKeyPair.getPublic().getEncoded());
		String senderPrivateKey = Base64.getEncoder().encodeToString(senderKeyPair.getPrivate().getEncoded());

		/** setting up vaults **/
		String brokerUrl = ActiveMQConnection.DEFAULT_BROKER_URL;
		Vaultage v1 = new Vaultage(null, Vaultage.DEFAULT_SERVER_ADDRESS, Vaultage.DEFAULT_SERVER_PORT);
		v1.connect(brokerUrl, senderPublicKey);

		Vaultage v2 = new Vaultage(null, Vaultage.DEFAULT_SERVER_ADDRESS, Vaultage.DEFAULT_SERVER_PORT + 1);
		v2.connect(brokerUrl, receiverPublicKey);

		Thread.sleep(SLEEP_TIME);

		// create the handler for vault2 for any incoming messages
		RequestMessageHandler handler = new RequestMessageHandler() {
			@Override
			public void process(VaultageMessage message, String senderPublicKey, Object vault) throws Exception {
				System.out.println("Received message: " + message.getValue("value"));
			}
		};
		v2.setRequestMessageHandler(handler);

		// subscribe vault2 to the broker server to listen for any incoming messages
		v2.subscribe(receiverPublicKey, receiverPrivateKey);

		Thread.sleep(SLEEP_TIME);

		// craft the vaultage message to be sent from vault1 to vault2
		VaultageMessage message = new VaultageMessage();
		message.setMessageType(MessageType.REQUEST);
		message.setFrom(senderPublicKey);
		message.setTo(receiverPublicKey);
		message.setOperation(VaultageHandler.class.getName());
		message.putValue("value", "Hello World!");

		// send the message
		v1.sendMessage(receiverPublicKey, senderPublicKey, senderPrivateKey, message);

		Thread.sleep(SLEEP_TIME);

		// disconnect vault1 and vault2 from the broker server
		v1.disconnect();
		v2.disconnect();

		// shutdown the direct message servers of vault1 and vault2
		v1.directMessageServer.shutdown();
		v2.directMessageServer.shutdown();

		// stop the broker server
		broker.stop();
		System.out.println("Finished!");
	}

	/***
	 * Constructor without parameters
	 */
	public Vaultage() {
	}

	/***
	 * Constructor with vault parameters.
	 * 
	 * @param vault a reference to the object of the vault that uses Vaultage
	 */
	public Vaultage(Vault vault) {
		this.vault = vault;
	}

	/***
	 * Constructor with vault object, ip and port of direct message server
	 * parameters. The ip and port are used to direct message server.
	 * 
	 * @param vault   a reference to the vault object that uses Vaultage
	 * @param address the ip or hostname of direct message server
	 * @param the     port of direct message server
	 */
	public Vaultage(Vault vault, String address, int port) {
		this.vault = vault;
		this.startServer(address, port);
	}

	/***
	 * A method to start direct message server.
	 * 
	 * @param address the ip or hostname of the direct message server
	 * @param port    the port of the direct message server
	 */
	public void startServer(String address, int port) {
		this.directMessageServerAddress = new InetSocketAddress(address, port);
		try {
//			directMessageServer = new NettyDirectMessageServer(this.directMessageServerAddress, this);
			directMessageServer = new SocketDirectMessageServer(this.directMessageServerAddress, this);
			directMessageServer.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/***
	 * Set the port of direct message server.
	 * 
	 * @param port the port of the direct message server
	 */
	public void setPort(int port) {
		this.directMessageServerAddress = new InetSocketAddress(port);
	}

	/***
	 * Get the port of direct message server.
	 * 
	 * @return
	 */
	public int getPort() {
		return this.directMessageServerAddress.getPort();
	}

	/***
	 * Get the a set of tokens. A token is created when sending a message.
	 * 
	 * @return
	 */
	public Set<String> getExpectedReplyTokens() {
		return expectedReplyTokens;
	}

	/***
	 * Serialise object to Json using GSON library
	 * 
	 * @param obj the object to be serialised
	 * @return
	 */
	public static String serialise(Object obj) {
		return Gson.toJson(obj);
	}

	/**
	 * De-serialise from JSON to object with type <T> using GSON library
	 * 
	 * @param <T>
	 * @param content
	 * @param c
	 * @return
	 */
	public static <T> T deserialise(String content, Class<T> c) {
		return Gson.fromJson(content, c);
	}

	/**
	 * De-serialise from JSON to object with type <T> and Type using GSON library
	 * 
	 * @param <T>
	 * @param content
	 * @param c
	 * @param type
	 * @return
	 */
	public static <T> T deserialise(String content, Type type) {
		return Gson.fromJson(content, type);
	}

	/***
	 * A method to send a message. Vaultage will try to send the message directly to
	 * the receiver (another vault), but if the connection cannot be established, it
	 * will send it to an ActiveMQ message broker. Message is encrypted.
	 * 
	 * @param topicId
	 * @param senderPublicKey
	 * @param senderPrivateKey
	 * @param message
	 * @throws InterruptedException
	 */
	public void sendMessage(String topicId, String senderPublicKey, String senderPrivateKey, VaultageMessage message)
			throws InterruptedException {
		this.sendMessage(topicId, senderPublicKey, senderPrivateKey, message, true);
	}

	/***
	 * A method to send a message. Vaultage will try to send the message directly to
	 * the receiver (another vault), but if the connection cannot be established, it
	 * will send it to an ActiveMQ message broker.
	 * 
	 * @param topicId
	 * @param senderPublicKey
	 * @param senderPrivateKey
	 * @param message
	 * @param isEncrypted      Message is encrypted or not
	 * @throws InterruptedException
	 */
	public void sendMessage(String topicId, String senderPublicKey, String senderPrivateKey, VaultageMessage message,
			boolean isEncrypted) throws InterruptedException {
		try {
			// get
			this.isEncrypted = isEncrypted;
				
			// add local address and port to the message
			if (directMessageServerAddress != null) {
				message.setSenderAddress(directMessageServerAddress.getAddress().getHostAddress());
				message.setSenderPort(directMessageServerAddress.getPort());
			}

			// Create a message
			String text = serialise(message).trim();

			// encrypt message
			String encryptedMessage = (isEncrypted)
					? VaultageEncryption.doubleEncrypt(text, topicId, senderPrivateKey).trim()
					: text;

			String encryptionFlag = (isEncrypted) ? "1" : "0";

			String concatenatedMessage = encryptionFlag + senderPublicKey + encryptedMessage;

			/** Make a direct connection to the receiver **/
			// get the receiver's ip address and port from the topic id or public key
			InetSocketAddress remoteServer = publicKeyToRemoteAddress.get(topicId);
			boolean remoteServerAvailable = false;
			if (remoteServer != null && !isForcedBrokeredMessaging) {

				/***
				 * Optimise this since a new connection has to be made when sending a message
				 */
				DirectMessageClient directMessageClient = new SocketDirectMessageClient(remoteServer);
				try {
					directMessageClient.connect();
					remoteServerAvailable = directMessageClient.isActive();
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (remoteServerAvailable) {
					directMessageClient.sendMessage(concatenatedMessage);
					directMessageClient.shutdown();
				}
			}

			/** Otherwise use a broker **/
			// if the remote direct message server is not available then use a broker
			if (!remoteServerAvailable) {
				// Create the destination (Topic or Queue)
				Topic destination = session.createTopic(topicId);

				// Create a MessageProducer from the Session to the Topic or Queue
				MessageProducer producer = session.createProducer(destination);

				// set the delivery mode
				producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

				// create the message
				TextMessage m = session.createTextMessage(concatenatedMessage);

				// send the message
				producer.send(m);
			}

//			System.out.println("Send to: " + topicId);

			// this is to record the token of a message that has been sent
			expectedReplyTokens.add(message.getToken());
//			System.out.println("SENT MESSAGE: " + topicId + "\n" + text);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/***
	 * A method to listen to ActiveMQ broker for incoming messages.
	 * 
	 * @param topicId
	 * @param receiverPrivateKey
	 * @param handlers
	 * @throws InterruptedException
	 */
	public void subscribe(String topicId, String receiverPrivateKey) throws InterruptedException {
		try {

			// give the private key to direct message server
			if (directMessageServer != null)
				directMessageServer.setPrivateKey(receiverPrivateKey);

			// Create the destination (Topic or Queue)
			Topic destination = session.createTopic(topicId);

			// Create a MessageConsumer from the Session to the Topic or Queue
//			MessageConsumer consumer = session.createConsumer(destination);

			TopicSubscriber subscriber = session.createDurableSubscriber(destination, receiverPrivateKey);

			subscriber.setMessageListener(new MessageListener() {

				@Override
				public void onMessage(Message message) {
					try {
						Thread t = new Thread() {

							@Override
							public void run() {
								try {
									TextMessage textMessage = (TextMessage) message;

									String mergedMessage = textMessage.getText();
									String encryptionFlag = mergedMessage.substring(0, 1);
									String senderPublicKey = mergedMessage.substring(1,
											1 + VaultageEncryption.PUBLIC_KEY_LENGTH);
									String encryptedMessage = mergedMessage.substring(
											1 + VaultageEncryption.PUBLIC_KEY_LENGTH, mergedMessage.length());

									String content = (encryptionFlag.equals("1")) ? VaultageEncryption.doubleDecrypt(
											encryptedMessage, senderPublicKey, receiverPrivateKey) : encryptedMessage;

//							 System.out.println("RECEIVED MESSAGE: " + topicId + "\n" + content);

									VaultageMessage vaultageMessage = Vaultage.deserialise(content,
											VaultageMessage.class);
									MessageType msgType = vaultageMessage.getMessageType();

									// save sender's ip and port to public key and address map
									if (vaultageMessage.getSenderAddress() != null
											&& vaultageMessage.getSenderPort() >= 0 && isForcedBrokeredMessaging)
										Vaultage.this.getPublicKeyToRemoteAddress().put(senderPublicKey,
												new InetSocketAddress(vaultageMessage.getSenderAddress(),
														vaultageMessage.getSenderPort()));

									switch (msgType) {
									case REQUEST:
										// calls the user vault method associated with the operation
										requestMessageHandler.process(vaultageMessage, senderPublicKey, vault);
										break;
									case RESPONSE:
										// calls the registered handler of the operation
										responseMessageHandler.process(vaultageMessage, senderPublicKey, vault);
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						};
						t.start();

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	/***
	 * A method to connect to an ActiveMQ broker
	 * 
	 * @param brokerAddress
	 * @return
	 * @throws Exception
	 */
	public boolean connect(String brokerAddress, String clientID) throws Exception {
		try {
			this.brokerAddress = brokerAddress;
			connectionFactory = new ActiveMQConnectionFactory(this.brokerAddress);
			connection = connectionFactory.createConnection();
			connection.setClientID(clientID);
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/***
	 * A method to disconnect from an ActiveMQ broker
	 * 
	 * @throws Exception
	 */
	public void disconnect() throws Exception {
		if (session != null)
			session.close();
		connection.stop();
		connection.close();
	}

	/***
	 * Get the brokerAddress of the ActiveMQ broker
	 * 
	 * @param brokerAddress
	 */
	public String getBrokerAddress() {
		return brokerAddress;
	}

	/***
	 * Set the brokerAddress of the ActiveMQ broker
	 * 
	 * @param brokerAddress
	 */
	public void setBrokerAddress(String address) {
		this.brokerAddress = address;
	}

	/***
	 * To shutdown the direct message server of this Vaultage.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void shutdownServer() throws IOException, InterruptedException {
		if (this.directMessageServer != null)
			this.directMessageServer.shutdown();
	}

	/***
	 * Get the address (ip/hostname and port) the direct message server
	 * 
	 * @return
	 */
	public InetSocketAddress getDirectMessageServerAddress() {
		return directMessageServerAddress;
	}

	/***
	 * Get the object of the Vault that uses this Vaultage
	 * 
	 * @return
	 */
	public Vault getVault() {
		return vault;
	}

	/***
	 * Set the request message handler, what the program should do when it receives
	 * a request.
	 * 
	 * @param requestMessageHandler
	 */
	public void setRequestMessageHandler(RequestMessageHandler requestMessageHandler) {
		this.requestMessageHandler = requestMessageHandler;
	}

	/***
	 * Set the response message handler, what the program should do when it receives
	 * a response from its previous request.
	 * 
	 * @param responseMessageHandler
	 */
	public void setResponseMessageHandler(ResponseMessageHandler responseMessageHandler) {
		this.responseMessageHandler = responseMessageHandler;
	}

	/***
	 * Get the request message handler. It defines what the program should do when
	 * it receives a request.
	 * 
	 * @return
	 */
	public RequestMessageHandler getRequestMessageHandler() {
		return requestMessageHandler;
	}

	/***
	 * Get the response message handler. It defines what the program should do when
	 * it receives a response from its previous request.
	 * 
	 * @return
	 */
	public ResponseMessageHandler getResponseMessageHandler() {
		return responseMessageHandler;
	}

	/***
	 * To get the remote addresses (ips/hostnames and ports) of other vaults using
	 * their public keys. Public key is used as the key to retrieve the remote
	 * address. This is useful to establish direct connection so that messaging
	 * doesn't have to go through the broker server.
	 * 
	 * @return
	 */
	public Map<String, InetSocketAddress> getPublicKeyToRemoteAddress() {
		return publicKeyToRemoteAddress;
	}

	/***
	 * To set the private key of this Vaultage with the private key of the Vault
	 * that uses this Vaultage. The private key is used by direct message server to
	 * decrypt messages.
	 * 
	 * @param privateKey the private key of the Vault that uses this Vaultage
	 */
	public void setPrivateKey(String privateKey) {
		directMessageServer.setPrivateKey(privateKey);
	}

	/***
	 * Get the direct message server for direct messaging.
	 * 
	 * @return
	 */
	public DirectMessageServer getDirectMessageServer() {
		return directMessageServer;
	}

	/**
	 * Forced to use brokered messaging if TRUE, otherwise use direct messaging if it's possible/available
	 * @return
	 */
	public boolean isForcedBrokeredMessaging() {
		return isForcedBrokeredMessaging;
	}

	/***
	 * Force to use brokered messaging if TRUE, otherwise use direct messaging if it's possible/available
	 * 
	 * @return
	 */
	public void forceBrokeredMessaging(boolean isForced) {
		this.isForcedBrokeredMessaging = isForced;
	}

	/***
	 * Is messaging encrypted
	 * @return
	 */
	public boolean isEncrypted() {
		return isEncrypted;
	}

	/***
	 * set the messaging encrypted or not encrypted
	 * @param isEncrypted
	 */
	public void setEncrypted(boolean isEncrypted) {
		this.isEncrypted = isEncrypted;
	}
}