package org.vaultage.core;

public abstract class ResponseMessageHandler {
	public abstract void process(VaultageMessage message, String senderPublicKey, Object vault) throws Exception;
}
