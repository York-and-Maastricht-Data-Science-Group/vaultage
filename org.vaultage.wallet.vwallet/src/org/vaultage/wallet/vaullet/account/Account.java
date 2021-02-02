package org.vaultage.wallet.vaullet.account;

import java.time.Instant;

import org.vaultage.wallet.vaullet.util.Currency;

public abstract class Account {

	private String accountId;
	private String userId;
	private Currency currency;
	private String lastTransaction; // a timestamp in String

	public Account(String accountId, String userId, Currency currency) {
		this.accountId = accountId;
		this.userId = userId;
		this.currency = currency;
	}

	public String getAccountId() {
		return accountId;
	}

	public String getUserId() {
		return userId;
	}

	public Currency getCurrency() {
		return currency;
	}

	public String getLastTransaction() {
		return lastTransaction;
	}

	public void updateLastTransaction() {
		this.lastTransaction = Instant.now().toString();

	}

}
