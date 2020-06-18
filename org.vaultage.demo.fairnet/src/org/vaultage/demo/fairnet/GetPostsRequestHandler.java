package org.vaultage.demo.fairnet;

import java.util.List;
import org.vaultage.core.VaultageMessage;

public class GetPostsRequestHandler extends GetPostsRequestBaseHandler {

	@Override
	public List<String> run(VaultageMessage senderMessage) throws Exception {
		FairnetVault localVault = (FairnetVault) this.vault;
		return localVault.getPosts(senderMessage.getFrom());
	}
}