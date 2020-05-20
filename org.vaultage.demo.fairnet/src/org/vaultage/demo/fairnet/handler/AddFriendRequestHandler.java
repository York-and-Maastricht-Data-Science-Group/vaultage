package org.vaultage.demo.fairnet.handler;

import org.vaultage.core.VaultageHandler;
import org.vaultage.core.VaultageMessage;
import org.vaultage.demo.fairnet.FairnetVault;
import org.vaultage.demo.fairnet.Friend;

public class AddFriendRequestHandler extends VaultageHandler {

	@Override
	public void run() {
		System.out.println("Add friend request");
		System.out.println("------------------");
		System.out.println("From: " + this.message.getFrom());
		System.out.println("Message: " + this.message.getValue("value"));

		try {
			Friend friend = new Friend(this.message.getSenderId(), this.message.getFrom());
			FairnetVault me = (FairnetVault) this.owner;
		
			me.addFriend(friend);
			VaultageMessage messageBack = new VaultageMessage();
			messageBack.setSenderId(me.getId());
			messageBack.setToken(this.message.getToken());
			messageBack.setFrom(me.getPublicKey());
			messageBack.setTo(this.message.getFrom());
			messageBack.setOperation(AddFriendResponseHandler.class.getName());
			messageBack.putValue("value", "1");
			me.getVaultAge().sendMessage(friend.getPublicKey(), me.getPublicKey(), me.getPrivateKey(), messageBack);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
