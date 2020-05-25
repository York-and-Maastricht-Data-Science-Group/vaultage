/**** protected region AddFriendRequestHandler on begin ****/
package org.vaultage.demo.fairnet.app;

import java.util.List;
import org.vaultage.core.VaultageMessage;
import org.vaultage.demo.fairnet.app.FairnetVault;
import org.vaultage.demo.fairnet.gen.AddFriendRequestBaseHandler;

public class AddFriendRequestHandler extends AddFriendRequestBaseHandler {

	@Override
	public Boolean run(VaultageMessage senderMessage) throws Exception {
		FairnetVault localVault = (FairnetVault) this.vault;
		return localVault.addFriend(senderMessage.getFrom());
	}
}
/**** protected region AddFriendRequestHandler end ****/