package org.vaultage.wallet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;

import com.fasterxml.jackson.databind.JsonNode;

public interface Wallet {

	public enum WalletEnvironment {
		PRODUCTION, SANDBOX
	}

	public String getWalletName();
	
	public String getWalletId(); 
	
	public String getClientId();

	public void setClientId(String clientId);

	public String getClientSecret();

	public void setClientSecret(String clientSecret);

	public String getUsername();

	public void setUsername(String username);

	public String getPassword();

	public void setPassword(String password);

	public String getBillAddress();

	public void setBillAddress(String billAddress);

	public String getBankAddress();

	public void setBankAddress(String bankAddress);

	public String getType();

	public String getBank();

	public void setBank(String bank);

	public String getCurrency();

	public void setCurrency(String currency);

	public String getName();

	public void setName(String name);

	public String getAccountNumber();

	public void setAccountNumber(String accountNumber);

	/***
	 * Returning HTML page that asks a user to give authorisation for the
	 * application.
	 * 
	 * @param clientId
	 * @param scope
	 * @param state
	 * @return
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws WalletException
	 */
	public String authorise(String clientId, String scope, String state)
			throws URISyntaxException, IOException, ClientProtocolException, WalletException;

	/***
	 * Get an access token using clientId and secret to be used for further
	 * operation or transaction.
	 * 
	 * @param clientId
	 * @param clientSecret
	 * @return
	 * @throws URISyntaxException
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws WalletException
	 */
	public String getAccessToken(String clientId, String clientSecret) throws URISyntaxException,
			UnsupportedEncodingException, IOException, WalletException;

	public JsonNode getUserInfo(String accessToken) throws IOException, WalletException;

	public JsonNode getAuthenticationMethods(String username, String password)
			throws ClientProtocolException, IOException, WalletException;

	public String createNewPersonalAccessToken(String description, String username, String password, String otpMethodId,
			String otpToken) throws ParseException, IOException, WalletException;

	public JsonNode getPersonalAccessTokens(String personalAccessToken)
			throws ParseException, IOException, WalletException;

	public JsonNode getAccounts(String accessToken) throws ClientProtocolException, IOException, WalletException;

	public JsonNode transfer(String originCardId, String destinationCardId, String currency, double amount,
			String accessToken, String message, String reference) throws WalletException, IOException;
}
