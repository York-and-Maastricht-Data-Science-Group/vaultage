package org.vaultage.wallet.vaullet.account;

import org.vaultage.wallet.vaullet.util.Currency;

public class Bank extends Account {
	
	private String bankName;
	private String accountNumber;
	
	public Bank(String accountId, String userId, Currency currency, String bankName, String accountNumber) {
		super(accountId, userId, currency);
		this.bankName = bankName;
		this.accountNumber = accountNumber;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public String getBankName() {
		return bankName;
	}
}
