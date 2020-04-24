package org.rdbd.demo.fairnet.handler;

import org.rdbd.core.server.RDBDHandler;
import org.rdbd.core.server.RDBDMessage;
import org.rdbd.demo.fairnet.User;

public class AddFriendResponseHandler extends RDBDHandler {

	
	@Override
	public void run() {
		System.out.println("Add friend request");
		System.out.println("------------------");
		System.out.println("From: " + this.message.getFrom());
		System.out.println("Message: " + this.message.getValue());

		User friend = new User(this.message.getFrom(), null);
		try {
			User me = (User) this.owner;
			me.addFriend(friend);
			RDBDMessage messageBack = new RDBDMessage();
			messageBack.setFrom(me.getPublicKey());
			messageBack.setTo(this.message.getFrom());
			messageBack.setOperation(AddFriendConfirmationHandler.class.getName());
			messageBack.setValue("1");
			me.getRdbd().sendMessage(friend.getPublicKey(), messageBack);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
