package org.vaultage.demo.fairnet.gen;

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


public abstract class FairnetVaultBase {

	protected String id = UUID.randomUUID().toString();
	protected String privateKey;
	protected String publicKey;
	protected boolean isListening;
	protected Vaultage vaultage;
	protected Map<String, VaultageHandler> handlers;	
	
	protected AddFriendRequestBaseHandler addFriendRequestBaseHandler;
	protected AddFriendResponseBaseHandler addFriendResponseBaseHandler;	
	protected GetPostRequestBaseHandler getPostRequestBaseHandler;
	protected GetPostResponseBaseHandler getPostResponseBaseHandler;	
	protected GetPostsRequestBaseHandler getPostsRequestBaseHandler;
	protected GetPostsResponseBaseHandler getPostsResponseBaseHandler;	
	
	
	public FairnetVaultBase() throws FileNotFoundException, IOException, NoSuchAlgorithmException {
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
	
	public void setAddFriendRequestBaseHandler(AddFriendRequestBaseHandler addFriendRequestBaseHandler) {
		this.addFriendRequestBaseHandler = addFriendRequestBaseHandler;
		this.addFriendRequestBaseHandler.setOwner(this);
		handlers.put(addFriendRequestBaseHandler.getClass().getName(), addFriendRequestBaseHandler);
	}
	
	public AddFriendRequestBaseHandler getAddFriendRequestBaseHandler() {
		return addFriendRequestBaseHandler;
	}
	
	public void setAddFriendResponseBaseHandler(AddFriendResponseBaseHandler addFriendResponseBaseHandler) {
		this.addFriendResponseBaseHandler = addFriendResponseBaseHandler;
		this.addFriendResponseBaseHandler.setOwner(this);
		handlers.put(addFriendResponseBaseHandler.getClass().getName(), addFriendResponseBaseHandler);
	}
	
	public AddFriendResponseBaseHandler getAddFriendResponseBaseHandler() {
		return addFriendResponseBaseHandler;
	}
	
	public void setGetPostRequestBaseHandler(GetPostRequestBaseHandler getPostRequestBaseHandler) {
		this.getPostRequestBaseHandler = getPostRequestBaseHandler;
		this.getPostRequestBaseHandler.setOwner(this);
		handlers.put(getPostRequestBaseHandler.getClass().getName(), getPostRequestBaseHandler);
	}
	
	public GetPostRequestBaseHandler getGetPostRequestBaseHandler() {
		return getPostRequestBaseHandler;
	}
	
	public void setGetPostResponseBaseHandler(GetPostResponseBaseHandler getPostResponseBaseHandler) {
		this.getPostResponseBaseHandler = getPostResponseBaseHandler;
		this.getPostResponseBaseHandler.setOwner(this);
		handlers.put(getPostResponseBaseHandler.getClass().getName(), getPostResponseBaseHandler);
	}
	
	public GetPostResponseBaseHandler getGetPostResponseBaseHandler() {
		return getPostResponseBaseHandler;
	}
	
	public void setGetPostsRequestBaseHandler(GetPostsRequestBaseHandler getPostsRequestBaseHandler) {
		this.getPostsRequestBaseHandler = getPostsRequestBaseHandler;
		this.getPostsRequestBaseHandler.setOwner(this);
		handlers.put(getPostsRequestBaseHandler.getClass().getName(), getPostsRequestBaseHandler);
	}
	
	public GetPostsRequestBaseHandler getGetPostsRequestBaseHandler() {
		return getPostsRequestBaseHandler;
	}
	
	public void setGetPostsResponseBaseHandler(GetPostsResponseBaseHandler getPostsResponseBaseHandler) {
		this.getPostsResponseBaseHandler = getPostsResponseBaseHandler;
		this.getPostsResponseBaseHandler.setOwner(this);
		handlers.put(getPostsResponseBaseHandler.getClass().getName(), getPostsResponseBaseHandler);
	}
	
	public GetPostsResponseBaseHandler getGetPostsResponseBaseHandler() {
		return getPostsResponseBaseHandler;
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
	protected abstract java.lang.Boolean addFriend(String requesterPublicKey) throws Exception;
	
	protected abstract Post getPost(String requesterPublicKey, String postId) throws Exception;
	
	protected abstract List<String> getPosts(String requesterPublicKey) throws Exception;
	
	
	
	
}
