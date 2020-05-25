/**** protected region GetPostsResponseHandler on begin ****/
package org.vaultage.demo.fairnet.app;

import org.vaultage.core.VaultageMessage;
import org.vaultage.demo.fairnet.gen.GetPostsResponseBaseHandler;

public class GetPostsResponseHandler extends GetPostsResponseBaseHandler {

	@Override
	public Object run(VaultageMessage senderMessage) throws Exception {
		return result;
	}
}
/**** protected region GetPostsResponseHandler end ****/