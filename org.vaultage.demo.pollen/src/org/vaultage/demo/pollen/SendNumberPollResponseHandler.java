package org.vaultage.demo.pollen;

import java.util.List;
import org.vaultage.core.VaultageMessage;
// import org.vaultage.demo.pollen.SendNumberPollResponseBaseHandler;

public class SendNumberPollResponseHandler extends SendNumberPollResponseBaseHandler {

	@Override
	public Object run(VaultageMessage senderMessage, double result) throws Exception {
		return result;
	}
}