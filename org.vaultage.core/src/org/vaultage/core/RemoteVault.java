package org.vaultage.core;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Set;

import org.vaultage.core.VaultageMessage.MessageType;
import org.vaultage.wallet.Wallet;

public class RemoteVault {

	protected Vault localVault;
	protected String remotePublicKey;

	public RemoteVault(Vault localVault, String remotePublicKey) {
		this.localVault = localVault;
		this.remotePublicKey = remotePublicKey;
	}

	public String getRemotePublicKey() {
		return remotePublicKey;
	}

	public String forceBrokeredMessaging(boolean forceBrokeredMessaging) throws Exception {
		return this.forceBrokeredMessaging(true);
	}

	public String forceBrokeredMessaging(boolean forceBrokeredMessaging, boolean isEncrypted) throws Exception {
		VaultageMessage request = new VaultageMessage();
		request.initToken();
		request.setSenderId(localVault.getId());
		request.setFrom(localVault.getPublicKey());
		request.setTo(remotePublicKey);
		request.setMessageType(MessageType.REQUEST);
		request.setOperation("forceBrokeredMessaging");

		request.putValue("forceBrokeredMessaging", Vaultage.serialise(forceBrokeredMessaging));

		localVault.getVaultage().sendMessage(request.getTo(), localVault.getPublicKey(), localVault.getPrivateKey(),
				request, isEncrypted);

		return request.getToken();
	}

	public String setEncrypted(boolean forceBrokeredMessaging) throws Exception {
		return this.setEncrypted(true);
	}

	public String setEncrypted(boolean setEncrypted, boolean isEncrypted) throws Exception {
		VaultageMessage request = new VaultageMessage();
		request.initToken();
		request.setSenderId(localVault.getId());
		request.setFrom(localVault.getPublicKey());
		request.setTo(remotePublicKey);
		request.setMessageType(MessageType.REQUEST);
		request.setOperation("setEncrypted");

		request.putValue("setEncrypted", Vaultage.serialise(setEncrypted));

		localVault.getVaultage().sendMessage(request.getTo(), localVault.getPublicKey(), localVault.getPrivateKey(),
				request, isEncrypted);

		return request.getToken();
	}

	public String getPaymentWallets() throws Exception {
		return this.getPaymentWallets(true);
	}

	public String getPaymentWallets(boolean isEncrypted) throws Exception {
		VaultageMessage request = new VaultageMessage();
		request.initToken();
		request.setSenderId(localVault.getId());
		request.setFrom(localVault.getPublicKey());
		request.setTo(remotePublicKey);
		request.setMessageType(MessageType.REQUEST);
		request.setOperation("getWallets");

		localVault.getVaultage().sendMessage(request.getTo(), localVault.getPublicKey(), localVault.getPrivateKey(),
				request, isEncrypted);

		return request.getToken();
	}

	public void respondToGetPaymentWallets(Set<Wallet> result, String token) throws Exception {
		this.respondToGetPaymentWallets(result, token, true);
	}

	public void respondToGetPaymentWallets(Set<Wallet> result, String token, boolean isEncrypted) throws Exception {

		VaultageMessage response = new VaultageMessage();
		response.setToken(token); // TODO: this token is not known by the vault
		response.setFrom(localVault.getPublicKey());
		response.setTo(remotePublicKey);
		response.setOperation("getWallets");
		response.setMessageType(MessageType.RESPONSE);
		response.setRemoteVaultType(this.getClass().getName());

		Method m = new Object() {
		}.getClass().getEnclosingMethod();
		Type x = m.getGenericParameterTypes()[0];
		String returnType = x.getTypeName();
		response.setReturnType(returnType);

		response.putValue("result", Vaultage.serialise(result));

		localVault.getVaultage().sendMessage(response.getTo(), localVault.getPublicKey(), localVault.getPrivateKey(),
				response, isEncrypted);
	}
}
