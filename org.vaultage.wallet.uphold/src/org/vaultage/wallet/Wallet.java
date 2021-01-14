package org.vaultage.wallet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;

public interface Wallet {
	
	public enum WalletEnvironment {
		PRODUCTION,
		SANDBOX_TEST
	}
	
	/***
	 * Returning HTML page that asks a user to give authorisation for the application.
	 * 
	 * @param clientId
	 * @param scope
	 * @param state
	 * @return
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public String authorise(String clientId, String scope, String state) throws URISyntaxException, IOException, ClientProtocolException;
	
	/***
	 * Get an access token to be used for further operation or transaction.
	 * 
	 * @param clientId
	 * @param clientSecret
	 * @return
	 * @throws URISyntaxException
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public String getAccessToken(String clientId, String clientSecret)
			throws URISyntaxException, UnsupportedEncodingException, IOException, ClientProtocolException;
}
