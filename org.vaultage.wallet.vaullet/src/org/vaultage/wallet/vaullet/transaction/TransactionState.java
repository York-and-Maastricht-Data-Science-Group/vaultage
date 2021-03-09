package org.vaultage.wallet.vaullet.transaction;

public enum TransactionState {
	PENDING, 
	WAITING,
	PROCESSING, 
	CANCELLED, 
	FAILED, 
	COMPLETED
}
