package org.vaultage.demo.fairnet.test;

import org.vaultage.core.Vault;
import org.vaultage.demo.fairnet.AddFriendResponseHandler;
import org.vaultage.demo.fairnet.FairnetVault;
import org.vaultage.demo.fairnet.Friend;
import org.vaultage.demo.fairnet.Post;
import org.vaultage.demo.fairnet.RemoteFairnetVault;

public class UnitTestAddFriendResponseHandler implements AddFriendResponseHandler {

	private boolean isSuccess = false;
	
	
	
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

	public boolean getResult() {
		return isSuccess;
	}

	@Override
	public void run(Vault localVault, RemoteFairnetVault remoteVault, String responseToken, Boolean result)
			throws Exception {
		// TODO Auto-generated method stub
		
	}
	
}
