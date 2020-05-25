package org.vaultage.demo.fairnet.gen;

import java.util.List;

import org.vaultage.core.VaultageMessage;
import org.vaultage.core.VaultageServer;
import org.vaultage.demo.fairnet.app.FairnetVault;

import org.vaultage.demo.fairnet.app.AddFriendRequestHandler;
import org.vaultage.demo.fairnet.app.AddFriendResponseHandler;	
import org.vaultage.demo.fairnet.app.GetPostRequestHandler;
import org.vaultage.demo.fairnet.app.GetPostResponseHandler;	
import org.vaultage.demo.fairnet.app.GetPostsRequestHandler;
import org.vaultage.demo.fairnet.app.GetPostsResponseHandler;	

public class RemoteRequester {

	protected VaultageServer vaultageServer;
	protected FairnetVault requesterVault;
	
	public RemoteRequester(VaultageServer vaultageServer, FairnetVault vault) throws Exception {
		this.vaultageServer = vaultageServer;
		this.requesterVault = vault;
	}
	
	public Boolean addFriend(String requesteePublicKey) throws Exception {
	
		VaultageMessage message = new VaultageMessage();
		message.setSenderId(requesterVault.getId());
		message.setFrom(requesterVault.getPublicKey());
		message.setTo(requesteePublicKey);
		message.setOperation(AddFriendRequestHandler.class.getName());
		
		
		this.requesterVault.getAddFriendResponseBaseHandler().setCallerThread(Thread.currentThread());
		
		this.requesterVault.getVaultage().sendMessage(message.getTo(), requesterVault.getPublicKey(),
		requesterVault.getPrivateKey(), message);
		
		synchronized (Thread.currentThread()) {
			Thread.currentThread().wait();
		}
		
		return (Boolean) requesterVault.getAddFriendResponseBaseHandler().getResult();
	}
	
	public Post getPost(String requesteePublicKey, String postId) throws Exception {
	
		VaultageMessage message = new VaultageMessage();
		message.setSenderId(requesterVault.getId());
		message.setFrom(requesterVault.getPublicKey());
		message.setTo(requesteePublicKey);
		message.setOperation(GetPostRequestHandler.class.getName());
		
		message.putValue("postId", postId);
		
		this.requesterVault.getGetPostResponseBaseHandler().setCallerThread(Thread.currentThread());
		
		this.requesterVault.getVaultage().sendMessage(message.getTo(), requesterVault.getPublicKey(),
		requesterVault.getPrivateKey(), message);
		
		synchronized (Thread.currentThread()) {
			Thread.currentThread().wait();
		}
		
		return (Post) requesterVault.getGetPostResponseBaseHandler().getResult();
	}
	
	public List<String> getPosts(String requesteePublicKey) throws Exception {
	
		VaultageMessage message = new VaultageMessage();
		message.setSenderId(requesterVault.getId());
		message.setFrom(requesterVault.getPublicKey());
		message.setTo(requesteePublicKey);
		message.setOperation(GetPostsRequestHandler.class.getName());
		
		
		this.requesterVault.getGetPostsResponseBaseHandler().setCallerThread(Thread.currentThread());
		
		this.requesterVault.getVaultage().sendMessage(message.getTo(), requesterVault.getPublicKey(),
		requesterVault.getPrivateKey(), message);
		
		synchronized (Thread.currentThread()) {
			Thread.currentThread().wait();
		}
		
		return (List<String>) requesterVault.getGetPostsResponseBaseHandler().getResult();
	}
	
	
	
	
}