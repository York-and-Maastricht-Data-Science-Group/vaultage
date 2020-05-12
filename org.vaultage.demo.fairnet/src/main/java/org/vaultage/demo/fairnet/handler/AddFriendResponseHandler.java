package org.vaultage.demo.fairnet.handler;

import org.vaultage.core.VaultAgeHandler;
import org.vaultage.core.VaultAgeMessage;
import org.vaultage.demo.fairnet.FairnetVault;
import org.vaultage.demo.fairnet.Friend;

public class AddFriendResponseHandler extends VaultAgeHandler {

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
			VaultAgeMessage messageBack = new VaultAgeMessage();
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
