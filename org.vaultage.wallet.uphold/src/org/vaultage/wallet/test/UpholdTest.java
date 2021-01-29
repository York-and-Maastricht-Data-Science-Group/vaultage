package org.vaultage.wallet.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vaultage.wallet.WalletException;
import org.vaultage.wallet.uphold.UpholdUtil;
import org.vaultage.wallet.uphold.UpholdWallet;

import com.fasterxml.jackson.databind.JsonNode;

/***
 * A class for testing the Uphold wrapper.
 * 
 * @author Alfa Yohannis
 */
public class UpholdTest {

	private String clientAId = "17c0b7f3f77755c4959248e10a2f0bf69dd56d5f";
	private String clientASecret = "5b0ed8d9b3cc78d9535cea4756be44b24c5cc251";

	private String usernameA = "ary506@york.ac.uk";
	private String passwordA = "Lolol3x!";
	private String clientPersonalAccessTokenA = "412b0a7ea69d1b785faed01e21c0fd533822b6eb";
	private String destinationCardIdA = "mr1hnetSLCpP8iPSno121cYepWvaFYZaqA";
	private String interledgerA = "$ilp-sandbox.uphold.com/bBLJYdjp9F9H";

	private String usernameB = "alfa.yohannis@york.ac.uk";
	private String passwordB = "Lolol3x!";
	private String clientPersonalAccessTokenB = "fe082cb503dcc63365edb9a630a98ecf7f21f4b2";
	private String destinationCardIdB = "mqyAoGRZX4xW8akQP2LEJdDAp12RyBUseT";
	private String interledgerB = "$ilp-sandbox.uphold.com/NL3gFpy6KwbY";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/***
	 * Test web application scenario .. more work to be done to simulate interaction
	 * between users and the application.
	 * 
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws WalletException
	 */
	@Test
	public void testWebApplicationFlow()
			throws ClientProtocolException, IOException, URISyntaxException, WalletException {

		String scope = "user:read";
		String state = UpholdUtil.randomBytes();

		UpholdWallet wallet = new UpholdWallet();

		String result = wallet.authorise(clientAId, scope, state);

		System.out.println(result);
		assertEquals(true, result.contains("</html>"));
	}

	/***
	 * Test Client credential flow and getting access token. 
	 * 
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws WalletException
	 */
	@Test
	public void testClientCredentialFlow()
			throws ClientProtocolException, IOException, URISyntaxException, WalletException {

		UpholdWallet wallet = new UpholdWallet();

		// get access token
		String accessToken = wallet.getAccessToken(clientAId, clientASecret);

		System.out.println(accessToken);
		assertEquals(true, accessToken.length() > 2);

		// get user info and get the first name using the access token
		JsonNode jsonNode = wallet.getUserInfo(accessToken);

		String firstName = jsonNode.get("firstName").asText();

		System.out.println(firstName);
		assertEquals("Alfa", firstName);

	}

	/***
	 * Test generating a personal access token and getting a list of personal access tokens.
	 * 
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws ParseException
	 * @throws WalletException
	 */
	@Test
	public void testPersonalAccessToken()
			throws ClientProtocolException, IOException, URISyntaxException, ParseException, WalletException {

		// OTP = One-Time Password
		String otpMethodId = null;
		String otpToken = "000000";

		UpholdWallet wallet = new UpholdWallet();

		// get available authentications
		JsonNode authenticationMethods = wallet.getAuthenticationMethods(usernameA, passwordA);
//		JsonNode authenticationMethods = wallet.getAuthenticationMethods(usernameB, passwordB);

		if (authenticationMethods != null) {
			for (JsonNode node : authenticationMethods) {
				JsonNode totpCheck = node.get("type");
				if (totpCheck != null) {
					otpMethodId = node.get("id").asText();
					break;
				}
			}
		}

		// create new personal access token
		String tokenDescription = "Submitted at " + (new Date()).toString();
		String personalAccessToken = wallet.createNewPersonalAccessToken(tokenDescription, usernameA, passwordA,
//		String personalAccessToken = wallet.createNewPersonalAccessToken(tokenDescription, usernameB, passwordB,
				otpMethodId, otpToken);

		// get all personal access tokens
		JsonNode personalAccessTokens = wallet.getPersonalAccessTokens(personalAccessToken);
		Iterator<JsonNode> iterator = personalAccessTokens.elements();
		boolean exist = false;
		while (iterator.hasNext()) {
			JsonNode element = iterator.next();
			if (element.get("description").asText().equals(tokenDescription)) {
				exist = true;
			}
		}

		assertEquals(true, exist);
	}

