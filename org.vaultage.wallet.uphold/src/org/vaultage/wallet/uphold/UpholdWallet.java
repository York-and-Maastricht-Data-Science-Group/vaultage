package org.vaultage.wallet.uphold;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.vaultage.wallet.Wallet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UpholdWallet implements Wallet {

	WalletEnvironment walletEnvironment;

	public UpholdWallet() {
		walletEnvironment = WalletEnvironment.SANDBOX_TEST;
	}

	public UpholdWallet(WalletEnvironment walletEnvironment) {
		this.walletEnvironment = walletEnvironment;
	}

	public String authorise(String clientId, String scope, String state)
			throws URISyntaxException, IOException, ClientProtocolException {

		URI authenticationUri = null;

		if (walletEnvironment == WalletEnvironment.SANDBOX_TEST) {
			authenticationUri = new URIBuilder() //
					.setScheme("https") //
					.setHost("sandbox.uphold.com") //
					.setPath("/authorize/" + clientId) //
					.setParameter("scope", scope) //
					.setParameter("state", state) //
					.build();
		} else {
			authenticationUri = new URIBuilder() //
					.setScheme("https") //
					.setHost("uphold.com") //
					.setPath("/authorize/" + clientId) //
					.setParameter("scope", scope) //
					.setParameter("state", state) //
					.build();
		}

		CloseableHttpClient authenticationHttpClient = HttpClients.createDefault();

		HttpPost authenticationRequest = new HttpPost(authenticationUri);
		CloseableHttpResponse authenticationResponse = authenticationHttpClient.execute(authenticationRequest);

		HttpEntity entity = authenticationResponse.getEntity();
		String result = EntityUtils.toString(entity);

		authenticationHttpClient.close();
		authenticationResponse.close();

		return result;
	}

	/***
	 * Get an access token to be used for further operation or transaction.
	 */
	public String getAccessToken(String clientId, String clientSecret)
			throws URISyntaxException, UnsupportedEncodingException, IOException, ClientProtocolException {
		
		String auth = UpholdUtil.credential(clientId, clientSecret);

		String getAccessTokenUri = null;
		if (walletEnvironment == WalletEnvironment.SANDBOX_TEST) {
			getAccessTokenUri = "https://api-sandbox.uphold.com/oauth2/token";
		} else {
			getAccessTokenUri = "https://api.uphold.com/oauth2/token";
		}

		HttpPost getAccessTokenRequest = new HttpPost(getAccessTokenUri);

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("grant_type", "client_credentials"));
		getAccessTokenRequest.setEntity(new UrlEncodedFormEntity(params));

		getAccessTokenRequest.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + auth);
		getAccessTokenRequest.addHeader("content-type", "application/x-www-form-urlencoded");

		CloseableHttpClient getAccessHttpClient = HttpClients.createDefault();
		CloseableHttpResponse getAccessTokenResponse = getAccessHttpClient.execute(getAccessTokenRequest);

		HttpEntity entity = getAccessTokenResponse.getEntity();
		String json = EntityUtils.toString(entity);

		getAccessHttpClient.close();
		getAccessTokenResponse.close();

		JsonNode jsonNode = (new ObjectMapper()).readTree(json);
		String accessToken = jsonNode.get("access_token").asText();
		return accessToken;
	}

	public JsonNode getUserInfo(String accessToken) throws IOException {
	
		String getUserInfoUri = null;
		if (walletEnvironment == WalletEnvironment.SANDBOX_TEST) {
			getUserInfoUri = "https://api-sandbox.uphold.com/v0/me";
		} else {
			getUserInfoUri = "https://api.uphold.com/v0/me";
		}

		HttpGet getUserInfoRequest = new HttpGet(getUserInfoUri);

		getUserInfoRequest.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

		CloseableHttpClient getUserInfoHttpClient = HttpClients.createDefault();
		CloseableHttpResponse getUserInfoResponse = getUserInfoHttpClient.execute(getUserInfoRequest);

		HttpEntity entity = getUserInfoResponse.getEntity();
		String json = EntityUtils.toString(entity);

		getUserInfoHttpClient.close();
		getUserInfoResponse.close();

		JsonNode jsonNode = (new ObjectMapper()).readTree(json);
		return jsonNode;

	}
}
