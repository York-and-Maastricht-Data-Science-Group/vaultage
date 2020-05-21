package org.vaultage.demo.fairnet.app;
/* protected region GetPostRequestHandlerImport1 on begin */
/* protected region GetPostRequestHandlerImport1 end */
import org.vaultage.core.VaultageMessage;
import org.vaultage.demo.fairnet.gen.GetPostRequestBaseHandler;
/* protected region GetPostRequestHandlerImport2 on begin */
/* protected region GetPostRequestHandlerImport2 end */
public class GetPostRequestHandler extends GetPostRequestBaseHandler {
	
	@Override
	/* protected region GetPostRequestHandler on begin */
	public Object run(VaultageMessage senderMessage) throws Exception {
		FairnetVault localVault = (FairnetVault) this.owner;
		String postId = senderMessage.getValue("postId");
		return localVault.getPost(senderMessage.getFrom(), postId);
	}
	/* protected region GetPostRequestHandler end */
}