	/***
	 * Test money transfer between cards of a same owner.
	 *  
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws WalletException
	 */
	@Test
	public void testMoneyTransferBetweenCards()
			throws ClientProtocolException, IOException, URISyntaxException, WalletException {

		UpholdWallet wallet = new UpholdWallet();

		// get access token
		String accessToken = wallet.getAccessToken(clientAId, clientASecret);
		System.out.println(accessToken);

		// get available accounts
		JsonNode accounts = wallet.getCards(accessToken);

		// get found an account with, at least, 0.01 USD and also the destination card
		String originCardId = null;
		String destinationCardId = null;

		Iterator<JsonNode> iterator1 = accounts.elements();
		int count = 0;
		int originIndex = 0;
		while (iterator1.hasNext()) {
			JsonNode element = iterator1.next();
			JsonNode n = element.get("normalized");
			Iterator<JsonNode> iterator2 = n.elements();
			while (iterator2.hasNext()) {
				JsonNode e = iterator2.next();
				if (originCardId == null && element.get("currency").asText().equals("BTC")
						&& e.get("available").asDouble() >= 0.01 && e.get("currency").asText().equals("USD")) {
					originCardId = element.get("id").asText();
					System.out.println("Transfer from: " + element.get("currency") + " " + element.get("id"));
					originIndex = count;
					break;
				}
			}
			count++;
		}

		int destionationIndex = (new Random()).nextInt(count);
		int i = 0;
		iterator1 = accounts.elements();
		while (iterator1.hasNext()) {
			JsonNode element = iterator1.next();
			if (i == originIndex || element.get("currency").asText().equals("GBP")
					|| element.get("currency").asText().equals("EUR")) {
				i++;
				continue;
			}
			if (i == destionationIndex) {
				destinationCardId = element.get("id").asText();
				System.out.println("Transfer to: " + element.get("currency") + " " + element.get("id"));
				break;
			}
			i++;
		}

		JsonNode transaction = wallet.transfer(originCardId, destinationCardId, "USD", 0.01, accessToken);

		System.out.println(transaction.toPrettyString());

		assertEquals(true, accounts != null);

	}

	/***
	 * Test money transfer between cards of different owners. 
	 * 
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws WalletException
	 */
	@Test
	public void testMoneyTransferToOtherUsersCard()
			throws ClientProtocolException, IOException, URISyntaxException, WalletException {

		UpholdWallet wallet = new UpholdWallet();

		// get access token
//		String accessToken = wallet.getAccessToken(clientAId, clientASecret);
		String accessToken = clientPersonalAccessTokenB;
//		String accessToken = clientPersonalAccessTokenA;

		System.out.println(accessToken);

		// get available accounts
		JsonNode accounts = wallet.getCards(accessToken);
//		System.out.println(accounts.toPrettyString());

		// get found an BTC account with, at least, 0.01 USD
		String originCardId = null;

		Iterator<JsonNode> iterator1 = accounts.elements();
		while (iterator1.hasNext()) {
			JsonNode element = iterator1.next();
			JsonNode n = element.get("normalized");
			Iterator<JsonNode> iterator2 = n.elements();
			while (iterator2.hasNext()) {
				JsonNode e = iterator2.next();
				if (originCardId == null && element.get("currency").asText().equals("BTC")
						&& e.get("available").asDouble() >= 0.01 && e.get("currency").asText().equals("USD")) {
					originCardId = element.get("id").asText();
					System.out.println("Transfer from: " + element.get("currency") + " " + element.get("id"));
					break;
				}
			}
		}

		// Other user's card id
//		String destinationCardId = destinationCardIdB;
//		String destinationCardId = destinationCardIdA;
		String destinationCardId = usernameA;
//		String destinationCardId = interledgerA;
//		String destinationCardId = "BrWBxMiLXmJA";

		JsonNode transaction = wallet.transfer(originCardId, destinationCardId, "USD", 0.03, accessToken);

		System.out.println(transaction.toPrettyString());

		assertEquals(true, accounts != null);
	}
}
