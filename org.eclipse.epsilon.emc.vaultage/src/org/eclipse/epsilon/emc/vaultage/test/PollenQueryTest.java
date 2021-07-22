package org.eclipse.epsilon.emc.vaultage.test;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.epsilon.emc.vaultage.VaultageModel;
import org.eclipse.epsilon.emc.vaultage.VaultageOperationContributor;
import org.eclipse.epsilon.eol.EolModule;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vaultage.core.Vault;
import org.vaultage.core.VaultageServer;
import org.vaultage.demo.pollen.NumberPoll;
import org.vaultage.demo.pollen.PollenBroker;
import org.vaultage.demo.pollen.RemoteUser;
import org.vaultage.demo.pollen.SendNumberPollResponseHandler;
import org.vaultage.demo.pollen.User;
import org.vaultage.demo.pollen.data.PollRepository;

public class PollenQueryTest {

	private static PollenBroker BROKER;
	private static double FIXED_RESPONSE = 10.0;

	private static User alice;
	private static User bob;
	private static User charlie;
	private static User dan;
	private static EolModule module;
	private static List<String> participants;
	private static NumberPoll salaryPoll;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		BROKER = new PollenBroker();
		BROKER.start(PollenBroker.BROKER_ADDRESS);

		VaultageServer brokerServer = new VaultageServer(PollenBroker.BROKER_ADDRESS);

		alice = new User();
		alice.setId("Alice");
		alice.setName("Alice");

		bob = new User();
		bob.setId("Bob");
		bob.setName("Bob");

		charlie = new User();
		charlie.setId("Charlie");
		charlie.setName("Charlie");

		dan = new User();
		dan.setId("Dan");
		dan.setName("Dan");

		alice.addOperationResponseHandler(new UnitTestNumberPollResponseHandler());
		bob.addOperationResponseHandler(new UnitTestNumberPollResponseHandler());
		charlie.addOperationResponseHandler(new UnitTestNumberPollResponseHandler());
		dan.addOperationResponseHandler(new UnitTestNumberPollResponseHandler());

		alice.register(brokerServer);
		bob.register(brokerServer);
		charlie.register(brokerServer);
		dan.register(brokerServer);

		participants = new ArrayList<>();
		participants.add(bob.getPublicKey());
		participants.add(charlie.getPublicKey());
		participants.add(dan.getPublicKey());

		salaryPoll = PollRepository.createSalaryPoll();
		salaryPoll.setOriginator(alice.getPublicKey());
		salaryPoll.setParticipants(participants);

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		dan.shutdownServer();
		dan.unregister();
		charlie.shutdownServer();
		charlie.unregister();
		bob.shutdownServer();
		bob.unregister();
		alice.shutdownServer();
		alice.unregister();
		BROKER.stop();
		System.out.println("Finished!");
	}

	@Before
	public void beforeTest() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		module = new EolModule();

		Set<Package> packages = new HashSet<Package>();
		packages.add(alice.getClass().getPackage());
		VaultageModel model = new VaultageModel(alice, packages, Arrays.asList(bob, charlie, dan, salaryPoll));
		model.setName("M");
		module.getContext().getModelRepository().addModel(model);
		module.getContext().getOperationContributorRegistry().add(new VaultageOperationContributor());
	}

	@Test
	public void testCreatePoll() throws Exception {
		String script = "var poll = new MultivaluedPoll;" //
				+ "poll.question = \"A or B?\";" //
				+ "return poll.question;";
		module.parse(script);
		Object result = module.execute();
		assertEquals("A or B?", result);
	}

	@Test
	public void testAverageSalary() throws Exception {
		String script = Files.readString(Paths.get("model/AverageSalary.eol"));
		module.parse(script);
		double result = Double.valueOf(module.execute().toString());
		assertEquals(FIXED_RESPONSE, result, 0);
	}

	/**
	 * Respond to number polls with a fixed value
	 */
	public static class UnitTestNumberPollResponseHandler extends SendNumberPollResponseHandler {

		@Override
		public void run(User me, RemoteUser other, String responseToken, java.lang.Double result) throws Exception {
			System.out.println(me.getName());
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
					synchronized (me.getOperationResponseHandler(SendNumberPollResponseHandler.class)) {
						me.addNumberPollAnswer(poll.getId(), result);
						me.getOperationResponseHandler(SendNumberPollResponseHandler.class).notify();
					}
				} else {
					throw new RuntimeException("I should be either originator or participant of the poll!");
				}
			}
		}

		@Override
		public void run(Vault localVault, RemoteUser remoteVault, String responseToken, Double result)
				throws Exception {
			// TODO Auto-generated method stub

		}
	}

}
