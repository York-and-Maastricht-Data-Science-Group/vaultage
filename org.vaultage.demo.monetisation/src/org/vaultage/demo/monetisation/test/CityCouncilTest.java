package org.vaultage.demo.monetisation.test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vaultage.core.VaultageServer;
import org.vaultage.demo.monetisation.CityCouncil;
import org.vaultage.demo.monetisation.GetPollResponseHandler;
import org.vaultage.demo.monetisation.MonetisationBroker;
import org.vaultage.demo.monetisation.Poll;
import org.vaultage.demo.monetisation.RemoteCityCouncil;
import org.vaultage.demo.monetisation.Respondent;
import org.vaultage.demo.monetisation.SubmitPollResponseHandler;
import org.vaultage.vwallet.VWalletServer;

public class CityCouncilTest {

	private static final long SLEEP_TIME = 1000;
	static MonetisationBroker BROKER;
	static double FIXED_RESPONSE = 10.0;
	
	@BeforeClass
	public static void startBroker() throws Exception {
		VWalletServer.main(new String[]{"80"});
		
		BROKER = new MonetisationBroker();
		BROKER.start(MonetisationBroker.BROKER_ADDRESS);
	}

	@AfterClass
	public static void stopBroker() throws Exception {	
		BROKER.stop();
		
		VWalletServer.shutdown();
	}
	
//	@Before
//	public void setUp() throws Exception {
//	}
//
//	@After
//	public void tearDown() throws Exception {
//	}

	@Test
	public void testAnsweringPoll() throws Exception {
		VaultageServer server = new VaultageServer(MonetisationBroker.BROKER_ADDRESS);
		
		// create city council
		CityCouncil council = new CityCouncil();
		council.setId("Council");
		council.register(server);
		
		// create the poll
		Poll poll = new Poll();
		poll.setTitle("York Lockdown");
		poll.setDescription("Bla...bla...bla...!");
		poll.setQuestions(new ArrayList<>());
		poll.getQuestions().add("Question 01?");
		poll.getQuestions().add("Question 02?");
		poll.setAnswers(new ArrayList<>());
		String pollId = poll.getId();
		
		council.getPolls().add(poll);
		
		// create the respondent
		Respondent respondent = new Respondent();
		respondent.setId("respondent-01");
		respondent.register(server);
		
		// set get poll response handler
		respondent.setGetPollResponseHandler(new GetPollResponseHandler() {
			@Override
			public void run(CityCouncil me, RemoteCityCouncil other, String responseToken, Poll result) throws Exception {
			}
			@Override
			public void run(Respondent me, RemoteCityCouncil other, String responseToken, Poll result) throws Exception {
				me.setRetrievedPoll(result);
				synchronized (this) {
					this.notify();
				}
			}
		});
		
		// retrieve the poll from city council
		synchronized (respondent.getGetPollResponseHandler()) {
			respondent.getPollFromCouncil(pollId, council);
			respondent.getGetPollResponseHandler().wait();
		}
		
		Poll retrievedPoll = respondent.getRetrievedPoll();
		
		// give answers
		retrievedPoll.getAnswers().add("Answer 01");
		retrievedPoll.getAnswers().add("Answer 02");
		
		// set submit poll response handler
		final Object[] actualValue = {null};
		respondent.setSubmitPollResponseHandler(new SubmitPollResponseHandler() {
			@Override
			public void run(CityCouncil me, RemoteCityCouncil other, String responseToken, String result) throws Exception {
			}
			@Override
			public void run(Respondent me, RemoteCityCouncil other, String responseToken, String result) throws Exception {
				actualValue[0] = result;
				synchronized (this) {
					this.notify();
				}
			}
		});
		
		// submit poll back to city council
		synchronized (respondent.getSubmitPollResponseHandler()) {
			respondent.submitPoll(retrievedPoll, council);
			respondent.getSubmitPollResponseHandler().wait();
		}
		
		assertEquals("OK", actualValue[0]);
	}

	
}
