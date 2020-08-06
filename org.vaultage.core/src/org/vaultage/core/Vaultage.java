package org.vaultage.core;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
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
 * The main class that is responsible to connect, disconnect, listen, send, and
 * receive messages to and from Apache ActiveMQ server.
 * 
 * @author Alfa Yohannis
 *
 */
public class Vaultage {

	public static final String DEFAULT_SERVER_ADDRESS = "localhost";
	public static final int DEFAULT_SERVER_PORT = 50000;

	public static Gson Gson = new GsonBuilder().setPrettyPrinting().create();

	private Object vault;
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



	/***
	 * Test or demo this Vaultage class
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		final int SLEEP_TIME = 500;

		BrokerService broker = BrokerFactory.createBroker(new URI("broker:(tcp://localhost:61616)"));
		broker.start();

		// encryption
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

		String brokerUrl = ActiveMQConnection.DEFAULT_BROKER_URL;
//		String brokerAddress = "vm://localhost";
		Vaultage v1 = new Vaultage();
		v1.connect(brokerUrl, senderPublicKey);

		Vaultage v2 = new Vaultage();
		v2.connect(brokerUrl, receiverPublicKey);

		Thread.sleep(SLEEP_TIME);

		HashMap<String, VaultageHandler> handlers = new HashMap<String, VaultageHandler>();
		handlers.put(VaultageHandler.class.getName(), new VaultageHandler() {
			public void run() {
				System.out.println("Received message: " + this.message.getValue("value"));
			}
		});

		v2.subscribe(receiverPublicKey, receiverPrivateKey);

		Thread.sleep(SLEEP_TIME);

		VaultageMessage message = new VaultageMessage();
		message.setMessageType(MessageType.REQUEST);
		message.setFrom(senderPublicKey);
		message.setTo(receiverPublicKey);
		message.setOperation(VaultageHandler.class.getName());
		message.putValue("value", "Hello World!");
		v1.sendMessage(receiverPublicKey, senderPublicKey, senderPrivateKey, message);

		Thread.sleep(SLEEP_TIME);

		v1.disconnect();
		v2.disconnect();

		broker.stop();
		System.out.println("Finished!");
	}

	/***
	 * 
	 */
	public Vaultage() {
	}

	/***
	 * 
	 * @param vault
	 */
	public Vaultage(Object vault) {
		this.vault = vault;
	}
	
	public Vaultage(Object vault, String address, int port) {
		this.vault = vault;
		this.startDirectMessageServer(address, port);
	}

