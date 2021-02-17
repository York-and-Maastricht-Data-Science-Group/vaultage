package org.vaultage.core;

import java.util.HashSet;
import java.util.Set;

public abstract class ResponseMessageHandler {
	
	public abstract void process(VaultageMessage message, String senderPublicKey, Object vault) throws Exception;

}
