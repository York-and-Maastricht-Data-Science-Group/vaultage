package org.rdbd.core.server;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

public class RDBDServer {

	public static void main(String[] args) {
		sendMessage("A", "Hello World!!!");
	}

	public static void sendMessage(String text) {
		thread(new Producer(text), false);
	}

	public static void sendMessage(String queueId, String text) {
		thread(new Producer(queueId, text), false);
	}

	public static void sendMessage(String url, String queueId, String text) {
		thread(new Producer(url, queueId, text), false);
	}

	private static void thread(Runnable runnable, boolean daemon) {
		Thread brokerThread = new Thread(runnable);
		brokerThread.setDaemon(daemon);
		brokerThread.start();
	}

	private static class Producer implements Runnable {

		String url;
		String queueId;
		String text;

		public Producer(String text) {
			url = ActiveMQConnection.DEFAULT_BROKER_URL;
//			url = "vm://localhost";
			queueId = "";
			this.text = text;
		}

		public Producer(String queue, String text) {
			url = ActiveMQConnection.DEFAULT_BROKER_URL;
			this.queueId = queue;
			this.text = text;
		}

		public Producer(String url, String queue, String text) {
			this.url = url;
			this.queueId = queue;
			this.text = text;
		}

		public void run() {
			try {

				// Create a ConnectionFactory
				ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);

				// Create a Connection
				Connection connection = connectionFactory.createConnection();
				connection.start();

				// Create a Session
				Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

				// Create the destination (Topic or Queue)
				Destination destination = session.createQueue(queueId);

				// Create a MessageProducer from the Session to the Topic or Queue
				MessageProducer producer = session.createProducer(destination);
				producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

				// Create a messages
				TextMessage message = session.createTextMessage(text);

				// Tell the producer to send the message
				producer.send(message);
				System.out.println("SENT MESSAGE: " + queueId + "\n" + text);

				// Clean up
				session.close();
				connection.close();
			} catch (Exception e) {
				System.out.println("Caught: " + e);
				e.printStackTrace();
			}
		}
	}
}