	public void startDirectMessageServer(String address, int port) {
		this.directMessageServerAddress = new InetSocketAddress(address, port);
		try {
			directMessageServer = new NettyDirectMessageServer(this.directMessageServerAddress, this);
			directMessageServer.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void setPort(int port) {
		this.directMessageServerAddress = new InetSocketAddress(port);
	}
	
	public int getPort() {
		return this.directMessageServerAddress.getPort();
	}
	

	public Set<String> getExpectedReplyTokens() {
		return expectedReplyTokens;
	}

	public void setExpectedReplyTokens(Set<String> expectedReplyTokens) {
		this.expectedReplyTokens = expectedReplyTokens;
	}

	public static String serialise(Object obj) {
		return Gson.toJson(obj);
	}

	public static <T> T deserialise(String content, Class<T> c) {
		return Gson.fromJson(content, c);
	}

	/***
	 * A method to send a message to ActiveMQ broker
	 * 
	 * @param topicId
	 * @param senderPublicKey
	 * @param senderPrivateKey
	 * @param message
	 * @throws InterruptedException
	 */
	public void sendMessage(String topicId, String senderPublicKey, String senderPrivateKey, VaultageMessage message)
			throws InterruptedException {
		try {
			// add local address and port to the message
			message.setSenderAddress(directMessageServerAddress.getAddress().getHostAddress());
			message.setSenderPort(directMessageServerAddress.getPort());
			
			// Create a message
			String text = serialise(message).trim();

			// encrypt message
			String encryptedMessage = VaultageEncryption.doubleEncrypt(text, topicId, senderPrivateKey).trim();
			String concatenatedMessage = senderPublicKey + encryptedMessage;
			
			System.out.println(senderPublicKey);
			System.out.println(encryptedMessage);			

			/** Make a direct connection to the receiver **/
			// get the receiver's ip address and port from the topic id or public key
			InetSocketAddress remoteServer = publicKeyToRemoteAddress.get(topicId);
			boolean remoteServerAvailable = false;
			if (remoteServer != null) {
				try {
					Socket socket = new Socket(remoteServer.getAddress(), remoteServer.getPort());
					remoteServerAvailable = socket.isConnected();
				} catch (Exception e) {
					remoteServerAvailable = false;
				}
			}

			// if the remote receiver is available, make a direct connection
			if (remoteServerAvailable) {
				DirectMessageClient directMessageClient = new NettyDirectMessageClient(remoteServer);
				directMessageClient.connect();
				directMessageClient.sendMessage(concatenatedMessage);
				directMessageClient.shutdown();
			}
			// if cannot directly to the receiver than use a broker
			else {
				// Create the destination (Topic or Queue)
				Topic destination = session.createTopic(topicId);

				// Create a MessageProducer from the Session to the Topic or Queue
				MessageProducer producer = session.createProducer(destination);

				// set the delivery mode
				producer.setDeliveryMode(DeliveryMode.PERSISTENT);

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
	 * A method to listen to ActiveMQ broker for incoming messages
	 * 
	 * @param topicId
	 * @param receiverPrivateKey
	 * @param handlers
	 * @throws InterruptedException
	 */
	public void subscribe(String topicId, String receiverPrivateKey) throws InterruptedException {
		try {
			
			//give the private key to direct message server
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
						TextMessage textMessage = (TextMessage) message;

						String mergedMessage = textMessage.getText();
						String senderPublicKey = mergedMessage.substring(0, VaultageEncryption.PUBLIC_KEY_LENGTH);
						String encryptedMessage = mergedMessage.substring(VaultageEncryption.PUBLIC_KEY_LENGTH,
								mergedMessage.length());

						String content = VaultageEncryption.doubleDecrypt(encryptedMessage, senderPublicKey,
								receiverPrivateKey);

//						 System.out.println("RECEIVED MESSAGE: " + topicId + "\n" + content);

						VaultageMessage vaultageMessage = Vaultage.deserialise(content, VaultageMessage.class);
						MessageType msgType = vaultageMessage.getMessageType();

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
			});

//			 System.out.println("Listening to: " + topicId);
//			// Wait for a message, 0 means listen forever
//			Message message = consumer.receive(0);

		} catch (JMSException e) {
			// TODO Auto-generated catch block
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

	public void shutdown() throws IOException, InterruptedException {
		this.directMessageServer.shutdown();
	}
	
	public void setRequestMessageHandler(RequestMessageHandler requestMessageHandler) {
		this.requestMessageHandler = requestMessageHandler;
	}

	public void setResponseMessageHandler(ResponseMessageHandler responseMessageHandler) {
		this.responseMessageHandler = responseMessageHandler;
	}

	public InetSocketAddress getDirectMessageServerAddress() {
		return directMessageServerAddress;
	}

	public Object getVault() {
		return vault;
	}

	public void setVault(Object vault) {
		this.vault = vault;
	}
	
	public RequestMessageHandler getRequestMessageHandler() {
		return requestMessageHandler;
	}

	public ResponseMessageHandler getResponseMessageHandler() {
		return responseMessageHandler;
	}
	
	public Map<String, InetSocketAddress> getPublicKeyToRemoteAddress() {
		return publicKeyToRemoteAddress;
	}

	public void setPrivateKey(String privateKey) {
		directMessageServer.setPrivateKey(privateKey);
	}
}