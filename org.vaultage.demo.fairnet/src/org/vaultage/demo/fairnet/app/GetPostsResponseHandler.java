package org.vaultage.demo.fairnet.app;
/* protected region GetPostsResponseHandlerImport1 on begin */
/* protected region GetPostsResponseHandlerImport1 end */
import org.vaultage.core.VaultageMessage;
import org.vaultage.demo.fairnet.gen.GetPostsResponseBaseHandler;
/* protected region GetPostsResponseHandlerImport2 on begin */
/* protected region GetPostsResponseHandlerImport2 end */
public class GetPostsResponseHandler extends GetPostsResponseBaseHandler {
	
	@Override
	/* protected region GetPostsResponseHandler on begin */
	public Object run(VaultageMessage senderMessage) throws Exception {
		return result;
	}
	/* protected region GetPostsResponseHandler end */
}