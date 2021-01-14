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
import org.apache.http.ParseException;
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

		URI uri = null;

		if (walletEnvironment == WalletEnvironment.SANDBOX_TEST) {
			uri = new URIBuilder() //
					.setScheme("https") //
					.setHost("sandbox.uphold.com") //
					.setPath("/authorize/" + clientId) //
					.setParameter("scope", scope) //
					.setParameter("state", state) //
					.build();
		} else {
			uri = new URIBuilder() //
					.setScheme("https") //
					.setHost("uphold.com") //
					.setPath("/authorize/" + clientId) //
					.setParameter("scope", scope) //
					.setParameter("state", state) //
					.build();
		}

		CloseableHttpClient httpClient = HttpClients.createDefault();

		HttpPost request = new HttpPost(uri);
		CloseableHttpResponse response = httpClient.execute(request);

		HttpEntity entity = response.getEntity();
		String result = EntityUtils.toString(entity);

		httpClient.close();
		response.close();

		return result;
	}

	/***
	 * Get an access token to be used for further operation or transaction.
	 */
	public String getAccessToken(String clientId, String clientSecret)
			throws URISyntaxException, UnsupportedEncodingException, IOException, ClientProtocolException {

		String auth = UpholdUtil.credential(clientId, clientSecret);

		String uri = null;
		if (walletEnvironment == WalletEnvironment.SANDBOX_TEST) {
			uri = "https://api-sandbox.uphold.com/oauth2/token";
		} else {
			uri = "https://api.uphold.com/oauth2/token";
		}

		HttpPost request = new HttpPost(uri);

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("grant_type", "client_credentials"));
		request.setEntity(new UrlEncodedFormEntity(params));

		request.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + auth);
		request.addHeader("content-type", "application/x-www-form-urlencoded");

		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = httpClient.execute(request);

		HttpEntity entity = response.getEntity();
		String json = EntityUtils.toString(entity);

		httpClient.close();
		response.close();

		JsonNode jsonNode = (new ObjectMapper()).readTree(json);
		String accessToken = jsonNode.get("access_token").asText();
		return accessToken;
	}

	public JsonNode getUserInfo(String accessToken) throws IOException {

		String uri = null;
		if (walletEnvironment == WalletEnvironment.SANDBOX_TEST) {
			uri = "https://api-sandbox.uphold.com/v0/me";
		} else {
			uri = "https://api.uphold.com/v0/me";
		}

		HttpGet request = new HttpGet(uri);

		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = httpClient.execute(request);

		HttpEntity entity = response.getEntity();
		String json = EntityUtils.toString(entity);

		httpClient.close();
		response.close();

		JsonNode jsonNode = (new ObjectMapper()).readTree(json);
		return jsonNode;

	}

	public JsonNode getAuthenticationMethods(String username, String password)
			throws ClientProtocolException, IOException {

		String auth = UpholdUtil.credential(username, password);

		String uri = null;
		if (walletEnvironment == WalletEnvironment.SANDBOX_TEST) {
			uri = "https://api-sandbox.uphold.com/v0/me/authentication_methods";
		} else {
			uri = "https://api.uphold.com/v0/me/authentication_methods";
		}

		HttpGet request = new HttpGet(uri);

		request.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + auth);

		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = httpClient.execute(request);

		HttpEntity entity = response.getEntity();
		String json = EntityUtils.toString(entity);

		httpClient.close();
		response.close();

		JsonNode jsonNode = (new ObjectMapper()).readTree(json);
		return jsonNode;
	}

	public String createNewPersonalAccessToken(String description, String username, String password, String otpMethodId,
			String otpToken) throws ParseException, IOException {

		String auth = UpholdUtil.credential(username, password);
		//YWxmYS55b2hhbm5pc0BnbWFpbC5jb206TG9sb2wzeCE=
		//YWxmYS55b2hhbm5pc0BnbWFpbC5jb206TG9sb2wzeCE=

		String uri = null;
		if (walletEnvironment == WalletEnvironment.SANDBOX_TEST) {
			uri = "https://api-sandbox.uphold.com/v0/me/tokens";
			otpToken = "000000";
		} else {
			uri = "https://api.uphold.com/v0/me/tokens";
		}

		HttpPost request = new HttpPost(uri);

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("description", description));
		request.setEntity(new UrlEncodedFormEntity(params));

		request.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + auth);
		request.addHeader("content-type", "application/x-www-form-urlencoded");
		if (otpMethodId != null) {
			request.addHeader("OTP-Method-Id", otpMethodId);
			request.addHeader("OTP-Token", otpToken);
		}

		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = httpClient.execute(request);

		HttpEntity entity = response.getEntity();
		String json = EntityUtils.toString(entity);

		httpClient.close();
		response.close();

		JsonNode jsonNode = (new ObjectMapper()).readTree(json);
		String accessToken = jsonNode.get("accessToken").asText();
		return accessToken;
	}
	
	public JsonNode getPersonalAccessTokens(String personalAccessToken) throws ParseException, IOException {


		String uri = null;
		if (walletEnvironment == WalletEnvironment.SANDBOX_TEST) {
			uri = "https://api-sandbox.uphold.com/v0/me/tokens";
		} else {
			uri = "https://api.uphold.com/v0/me/tokens";
		}

		HttpGet request = new HttpGet(uri);

		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + personalAccessToken);
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = httpClient.execute(request);

		HttpEntity entity = response.getEntity();
		String json = EntityUtils.toString(entity);

		httpClient.close();
		response.close();

		JsonNode jsonNode = (new ObjectMapper()).readTree(json);
//		String accessToken = jsonNode.get("access_token").asText();
		return jsonNode;
	}
}
