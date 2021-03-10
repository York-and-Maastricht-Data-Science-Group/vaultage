package org.vaultage.core;

import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.vaultage.util.VaultageEncryption;
import org.vaultage.wallet.Wallet;

public abstract class Vault {

	protected String id = UUID.randomUUID().toString();
	protected String privateKey;
	protected String publicKey;
	protected boolean isListening;
	protected Vaultage vaultage;
	protected VaultageServer vaultageServer;

	protected Set<OperationResponseHandler> operationResponseHandlers = new HashSet<>();

	protected Set<Wallet> wallets = new HashSet<>();
	protected Wallet defaultWallet;

	public Vault() throws Exception {
		this.isListening = false;
		this.vaultage = new Vaultage(this);
		KeyPair keyPair = VaultageEncryption.generateKeys();
		this.publicKey = VaultageEncryption.getPublicKey(keyPair);
		this.privateKey = VaultageEncryption.getPrivateKey(keyPair);
		initialiseMessageHandlers();
	}

	public Vault(String address, int port) throws Exception {
		this.isListening = false;
		this.vaultage = new Vaultage(this, address, port);
		KeyPair keyPair = VaultageEncryption.generateKeys();
		this.publicKey = VaultageEncryption.getPublicKey(keyPair);
		this.privateKey = VaultageEncryption.getPrivateKey(keyPair);
		initialiseMessageHandlers();
	}

	protected abstract void initialiseMessageHandlers() throws NoSuchAlgorithmException;

	public VaultageServer getVaultageServer() {
		return this.vaultageServer;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Vaultage getVaultage() {
		return vaultage;
	}

	public void setVaultage(Vaultage vaultage) {
		this.vaultage = vaultage;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public void addOperationResponseHandler(OperationResponseHandler responseHandler) {
		Set<OperationResponseHandler> responseHandlers = this.operationResponseHandlers;
		Iterator<OperationResponseHandler> iterator = responseHandlers.iterator();
		while (iterator.hasNext()) {
			OperationResponseHandler handler = iterator.next();
			if (handler.equals(responseHandler)) {
				iterator.remove();
			}
			for (Class<?> i : handler.getClass().getInterfaces()) {
				if (i.equals(responseHandler.getClass())) {
					iterator.remove();
					break;
				}
			}
		}
		responseHandlers.add(responseHandler);
	}

	public OperationResponseHandler getOperationResponseHandler(Class<?> interfaceOrClass) {
		for (OperationResponseHandler handler : this.operationResponseHandlers) {
			if (handler.getClass().equals(interfaceOrClass)) {
				return handler;
			}
			for (Class<?> i : handler.getClass().getInterfaces()) {
				if (i.equals(interfaceOrClass)) {
					return handler;
				}
			}
		}
		return null;
	}

	public boolean register(VaultageServer vaultageServer) throws Exception {
		boolean isSuccess = vaultage.connect(vaultageServer.getAddress(), publicKey);
		this.vaultageServer = vaultageServer;
		if (isSuccess) {
			vaultage.subscribe(publicKey, privateKey);
			return true;
		}
		return false;
	}

	public void unregister() throws Exception {
		vaultage.disconnect();
	}

	/***
	 * A method to start the direct messaging server of this vault.
	 * 
	 * @param address
	 * @param port
	 */
	public void startServer(String address, int port) {
		this.vaultage.startServer(address, port);
		this.vaultage.setPrivateKey(this.getPrivateKey());
	}

	/***
	 * A method to stop the direct messaging server of this vault.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void shutdownServer() throws IOException, InterruptedException {
		vaultage.shutdownServer();
	}

	/***
	 * Get all available wallets.
	 * 
	 * @return
	 */
	public Set<Wallet> getWallets() {
		return wallets;
	}

	/***
	 * Get the default wallet.
	 * 
	 * @return
	 */
	public Wallet getDefaultWallet() {
		return defaultWallet;
	}

	/***
	 * Set the default wallet and add the wallets collection if it doesn't exist.
	 * 
	 * @param defaultWallet
	 */
	public void setDefaultWallet(Wallet defaultWallet) {
		this.defaultWallet = defaultWallet;
		wallets.add(defaultWallet);
	}

	/***
	 * Send available local vault's wallets to the requester so that it can choose
	 * which wallet is the destination wallet for payment
	 * 
	 * @param requesterPublicKey
	 * @param requestToken
	 * @throws Exception
	 */
	public void getPaymentWallets(String requesterPublicKey, String requestToken) throws Exception {
		RemoteVault responder = new RemoteVault(this, requesterPublicKey);
		Set<Wallet> w = getWallets();
		responder.respondToGetPaymentWallets(w, requestToken);
	}

	/***
	 * Force messaging in brokered mode
	 * @param requesterPublicKey
	 * @param requestToken
	 * @param forceBrokeredMessaging
	 * @throws Exception
	 */
	protected void forceBrokeredMessaging(String requesterPublicKey, String requestToken,
			boolean forceBrokeredMessaging) throws Exception {
		this.getVaultage().forceBrokeredMessaging(forceBrokeredMessaging);
	}

	/***
	 * set messaging encrypted or un-encrypted
	 * @param requesterPublicKey
	 * @param requestToken
	 * @param setEncrypted
	 * @throws Exception
	 */
	protected void setEncrypted(String requesterPublicKey, String requestToken, boolean setEncrypted) throws Exception {
		this.getVaultage().setEncrypted(setEncrypted);
	}
}
