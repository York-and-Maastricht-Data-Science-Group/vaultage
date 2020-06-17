package org.vaultage.demo.pollen.test;

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

	//	Scanner scanner = new Scanner(System.in);

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

		// Alice
		User alice = new User();
		alice.setId("Alice");
		alice.setName("Alice");
		alice.setSendNumberPollResponseHandler(new SendNumberPollResponseHandler() {

			@Override
			public void run(User me, RemoteUser other, String responseToken, double result) throws Exception {
				NumberPoll poll = me.getPolls().get(responseToken);
				me.setNumberPollResult(poll.getId(), result);
				synchronized (this) {
					this.notify();
				}
			}
		});

		// Bob
		User bob = new User();
		bob.setId("Bob");
		bob.setName("Bob");

		VaultageServer server = new VaultageServer(PollenBroker.BROKER_ADDRESS);

		// register participants
		alice.register(server);
		bob.register(server);

		List<String> participants = new ArrayList<>();
		participants.add(bob.getPublicKey());
		participants.add(alice.getPublicKey());

		NumberPoll salaryPoll = PollRepository.createSalaryPoll();
		salaryPoll.setOriginator(alice.getPublicKey());
		salaryPoll.setParticipants(participants);

		// send poll, initiated by Alice
		RemoteUser firstParticipant = new RemoteUser(alice, participants.get(0));
		synchronized (alice.getSendNumberPollResponseHandler()) {
			System.out.println("Sending poll question to Bob");
			String token = firstParticipant.sendNumberPoll(salaryPoll);
			alice.getPolls().put(token, salaryPoll);
			alice.getSendNumberPollResponseHandler().wait(); // wait for the response
		}
		double pollResult = alice.getNumberPollResult(salaryPoll.getId());
		System.out.println("(Alice) Poll result  = " + pollResult);

		alice.unregister();
		bob.unregister();
	}

}
