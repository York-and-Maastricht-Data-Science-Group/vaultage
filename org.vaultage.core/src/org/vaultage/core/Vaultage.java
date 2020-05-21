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
		Vaultage rdbd1 = new Vaultage();
		rdbd1.connect(address);

		Vaultage rdbd2 = new Vaultage();
		rdbd2.connect(address);

		HashMap<String, VaultageHandler> handlers = new HashMap<String, VaultageHandler>();
		handlers.put(VaultageHandler.class.getName(), new VaultageHandler() {
			public void run() {
				System.out.println("Received message: " + this.message.getValue("value"));

			}
		});
		Thread t2 = rdbd2.listenMessage("bob",
				Base64.getEncoder().encodeToString(receiverKeyPair.getPrivate().getEncoded()), handlers);

		VaultageMessage message = new VaultageMessage();
		message.setFrom("alice");
		message.setTo("bob");
		message.setOperation(VaultageHandler.class.getName());
		message.putValue("value", "Hello World!");
		Thread t1 = rdbd1.sendMessage(message.getTo(),
				Base64.getEncoder().encodeToString(senderKeyPair.getPublic().getEncoded()),
				Base64.getEncoder().encodeToString(senderKeyPair.getPrivate().getEncoded()), message);

//		synchronized (t1) {
//			t1.wait();
//		}

//		synchronized (t2) {
//			t2.wait();
//		}

		Thread.sleep(1000);

		rdbd1.disconnect();
		rdbd2.disconnect();
		System.out.println("Finished!");
	}

	/***
	 * 
	 */
	public Vaultage() {
		threads = new HashSet<VaultageHandler>();
	}

	public Thread sendMessage(String queueId, String senderPublicKey, String senderPrivateKey,
			VaultageMessage message) throws InterruptedException {
		Thread brokerThread = new Producer(queueId, senderPublicKey, senderPrivateKey, message);
		brokerThread.setDaemon(false);
		brokerThread.start();
		return brokerThread;
	}

	public Thread listenMessage(String queueId, String receiverPrivateKey, Map<String, VaultageHandler> handlers) throws InterruptedException {
		this.isListening = true;
		Thread brokerThread = new Consumer(queueId, receiverPrivateKey, handlers);
		brokerThread.setDaemon(false);
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
		this.stopListening();

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

		String queueId;
		String senderPublicKey;
		String senderPrivateKey;
		VaultageMessage message;
		String text;

		public Producer(String queue, String senderPublicKey, String senderPrivateKey, VaultageMessage message) {
			this.queueId = queue;
			this.senderPublicKey = senderPublicKey;
			this.senderPrivateKey = senderPrivateKey;
			this.message = message;
		}

		public void run() {
			try {
				System.out.println("Send to: " + queueId);

				// Create the destination (Topic or Queue)
				Destination destination = session.createQueue(queueId);

				// Create a MessageProducer from the Session to the Topic or Queue
				MessageProducer producer = session.createProducer(destination);
				producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

				// Create a message
				text = Gson.toJson(message).trim();

				// encrypt message
				String encryptedMessage = VaultageEncryption.doubleEncrypt(text, queueId, senderPrivateKey).trim();

				TextMessage message = session.createTextMessage(this.senderPublicKey + encryptedMessage);

				producer.send(message);

				expectedReplyTokens.add(this.message.getToken());
				System.out.println("SENT MESSAGE: " + queueId + "\n" + text);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private class Consumer extends Thread {
		private String queueId;
		private String senderPublicKey;
		private String receiverPrivateKey;
		private String json;
		private Map<String, VaultageHandler> handlers;

		public Consumer(String queueId, String receiverPrivateKey, Map<String, VaultageHandler> handlers) {
			this.queueId = queueId;
			this.handlers = handlers;
			this.receiverPrivateKey = receiverPrivateKey;
		}

		public void run() {
			try {

				// Create the destination (Topic or Queue)
//				System.out.println(session + " - " + queueId);
				Destination destination = session.createQueue(queueId);

				// Create a MessageConsumer from the Session to the Topic or Queue
				MessageConsumer consumer = session.createConsumer(destination);

				System.out.println("Listening to " + queueId);
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

						System.out.println("RECEIVED MESSAGE: " + queueId + "\n" + json);

						VaultageMessage rdbdMessage = Gson.fromJson(json, VaultageMessage.class);
						String operation = rdbdMessage.getOperation();

						VaultageHandler handler = handlers.get(operation);
						if (handler != null && !handler.isAlive()) {
							threads.add(handler);
//							System.out.println("Run: " + handler.getName());
							handler.execute(queueId, rdbdMessage);
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

	public void stopListening() {
		this.isListening = false;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

}