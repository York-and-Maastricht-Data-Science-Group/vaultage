package org.vaultage.demo.pollen.app.cli;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.vaultage.core.VaultageServer;
import org.vaultage.demo.pollen.app.SendNumberPollRequestHandler;
import org.vaultage.demo.pollen.app.SendNumberPollResponseHandler;
import org.vaultage.demo.pollen.app.User;
import org.vaultage.demo.pollen.data.PollRepository;
import org.vaultage.demo.pollen.gen.NumberPoll;
import org.vaultage.demo.pollen.gen.RemoteRequester;
import org.vaultage.demo.pollen.util.PollenUtil;

public class SimpleClient {

	public static final int STARTING_POLL = 0;
	public static final int ANSWERING_POLLS = 1;

	public static void main(String[] args) throws Exception {
		User user = new User();
		user.setName(args[0]);
		PollenCLI.greetUser(user);

		// save public key to directory 'keys' to simplify sharing pub keys between
		// users
		PollenUtil.savePublicKey(user);
		
		// set user request and response handlers
		user.setSendNumberPollRequestBaseHandler(new SendNumberPollRequestHandler());
		user.setSendNumberPollResponseBaseHandler(new SendNumberPollResponseHandler());

		// setting the address of Vaultage server
		String address = "vm://localhost";
		VaultageServer pollen = new VaultageServer(address);
		
		// connect to activemq
		user.register(pollen);

		// select what the client will do
		Scanner scan = new Scanner(System.in);

		System.out.println("Select one of these options:");
		int choice = PollenCLI.readOption(scan, Arrays.asList("Start a poll", "Answer polls"));

		switch (choice) {
		case STARTING_POLL:
			System.out.println("Starting a poll");
			startAPoll(user, pollen);

			break;
		case ANSWERING_POLLS:
			System.out.println("Answering polls");
			// if answering polls, wait for the poll
			// when reached, show the question in the console and wait for the answer
			// send answer to the next
			break;
		}
		scan.close();
	}

	private static void startAPoll(User user, VaultageServer vaultageServer) throws Exception {
		List<String> participants = PollenUtil.getColleagues();
		participants.remove(user.getName());
		System.out.println("Create salary poll ...");
		NumberPoll poll = PollRepository.createSalaryPoll();
		poll.setOriginator(user.getPublicKey());
		poll.setParticipants(participants);
		RemoteRequester remoteRequester = new RemoteRequester(vaultageServer, user);

		if (participants.size() > 0) {
			System.out.println("Send the poll to " + participants.get(0) + " ...");
			String requesteePublicKey = PollenUtil.getPublicKey(participants.get(0));
			remoteRequester.sendNumberPoll(requesteePublicKey, poll);
		} else {
			System.out.println("No participants available. Please run another User to share its public key.");
		}

	}

}
