package org.vaultage.core;

public abstract class RequestMessageHandler {
	public abstract void process(VaultageMessage message, String senderPublicKey, Object vault) throws Exception;
}
