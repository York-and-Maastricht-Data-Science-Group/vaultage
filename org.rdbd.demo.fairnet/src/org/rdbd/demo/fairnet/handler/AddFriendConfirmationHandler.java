package org.rdbd.demo.fairnet.handler;

import org.rdbd.core.server.RDBDHandler;
import org.rdbd.demo.fairnet.User;

public class AddFriendConfirmationHandler extends RDBDHandler {

	@Override
	public void run() {
		System.out.println("Add friend confirmed");
		System.out.println("------------------");
		System.out.println("From: " + this.message.getFrom());
		System.out.println("Message: " + this.message.getValue());
		if ("1".equals(this.message.getValue())) {
			try {
				User newFriend = new User(this.message.getFrom(), null);
				((User) this.owner).addFriend(newFriend);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
