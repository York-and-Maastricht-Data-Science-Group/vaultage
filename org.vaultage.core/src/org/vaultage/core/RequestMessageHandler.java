package org.vaultage.core;

public abstract class RequestMessageHandler {

	public void process(VaultageMessage message, String senderPublicKey, Object vault) throws Exception {
		switch (message.getOperation()) {

		case "getPaymentWallets": {
			((Vault) vault).getPaymentWallets(senderPublicKey, message.getToken());
		}

		}
	}
}
