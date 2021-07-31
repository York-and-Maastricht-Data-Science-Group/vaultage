package org.vaultage.core;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.epsilon.eol.types.EolSequence;
import org.vaultage.core.VaultageMessage.MessageType;
import org.vaultage.wallet.Wallet;

public class RemoteVault {

	protected Vault localVault;
	protected String remotePublicKey;
	protected InetSocketAddress receiverSocketAddress;

	public RemoteVault(Vault localVault, String remotePublicKey) {
		this.localVault = localVault;
		this.remotePublicKey = remotePublicKey;
	}

	public RemoteVault(Vault localVault, String remotePublicKey, InetSocketAddress receiverSocketAddress) {
		this.localVault = localVault;
		this.remotePublicKey = remotePublicKey;
		this.receiverSocketAddress = receiverSocketAddress;
	}

	public String getRemotePublicKey() {
		return remotePublicKey;
	}

	public Vault getLocalVault() {
		return localVault;
	}

	public void setLocalVault(Vault localVault) {
		this.localVault = localVault;
	}

	public void setRemotePublicKey(String remotePublicKey) {
		this.remotePublicKey = remotePublicKey;
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
		response.setToken(token);
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

	public String query(String query, Map<String, Object> parameters) throws Exception {
		return this.query(query, parameters, true);
	}

	public String query(String query, Map<String, Object> parameters, boolean isEncrypted) throws Exception {
		VaultageMessage request = new VaultageMessage();
		request.initToken();
		request.setSenderId(localVault.getId());
		request.setFrom(localVault.getPublicKey());
		request.setTo(remotePublicKey);
		request.setMessageType(MessageType.REQUEST);
		request.setOperation("query");

		request.putValue("query", Vaultage.serialise(query));
		request.putValue("parameters", Vaultage.serialise(parameters));

		localVault.getVaultage().sendMessage(request.getTo(), localVault.getPublicKey(), localVault.getPrivateKey(),
				request, isEncrypted);

		return request.getToken();
	}

	public void respondToQuery(Object result, String token) throws InterruptedException {
		this.respondToQuery(result, token, true);
	}

	public void respondToQuery(Object result, String token, boolean isEncrypted) throws InterruptedException {
		VaultageMessage response = new VaultageMessage();
		response.setToken(token);
		response.setTo(remotePublicKey);
		response.setOperation("query");
		response.setMessageType(MessageType.RESPONSE);
		response.setRemoteVaultType(this.getClass().getName());

		Method m = new Object() {
		}.getClass().getEnclosingMethod();
		Type returnType = m.getGenericParameterTypes()[0];
		String returnTypeName = returnType.getTypeName();
		returnTypeName = result.getClass().getTypeName();

		// handle parameterised type result
		if (returnType.equals(Object.class)) {
			Class<?> type = result.getClass();
			if (result instanceof Collection) {
				Iterator<?> iterator = ((Collection<?>) result).iterator();
				Object element = (iterator.hasNext()) ? iterator.next() : null;
				if (element != null) {
					Class<?> subtype = element.getClass();
					returnTypeName = type.getName() + "<" + subtype.getName() + ">";
				} else {
					returnTypeName = type.getName();
				}
			} else if (result instanceof Map) {
				Iterator<?> iterator = ((Map<?, ?>) result).entrySet().iterator();
				Entry<?, ?> element = (iterator.hasNext()) ? (Entry<?, ?>) iterator.next() : null;
				if (element != null) {
					Class<?> subtype1 = element.getKey().getClass();
					Class<?> subtype2 = element.getValue().getClass();
					returnTypeName = type.getName() + "<" + subtype1.getName() + ", " + subtype2.getName() + ">";
				} else {
					returnTypeName = type.getName();
				}
			}
		}

		response.setReturnType(returnTypeName);
		response.putValue("result", Vaultage.serialise(result));

		localVault.getVaultage().sendMessage(response.getTo(), localVault.getPublicKey(), localVault.getPrivateKey(),
				response, isEncrypted);

	}
}
