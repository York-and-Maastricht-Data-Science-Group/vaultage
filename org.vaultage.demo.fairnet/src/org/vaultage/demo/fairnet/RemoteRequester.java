package org.vaultage.demo.fairnet;

import java.util.List;

import org.vaultage.core.VaultageMessage;
import org.vaultage.core.VaultageServer;
import org.vaultage.demo.fairnet.handler.AddFriendRequestHandler;
import org.vaultage.demo.fairnet.handler.GetPostRequestHandler;
import org.vaultage.demo.fairnet.handler.GetPostsRequestHandler;

public class RemoteRequester {

	protected VaultageServer vaultageServer;
	protected FairnetVault requesterVault;

	public RemoteRequester(VaultageServer vaultageServer, FairnetVault vault) throws Exception {
		this.vaultageServer = vaultageServer;
		this.requesterVault = vault;
	}

	public Friend addFriend(String friendPublicKey) throws InterruptedException {

		VaultageMessage message = new VaultageMessage();
		message.setSenderId(requesterVault.getId());
		message.setFrom(requesterVault.getPublicKey());
		message.setTo(friendPublicKey);
		message.setOperation(AddFriendRequestHandler.class.getName());
		message.putValue("value", null);

		this.requesterVault.getAddFriendResponseHandler().setCallerThread(Thread.currentThread());
		
		this.requesterVault.getVaultAge().sendMessage(message.getTo(), requesterVault.getPublicKey(),
				requesterVault.getPrivateKey(), message);

		synchronized (Thread.currentThread()) {
			Thread.currentThread().wait();
		}
		
		return this.requesterVault.getAddFriendResponseHandler().getNewFriend();
	}

	/***
	 * Get a post of a friend using its id
	 * @param friendPublicKey
	 * @param postId
	 * @return
	 * @throws InterruptedException
	 */
	public Post getPost(String friendPublicKey, String postId) throws InterruptedException {

		VaultageMessage message = new VaultageMessage();
		message.setSenderId(requesterVault.getId());
		message.setFrom(requesterVault.getPublicKey());
		message.setTo(friendPublicKey);
		message.setOperation(GetPostRequestHandler.class.getName());
		message.putValue("value", postId);

		this.requesterVault.getGetPostResponseHandler().setCallerThread(Thread.currentThread());
		
		this.requesterVault.getVaultAge().sendMessage(message.getTo(), requesterVault.getPublicKey(),
				requesterVault.getPrivateKey(), message);

		synchronized (Thread.currentThread()) {
			Thread.currentThread().wait();
		}
		
		Post post = requesterVault.getGetPostResponseHandler().getPost();
		return post;
	}

	/**
	 * Get a list of a friend's post ids
	 * @param friendPublicKey
	 * @return
	 * @throws InterruptedException
	 */
	public List<String> getPosts(String friendPublicKey) throws InterruptedException {
		VaultageMessage message = new VaultageMessage();
		message.setSenderId(requesterVault.getId());
		message.setFrom(requesterVault.getPublicKey());
		message.setTo(friendPublicKey);
		message.setOperation(GetPostsRequestHandler.class.getName());

		this.requesterVault.getGetPostsResponseHandler().setCallerThread(Thread.currentThread());
		
		this.requesterVault.getVaultAge().sendMessage(message.getTo(), requesterVault.getPublicKey(),
				requesterVault.getPrivateKey(), message);

		synchronized (Thread.currentThread()) {
			Thread.currentThread().wait();
		}

		List<String> postIds = requesterVault.getGetPostsResponseHandler().getPostIds();
		return postIds;
	}

}