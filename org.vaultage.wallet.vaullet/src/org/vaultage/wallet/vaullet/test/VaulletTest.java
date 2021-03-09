package org.vaultage.wallet.vaullet.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vaultage.wallet.vaullet.VaulletServer;
import org.vaultage.wallet.vaullet.account.Account;
import org.vaultage.wallet.vaullet.account.Card;
import org.vaultage.wallet.vaullet.transaction.TransactionState;
import org.vaultage.wallet.vaullet.user.User;
import org.vaultage.wallet.vaullet.util.Currency;
import org.vaultage.wallet.vaullet.util.Path;

import io.javalin.http.Context;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;

public class VaulletTest {

	private static String port = "3001";
	private static String address;

	private Context ctx = mock(Context.class);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		// initialise host address and port
		address = "http://localhost:" + port;
		VaulletServer.main(new String[] { port });

		// initialise users
		VaulletServer.userDao.getUsers().addAll( //
				Arrays.asList(new User("user1", "p1", "t1"), //
						new User("user2", "p2", "t2"), //
						new User("user3", "p3", "t3") //
				));

		// initialise accounts
		VaulletServer.accountDao.getAccounts() //
				.addAll(Arrays.asList( //
						new Card("a1", "user1", Currency.GBP), //
						new Card("a2", "user2", Currency.GBP), //
						new Card("a3", "user3", Currency.GBP) //
				));

		// deposit 10 for every wallet account
		for (Account account : VaulletServer.accountDao.getAccounts()) {
			if (account instanceof Card) {
				((Card) account).deposit(10);
			}
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		VaulletServer.shutdown();
	}

	@Test
	public void testGetToken() {
		HttpResponse<String> response = Unirest.post(address + "/token") //
				.field("userId", "user1") //
				.field("password", "p1") //
				.asString();

		assertEquals("t1", response.getBody());
	}

	@Test
	public void testGetAccount() {
		HttpResponse<String> response = Unirest.post(address + Path.Web.TOKEN) //
				.field("userId", "user1") //
				.field("password", "p1") //
				.asString();
		String token = response.getBody();

		HttpResponse<JsonNode> jsonNode = Unirest.post(address + Path.Web.ACCOUNTS) //
				.field("token", token) //
				.asJson();
		JsonNode accounts = jsonNode.getBody();
		String accountId = accounts.getArray().getJSONObject(0).getString("userId");

		assertEquals("user1", accountId);
	}

	/***
	 * Test transferring money between two different users
	 */
	@Test
	public void testTransaction() {

		double amount = 0.01;
		
		// get user1's token
		HttpResponse<String> response1 = Unirest.post(address + Path.Web.TOKEN) //
				.field("userId", "user1") //
				.field("password", "p1") //
				.asString();
		String token1 = response1.getBody();

		// get user1's account
		HttpResponse<JsonNode> jsonNode1 = Unirest.post(address + Path.Web.ACCOUNTS) //
				.field("token", token1) //
				.asJson();
		JSONObject account1 = jsonNode1.getBody().getArray().getJSONObject(0);
		String accountId1 = account1.getString("accountId");

		// get user2's token
		HttpResponse<String> response2 = Unirest.post(address + Path.Web.TOKEN) //
				.field("userId", "user2") //
				.field("password", "p2") //
				.asString();
		String token2 = response2.getBody();

		// get user2's account
		HttpResponse<JsonNode> jsonNode2 = Unirest.post(address + Path.Web.ACCOUNTS) //
				.field("token", token2) //
				.asJson();
		JSONObject account2 = jsonNode2.getBody().getArray().getJSONObject(0);
		String accountId2 = account2.getString("accountId");

		// do the transfer
		HttpResponse<JsonNode> jsonNode = Unirest.post(address + Path.Web.TRANSACTION) //
				.field("origin", accountId1) //
				.field("currency", Currency.GBP.toString()) //
				.field("amount", String.valueOf(amount)) //
				.field("destination", accountId2) //
				.field("token", token1) //
				.asJson();
		JSONObject transaction = jsonNode.getBody().getObject();
		System.out.println(jsonNode.getBody().toPrettyString());

		assertEquals(TransactionState.COMPLETED.toString(), transaction.get("transactionState"));
	}

}
