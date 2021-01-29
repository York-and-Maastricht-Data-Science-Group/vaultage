package org.vaultage.vwallet.transaction;

public enum TransactionState {
	PENDING, 
	WAITING,
	PROCESSING, 
	CANCELLED, 
	FAILED, 
	COMPLETED
}
