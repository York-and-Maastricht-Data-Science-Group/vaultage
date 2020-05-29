/**** protected region SendNumberPollResponseHandler on begin ****/
package org.vaultage.demo.pollen;

import org.vaultage.core.VaultageMessage;

public class SendNumberPollResponseHandler extends SendNumberPollResponseBaseHandler {

	@Override
	public Object run(VaultageMessage senderMessage) throws Exception {
		return result;
	}
}
/**** protected region SendNumberPollResponseHandler end ****/