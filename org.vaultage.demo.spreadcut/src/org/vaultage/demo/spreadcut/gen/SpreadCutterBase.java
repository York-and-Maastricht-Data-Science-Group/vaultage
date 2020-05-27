package org.vaultage.demo.spreadcut.gen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import org.vaultage.util.VaultageEncryption;
import org.vaultage.core.Vaultage;
import org.vaultage.core.VaultageHandler;
import org.vaultage.core.VaultageServer;


public abstract class SpreadCutterBase {

	protected String id = UUID.randomUUID().toString();
	protected String privateKey;
	protected String publicKey;
	protected boolean isListening;
	protected Vaultage vaultage;
	protected Map<String, VaultageHandler> handlers;	
	
	protected GetContactsRequestBaseHandler getContactsRequestBaseHandler;
	protected GetContactsResponseBaseHandler getContactsResponseBaseHandler;	
	protected ConfirmContactRequestBaseHandler confirmContactRequestBaseHandler;
	protected ConfirmContactResponseBaseHandler confirmContactResponseBaseHandler;	
	
	
	public SpreadCutterBase() throws FileNotFoundException, IOException, NoSuchAlgorithmException {
		this.isListening = false;
		this.vaultage = new Vaultage();
		this.handlers = new HashMap<String, VaultageHandler>();
		
		KeyPair keyPair = VaultageEncryption.generateKeys();
		this.publicKey = VaultageEncryption.getPublicKey(keyPair);
		this.privateKey = VaultageEncryption.getPrivateKey(keyPair);
	}
	
	public String getId(){
		return this.id;
	}
	
	public void setId(String id){
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
	
	public void setGetContactsRequestBaseHandler(GetContactsRequestBaseHandler getContactsRequestBaseHandler) {
		this.getContactsRequestBaseHandler = getContactsRequestBaseHandler;
		this.getContactsRequestBaseHandler.setOwner(this);
		handlers.put(getContactsRequestBaseHandler.getClass().getName(), getContactsRequestBaseHandler);
	}
	
	public GetContactsRequestBaseHandler getGetContactsRequestBaseHandler() {
		return getContactsRequestBaseHandler;
	}
	
	public void setGetContactsResponseBaseHandler(GetContactsResponseBaseHandler getContactsResponseBaseHandler) {
		this.getContactsResponseBaseHandler = getContactsResponseBaseHandler;
		this.getContactsResponseBaseHandler.setOwner(this);
		handlers.put(getContactsResponseBaseHandler.getClass().getName(), getContactsResponseBaseHandler);
	}
	
	public GetContactsResponseBaseHandler getGetContactsResponseBaseHandler() {
		return getContactsResponseBaseHandler;
	}
	
	public void setConfirmContactRequestBaseHandler(ConfirmContactRequestBaseHandler confirmContactRequestBaseHandler) {
		this.confirmContactRequestBaseHandler = confirmContactRequestBaseHandler;
		this.confirmContactRequestBaseHandler.setOwner(this);
		handlers.put(confirmContactRequestBaseHandler.getClass().getName(), confirmContactRequestBaseHandler);
	}
	
	public ConfirmContactRequestBaseHandler getConfirmContactRequestBaseHandler() {
		return confirmContactRequestBaseHandler;
	}
	
	public void setConfirmContactResponseBaseHandler(ConfirmContactResponseBaseHandler confirmContactResponseBaseHandler) {
		this.confirmContactResponseBaseHandler = confirmContactResponseBaseHandler;
		this.confirmContactResponseBaseHandler.setOwner(this);
		handlers.put(confirmContactResponseBaseHandler.getClass().getName(), confirmContactResponseBaseHandler);
	}
	
	public ConfirmContactResponseBaseHandler getConfirmContactResponseBaseHandler() {
		return confirmContactResponseBaseHandler;
	}
	
	
	
	
	public boolean register(VaultageServer fairnet) throws Exception {
		boolean isSuccess = vaultage.connect(fairnet.getAddress());
		if (isSuccess) {
			vaultage.subscribe(publicKey, privateKey, handlers);
			return true;
		}
		return false;
	}
	
	public void unregister() throws Exception {
		vaultage.disconnect();
	}
	
	// operations
	protected abstract List<ProximityContact> getContacts(String requesterPublicKey, String timestamp) throws Exception;
	
	protected abstract boolean confirmContact(String requesterPublicKey, String timestamp) throws Exception;
	
	
	
	
}
