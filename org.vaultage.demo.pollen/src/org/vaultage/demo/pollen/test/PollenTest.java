package org.vaultage.demo.pollen.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vaultage.core.VaultageServer;
import org.vaultage.demo.pollen.NumberPoll;
import org.vaultage.demo.pollen.OnPollReceivedListener;
import org.vaultage.demo.pollen.PollAnswer;
import org.vaultage.demo.pollen.PollenBroker;
import org.vaultage.demo.pollen.RemoteUser;
import org.vaultage.demo.pollen.SendNumberPollResponseHandler;
import org.vaultage.demo.pollen.User;
import org.vaultage.demo.pollen.data.PollRepository;

public class PollenTest {

	static PollenBroker BROKER;

	// Scanner scanner = new Scanner(System.in);

	@BeforeClass
	public static void startBroker() throws Exception {
		BROKER = new PollenBroker();
		BROKER.start(PollenBroker.BROKER_ADDRESS);
	}

	@AfterClass
	public static void stopBroker() throws Exception {
		BROKER.stop();
	}

	@Test
	public void testInteractiveSalaryPoll() throws Exception {

		Scanner scanner = new Scanner(System.in);

		// Alice
		User alice = new User();
		alice.setId("Alice");
		alice.setName("Alice");

		// Bob
		User bob = new User();
		bob.setId("Bob");
		bob.setName("Bob");

		// Charlie
		User charlie = new User();
		charlie.setId("Charlie");
		charlie.setName("Charlie");

		VaultageServer server = new VaultageServer(PollenBroker.BROKER_ADDRESS);

		// register participants
		alice.register(server);
		bob.register(server);
		charlie.register(server);

		List<String> participants = new ArrayList<>();
		participants.add(bob.getPublicKey());
		participants.add(charlie.getPublicKey());
		participants.add(alice.getPublicKey());

		NumberPoll salaryPoll = PollRepository.createSalaryPoll();
		salaryPoll.setOriginator(alice.getPublicKey());
		salaryPoll.setParticipants(participants);

		alice.setSendNumberPollResponseHandler(new UnitTestNumberPollResponseHandler());
		bob.setSendNumberPollResponseHandler(new UnitTestNumberPollResponseHandler());
		charlie.setSendNumberPollResponseHandler(new UnitTestNumberPollResponseHandler());

		double bobAnswer = 100;
		bob.setOnPollReceivedListener(new OnPollReceivedListener() {
			@Override
			public void onPollReceived(User user, NumberPoll poll) {
				System.out.println(poll.getQuestion());
				System.out.println(user.getName() + ", type your answer: ");
				PollAnswer pa = bob.getPollAnswer(poll.getId());
				pa.submitAnswer(bobAnswer);
			}
		});
		
		double charlieAnswer = 150;
		charlie.setOnPollReceivedListener(new OnPollReceivedListener() {
			@Override
			public void onPollReceived(User user, NumberPoll poll) {
				System.out.println(poll.getQuestion());
				System.out.println(user.getName() + ", type your answer: ");
				PollAnswer pa = user.getPollAnswer(poll.getId());
				pa.submitAnswer(charlieAnswer);
			}
		});

		// send poll, initiated by Alice
		RemoteUser firstParticipant = new RemoteUser(alice, participants.get(0));
		synchronized (alice.getSendNumberPollResponseHandler()) {
			System.out.println("Sending poll question to Bob");
			String token = firstParticipant.sendNumberPoll(salaryPoll);
			alice.getPolls().put(token, salaryPoll);
			SendNumberPollResponseHandler handler = alice.getSendNumberPollResponseHandler();
			handler.wait(); // wait for the response
		}

		double fakeValue = alice.getPollFakeValue(salaryPoll.getId());
		double actualResult = ((UnitTestNumberPollResponseHandler) alice.getSendNumberPollResponseHandler())
				.getResult();

		System.out.println("(Alice) Poll result  = " + actualResult);
		assertEquals(fakeValue + bobAnswer + charlieAnswer, actualResult, 0);

		alice.unregister();
		bob.unregister();

		scanner.close();
	}

}
