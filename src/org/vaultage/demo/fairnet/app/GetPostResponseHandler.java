package org.vaultage.demo.fairnet.app;
/* protected region GetPostResponseHandlerImport1 on begin */
/* protected region GetPostResponseHandlerImport1 end */
import org.vaultage.core.VaultageMessage;
import org.vaultage.demo.fairnet.gen.GetPostResponseBaseHandler;
/* protected region GetPostResponseHandlerImport2 on begin */
/* protected region GetPostResponseHandlerImport2 end */
public class GetPostResponseHandler extends GetPostResponseBaseHandler {
	
	@Override
	/* protected region GetPostResponseHandler on begin */
	public Object run(VaultageMessage senderMessage) throws Exception {
		return result;
	}
	/* protected region GetPostResponseHandler end */
}