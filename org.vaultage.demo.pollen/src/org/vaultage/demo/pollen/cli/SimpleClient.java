package org.vaultage.demo.pollen.cli;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import org.vaultage.core.Vaultage;
import org.vaultage.core.VaultageMessage;
import org.vaultage.core.VaultageServer;
import org.vaultage.demo.pollen.NumberPoll;
import org.vaultage.demo.pollen.OnPollReceivedListener;
import org.vaultage.demo.pollen.PollAnswer;
import org.vaultage.demo.pollen.PollenBroker;
import org.vaultage.demo.pollen.RemoteUser;
//import org.vaultage.demo.pollen.SendNumberPollRequestHandler;
import org.vaultage.demo.pollen.SendNumberPollResponseHandler;
import org.vaultage.demo.pollen.User;
import org.vaultage.demo.pollen.test.UnitTestNumberPollResponseHandler;
//import org.vaultage.demo.pollen.custom.CustomRemoteUser;
import org.vaultage.demo.pollen.util.PollenUtil;

/***
 * NOTE: So far, this demo can only run with the REAL ActiveMQ server. It will
 * NOT work if we use the VIRTUAL ActiveMQ. To run this demo, start the ActiveMQ
 * server, and then run this demo.
 * 
 * It can accomodate any number of participants as long as the users responding
 * polls are started and listening (option 2) before the person starting the
 * poll does so
 * 
 * Note: delete the contents of the keys directory before starting any test
 * unless using the same number of participants as files are in the directory
 * (fixes problems when certain participant is not started but its file key is
 * found)
 * 
 * I will figure out this later.
 */

public class SimpleClient {

	static PollenBroker BROKER;

	public static final int STARTING_POLL = 0;
	public static final int ANSWERING_POLLS = 1;
	public static final int EXIT = 2;

	public static final double MIN_RANDOM = 100;
	public static final double MAX_RANDOM = 200;

	// TODO: treat this storage in a more elegant way (good enough for the demo)
	static Map<String, Double> poll2fakeValue = new HashMap<>();

	public static void main(String[] args) throws Exception {

		try {
			BROKER = new PollenBroker();
			BROKER.start(PollenBroker.BROKER_ADDRESS);
		} catch (Exception e) {
		}

		final Scanner scan = new Scanner(System.in);

		User user = new User();
		user.setName(args[0]);
		PollenCLI.greetUser(user);

		// save public key to directory 'keys' to simplify sharing pub keys between
		// users
		PollenUtil.savePublicKey(user);

		// set the address of Vaultage server
		VaultageServer pollenServer = new VaultageServer(PollenBroker.BROKER_ADDRESS);
		user.register(pollenServer);

		// set sendNumberPollResponseHandler
		user.setSendNumberPollResponseHandler(new UnitTestNumberPollResponseHandler());

		// an implementation of the OnPollReceivedListener class
		// it shows the question to a user and asks to type the answer
		user.setOnPollReceivedListener(new OnPollReceivedListener() {
			@Override
			public void onPollReceived(User user, NumberPoll poll) {
				System.out.println(poll.getQuestion());
				System.out.print("Type your answer (numeric): ");
				PollAnswer pollAnswer = user.getPollAnswer(poll.getId());
				double answer = 0;
				
				boolean isCorrect = false;
				while (!isCorrect) {
					String temp = scan.nextLine();
					try {
						answer = Double.valueOf(temp);
						isCorrect = true;
					} catch (Exception e) {
						System.out.println("Input is not a number!");
						System.out.print("Type your answer (numeric): ");
						isCorrect = false;
					}
				}
				
				pollAnswer.submitAnswer(answer);
			}
		});
		//----
		
		// select what the client will do
		System.out.println("Select one of these options:");
		int choice = PollenCLI.readOption(scan, Arrays.asList("Start a poll", "Answer polls", "Exit"));

		switch (choice) {
		case STARTING_POLL:
			System.out.println("Starting a poll");
			startAPoll(scan, user, pollenServer);
			break;
		case ANSWERING_POLLS:
			System.out.println("Waiting for incoming polls");
			break;

		}

		Thread.sleep(120000);
		System.out.println("Shutting down ...");
		scan.close();
		BROKER.stop();
		System.out.println("Terminated");
	}

	private static void startAPoll(Scanner scan, User localUser, VaultageServer vaultageServer) throws Exception {

		NumberPoll newPoll = PollenCLI.createNumberPoll(scan);
		newPoll.setOriginator(localUser.getPublicKey());

		// participants stores the list of public keys
		List<String> participants = PollenUtil.getParticipants(localUser.getName());
		newPoll.setParticipants(participants);

		if (participants.size() > 0) {
			String remoteUserPK = participants.get(0);
			RemoteUser remoteUser = new RemoteUser(localUser, remoteUserPK);
			synchronized (localUser.getSendNumberPollResponseHandler()) {
				String token = remoteUser.sendNumberPoll(newPoll);
				localUser.getPolls().put(token, newPoll);
				SendNumberPollResponseHandler handler = localUser.getSendNumberPollResponseHandler();
				handler.wait();
			}
			double fakeTotal = ((UnitTestNumberPollResponseHandler) localUser.getSendNumberPollResponseHandler()).getResult();
			System.out.println("Fake Total = " + fakeTotal);
			double realTotal = fakeTotal - localUser.getPollFakeValue(newPoll.getId()) ;
			System.out.println("Real Total = " + realTotal);
			System.console();
		} else {
			System.out.println("No participants available. Please run another User to share its public key.");
		}
	}

}