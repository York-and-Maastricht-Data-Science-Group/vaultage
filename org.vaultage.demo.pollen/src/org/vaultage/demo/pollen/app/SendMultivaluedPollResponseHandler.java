/**** protected region SendMultivaluedPollResponseHandler on begin ****/
package org.vaultage.demo.pollen.app;

import org.vaultage.core.VaultageMessage;
import org.vaultage.demo.pollen.gen.SendMultivaluedPollResponseBaseHandler;

public class SendMultivaluedPollResponseHandler extends SendMultivaluedPollResponseBaseHandler {

	@Override
	public Object run(VaultageMessage senderMessage) throws Exception {
		return result;
	}
}
/**** protected region SendMultivaluedPollResponseHandler end ****/