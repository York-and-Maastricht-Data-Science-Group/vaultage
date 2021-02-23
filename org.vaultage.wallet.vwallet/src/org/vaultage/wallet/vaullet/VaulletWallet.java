package org.vaultage.wallet.vaullet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.UUID;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.vaultage.wallet.Wallet;
import org.vaultage.wallet.WalletException;
import org.vaultage.wallet.vaullet.util.Currency;
import org.vaultage.wallet.vaullet.util.Path;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public class VaulletWallet implements Wallet {

	public final String WALLET_NAME = "Vaullet";
	private final String host;

	private String clientId;
	private String clientSecret;
	private String username;
	private String password;

	private String type = "Vaullet";
	private String name;
	private String billAddress;
	private String bank;
	private String bankAddress;
	private String currency;
	private String accountNumber;
	private String walletId;
	
	public VaulletWallet(String walletServerAddress) {
		this.host = walletServerAddress;
		this.walletId = UUID.randomUUID().toString();
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String authorise(String clientId, String scope, String state)
			throws URISyntaxException, IOException, ClientProtocolException, WalletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAccessToken(String username, String password) throws URISyntaxException,
			UnsupportedEncodingException, IOException, ClientProtocolException, WalletException {

		HttpResponse<String> response = Unirest.post(host + "/token") //
				.field("userId", username) //
				.field("password", password) //
				.asString();

		return response.getBody();
	}

	@Override
	public JsonNode getUserInfo(String accessToken) throws IOException, WalletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JsonNode getAuthenticationMethods(String username, String password)
			throws ClientProtocolException, IOException, WalletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createNewPersonalAccessToken(String description, String username, String password, String otpMethodId,
			String otpToken) throws ParseException, IOException, WalletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JsonNode getPersonalAccessTokens(String personalAccessToken)
			throws ParseException, IOException, WalletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JsonNode getAccounts(String accessToken) throws ClientProtocolException, IOException, WalletException {

		HttpResponse<kong.unirest.JsonNode> jsonNode = Unirest.post(host + Path.Web.ACCOUNTS) //
				.field("token", accessToken) //
				.asJson();
		kong.unirest.JsonNode accounts = jsonNode.getBody();
		JsonNode temp = (new ObjectMapper()).readTree(accounts.toString());
		return temp;
	}

	@Override
	public JsonNode transfer(String originCardId, String destinationCardId, String currency, double amount,
			String accessToken, String message, String reference) throws WalletException, IOException {

		HttpResponse<kong.unirest.JsonNode> jsonNode = Unirest.post(host + Path.Web.TRANSACTION) //
				.field("origin", originCardId) //
				.field("currency", Currency.GBP.toString()) //
				.field("amount", String.valueOf(amount)) //
				.field("destination", destinationCardId) //
				.field("token", accessToken) //
				.asJson();
		kong.unirest.JsonNode transaction = jsonNode.getBody();
		JsonNode temp = (new ObjectMapper()).readTree(transaction.toString());
		return temp;
	}

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBillAddress() {
		return billAddress;
	}

	public void setBillAddress(String billAddress) {
		this.billAddress = billAddress;
	}

	public String getBank() {
		return bank;
	}

	public void setBank(String bank) {
		this.bank = bank;
	}

	public String getBankAddress() {
		return bankAddress;
	}

	public void setBankAddress(String bankAddress) {
		this.bankAddress = bankAddress;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getHost() {
		return host;
	}

	@Override
	public String getWalletId() {
		return this.walletId;
	}

	@Override
	public String getWalletName() {
		return this.WALLET_NAME;
	}

	
}
