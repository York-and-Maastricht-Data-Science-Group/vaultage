package org.vaultage.demo.pollen.app.cli;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.apache.activemq.ActiveMQConnection;
import org.vaultage.core.Vaultage;
import org.vaultage.core.VaultageMessage;
import org.vaultage.core.VaultageServer;
import org.vaultage.demo.pollen.app.SendNumberPollRequestHandler;
import org.vaultage.demo.pollen.app.SendNumberPollResponseHandler;
import org.vaultage.demo.pollen.app.User;
import org.vaultage.demo.pollen.data.PollRepository;
import org.vaultage.demo.pollen.gen.NumberPoll;
import org.vaultage.demo.pollen.gen.RemoteRequester;
import org.vaultage.demo.pollen.util.PollenUtil;

/***
 * NOTE: 
 * So far, this demo can only run with the REAL ActiveMQ server. It will NOT work if we use the VIRTUAL ActiveMQ.
 * To run this demo, start the ActiveMQ server, and then run this demo. 
 * Also, it can only accommodate 3 users. Charlie and Bob should be started and listening/subscribing first before
 * Alice starts a polling. Otherwise, Charlie and Bob cannot get Alice's poll.
 * I will figure out this later.       
 */

public class SimpleClient {

	public static final int STARTING_POLL = 0;
	public static final int ANSWERING_POLLS = 1;

	public static void main(String[] args) throws Exception {

		Scanner scan = new Scanner(System.in);

		User user = new User();
		user.setName(args[0]);
		PollenCLI.greetUser(user);

		// save public key to directory 'keys' to simplify sharing pub keys between
		// users
		PollenUtil.savePublicKey(user);

		// set the address of Vaultage server
		String address = ActiveMQConnection.DEFAULT_BROKER_URL;
//		String address = "vm://localhost";
		VaultageServer pollen = new VaultageServer(address);

		// set handlers
		user.setSendNumberPollRequestBaseHandler(new SendNumberPollRequestHandler(scan) {

			@Override
			public double run(VaultageMessage senderMessage) throws Exception {
				User localVault = (User) this.vault;
				String json = senderMessage.getValue("poll");
				NumberPoll poll = Vaultage.Gson.fromJson(json, NumberPoll.class);

				if (localVault.getPublicKey().equals(poll.getOriginator())) {
					double myFakeSalary = 1;
					result = myFakeSalary;
					return (double) result;
				} else {
					localVault.getPolls().put(poll.getId(), poll);

					PollenCLI.displayPoll(poll);

					double total = 0;

					String mySalary = "";

					System.out.print("Answer: ");
					if (scan.hasNext())
						mySalary = scan.next();

					for (String publicKey : poll.getParticipants()) {
						if (!publicKey.equals(localVault.getPublicKey())
								&& !publicKey.equals(senderMessage.getFrom())) {
							total = total + (new RemoteRequester(localVault.getVaultageServer(), localVault))
									.sendNumberPoll(publicKey, poll);
						}
					}

					result = total + Double.valueOf(mySalary);
					return (double) result;
				}
			}
		});
		user.setSendNumberPollResponseBaseHandler(new SendNumberPollResponseHandler());

		// select what the client will do

		System.out.println("Select one of these options:");
		int choice = PollenCLI.readOption(scan, Arrays.asList("Start a poll", "Answer polls"));

		switch (choice) {
		case STARTING_POLL:
			System.out.println("Starting a poll");
			startAPoll(user, pollen);
			break;
		case ANSWERING_POLLS:
			System.out.println("Answering polls");
			System.out.println("Waiting for incoming polls ...");
			answerAPoll(user, pollen);
			break;
		}
		
		synchronized (Thread.currentThread()) {
			Thread.currentThread().wait(120000);
		}
		
		scan.close();
	}

	private static void startAPoll(User user, VaultageServer vaultageServer) throws Exception {
		List<String> participants = PollenUtil.getParticipants();
		System.out.println("Create salary poll ...");
		NumberPoll poll = PollRepository.createSalaryPoll();
		poll.setOriginator(user.getPublicKey());
		poll.setParticipants(participants);

		if (participants.size() > 0) {
			// connect to activemq
			user.register(vaultageServer);

			int userIndex = participants.indexOf(user.getPublicKey());
			int nextUserIndex = userIndex + 1;
			if (nextUserIndex == participants.size()) {
				nextUserIndex = 0;
			}
			String nextParticipant = participants.get(nextUserIndex);
			System.out.println("Send the poll to " + nextParticipant + " ...");
			RemoteRequester remoteRequester = new RemoteRequester(vaultageServer, user);
			double total = remoteRequester.sendNumberPoll(nextParticipant, poll);
			System.out.println("Fake total: " + total);
		} else {
			System.out.println("No participants available. Please run another User to share its public key.");
		}
	}

	private static void answerAPoll(User user, VaultageServer vaultageServer) throws Exception {
		user.register(vaultageServer);
	}

}
