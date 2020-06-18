package org.vaultage.demo.fairnet;

import org.vaultage.core.VaultageMessage;

public class GetPostResponseHandler extends GetPostResponseBaseHandler {

	@Override
	public Object run(VaultageMessage senderMessage, Post result) throws Exception {
		return result;
	}
}