package org.vaultage.vwallet.account;

import org.vaultage.vwallet.util.Currency;

public class Card extends Account {

	private double balance;
	private double available;

	public Card(String accountId, String userId, Currency currency) {
		super(accountId, userId, currency);
		this.balance = 0.0;
		this.available = 0.0;
	}

	public void deposit(double amount) {
		this.balance = balance + amount;
		this.updateLastTransaction();
	}

	public void withdraw(double amount) {
		this.balance = balance - amount;
		this.updateLastTransaction();
	}

	public double getBalance() {
		return balance;
	}

	public double getAvailable() {
		return available;
	}

}
