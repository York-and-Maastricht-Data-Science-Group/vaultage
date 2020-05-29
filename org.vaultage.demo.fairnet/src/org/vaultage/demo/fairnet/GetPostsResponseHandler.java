/**** protected region GetPostsResponseHandler on begin ****/
package org.vaultage.demo.fairnet;

import org.vaultage.core.VaultageMessage;

public class GetPostsResponseHandler extends GetPostsResponseBaseHandler {

	@Override
	public Object run(VaultageMessage senderMessage) throws Exception {
		return result;
	}
}
/**** protected region GetPostsResponseHandler end ****/