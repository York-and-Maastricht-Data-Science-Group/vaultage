/**** protected region GetPostRequestHandler on begin ****/
package org.vaultage.demo.fairnet.app;

import org.vaultage.demo.fairnet.app.FairnetVault;
import org.vaultage.demo.fairnet.gen.*;
import java.util.List;

import org.vaultage.core.Vaultage;
import org.vaultage.core.VaultageMessage;

public class GetPostRequestHandler extends GetPostRequestBaseHandler {

	@Override
	public Post run(VaultageMessage senderMessage) throws Exception {
		FairnetVault localVault = (FairnetVault) this.vault;
		String postId = (String) Vaultage.Gson.fromJson(senderMessage.getValue("postId"), String.class);
		return localVault.getPost(senderMessage.getFrom(), postId);
	}
}
/**** protected region GetPostRequestHandler end ****/