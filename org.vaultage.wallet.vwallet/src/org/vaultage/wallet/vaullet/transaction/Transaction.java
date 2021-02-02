package org.vaultage.wallet.vaullet.transaction;

import java.time.Instant;

import org.vaultage.wallet.vaullet.account.Account;
import org.vaultage.wallet.vaullet.account.Bank;
import org.vaultage.wallet.vaullet.account.Card;
import org.vaultage.wallet.vaullet.util.Currency;

public class Transaction {

	private String createdAt;
	private String lastUpdate;
	private TransactionState transactionState = TransactionState.PENDING;
	private Currency currency;
	private double amount;
	private Account origin;
	private Account destination;

	public Transaction(Account origin, Account destination, Currency currency, double amount) {
		this.origin = origin;
		this.destination = destination;
		this.currency = currency;
		this.amount = amount;
		this.createdAt = Instant.now().toString();
	}
	
	public Transaction execute() throws InvalidTransactionException {
		
		// wallet to wallet
		if (origin instanceof Card && destination instanceof Card ) {
			((Card)origin).withdraw(amount);
			((Card)destination).deposit(amount);
			this.transactionState = TransactionState.COMPLETED;
		} 
		// wallet to bank
		else if (origin instanceof Card && destination instanceof Bank) {
			
		} 
		// bank to wallet
		else if (origin instanceof Bank && destination instanceof Card) {
			
		} else {
			throw new InvalidTransactionException();
		}
		
		this.lastUpdate = Instant.now().toString();
		return this;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public TransactionState getTransactionState() {
		return transactionState;
	}

	public double getAmount() {
		return amount;
	}

	public Account getOrigin() {
		return origin;
	}

	public Account getDestination() {
		return destination;
	}

	public Currency getCurrency() {
		return currency;
	}

	public String getLastUpdate() {
		return lastUpdate;
	}

}
