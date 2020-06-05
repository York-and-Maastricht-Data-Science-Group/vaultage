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
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
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

	private String address;
	private ActiveMQConnectionFactory connectionFactory;
	private Connection connection;
	private Session session;
	private Set<VaultageHandler> threads = new HashSet<VaultageHandler>();
	private Set<String> expectedReplyTokens = new HashSet<>();

	public Set<String> getExpectedReplyTokens() {
		return expectedReplyTokens;
	}

	public void setExpectedReplyTokens(Set<String> expectedReplyTokens) {
		this.expectedReplyTokens = expectedReplyTokens;
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
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(VaultageEncryption.ALGORITHM);
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
		v1.connect(address);

		Vaultage v2 = new Vaultage();
		v2.connect(address);

		Thread.sleep(SLEEP_TIME);

		HashMap<String, VaultageHandler> handlers = new HashMap<String, VaultageHandler>();
		handlers.put(VaultageHandler.class.getName(), new VaultageHandler() {
			public void run() {
				System.out.println("Received message: " + this.message.getValue("value"));
			}
		});

		v2.subscribe(receiverPublicKey, receiverPrivateKey, handlers);

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
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

			// Create a message
			String text = Gson.toJson(message).trim();

			// encrypt message
			String encryptedMessage = VaultageEncryption.doubleEncrypt(text, topicId, senderPrivateKey).trim();

			TextMessage m = session.createTextMessage(senderPublicKey + encryptedMessage);

			producer.send(m);
			
			System.out.println("Send to: " + topicId);
			
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
	public void subscribe(String topicId, String receiverPrivateKey, Map<String, VaultageHandler> handlers)
			throws InterruptedException {
		try {
			// Create the destination (Topic or Queue)
			Topic destination = session.createTopic(topicId);

			// Create a MessageConsumer from the Session to the Topic or Queue
			MessageConsumer consumer = session.createConsumer(destination);

			consumer.setMessageListener(new MessageListener() {

				@Override
				public void onMessage(Message message) {
					try {
						TextMessage textMessage = (TextMessage) message;

						String mergedMessage = textMessage.getText();
						String senderPublicKey = mergedMessage.substring(0, 128);
						String encryptedMessage = mergedMessage.substring(128, mergedMessage.length());

						String json = VaultageEncryption.doubleDecrypt(encryptedMessage, senderPublicKey,
								receiverPrivateKey);

//						System.out.println("RECEIVED MESSAGE: " + topicId + "\n" + json);

						VaultageMessage vaultageMessage = Gson.fromJson(json, VaultageMessage.class);
						String operation = vaultageMessage.getOperation();

						VaultageHandler handler = handlers.get(operation);
						if (handler != null && !handler.isAlive()) {
							threads.add(handler);
//							System.out.println("Run: " + handler.getName());
							handler.execute(topicId, vaultageMessage);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			 System.out.println("Listening to: " + topicId);
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
	public boolean connect(String address) throws Exception {
		try {
			this.address = address;
			connectionFactory = new ActiveMQConnectionFactory(this.address);
			connection = connectionFactory.createConnection();
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
		for (VaultageHandler h : threads) {
			if (h.isAlive()) {
				h.interrupt();
			}
		}
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

}