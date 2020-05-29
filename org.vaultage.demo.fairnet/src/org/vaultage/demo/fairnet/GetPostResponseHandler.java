/**** protected region GetPostResponseHandler on begin ****/
package org.vaultage.demo.fairnet;

import org.vaultage.core.VaultageMessage;

public class GetPostResponseHandler extends GetPostResponseBaseHandler {

	@Override
	public Object run(VaultageMessage senderMessage) throws Exception {
		return result;
	}
}
/**** protected region GetPostResponseHandler end ****/