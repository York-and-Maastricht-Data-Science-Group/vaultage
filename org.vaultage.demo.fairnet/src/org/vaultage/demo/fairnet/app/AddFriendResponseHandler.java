/**** protected region AddFriendResponseHandler on begin ****/
package org.vaultage.demo.fairnet.app;

import org.vaultage.core.VaultageMessage;
import org.vaultage.demo.fairnet.gen.AddFriendResponseBaseHandler;
import org.vaultage.demo.fairnet.gen.Friend;

public class AddFriendResponseHandler extends AddFriendResponseBaseHandler {

	@Override
	public Object run(VaultageMessage senderMessage) throws Exception {
		FairnetVault vault = (FairnetVault) this.vault;
		Friend friend = new Friend();
		friend.setPublicKey(senderMessage.getFrom());
		vault.getFriends().add(friend);
		return true;
	}
}
/**** protected region AddFriendResponseHandler end ****/