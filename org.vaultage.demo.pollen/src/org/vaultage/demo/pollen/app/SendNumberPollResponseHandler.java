/**** protected region SendNumberPollResponseHandler on begin ****/
package org.vaultage.demo.pollen.app;

import org.vaultage.core.VaultageMessage;
import org.vaultage.demo.pollen.gen.SendNumberPollResponseBaseHandler;

public class SendNumberPollResponseHandler extends SendNumberPollResponseBaseHandler {

	@Override
	public Object run(VaultageMessage senderMessage) throws Exception {
		return result;
	}
}
/**** protected region SendNumberPollResponseHandler end ****/