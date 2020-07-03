package org.vaultage.demo.pollen.test;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vaultage.core.VaultageServer;
import org.vaultage.demo.pollen.NumberPoll;
import org.vaultage.demo.pollen.PollenBroker;
import org.vaultage.demo.pollen.RemoteUser;
import org.vaultage.demo.pollen.SendNumberPollResponseHandler;
import org.vaultage.demo.pollen.User;
import org.vaultage.demo.pollen.data.PollRepository;

public class PollenTest {

	static PollenBroker BROKER;
	static double FIXED_RESPONSE = 10.0;

	@BeforeClass
	public static void startBroker() throws Exception {
		BROKER = new PollenBroker();
		BROKER.start(PollenBroker.BROKER_ADDRESS);
	}

	@AfterClass
	public static void stopBroker() throws Exception {
		BROKER.stop();
	}

	/**
	 * Respond to number polls with a fixed value
	 */
	public class UnitTestNumberPollResponseHandler implements SendNumberPollResponseHandler {

		@Override
		public void run(User me, RemoteUser other, String responseToken, double result) throws Exception {
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
				double myResponse = FIXED_RESPONSE; // respond with a fixed value
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
	}

	@Test
	public void testThreeParticipantsPoll() throws Exception {

		User alice = new User();
		alice.setId("Alice");
		alice.setName("Alice");

		User bob = new User();
		bob.setId("Bob");
		bob.setName("Bob");

		User charlie = new User();
		charlie.setId("Charlie");
		charlie.setName("Charlie");

		alice.setSendNumberPollResponseHandler(new UnitTestNumberPollResponseHandler());
		bob.setSendNumberPollResponseHandler(new UnitTestNumberPollResponseHandler());
		charlie.setSendNumberPollResponseHandler(new UnitTestNumberPollResponseHandler());

		VaultageServer server = new VaultageServer(PollenBroker.BROKER_ADDRESS);
		alice.register(server);
		bob.register(server);
		charlie.register(server);

		List<String> participants = new ArrayList<>();
		participants.add(bob.getPublicKey());
		participants.add(charlie.getPublicKey());

		NumberPoll salaryPoll = PollRepository.createSalaryPoll();
		salaryPoll.setOriginator(alice.getPublicKey());
		salaryPoll.setParticipants(participants);

		RemoteUser firstParticipant = new RemoteUser(alice, participants.get(0));
		synchronized (alice.getSendNumberPollResponseHandler()) {
			String token = firstParticipant.sendNumberPoll(salaryPoll);
			alice.addInitiatedNumberPoll(salaryPoll, token);
			alice.getSendNumberPollResponseHandler().wait(); // wait for the response
		}

		double fakeValue = alice.getNumberPollFakeValue(salaryPoll.getId());
		double result = alice.getNumberPollAnswer(salaryPoll.getId());

		// the total response should be the fixed one times the number of participants
		assertTrue((result - fakeValue) == FIXED_RESPONSE * 2);

		alice.unregister();
		bob.unregister();
		charlie.unregister();
	}

	//	@Test
	//	public void testInteractiveSalaryPoll() throws Exception {
	//
	//		Scanner scanner = new Scanner(System.in);
	//
	//		// Alice
	//		User alice = new User();
	//		alice.setId("Alice");
	//		alice.setName("Alice");
	//
	//		// Bob
	//		User bob = new User();
	//		bob.setId("Bob");
	//		bob.setName("Bob");
	//
	//		// Charlie
	//		User charlie = new User();
	//		charlie.setId("Charlie");
	//		charlie.setName("Charlie");
	//
	//		VaultageServer server = new VaultageServer(PollenBroker.BROKER_ADDRESS);
	//
	//		// register participants
	//		alice.register(server);
	//		bob.register(server);
	//		charlie.register(server);
	//
	//		List<String> participants = new ArrayList<>();
	//		participants.add(bob.getPublicKey());
	//		participants.add(charlie.getPublicKey());
	//		participants.add(alice.getPublicKey());
	//
	//		NumberPoll salaryPoll = PollRepository.createSalaryPoll();
	//		salaryPoll.setOriginator(alice.getPublicKey());
	//		salaryPoll.setParticipants(participants);
	//
	//		alice.setSendNumberPollResponseHandler(new UnitTestNumberPollResponseHandler());
	//		bob.setSendNumberPollResponseHandler(new UnitTestNumberPollResponseHandler());
	//		charlie.setSendNumberPollResponseHandler(new UnitTestNumberPollResponseHandler());
	//
	//		double bobAnswer = 100;
	//		bob.setOnPollReceivedListener(new OnPollReceivedListener() {
	//			@Override
	//			public void onPollReceived(User user, NumberPoll poll) {
	//				System.out.println(poll.getQuestion());
	//				System.out.println(user.getName() + ", type your answer: ");
	//				PollAnswer pa = bob.getPollAnswer(poll.getId());
	//				pa.submitAnswer(bobAnswer);
	//			}
	//		});
	//		
	//		double charlieAnswer = 150;
	//		charlie.setOnPollReceivedListener(new OnPollReceivedListener() {
	//			@Override
	//			public void onPollReceived(User user, NumberPoll poll) {
	//				System.out.println(poll.getQuestion());
	//				System.out.println(user.getName() + ", type your answer: ");
	//				PollAnswer pa = user.getPollAnswer(poll.getId());
	//				pa.submitAnswer(charlieAnswer);
	//			}
	//		});
	//
	//		// send poll, initiated by Alice
	//		RemoteUser firstParticipant = new RemoteUser(alice, participants.get(0));
	//		synchronized (alice.getSendNumberPollResponseHandler()) {
	//			System.out.println("Sending poll question to Bob");
	//			String token = firstParticipant.sendNumberPoll(salaryPoll);
	//			alice.getPolls().put(token, salaryPoll);
	//			SendNumberPollResponseHandler handler = alice.getSendNumberPollResponseHandler();
	//			handler.wait(); // wait for the response
	//		}
	//
	//		double fakeValue = alice.getPollFakeValue(salaryPoll.getId());
	//		double actualResult = ((UnitTestNumberPollResponseHandler) alice.getSendNumberPollResponseHandler())
	//				.getResult();
	//
	//		System.out.println("(Alice) Poll result  = " + actualResult);
	//		assertEquals(fakeValue + bobAnswer + charlieAnswer, actualResult, 0);
	//
	//		alice.unregister();
	//		bob.unregister();
	//
	//		scanner.close();
	//	}

}
