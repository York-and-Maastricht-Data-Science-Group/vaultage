package org.vaultage.demo.pollen.cli;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.vaultage.core.VaultageServer;
import org.vaultage.demo.pollen.NumberPoll;
import org.vaultage.demo.pollen.PollenBroker;
import org.vaultage.demo.pollen.RemoteUser;
import org.vaultage.demo.pollen.SendNumberPollResponseHandler;
import org.vaultage.demo.pollen.User;
import org.vaultage.demo.pollen.util.PollenUtil;

/***
 * This demo can accomodate any number of participants as long as the users
 * responding polls are started and listening (option 2) before the user
 * starting the poll does so
 * 
 * Note: delete the contents of the keys directory before starting any test
 * unless using the same number of participants as files are in the directory
 * (fixes problems when certain participant is not started but its file key is
 * found)
 * 
 * Second Note: Before starting any clients, start the Pollen Broker with the
 * "launchClients/LaunchPollenBroker.launch" run configuration
 */

public class SimpleClient {

	static PollenBroker BROKER;

	public static final int STARTING_POLL = 0;
	public static final int ANSWERING_POLLS = 1;
	public static final int EXIT = 2;

	public static void main(String[] args) throws Exception {

		final Scanner scan = new Scanner(System.in);

		User user = new User();
		user.setName(args[0]);
		PollenCLI.greetUser(user);

		// save public key to directory 'keys' to simplify sharing keys between users
		PollenUtil.savePublicKey(user);

		// set the address of Vaultage server
		VaultageServer pollenServer = new VaultageServer(PollenBroker.BROKER_ADDRESS);
		user.register(pollenServer);

		// set sendNumberPollResponseHandler
		user.setSendNumberPollResponseHandler(new SendNumberPollResponseHandler() {

			@Override
			public void run(User me, RemoteUser other, String responseToken, java.lang.Double result) throws Exception {
				NumberPoll poll = me.getPendingNumberPollByResponseToken(responseToken);
				if (poll != null) {
					int index = poll.getParticipants().indexOf(me.getPublicKey());
					String previousParticipant;
					if (index == 0) {
						previousParticipant = poll.getOriginator();
					}
					else {
						previousParticipant = poll.getParticipants().get(index - 1);
					}
					RemoteUser previous = new RemoteUser(me, previousParticipant);
					String requestToken = me.getMappedRequestToken(responseToken);
					PollenCLI.displayPoll(poll);
					double myResponse = PollenCLI.readValue(scan);
					previous.respondToSendNumberPoll(result + myResponse, requestToken);
				}
				else {
					poll = me.getInitiatedNumberPoll(responseToken);
					if (poll != null) {
						synchronized (me.getSendNumberPollResponseHandler()) {
							me.addNumberPollAnswer(poll.getId(), result);
							me.getSendNumberPollResponseHandler().notify();
						}
					}
					else {
						throw new RuntimeException("I should be either originator or participant of the poll!");
					}
				}
			}
		});

		int choice;
		do {
			System.out.println("Select one of these options:");
			choice = PollenCLI.readOption(scan, Arrays.asList("Start a poll", "Answer polls", "Exit"));

			switch (choice) {
			case STARTING_POLL:
				System.out.println("Starting a poll");
				startAPoll(scan, user, pollenServer);
				break;
			case ANSWERING_POLLS:
				System.out.println("Waiting for incoming polls.");
				Thread.sleep(240000);
				break;
			}
		} while (choice != EXIT);
    
		scan.close();
		user.unregister();
		System.out.println("Terminated");
	}

	private static void startAPoll(Scanner scan, User localUser, VaultageServer vaultageServer) throws Exception {

		NumberPoll newPoll = PollenCLI.createNumberPoll(scan);
		newPoll.setOriginator(localUser.getPublicKey());

		List<String> participants = PollenUtil.getParticipants(localUser.getName());
		newPoll.setParticipants(participants);

		if (participants.size() > 0) {
			String firstParticipant = participants.get(0);
			RemoteUser remoteUser = new RemoteUser(localUser, firstParticipant);
			synchronized (localUser.getSendNumberPollResponseHandler()) {
				String token = remoteUser.sendNumberPoll(newPoll);
				localUser.addInitiatedNumberPoll(newPoll, token);
				localUser.getSendNumberPollResponseHandler().wait();
			}
			double totalResult = localUser.getNumberPollAnswer(newPoll.getId());
			System.out.println("Total result (including fake value) = " + totalResult);
			double realResult = totalResult - localUser.getNumberPollFakeValue(newPoll.getId());
			System.out.println("Real Total = " + realResult);
		} else {
			System.out.println("No participants available. Please run another User to share its public key.");
		}
	}

}