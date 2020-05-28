package org.vaultage.core;

import java.security.KeyFactory;
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
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.vaultage.util.VaultageEncryption;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/***
 * The main class that is responsible to listen, send, and receive messages to
 * and from Apache ActiveMQ server.
 * 
 * @author Ryano
 *
 */
public class Vaultage {

	public static Gson Gson = new GsonBuilder().setPrettyPrinting().create();

	private String address;
	private ActiveMQConnectionFactory connectionFactory;
	private Connection connection;
	private Session session;
	private boolean isListening;
	private Set<VaultageHandler> threads;
	private Set<String> expectedReplyTokens = new HashSet<>();

	public Set<String> getExpectedReplyTokens() {
		return expectedReplyTokens;
	}

	public void setExpectedReplyTokens(Set<String> expectedReplyTokens) {
		this.expectedReplyTokens = expectedReplyTokens;
	}

	/***
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		// encryption
		KeyPair receiverKeyPair;
		KeyPair senderKeyPair;
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(VaultageEncryption.ALGORITHM);
		keyPairGen.initialize(VaultageEncryption.KEY_LENGTH);

		receiverKeyPair = keyPairGen.generateKeyPair();
		senderKeyPair = keyPairGen.generateKeyPair();

//		String address = ActiveMQConnection.DEFAULT_BROKER_URL;
		String address = "vm://localhost";
		Vaultage v1 = new Vaultage();
		v1.connect(address);

		Vaultage v2 = new Vaultage();
		v2.connect(address);

		HashMap<String, VaultageHandler> handlers = new HashMap<String, VaultageHandler>();
		handlers.put(VaultageHandler.class.getName(), new VaultageHandler() {
			public void run() {
				System.out.println("Received message: " + this.message.getValue("value"));

			}
		});
		Thread t2 = v2.subscribe("bob",
				Base64.getEncoder().encodeToString(receiverKeyPair.getPrivate().getEncoded()), handlers);

		VaultageMessage message = new VaultageMessage();
		message.setFrom("alice");
		message.setTo("bob");
		message.setOperation(VaultageHandler.class.getName());
		message.putValue("value", "Hello World!");
		Thread t1 = v1.sendMessage(message.getTo(),
				Base64.getEncoder().encodeToString(senderKeyPair.getPublic().getEncoded()),
				Base64.getEncoder().encodeToString(senderKeyPair.getPrivate().getEncoded()), message);

//		synchronized (t1) {
//			t1.wait();
//		}

//		synchronized (t2) {
//			t2.wait();
//		}

		Thread.sleep(1000);

		v1.disconnect();
		v2.disconnect();
		System.out.println("Finished!");
	}

	/***
	 * 
	 */
	public Vaultage() {
		threads = new HashSet<VaultageHandler>();
	}

	public Thread sendMessage(String topicId, String senderPublicKey, String senderPrivateKey,
			VaultageMessage message) throws InterruptedException {
		Thread brokerThread = new Producer(topicId, senderPublicKey, senderPrivateKey, message);
		brokerThread.setDaemon(false);
		brokerThread.setName(brokerThread.getClass().getName() + "-" + topicId);
		brokerThread.start();
		return brokerThread;
	}

	public Thread subscribe(String topicId, String receiverPrivateKey, Map<String, VaultageHandler> handlers) throws InterruptedException {
		this.isListening = true;
		Thread brokerThread = new Consumer(topicId, receiverPrivateKey, handlers);
		brokerThread.setDaemon(true);
		brokerThread.setName(brokerThread.getClass().getName() + "-" + topicId);
		brokerThread.start();
		return brokerThread;
	}

	public boolean connect(String address) throws Exception {
		this.address = address;
		connectionFactory = new ActiveMQConnectionFactory(this.address);
		connection = connectionFactory.createConnection();
		connection.start();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		return true;
	}

	public void disconnect() throws Exception {
		this.unsubscribe();

		for (VaultageHandler h : threads) {
			if (h.isAlive()) {
				h.interrupt();
			}
		}
//		session.rollback();
		session.close();
		connection.stop();
		connection.close();
	}

	private class Producer extends Thread {

		String topicId;
		String senderPublicKey;
		String senderPrivateKey;
		VaultageMessage message;
		String text;

		public Producer(String topicId, String senderPublicKey, String senderPrivateKey, VaultageMessage message) {
			this.topicId = topicId;
			this.senderPublicKey = senderPublicKey;
			this.senderPrivateKey = senderPrivateKey;
			this.message = message;
		}

		public void run() {
			try {
//				System.out.println("Send to: " + topicId);

				// Create the destination (Topic or Queue)
				Destination destination = session.createTopic(topicId);

				// Create a MessageProducer from the Session to the Topic or Queue
				MessageProducer producer = session.createProducer(destination);
				producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

				// Create a message
				text = Gson.toJson(message).trim();

				// encrypt message
				String encryptedMessage = VaultageEncryption.doubleEncrypt(text, topicId, senderPrivateKey).trim();

				TextMessage message = session.createTextMessage(this.senderPublicKey + encryptedMessage);

				producer.send(message);

				expectedReplyTokens.add(this.message.getToken());
//				System.out.println("SENT MESSAGE: " + topicId + "\n" + text);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private class Consumer extends Thread {
		private String topicId;
		private String senderPublicKey;
		private String receiverPrivateKey;
		private String json;
		private Map<String, VaultageHandler> handlers;

		public Consumer(String topicId, String receiverPrivateKey, Map<String, VaultageHandler> handlers) {
			this.topicId = topicId;
			this.handlers = handlers;
			this.receiverPrivateKey = receiverPrivateKey;
		}

		public void run() {
			try {

				// Create the destination (Topic or Queue)
//				System.out.println(session + " - " + queueId);
				Destination destination = session.createTopic(topicId);

				// Create a MessageConsumer from the Session to the Topic or Queue
				MessageConsumer consumer = session.createConsumer(destination);

//				System.out.println("Listening to " + topicId);
				// listen forever until listening is turned off
				while (isListening) {

					// Wait for a message, 0 means listen forever
					Message message = consumer.receive(0);

					if (message != null) {

						TextMessage textMessage = (TextMessage) message;

						String mergedMessage = textMessage.getText();
						this.senderPublicKey = mergedMessage.substring(0, 128);
//						System.out.println("X: " + this.senderPublicKey);
						String encryptedMessage = mergedMessage.substring(128, mergedMessage.length());

//						System.out.println("M: " + encryptedMessage);
//						System.out.println("B:" + this.senderPublicKey.getBytes().length + ", " + encryptedMessage.getBytes().length);
//						System.out.println("S:" + this.senderPublicKey.length() + ", " + encryptedMessage.length());
//						System.out.println("A:" + textMessage.getText().length());

						json = VaultageEncryption.doubleDecrypt(encryptedMessage, senderPublicKey, receiverPrivateKey);

//						System.out.println("RECEIVED MESSAGE: " + topicId + "\n" + json);

						VaultageMessage vaultageMessage = Gson.fromJson(json, VaultageMessage.class);
						String operation = vaultageMessage.getOperation();
						
						VaultageHandler handler = handlers.get(operation);
						if (handler != null && !handler.isAlive()) {
							threads.add(handler);
//							System.out.println("Run: " + handler.getName());
							handler.execute(topicId, vaultageMessage);
						} else {
//							System.out.println();
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void unsubscribe() {
		this.isListening = false;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

}