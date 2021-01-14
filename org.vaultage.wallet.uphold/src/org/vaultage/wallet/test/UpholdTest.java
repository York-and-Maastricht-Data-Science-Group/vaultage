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

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
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
import org.vaultage.wallet.uphold.UpholdUtil;
import org.vaultage.wallet.uphold.UpholdWallet;

import com.fasterxml.jackson.databind.JsonNode;

public class UpholdTest {

	private String clientAId = "4b383af8566eba361aebb7b2940ca6e038fd3772";
	private String clientASecret = "c67408e73c9ff6d2ac732f2632d77e65ef7d1705";
	
	private String usernameA = "alfa.yohannis@gmail.com";
	private String passwordA = "Lolol3x!";

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

	@Test
	public void testWebApplicationFlow() throws ClientProtocolException, IOException, URISyntaxException {

		String scope = "user:read";
		String state = UpholdUtil.randomBytes();

		UpholdWallet wallet = new UpholdWallet();

		String result = wallet.authorise(clientAId, scope, state);

		System.out.println(result);
		assertEquals(true, result.contains("</html>"));
	}

	@Test
	public void testClientCredentialFlow() throws ClientProtocolException, IOException, URISyntaxException {

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

	@Test
	public void testPersonalAccessToken() throws ClientProtocolException, IOException, URISyntaxException {

		// OTP = One-Time Password
		String otpMethodId = null;
		String otpToken = "000000";

		UpholdWallet wallet = new UpholdWallet();

		// get available authentications
		JsonNode authenticationMethods = wallet.getAuthenticationMethods(usernameA, passwordA);

		JsonNode totpCheck = authenticationMethods.get("totp");
		if (totpCheck != null) {
			otpMethodId = totpCheck.get("id").asText();
		}
		// create new personal access token
		String tokenDescription = "Submitted at " + (new Date()).toString();
		String personalAccessToken = wallet.createNewPersonalAccessToken(tokenDescription, usernameA, passwordA,
				otpMethodId, otpToken);

		// get all personal access tokens
		JsonNode personalAccessTokens = wallet.getPersonalAccessTokens(personalAccessToken);
		Iterator<JsonNode> iterator = personalAccessTokens.elements(); 
		boolean exist = false;
		while (iterator.hasNext()) {
			JsonNode element = iterator.next();
			if (element.get("description").asText().equals(tokenDescription)){
				exist = true;
			}
		}
		
		assertEquals(true,exist);
	}
}
