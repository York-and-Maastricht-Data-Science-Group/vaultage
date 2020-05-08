package org.rdbd.demo.fairnet.handler;

import java.util.Set;

import org.rdbd.core.RDBDHandler;
import org.rdbd.demo.fairnet.FairnetVault;
import org.rdbd.demo.fairnet.Friend;

/***
 * A class to response to the return message of a friend request sent by the
 * owner of this handler to his/her candidate friend
 * 
 * @author Ryano
 *
 */
public class AddFriendConfirmationHandler extends RDBDHandler {

	private String status;
	private Friend newFriend;

	@Override
	public void run() {
		System.out.println("Add friend confirmed");
		System.out.println("------------------");
		System.out.println("From: " + this.message.getFrom());
		System.out.println("Message: " + this.message.getValue());

		Set<String> expectedReplyTokens = ((FairnetVault) this.getOwner()).getRdbd().getExpectedReplyTokens();

		// if the message back from the other side is legal then
		if (expectedReplyTokens.contains(this.message.getToken())) {

			status = this.message.getValue();
			if ("1".equals(this.status)) {
				try {
					Friend newFriend = new Friend(message.getSenderId(), this.message.getFrom());
					((FairnetVault) this.owner).addFriend(newFriend);
					this.newFriend = newFriend;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			expectedReplyTokens.remove(this.message.getToken());
		}
		// other wise do nothing and set status to "0"
		else {
			this.status = "0";
		}
	}

	public String getStatus() {
		return status;
	}

	public Friend getNewFriend() {
		return newFriend;
	}
}
