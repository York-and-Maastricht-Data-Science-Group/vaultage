package org.vaultage.demo.pollen.test;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vaultage.core.Vault;
import org.vaultage.core.Vaultage;
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
		public void run(User me, RemoteUser other, String responseToken, java.lang.Double result) throws Exception {
			NumberPoll poll = me.getPendingNumberPollByResponseToken(responseToken);
			if (poll != null) {
				int index = poll.getParticipants().indexOf(me.getPublicKey());
				String previousParticipant;
				if (index == 0) {
					previousParticipant = poll.getOriginator();
				} else {
					previousParticipant = poll.getParticipants().get(index - 1);
				}
				RemoteUser previous = new RemoteUser(me, previousParticipant);
				String requestToken = me.getMappedRequestToken(responseToken);
				double myResponse = FIXED_RESPONSE; // respond with a fixed value
				previous.respondToSendNumberPoll(result + myResponse, requestToken);
			} else {
				poll = me.getInitiatedNumberPoll(responseToken);
				if (poll != null) {
					synchronized (me.getSendNumberPollResponseHandler()) {
						me.addNumberPollAnswer(poll.getId(), result);
						me.getSendNumberPollResponseHandler().notify();
					}
				} else {
					throw new RuntimeException("I should be either originator or participant of the poll!");
				}
			}
		}
	}

	@Test
	public void testSeveralParticipantsPoll() throws Exception {

		VaultageServer server = new VaultageServer(PollenBroker.BROKER_ADDRESS);
		
		User alice = new User();
		User bob = new User();
		User charlie = new User();
		User dan = new User();
		
		double fakeValue;
		double result;
		
		NumberPoll salaryPoll = runScenario(server, alice, bob, charlie, dan);

		fakeValue = alice.getNumberPollFakeValue(salaryPoll.getId());
		result = alice.getNumberPollAnswer(salaryPoll.getId());

		// the total response should be the fixed one times the number of participants
		assertTrue((result - fakeValue) == FIXED_RESPONSE * 3);

		alice.unregister();
		bob.unregister();
		charlie.unregister();
		dan.unregister();
	}
	
	@Test
	public void testSeveralParticipantsPollDirectMessage() throws Exception {

		VaultageServer server = new VaultageServer(PollenBroker.BROKER_ADDRESS);
		
		User alice = new User();
		User bob = new User();
		User charlie = new User();
		User dan = new User();
		
		//start the direct message server of each vault
		int port = new Integer(Vaultage.DEFAULT_SERVER_PORT);
		alice.startServer("127.0.0.1", port++);
		bob.startServer("192.168.56.1", port++);
		charlie.startServer("192.168.14.2", port++);
		dan.startServer("192.168.99.80", port++);
		
		/**
		 * Set up all vaults to trust each other. Therefore, they don't have to use a
		 * broker to communicate. Initially, vaults will use a broker as a channel to
		 * communicate to get the trusted addresses of other vaults. If other vaults are
		 * trusted, they will use direct messaging instead. It the remote vaults cannot
		 * be reached by direct messaging, a broker will be re-used.
		 **/
		User[] users = { alice, bob, charlie, dan };
		for (User userLocal : users) {
			for (User userRemote : users) {
				if (!userLocal.equals(userRemote)) {
					userLocal.getVaultage().getPublicKeyToRemoteAddress().put(userRemote.getPublicKey(),
							userRemote.getVaultage().getDirectMessageServerAddress());
				}
			}
		}
		
		double fakeValue;
		double result;
		
		NumberPoll salaryPoll = runScenario(server, alice, bob, charlie, dan);

		fakeValue = alice.getNumberPollFakeValue(salaryPoll.getId());
		result = alice.getNumberPollAnswer(salaryPoll.getId());

		// the total response should be the fixed one times the number of participants
		assertTrue((result - fakeValue) == FIXED_RESPONSE * 3);

		alice.unregister();
		bob.unregister();
		charlie.unregister();
		dan.unregister();
		
		alice.shutdownServer();
		bob.shutdownServer();
		charlie.shutdownServer();
		dan.shutdownServer();
	}


	protected NumberPoll runScenario(VaultageServer server, User alice, User bob, User charlie, User dan)
			throws Exception, InterruptedException {
		alice.setId("Alice");
		alice.setName("Alice");

		bob.setId("Bob");
		bob.setName("Bob");

		charlie.setId("Charlie");
		charlie.setName("Charlie");
		
		dan.setId("Dan");
		dan.setName("Dan");

		alice.setSendNumberPollResponseHandler(new UnitTestNumberPollResponseHandler());
		bob.setSendNumberPollResponseHandler(new UnitTestNumberPollResponseHandler());
		charlie.setSendNumberPollResponseHandler(new UnitTestNumberPollResponseHandler());
		dan.setSendNumberPollResponseHandler(new UnitTestNumberPollResponseHandler());

		alice.register(server);
		bob.register(server);
		charlie.register(server);
		dan.register(server);

		List<String> participants = new ArrayList<>();
		participants.add(bob.getPublicKey());
		participants.add(charlie.getPublicKey());
		participants.add(dan.getPublicKey());

		NumberPoll salaryPoll = PollRepository.createSalaryPoll();
		salaryPoll.setOriginator(alice.getPublicKey());
		salaryPoll.setParticipants(participants);

		RemoteUser firstParticipant = new RemoteUser(alice, participants.get(0));
		synchronized (alice.getSendNumberPollResponseHandler()) {
			String token = firstParticipant.sendNumberPoll(salaryPoll);
			alice.addInitiatedNumberPoll(salaryPoll, token);
			alice.getSendNumberPollResponseHandler().wait(); // wait for the response
		}
		return salaryPoll;
	}
}
