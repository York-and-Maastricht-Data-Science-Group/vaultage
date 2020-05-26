package org.vaultage.demo.pollen.app.cli;

import java.util.List;
import java.util.Scanner;

import org.vaultage.demo.pollen.app.User;
import org.vaultage.demo.pollen.data.PollRepository;
import org.vaultage.demo.pollen.gen.MultivaluedPoll;
import org.vaultage.demo.pollen.gen.NumberPoll;

public class PollenCLI {

	public static void main(String[] args) {
		Scanner s = new Scanner(System.in);

		MultivaluedPoll mp = PollRepository.createElectionsPoll();
		System.out.println(mp.getQuestion());
		int option = readOption(s, mp.getOptions());
		System.out.printf("Inserted %s\n", mp.getOptions().get(option));

		System.out.println();

		NumberPoll np = PollRepository.createSalaryPoll();
		System.out.println(np.getQuestion());
		double value = readValue(s);
		System.out.printf("Inserted %f\n", value);
		
		System.out.println("Done.");
		s.close();
	}

	public static void displayPoll(NumberPoll poll) {
		System.out.println(poll.getQuestion());
	}

	public static void displayPoll(MultivaluedPoll poll) {
		System.out.println(poll.getQuestion());
		List<String> options = poll.getOptions();
		for (int i = 0; i < options.size(); i++) {
			System.out.printf("  %d. %s\n", i + 1, options.get(i));
		}
	}

	public static void greetUser(User user) {
		System.out.printf("%s, welcome to Pollen\n", user.getName());
		System.out.printf("Your public key is %s\n", user.getPublicKey());
		System.out.println();
	}

	public static int readOption(Scanner s, List<String> options) {
		int choice = -1;
		do {
			for (int i = 0; i < options.size(); i++) {
				System.out.printf("  %d. %s\n", i + 1, options.get(i));
			}
			System.out.print("Insert your selection: ");
			choice = s.nextInt();
		} while (choice < 0 || choice > options.size());

		return choice - 1;
	}

	public static double readValue(Scanner s) {
		System.out.print("Value : ");
		double d = s.nextDouble();
		return d;
	}
}
