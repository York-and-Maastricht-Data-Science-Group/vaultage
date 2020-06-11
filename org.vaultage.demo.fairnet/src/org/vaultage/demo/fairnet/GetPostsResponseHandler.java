package org.vaultage.demo.fairnet;

import java.util.List;
import org.vaultage.core.VaultageMessage;

public class GetPostsResponseHandler extends GetPostsResponseBaseHandler {

	@Override
	public Object run(VaultageMessage senderMessage, List<String> result) throws Exception {
		return result;
	}
}