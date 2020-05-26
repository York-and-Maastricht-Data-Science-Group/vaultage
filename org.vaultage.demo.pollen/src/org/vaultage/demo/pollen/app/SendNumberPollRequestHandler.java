/**** protected region SendNumberPollRequestHandler on begin ****/
package org.vaultage.demo.pollen.app;

import org.vaultage.demo.pollen.gen.*;
import java.util.List;
import org.vaultage.core.VaultageMessage;

public class SendNumberPollRequestHandler extends SendNumberPollRequestBaseHandler {

	@Override
	public double run(VaultageMessage senderMessage) throws Exception {	
		return (double) result;
		
	}
}
/**** protected region SendNumberPollRequestHandler end ****/