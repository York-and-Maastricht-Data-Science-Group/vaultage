package org.vaultage.core;

import java.util.Map;

public abstract class RequestMessageHandler {

	public void process(VaultageMessage message, String senderPublicKey, Object vault) throws Exception {

		switch (message.getOperation()) {

		case "getPaymentWallets": {
			((Vault) vault).getPaymentWallets(senderPublicKey, message.getToken());
		}
			break;
		case "forceBrokeredMessaging": {
			boolean forceBrokeredMessaging = Vaultage.deserialise(message.getValue("forceBrokeredMessaging"),
					Boolean.class);
			((Vault) vault).forceBrokeredMessaging(senderPublicKey, message.getToken(), forceBrokeredMessaging);
		}
			break;
		case "setEncrypted": {
			boolean setEncrypted = Vaultage.deserialise(message.getValue("setEncrypted"), Boolean.class);
			((Vault) vault).setEncrypted(senderPublicKey, message.getToken(), setEncrypted);
		}
			break;
		case "query": {
			String query = Vaultage.deserialise(message.getValue("query"), String.class);
			Map<String, Object> parameters = Vaultage.deserialise(message.getValue("parameters"), Map.class);
			((Vault) vault).query(senderPublicKey, message.getToken(), query, parameters);
		}
			break;
		}
	}
}
