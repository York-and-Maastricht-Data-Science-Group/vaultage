/**** protected region GetPostResponseHandler on begin ****/
package org.vaultage.demo.fairnet.app;

import org.vaultage.core.VaultageMessage;
import org.vaultage.demo.fairnet.gen.GetPostResponseBaseHandler;

public class GetPostResponseHandler extends GetPostResponseBaseHandler {

	@Override
	public Object run(VaultageMessage senderMessage) throws Exception {
		return result;
	}
}
/**** protected region GetPostResponseHandler end ****/