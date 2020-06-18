package org.vaultage.demo.fairnet.test;

import org.vaultage.demo.fairnet.AddFriendResponseHandler;
import org.vaultage.demo.fairnet.FairnetVault;
import org.vaultage.demo.fairnet.Friend;
import org.vaultage.demo.fairnet.RemoteFairnetVault;

public class UnitTestAddFriendResponseHandler implements AddFriendResponseHandler {

	private boolean isSuccess = false;
	
	public boolean isSuccess() {
		return isSuccess;
	}
	
	@Override
	public void run(FairnetVault me, RemoteFairnetVault other, String responseToken, Boolean result) throws Exception {
		Friend friend = new Friend();
		friend.setPublicKey(other.getRemotePublicKey());
		me.getFriends().add(friend);
		isSuccess = true;
		
		synchronized (this) {
			this.notify();
		}
	}
}
