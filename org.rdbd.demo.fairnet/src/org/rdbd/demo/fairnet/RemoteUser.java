package org.rdbd.demo.fairnet;

import org.rdbd.core.RDBDMessage;
import org.rdbd.demo.fairnet.handler.AddFriendResponseHandler;
import org.rdbd.demo.fairnet.handler.GetPostResponseHandler;
import org.rdbd.demo.fairnet.handler.GetPostsResponseHandler;

public class RemoteUser {

	protected Fairnet fairnet;
	protected FairnetVault me;

	public RemoteUser(Fairnet fairnet, FairnetVault user) throws Exception {
		this.fairnet = fairnet;
		this.me = user;
	}

	public Friend addFriend(String friendPublicKey) {

		RDBDMessage message = new RDBDMessage();
		message.setSenderId(me.getId());
		message.setFrom(me.getPublicKey());
		message.setTo(friendPublicKey);
		message.setOperation(AddFriendResponseHandler.class.getName());
		message.setValue("Hi, Bob! I'm Alice!");

		this.me.getRdbd().sendMessage(message.getTo(), me.getPublicKey(), me.getPrivateKey(),
				message);
		
		return this.me.getAddFriendConfirmedHandler().getNewFriend();
	}

	public void getPosts(FairnetVault otherUser) {
		RDBDMessage message = new RDBDMessage();
		message.setSenderId(me.getId());
		message.setFrom(me.getPublicKey());
		message.setTo(otherUser.getPublicKey());
		message.setOperation(GetPostsResponseHandler.class.getName());

		this.me.getRdbd().sendMessage(message.getTo(), me.getPublicKey(), me.getPrivateKey(),
				message);
	}

	public Post getPost(FairnetVault otherUser, String postId) {

		RDBDMessage message = new RDBDMessage();
		message.setSenderId(me.getId());
		message.setFrom(me.getPublicKey());
		message.setTo(otherUser.getPublicKey());
		message.setOperation(GetPostResponseHandler.class.getName());
		message.setValue(postId);

		this.me.getRdbd().sendMessage(message.getTo(), me.getPublicKey(), me.getPrivateKey(),
				message);

		me.getGetPostConfirmationHandler();
		try {
			Thread.sleep(40);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Post post = me.getGetPostConfirmationHandler().getPost();
		return post;
	}

}