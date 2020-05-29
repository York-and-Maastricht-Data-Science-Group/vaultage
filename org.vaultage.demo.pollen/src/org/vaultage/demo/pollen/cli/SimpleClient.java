package org.vaultage.demo.pollen.cli;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.activemq.ActiveMQConnection;
import org.vaultage.core.Vaultage;
import org.vaultage.core.VaultageMessage;
import org.vaultage.core.VaultageServer;
import org.vaultage.demo.pollen.SendNumberPollRequestHandler;
import org.vaultage.demo.pollen.SendNumberPollResponseHandler;
import org.vaultage.demo.pollen.User;
import org.vaultage.demo.pollen.custom.CustomRemoteUser;
import org.vaultage.demo.pollen.NumberPoll;
import org.vaultage.demo.pollen.util.PollenUtil;

/***
 * NOTE:
 * So far, this demo can only run with the REAL ActiveMQ server. It will NOT work if we use the VIRTUAL ActiveMQ.
 * To run this demo, start the ActiveMQ server, and then run this demo.
 * 
 * It can accomodate any number of participants as long as the users responding
 * polls are started and listening (option 2) before the person starting the poll does so
 * 
 * Note: delete the contents of the keys directory before starting any test unless
 * using the same number of participants as files are in the directory
 * (fixes problems when certain participant is not started but its file key is found)
 * 
 * I will figure out this later.
 */

public class SimpleClient {

	public static final int STARTING_POLL = 0;
	public static final int ANSWERING_POLLS = 1;

	public static final double MIN_RANDOM = 1000000;
	public static final double MAX_RANDOM = 10000000;

	// TODO: treat this storage in a more elegant way (good enough for the demo)
	static Map<String, Double> poll2fakeValue = new HashMap<>();

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
		VaultageServer pollenServer = new VaultageServer(address);
		user.register(pollenServer);

		// set handlers
		user.setSendNumberPollRequestBaseHandler(new SendNumberPollRequestHandler(scan) {

			@Override
			public double run(VaultageMessage senderMessage) throws Exception {
				User localVault = (User) this.vault;
				String json = senderMessage.getValue("poll");
				NumberPoll poll = Vaultage.Gson.fromJson(json, NumberPoll.class);

				if (localVault.getPublicKey().equals(poll.getOriginator())) {

					System.out.println("The poll request is back to me as the originator");
					
					double fakeValue = ThreadLocalRandom.current().nextDouble(MIN_RANDOM, MAX_RANDOM);
					result = fakeValue;
					poll2fakeValue.put(poll.getId(), fakeValue);


					System.out.printf("Sending fake value: %f\n", fakeValue);

					return (double) result;

				} else {
					localVault.getPolls().put(poll.getId(), poll);

					System.out.println("\nNew poll received!");
					
					List<String> participants = poll.getParticipants();
					int index = participants.indexOf(localVault.getPublicKey());
					
					System.out.printf("I am participant %d\n\n", index + 1);

					PollenCLI.displayPoll(poll);
					double answer = PollenCLI.readValue(scan);

					String nextParticipant;
					if (index + 1 < participants.size()) {
						nextParticipant = participants.get(index + 1);
						System.out.println("\nSending to next participant");
					}
					else {
						// we were the last participant. back to originator
						nextParticipant = poll.getOriginator();
						System.out.println("\nSending back to originator");
					}
					double currentTotal =
							(new CustomRemoteUser(
									localVault.getVaultageServer(),
									localVault,
									nextParticipant))
							.sendNumberPoll(poll);
					
					System.out.printf("\nCurrent accumulated value: %f\n", currentTotal);
					
					result = currentTotal + answer;
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
			startAPoll(scan, user, pollenServer);
			break;
		case ANSWERING_POLLS:
			System.out.println("Waiting for incoming polls");
			break;
		}
		
		synchronized (Thread.currentThread()) {
			Thread.currentThread().wait(120000);
		}
		
		scan.close();
	}

	private static void startAPoll(Scanner scan, User user, VaultageServer vaultageServer) throws Exception {

		NumberPoll poll = PollenCLI.createNumberPoll(scan);
		poll.setOriginator(user.getPublicKey());
		// participants stores the list of public keys
		List<String> participants = PollenUtil.getParticipants(user.getName());
		poll.setParticipants(participants);

		if (participants.size() > 0) {
			String firstParticipant = participants.get(0);
			System.out.println("Sending the poll to " + firstParticipant);
			CustomRemoteUser remoteUser =
					new CustomRemoteUser(vaultageServer, user, firstParticipant);
			double total = remoteUser.sendNumberPoll(poll);
			System.out.println("Fake total: " + total);
			double randomValue = poll2fakeValue.get(poll.getId());
			System.out.println("Real total: " + (total - randomValue));
		} else {
			System.out.println("No participants available. Please run another User to share its public key.");
		}
	}

}
