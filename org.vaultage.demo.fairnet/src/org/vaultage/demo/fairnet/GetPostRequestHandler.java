package org.vaultage.demo.fairnet;

import org.vaultage.core.VaultageMessage;

public class GetPostRequestHandler extends GetPostRequestBaseHandler {

	@Override
	public Post run(VaultageMessage senderMessage, String postId) throws Exception {
		FairnetVault localVault = (FairnetVault) this.vault;
		return localVault.getPost(senderMessage.getFrom(), postId);
	}
}