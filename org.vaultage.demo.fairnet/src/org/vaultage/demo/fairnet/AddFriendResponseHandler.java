package org.vaultage.demo.fairnet;

import java.util.List;
import org.vaultage.core.VaultageMessage;
// import org.vaultage.demo.fairnet.AddFriendResponseBaseHandler;

public class AddFriendResponseHandler extends AddFriendResponseBaseHandler {

	@Override
	public Object run(VaultageMessage senderMessage, java.lang.Boolean result) throws Exception {
		FairnetVault vault = (FairnetVault) this.vault;
		Friend friend = new Friend();
		friend.setPublicKey(senderMessage.getFrom());
		vault.getFriends().add(friend);
		return true;
	}
}