package org.vaultage.core;

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

	public static Gson Gson = new GsonBuilder().setPrettyPrinting().create();

	private Object vault;
	private String address;
	private ActiveMQConnectionFactory connectionFactory;
	private Connection connection;
	private Session session;
	private Set<String> expectedReplyTokens = new HashSet<>();

	private RequestMessageHandler requestMessageHandler;
	private ResponseMessageHandler responseMessageHandler;

	public Set<String> getExpectedReplyTokens() {
		return expectedReplyTokens;
	}

	public void setExpectedReplyTokens(Set<String> expectedReplyTokens) {
		this.expectedReplyTokens = expectedReplyTokens;
	}

	public Vaultage() {
	}

	public Vaultage(Object vault) {
		this.vault = vault;
	}

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
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(VaultageEncryption.CIPHER_ALGORITHM);
		keyPairGen.initialize(VaultageEncryption.KEY_LENGTH);

		receiverKeyPair = keyPairGen.generateKeyPair();
		senderKeyPair = keyPairGen.generateKeyPair();

		String receiverPublicKey = Base64.getEncoder().encodeToString(receiverKeyPair.getPublic().getEncoded());
		String receiverPrivateKey = Base64.getEncoder().encodeToString(receiverKeyPair.getPrivate().getEncoded());

		String senderPublicKey = Base64.getEncoder().encodeToString(senderKeyPair.getPublic().getEncoded());
		String senderPrivateKey = Base64.getEncoder().encodeToString(senderKeyPair.getPrivate().getEncoded());

		String address = ActiveMQConnection.DEFAULT_BROKER_URL;
//		String address = "vm://localhost";
		Vaultage v1 = new Vaultage();
		v1.connect(address, senderPublicKey);

		Vaultage v2 = new Vaultage();
		v2.connect(address, receiverPublicKey);

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
			// Create the destination (Topic or Queue)
			Topic destination = session.createTopic(topicId);

			// Create a MessageProducer from the Session to the Topic or Queue
			MessageProducer producer = session.createProducer(destination);
			producer.setDeliveryMode(DeliveryMode.PERSISTENT);

			// Create a message
			String text = serialise(message).trim();

			// encrypt message
			String encryptedMessage = VaultageEncryption.doubleEncrypt(text, topicId, senderPrivateKey).trim();

			TextMessage m = session.createTextMessage(senderPublicKey + encryptedMessage);

			producer.send(m);

//			System.out.println("Send to: " + topicId);

			expectedReplyTokens.add(message.getToken());
			System.out.println("SENT MESSAGE: " + topicId + "\n" + text);

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
	public void subscribe(String topicId, String receiverPrivateKey)
			throws InterruptedException {
		try {
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

						String content = VaultageEncryption.doubleDecrypt(encryptedMessage,
								senderPublicKey,
								receiverPrivateKey);

						 System.out.println("RECEIVED MESSAGE: " + topicId + "\n" + content);

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
	 * @param address
	 * @return
	 * @throws Exception
	 */
	public boolean connect(String address, String clientID) throws Exception {
		try {
			this.address = address;
			connectionFactory = new ActiveMQConnectionFactory(this.address);
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
	 * Get the address of the ActiveMQ broker
	 * 
	 * @param address
	 */
	public String getAddress() {
		return address;
	}

	/***
	 * Set the address of the ActiveMQ broker
	 * 
	 * @param address
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	public void setRequestMessageHandler(RequestMessageHandler requestMessageHandler) {
		this.requestMessageHandler = requestMessageHandler;
	}

	public void setResponseMessageHandler(ResponseMessageHandler responseMessageHandler) {
		this.responseMessageHandler = responseMessageHandler;
	}

}