package org.vaultage.demo.pollen.app.cli;

import java.util.Arrays;
import java.util.Scanner;

import org.vaultage.demo.pollen.app.User;

public class SimpleClient {

	public static final int STARTING_POLL = 0;
	public static final int ANSWERING_POLLS = 1;

	public static void main(String[] args) throws Exception {
		User user = new User();
		user.setName(args[0]);
		PollenCLI.greetUser(user);

		// select what the client will do
		Scanner scan = new Scanner(System.in);

		System.out.println("Select one of these options:");
		int choice = PollenCLI.readOption(scan,
				Arrays.asList("Start a poll", "Answer polls"));

		switch (choice) {
		case STARTING_POLL:
			System.out.println("Starting a poll");
			// if starting a poll, offer the existing ones 
			// ask for public keys
			// when finished, click intro
			// send message to others, and start waiting
			// when receiving the results back, show in screen
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
}
