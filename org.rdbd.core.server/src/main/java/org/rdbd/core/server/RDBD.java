package org.rdbd.core.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class RDBD implements MessageListener {

	private String url;
	private ActiveMQConnectionFactory connectionFactory;
	private Connection connection;
	private Session session;
	private boolean isListening;
	private Gson gson;
	private Set<Thread> threads;

	/***
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

//		String address = ActiveMQConnection.DEFAULT_BROKER_URL;
		String address = "vm://localhost";
		RDBD rdbd1 = new RDBD();
		rdbd1.connect(address);

		RDBD rdbd2 = new RDBD();
		rdbd2.connect(address);

		HashMap<String, RDBDHandler> handlers = new HashMap<String, RDBDHandler>();
		handlers.put(RDBDHandler.class.getName(), new RDBDHandler() {
			public void run() {
				System.out.println("Received message: " + this.message.getValue());

			}
		});
		Thread t2 = rdbd2.listenMessage("bob", handlers);

		RDBDMessage message = new RDBDMessage();
		message.setFrom("alice");
		message.setTo("bob");
		message.setOperation(RDBDHandler.class.getName());
		message.setValue("Hello World!");
		Thread t1 = rdbd1.sendMessage(message.getTo(), message);

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
	public RDBD() {
		gson = new GsonBuilder().setPrettyPrinting().create();
		threads = new HashSet<Thread>();
	}

	public Thread sendMessage(String queueId, RDBDMessage message) {
		return thread(new Producer(queueId, message), false);
	}

	public Thread listenMessage(String queueId, Map<String, RDBDHandler> handlers) {
		this.isListening = true;
		return thread(new Consumer(queueId, handlers), false);
	}

	private Thread thread(Runnable runnable, boolean daemon) {
		Thread brokerThread = new Thread(runnable);
		brokerThread.setDaemon(daemon);
		brokerThread.start();
		return brokerThread;
	}

	public boolean connect(String url) throws Exception {
		this.url = url;
		connectionFactory = new ActiveMQConnectionFactory(this.url);
		connection = connectionFactory.createConnection();
		connection.start();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		return true;
	}

	public void disconnect() throws Exception {
		this.stopListening();

		for(Thread t: threads) {
			if (t.isAlive()) {
				t.interrupt();
			}
		}

		session.close();
		connection.stop();
		connection.close();
	}

	private class Producer implements Runnable {

		String queueId;
		RDBDMessage message;
		String text;

		public Producer(String queue, RDBDMessage message) {
			this.queueId = queue;
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
				text = gson.toJson(message);
				TextMessage message = session.createTextMessage(text);

				producer.send(message);
				System.out.println("SENT MESSAGE: " + queueId + "\n" + text);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private class Consumer implements Runnable {
		private String queueId;
		private String json;
		private Map<String, RDBDHandler> handlers;

		public Consumer(String queueId, Map<String, RDBDHandler> handlers) {
			this.queueId = queueId;
			this.handlers = handlers;
		}

		public void run() {
			try {

				// Create the destination (Topic or Queue)
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

						json = textMessage.getText();
						System.out.println("RECEIVED MESSAGE: " + queueId + "\n" + json);

						RDBDMessage rdbdMessage = gson.fromJson(json, RDBDMessage.class);
						String operation = rdbdMessage.getOperation();

						RDBDHandler handler = handlers.get(operation);
						if (handler != null) {
							handler.execute(queueId, rdbdMessage);
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

	public void onMessage(Message message) {

	}
}