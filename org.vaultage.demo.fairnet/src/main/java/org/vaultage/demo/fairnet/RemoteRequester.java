package org.vaultage.demo.fairnet;

import java.util.List;

import org.vaultage.core.VaultAgeMessage;
import org.vaultage.core.VaultAgeServer;
import org.vaultage.demo.fairnet.handler.AddFriendRequestHandler;
import org.vaultage.demo.fairnet.handler.GetPostRequestHandler;
import org.vaultage.demo.fairnet.handler.GetPostsRequestHandler;

public class RemoteRequester {

	private static final int SLEEP_TIME = 40;
	protected VaultAgeServer vaultageServer;
	protected FairnetVault requesterVault;

	public RemoteRequester(VaultAgeServer vaultageServer, FairnetVault vault) throws Exception {
		this.vaultageServer = vaultageServer;
		this.requesterVault = vault;
	}

	public Friend addFriend(String friendPublicKey) {

		VaultAgeMessage message = new VaultAgeMessage();
		message.setSenderId(requesterVault.getId());
		message.setFrom(requesterVault.getPublicKey());
		message.setTo(friendPublicKey);
		message.setOperation(AddFriendRequestHandler.class.getName());

		this.requesterVault.getVaultAge().sendMessage(message.getTo(), requesterVault.getPublicKey(),
				requesterVault.getPrivateKey(), message);

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

		VaultAgeMessage message = new VaultAgeMessage();
		message.setSenderId(requesterVault.getId());
		message.setFrom(requesterVault.getPublicKey());
		message.setTo(friendPublicKey);
		message.setOperation(GetPostRequestHandler.class.getName());
		message.setValue(postId);

		this.requesterVault.getVaultAge().sendMessage(message.getTo(), requesterVault.getPublicKey(),
				requesterVault.getPrivateKey(), message);

		Thread.sleep(SLEEP_TIME);
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
		VaultAgeMessage message = new VaultAgeMessage();
		message.setSenderId(requesterVault.getId());
		message.setFrom(requesterVault.getPublicKey());
		message.setTo(friendPublicKey);
		message.setOperation(GetPostsRequestHandler.class.getName());

		this.requesterVault.getVaultAge().sendMessage(message.getTo(), requesterVault.getPublicKey(),
				requesterVault.getPrivateKey(), message);

		Thread.sleep(SLEEP_TIME);
		List<String> postIds = requesterVault.getGetPostsResponseHandler().getPostIds();
		return postIds;
	}

}