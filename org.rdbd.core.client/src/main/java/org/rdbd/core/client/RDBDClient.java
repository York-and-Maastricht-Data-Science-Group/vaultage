package org.rdbd.core.client;

import java.util.ArrayList;
import java.util.List;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

public class RDBDClient {

	public static boolean terminate = false;
	
	public static void main(String[] args) {
		listenMessage("A", new ArrayList<RDBDAction>());
	}

	public static void listenMessage(List<RDBDAction> actions) {
		thread(new Consumer(actions), false);
	}

	public static void listenMessage(String queueId, List<RDBDAction> actions) {
		thread(new Consumer(queueId, actions), false);
	}

	public static void listenMessage(String url, String queueId, List<RDBDAction> actions) {
		thread(new Consumer(url, queueId, actions), false);
	}

	private static void thread(Runnable runnable, boolean daemon) {
		Thread brokerThread = new Thread(runnable);
		brokerThread.setDaemon(daemon);
		brokerThread.start();
	}

	public static class Consumer implements Runnable, ExceptionListener {

		String url;
		String queueId;
		String text;
		List<RDBDAction> actions;

		public Consumer(List<RDBDAction> actions) {
			url = ActiveMQConnection.DEFAULT_BROKER_URL;
//			url = "vm://localhost";
			queueId = "";
			this.actions = actions;
		}

		public Consumer(String queue, List<RDBDAction> actions) {
			url = ActiveMQConnection.DEFAULT_BROKER_URL;
			this.queueId = queue;
			this.actions = actions;
		}

		public Consumer(String url, String queue, List<RDBDAction> actions) {
			this.url = url;
			this.queueId = queue;
			this.actions = actions;
		}

		public void run() {
			try {

				// Create a ConnectionFactory
				ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);

				// Create a Connection
				Connection connection = connectionFactory.createConnection();
				connection.start();

				connection.setExceptionListener(this);

				// Create a Session
				Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

				// Create the destination (Topic or Queue)
				Destination destination = session.createQueue(queueId);

				// Create a MessageConsumer from the Session to the Topic or Queue
				MessageConsumer consumer = session.createConsumer(destination);

				while (!terminate) {
					// Wait for a message, 0 means listen forever
					Message message = consumer.receive(0);

					if (message instanceof TextMessage) {
						TextMessage textMessage = (TextMessage) message;
						text = textMessage.getText();
//						System.out.println("RECEIVED MESSAGE:\n" + text);

						// execute every action registered by client for each message
						for (RDBDAction action : actions) {
							action.execute(text);
						}
					}
				}

				consumer.close();
				session.close();
				connection.close();
			} catch (Exception e) {
				System.out.println("Caught: " + e);
				e.printStackTrace();
			}

//			listenMessage(queueId, actions);
		}

		public synchronized void onException(JMSException ex) {
			System.out.println("JMS Exception occured.  Shutting down client.");
		}
	}
}