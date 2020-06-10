package org.vaultage.demo.pollen.test;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.junit.Test;
import org.vaultage.core.Vaultage;
import org.vaultage.core.VaultageMessage;
import org.vaultage.core.VaultageServer;
import org.vaultage.demo.pollen.SendNumberPollRequestHandler;
import org.vaultage.demo.pollen.SendNumberPollResponseHandler;
import org.vaultage.demo.pollen.User;
import org.vaultage.demo.pollen.data.PollRepository;
import org.vaultage.demo.pollen.NumberPoll;
import org.vaultage.demo.pollen.RemoteRequester;
import org.vaultage.demo.pollen.util.PollenUtil;

public class PollenTest {

	Scanner scanner = new Scanner(System.in);

	@Test
	public void testSalaryPoll() throws Exception {

		// setting the address of Vaultage server
		String address = "vm://localhost";
		final VaultageServer pollenBroker = new VaultageServer(address);

		// Alice
		double aliceRealSalary = 50;
		User alice = new User();
		alice.setId("Alice");
		alice.setName("Alice");
		PollenUtil.savePublicKey(alice);
		alice.setSendNumberPollRequestBaseHandler(new SendNumberPollRequestHandler(scanner) {
			@Override
			public double run(VaultageMessage senderMessage, NumberPoll poll) throws Exception {
				User localVault = (User) this.vault;
				double total = localVault.sendNumberPoll(senderMessage.getFrom(), poll);
				double mySalary = aliceRealSalary;
				localVault.addPollRealValue(poll.getId(), mySalary);
				result = total + mySalary;
				return (double) result;
			}
		});
		alice.setSendNumberPollResponseBaseHandler(new SendNumberPollResponseHandler());

		// Bob
		User bob = new User();
		bob.setId("Bob");
		bob.setName("Bob");
		PollenUtil.savePublicKey(bob);
		bob.setSendNumberPollRequestBaseHandler(new SendNumberPollRequestHandler(scanner) {
			@Override
			public double run(VaultageMessage senderMessage, NumberPoll poll) throws Exception {
				User localVault = (User) this.vault;
//				System.out.print("(" + bob.getName() + ") My salary is = ");

				double total = localVault.sendNumberPoll(senderMessage.getFrom(), poll);

				double mySalary = 100.0;
				localVault.addPollRealValue(poll.getId(), mySalary);

				result = total + mySalary;
				return (double) result;
			}
		});
		bob.setSendNumberPollResponseBaseHandler(new SendNumberPollResponseHandler());

		// Charlie
		User charlie = new User();
		charlie.setId("Charlie");
		charlie.setName("Charlie");
		PollenUtil.savePublicKey(charlie);
		charlie.setSendNumberPollRequestBaseHandler(new SendNumberPollRequestHandler(scanner) {

			@Override
			public double run(VaultageMessage senderMessage, NumberPoll poll) throws Exception {
				User localVault = (User) this.vault;

				double total = localVault.sendNumberPoll(senderMessage.getFrom(), poll);
				double mySalary = 150.0;
				localVault.addPollRealValue(poll.getId(), mySalary);
				result = total + mySalary;
				return (double) result;
			}
		});
		charlie.setSendNumberPollResponseBaseHandler(new SendNumberPollResponseHandler());

		// register participants
		alice.register(pollenBroker);
		bob.register(pollenBroker);
		charlie.register(pollenBroker);

		List<String> participants = new ArrayList<>();
		participants.add(bob.getPublicKey());
		participants.add(charlie.getPublicKey());
		participants.add(alice.getPublicKey());

		// initialise salary poll
		NumberPoll salaryPoll = PollRepository.createSalaryPoll();
		salaryPoll.setOriginator(alice.getPublicKey());
		salaryPoll.setParticipants(participants);

		// send poll, initiated by Alice
		double fakeTotal = alice.getRemoteRequester().sendNumberPoll(participants.get(0), salaryPoll);
		double aliceFakeSalary = alice.getPollFakeValue(salaryPoll.getId());
		System.out.println("Total real salary = fakeTotal - aliceFakeSalary");
		System.out
				.println("Total real salary = " + String.valueOf(fakeTotal) + " - " + String.valueOf(aliceFakeSalary));
		double realTotal = fakeTotal - aliceFakeSalary;
		System.out.println("Total real salary = " + realTotal);
		double expectedTotal = alice.getPollRealValue(salaryPoll.getId()) + bob.getPollRealValue(salaryPoll.getId())
				+ charlie.getPollRealValue(salaryPoll.getId());
		System.out.println("expectedTotal = " + alice.getPollRealValue(salaryPoll.getId()) + " + "
				+ bob.getPollRealValue(salaryPoll.getId()) + " + " + charlie.getPollRealValue(salaryPoll.getId()));
		assertEquals(expectedTotal, realTotal, 0);

		alice.unregister();
		bob.unregister();
		charlie.unregister();
	}
	
