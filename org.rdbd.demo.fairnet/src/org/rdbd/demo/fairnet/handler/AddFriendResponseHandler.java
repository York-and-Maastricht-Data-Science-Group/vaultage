package org.rdbd.demo.fairnet.handler;

import org.rdbd.core.RDBDHandler;
import org.rdbd.core.RDBDMessage;
import org.rdbd.demo.fairnet.FairnetVault;
import org.rdbd.demo.fairnet.Friend;

public class AddFriendResponseHandler extends RDBDHandler {

	@Override
	public void run() {
		System.out.println("Add friend request");
		System.out.println("------------------");
		System.out.println("From: " + this.message.getFrom());
		System.out.println("Message: " + this.message.getValue());

		try {
			Friend friend = new Friend(this.message.getSenderId(), this.message.getFrom());
			FairnetVault me = (FairnetVault) this.owner;
		
			me.addFriend(friend);
			RDBDMessage messageBack = new RDBDMessage();
			messageBack.setSenderId(me.getId());
			messageBack.setToken(this.message.getToken());
			messageBack.setFrom(me.getPublicKey());
			messageBack.setTo(this.message.getFrom());
			messageBack.setOperation(AddFriendConfirmationHandler.class.getName());
			messageBack.setValue("1");
			me.getRdbd().sendMessage(friend.getPublicKey(), me.getPublicKey(), me.getPrivateKey(), messageBack);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
