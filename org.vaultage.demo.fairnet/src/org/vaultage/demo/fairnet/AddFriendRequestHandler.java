package org.vaultage.demo.fairnet;

import java.util.List;
import org.vaultage.core.VaultageMessage;

public class AddFriendRequestHandler extends AddFriendRequestBaseHandler {

	@Override
	public java.lang.Boolean run(VaultageMessage message) throws Exception {	
		FairnetVault localVault = (FairnetVault) this.vault;
		return localVault.addFriend(message.getFrom());
	}
}