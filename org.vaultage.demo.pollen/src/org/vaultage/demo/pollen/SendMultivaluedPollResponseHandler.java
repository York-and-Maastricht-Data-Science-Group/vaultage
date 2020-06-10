package org.vaultage.demo.pollen;

import java.util.List;
import org.vaultage.core.VaultageMessage;
// import org.vaultage.demo.pollen.SendMultivaluedPollResponseBaseHandler;

public class SendMultivaluedPollResponseHandler extends SendMultivaluedPollResponseBaseHandler {

	@Override
	public Object run(VaultageMessage senderMessage, List<Integer> result) throws Exception {
		return result;
	}
}