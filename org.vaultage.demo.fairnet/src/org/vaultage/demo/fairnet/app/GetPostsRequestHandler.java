/**** protected region GetPostsRequestHandler on begin ****/
package org.vaultage.demo.fairnet.app;

import java.util.List;

import org.vaultage.core.VaultageMessage;
import org.vaultage.demo.fairnet.app.FairnetVault;
import org.vaultage.demo.fairnet.gen.GetPostsRequestBaseHandler;

public class GetPostsRequestHandler extends GetPostsRequestBaseHandler {

	@Override
	public List<String> run(VaultageMessage senderMessage) throws Exception {
		FairnetVault localVault = (FairnetVault) this.vault;
		return localVault.getPosts(senderMessage.getFrom());
	}
}
/**** protected region GetPostsRequestHandler end ****/