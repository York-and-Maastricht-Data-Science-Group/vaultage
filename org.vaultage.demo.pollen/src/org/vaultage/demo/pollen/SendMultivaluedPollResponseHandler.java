/**** protected region SendMultivaluedPollResponseHandler on begin ****/
package org.vaultage.demo.pollen;

import org.vaultage.core.VaultageMessage;

public class SendMultivaluedPollResponseHandler extends SendMultivaluedPollResponseBaseHandler {

	@Override
	public Object run(VaultageMessage senderMessage) throws Exception {
		return result;
	}
}
/**** protected region SendMultivaluedPollResponseHandler end ****/