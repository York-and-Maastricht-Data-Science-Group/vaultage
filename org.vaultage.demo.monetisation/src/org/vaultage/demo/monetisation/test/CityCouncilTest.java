package org.vaultage.demo.monetisation.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vaultage.core.Vault;
import org.vaultage.core.VaultageServer;
import org.vaultage.demo.monetisation.CityCouncil;
import org.vaultage.demo.monetisation.GetQuestionnaireResponseHandler;
import org.vaultage.demo.monetisation.MonetisationBroker;
import org.vaultage.demo.monetisation.Questionnaire;
import org.vaultage.demo.monetisation.RemoteCityCouncil;
import org.vaultage.demo.monetisation.Respondent;
import org.vaultage.demo.monetisation.SubmitQuestionnaireResponseHandler;
import org.vaultage.wallet.Wallet;
import org.vaultage.wallet.uphold.UpholdWallet;
import org.vaultage.wallet.vaullet.VaulletServer;
import org.vaultage.wallet.vaullet.VaulletWallet;
import org.vaultage.wallet.vaullet.account.Account;
import org.vaultage.wallet.vaullet.account.Card;
import org.vaultage.wallet.vaullet.user.User;
import org.vaultage.wallet.vaullet.util.Currency;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CityCouncilTest {

	private static final long SLEEP_TIME = 1000;
	static MonetisationBroker BROKER;
	static double FIXED_RESPONSE = 10.0;

	private static String port = "3001";
	private static String vaulletServerAddress;

	private static String cityCouncilClientId = "17c0b7f3f77755c4959248e10a2f0bf69dd56d5f";
	private static String cityCouncilClientSecret = "5b0ed8d9b3cc78d9535cea4756be44b24c5cc251";

	private static String cityCouncilUsername = "ary506@york.ac.uk";
	private static String cityCouncilPassword = "Lolol3x!";
	private static String cityCouncilPersonalAccessToken = "412b0a7ea69d1b785faed01e21c0fd533822b6eb";
	private static String cityCouncilCardId = "mr1hnetSLCpP8iPSno121cYepWvaFYZaqA";

	private static String respondentUsername = "alfa.yohannis@york.ac.uk";
	private static String respondentPassword = "Lolol3x!";
	private static String respondentPersonalAccessToken = "fe082cb503dcc63365edb9a630a98ecf7f21f4b2";
	private static String respondentCardId = "mqyAoGRZX4xW8akQP2LEJdDAp12RyBUseT";

	@BeforeClass
	public static void startBroker() throws Exception {

		// start broker server
		BROKER = new MonetisationBroker();
		BROKER.start(MonetisationBroker.BROKER_ADDRESS);

//		// initialise host vaulletServerAddress and port for vaullet
//		vaulletServerAddress = "http://localhost:" + port;
//		VaulletServer.main(new String[] { port });
//
//		// initialise users
//		VaulletServer.userDao.getUsers().addAll( //
//				Arrays.asList(new User(cityCouncilUsername, cityCouncilPassword, cityCouncilPersonalAccessToken), //
//						new User(respondentUsername, respondentPassword, respondentPersonalAccessToken) //
//				));
//
//		// initialise accounts
//		VaulletServer.accountDao.getAccounts() //
//				.addAll(Arrays.asList( //
//						new Card(cityCouncilCardId, cityCouncilUsername, Currency.GBP), //
//						new Card(respondentCardId, respondentUsername, Currency.GBP) //
//				));
//
//		// deposit 10 for every wallet account
//		for (Account account : VaulletServer.accountDao.getAccounts()) {
//			if (account instanceof Card) {
//				((Card) account).deposit(10);
//			}
//		}
	}

	@AfterClass
	public static void stopBroker() throws Exception {
		BROKER.stop();

//		VaulletServer.shutdown();
	}
	
	
	@Test
	public void testGetQuestionnaire() throws Exception {
		VaultageServer server = new VaultageServer(MonetisationBroker.BROKER_ADDRESS);

		// create city council
		CityCouncil council = new CityCouncil();
		council.setId("Council");
		council.register(server);

		Wallet councilWallet = new UpholdWallet();
//		Wallet councilWallet = new VaulletWallet(vaulletServerAddress);
		councilWallet.setClientId(cityCouncilClientId);
		councilWallet.setClientSecret(cityCouncilClientSecret);
		councilWallet.setUsername(cityCouncilUsername);
		councilWallet.setPassword(cityCouncilPassword);
		councilWallet.setAccountNumber(cityCouncilCardId);
		councilWallet.setName("York City Council");
		council.setDefaultWallet(councilWallet);

		// create the poll
		Questionnaire poll = new Questionnaire();
		poll.setTitle("York Lockdown " + (new Date()).toString());
		poll.setDescription("Bla...bla...bla...!");
		poll.setQuestions(new ArrayList<>());
		poll.getQuestions().add("Question 01?");
		poll.getQuestions().add("Question 02?");
		poll.setPrice(0.01);
		poll.setAnswers(new ArrayList<>());

		// get the poll's id to be used later
		// when retrieving it from a respondent
		String pollId = poll.getId();

		// set the poll as one of the city council's polls
		council.getQuestionnaires().add(poll);

		// create the respondent
		Respondent respondent = new Respondent();
		respondent.setId("respondent-01");
		respondent.register(server);

		Wallet respondentWallet = new UpholdWallet();
//		Wallet respondentWallet = new VaulletWallet(vaulletServerAddress);
		respondentWallet.setUsername(respondentUsername);
		respondentWallet.setPassword(respondentPassword);
		respondentWallet.setAccountNumber(respondentCardId);
		respondentWallet.setName("Alfa Yohannis");

		respondent.setDefaultWallet(respondentWallet);

		// set get poll response handler	
		respondent.addOperationResponseHandler(new GetQuestionnaireResponseHandler() {
			@Override
			public void run(CityCouncil me, RemoteCityCouncil other, String responseToken, Questionnaire result)
					throws Exception {
			}

			@Override
			public void run(Vault me, RemoteCityCouncil other, String responseToken, Questionnaire result)
					throws Exception {
				respondent.getClass().cast(me).setRetrievedQuestionnaire(result);
				synchronized (this) {
					this.notify();
				}
			}
		});
		
		// retrieve the poll from the city council using the pollId
		synchronized (respondent.getOperationResponseHandler(GetQuestionnaireResponseHandler.class)) {
			respondent.getQuestionnaireFromCouncil(pollId, council);
			respondent.getOperationResponseHandler(GetQuestionnaireResponseHandler.class).wait();
		}

		Questionnaire retrievedQuestionnaire = respondent.getRetrievedQuestionnaire();


		assertEquals(true, retrievedQuestionnaire != null);
	}

	@Test
	public void testAnsweringQuestionnaire() throws Exception {
		VaultageServer server = new VaultageServer(MonetisationBroker.BROKER_ADDRESS);

		// create city council
		CityCouncil council = new CityCouncil();
		council.setId("Council");
		council.register(server);

		Wallet councilWallet = new UpholdWallet();
//		Wallet councilWallet = new VaulletWallet(vaulletServerAddress);
		councilWallet.setClientId(cityCouncilClientId);
		councilWallet.setClientSecret(cityCouncilClientSecret);
		councilWallet.setUsername(cityCouncilUsername);
		councilWallet.setPassword(cityCouncilPassword);
		councilWallet.setAccountNumber(cityCouncilCardId);
		councilWallet.setName("York City Council");
		council.setDefaultWallet(councilWallet);

		// create the poll
		Questionnaire poll = new Questionnaire();
		poll.setTitle("York Lockdown " + (new Date()).toString());
		poll.setDescription("Bla...bla...bla...!");
		poll.setQuestions(new ArrayList<>());
		poll.getQuestions().add("Question 01?");
		poll.getQuestions().add("Question 02?");
		poll.setPrice(0.01);
		poll.setAnswers(new ArrayList<>());

		// get the poll's id to be used later
		// when retrieving it from a respondent
		String pollId = poll.getId();

		// set the poll as one of the city council's polls
		council.getQuestionnaires().add(poll);

		// create the respondent
		Respondent respondent = new Respondent();
		respondent.setId("respondent-01");
		respondent.register(server);

		Wallet respondentWallet = new UpholdWallet();
//		Wallet respondentWallet = new VaulletWallet(vaulletServerAddress);
		respondentWallet.setUsername(respondentUsername);
		respondentWallet.setPassword(respondentPassword);
		respondentWallet.setAccountNumber(respondentCardId);
		respondentWallet.setName("Alfa Yohannis");

		respondent.setDefaultWallet(respondentWallet);

		// set get poll response handler
		respondent.addOperationResponseHandler(new GetQuestionnaireResponseHandler() {
			@Override
			public void run(CityCouncil me, RemoteCityCouncil other, String responseToken, Questionnaire result)
					throws Exception {
			}

			@Override
			public void run(Vault me, RemoteCityCouncil other, String responseToken, Questionnaire result)
					throws Exception {
				respondent.getClass().cast(me).setRetrievedQuestionnaire(result);
				synchronized (this) {
					this.notify();
				}
			}
		});

		// retrieve the poll from the city council using the pollId
		synchronized (respondent.getOperationResponseHandler(GetQuestionnaireResponseHandler.class)) {
			respondent.getQuestionnaireFromCouncil(pollId, council);
			respondent.getOperationResponseHandler(GetQuestionnaireResponseHandler.class).wait();
		}

		Questionnaire retrievedQuestionnaire = respondent.getRetrievedQuestionnaire();

		// give answers
		retrievedQuestionnaire.getAnswers().add("Answer 01");
		retrievedQuestionnaire.getAnswers().add("Answer 02");

		// set setSubmitQuestionnaireResponseHandler
		final String[] actualValue = { null };
		respondent.addOperationResponseHandler(new SubmitQuestionnaireResponseHandler() {
			@Override
			public void run(CityCouncil me, RemoteCityCouncil other, String responseToken, String result)
					throws Exception {
			}

			@Override
			public void run(Vault me, RemoteCityCouncil other, String responseToken, String result)
					throws Exception {
				actualValue[0] = result;
				synchronized (this) {
					this.notify();
				}
			}
		});

		// submit the poll back to the city council
		synchronized (respondent.getOperationResponseHandler(SubmitQuestionnaireResponseHandler.class)) {
			respondent.submitQuestionnaire(retrievedQuestionnaire, council);
			respondent.getOperationResponseHandler(SubmitQuestionnaireResponseHandler.class).wait();
		}

		String instanceId = retrievedQuestionnaire.getInstanceId();
		JsonNode jsonNode = (new ObjectMapper()).readTree(actualValue[0]);
		String transactionId = jsonNode.get("id").asText();
		String reference = jsonNode.get("reference").asText();
		System.out.println(jsonNode.toPrettyString());
		System.out.println(transactionId);
		System.out.println(reference);
		System.out.println(instanceId);

		// this code could be extended by checking transaction id on the respondent's
		// payment wallet to check if the transaction  succeed

		assertEquals(instanceId, reference);
	}

}