	@Test
	public void testSalaryPollWithScanner() throws Exception {

		// setting the address of Vaultage server
		String address = "vm://localhost";
		final VaultageServer pollenBroker = new VaultageServer(address);

		// Alice
//		double aliceRealSalary = 50;
		User alice = new User();
//		alice.waitTime = 30000;
		alice.setId("Alice");
		alice.setName("Alice");
		PollenUtil.savePublicKey(alice);
		alice.setSendNumberPollRequestBaseHandler(new SendNumberPollRequestHandler(scanner) {
			@Override
			public double run(VaultageMessage senderMessage, NumberPoll poll) throws Exception {
				User localVault = (User) this.vault;
				
				System.out.print("(" + localVault.getName() + ") My salary is = ");
				double mySalary = scanner.nextDouble();
				localVault.addPollRealValue(poll.getId(), mySalary);
				
				double total = localVault.sendNumberPoll(senderMessage.getFrom(), poll);
			
				result = total + mySalary;
				return (double) result;
			}
		});
		alice.setSendNumberPollResponseBaseHandler(new SendNumberPollResponseHandler());

		// Bob
		User bob = new User();
//		bob.waitTime = 20000;
		bob.setId("Bob");
		bob.setName("Bob");
		PollenUtil.savePublicKey(bob);
		bob.setSendNumberPollRequestBaseHandler(new SendNumberPollRequestHandler(scanner) {
			@Override
			public double run(VaultageMessage senderMessage, NumberPoll poll) throws Exception {
				User localVault = (User) this.vault;
				
				System.out.print("(" + localVault.getName() + ") My salary is = ");
				double mySalary = scanner.nextDouble();
				localVault.addPollRealValue(poll.getId(), mySalary);

				double total = localVault.sendNumberPoll(senderMessage.getFrom(), poll);

				result = total + mySalary;
				return (double) result;
			}
		});
		bob.setSendNumberPollResponseBaseHandler(new SendNumberPollResponseHandler());

		// Charlie
		User charlie = new User();
//		charlie.waitTime = 10000;
		charlie.setId("Charlie");
		charlie.setName("Charlie");
		PollenUtil.savePublicKey(charlie);
		charlie.setSendNumberPollRequestBaseHandler(new SendNumberPollRequestHandler(scanner) {

			@Override
			public double run(VaultageMessage senderMessage, NumberPoll poll) throws Exception {
				User localVault = (User) this.vault;

				System.out.print("(" + localVault.getName() + ") My salary is = ");
				double mySalary = scanner.nextDouble();
				localVault.addPollRealValue(poll.getId(), mySalary);
				
				double total = localVault.sendNumberPoll(senderMessage.getFrom(), poll);

				result = total + mySalary;
				return (double) result;
			}
		});
		charlie.setSendNumberPollResponseBaseHandler(new SendNumberPollResponseHandler());

		// register participants
		alice.register(pollenBroker);
		bob.register(pollenBroker);
		charlie.register(pollenBroker);

		List<String> participants = new ArrayList<>();
		participants.add(bob.getPublicKey());
		participants.add(charlie.getPublicKey());
		participants.add(alice.getPublicKey());

		// initialise salary poll
		NumberPoll salaryPoll = PollRepository.createSalaryPoll();
		salaryPoll.setOriginator(alice.getPublicKey());
		salaryPoll.setParticipants(participants);

		// send poll, initiated by Alice
		double fakeTotal = alice.getRemoteRequester().sendNumberPoll(participants.get(0), salaryPoll);
		double aliceFakeSalary = alice.getPollFakeValue(salaryPoll.getId());
		System.out.println("Total real salary = fakeTotal - aliceFakeSalary");
		System.out
				.println("Total real salary = " + String.valueOf(fakeTotal) + " - " + String.valueOf(aliceFakeSalary));
		double realTotal = fakeTotal - aliceFakeSalary;
		System.out.println("Total real salary = " + realTotal);
		double expectedTotal = alice.getPollRealValue(salaryPoll.getId()) + bob.getPollRealValue(salaryPoll.getId())
				+ charlie.getPollRealValue(salaryPoll.getId());
		System.out.println("expectedTotal = " + alice.getPollRealValue(salaryPoll.getId()) + " + "
				+ bob.getPollRealValue(salaryPoll.getId()) + " + " + charlie.getPollRealValue(salaryPoll.getId()));
		assertEquals(expectedTotal, realTotal, 0);

		alice.unregister();
		bob.unregister();
		charlie.unregister();
	}

}
