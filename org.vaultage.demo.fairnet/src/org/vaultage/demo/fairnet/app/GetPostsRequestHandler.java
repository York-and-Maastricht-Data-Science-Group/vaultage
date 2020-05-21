package org.vaultage.demo.fairnet.app;
/* protected region GetPostsRequestHandlerImport1 on begin */
import java.util.ArrayList;
import java.util.stream.Collectors;
/* protected region GetPostsRequestHandlerImport1 end */
import org.vaultage.core.VaultageMessage;
import org.vaultage.demo.fairnet.gen.GetPostsRequestBaseHandler;
/* protected region GetPostsRequestHandlerImport2 on begin */
import org.vaultage.demo.fairnet.gen.Post;
/* protected region GetPostsRequestHandlerImport2 end */
public class GetPostsRequestHandler extends GetPostsRequestBaseHandler {
	
	@Override
	/* protected region GetPostsRequestHandler on begin */
	public Object run(VaultageMessage senderMessage) throws Exception {
		FairnetVault localVault = (FairnetVault) this.owner;
		return localVault.getPosts(senderMessage.getFrom());
	}
	/* protected region GetPostsRequestHandler end */
}