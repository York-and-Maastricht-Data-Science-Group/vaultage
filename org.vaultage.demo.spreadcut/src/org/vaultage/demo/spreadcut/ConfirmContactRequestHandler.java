/**** protected region ConfirmContactRequestHandler on begin ****/
package org.vaultage.demo.spreadcut;

// import org.vaultage.demo.spreadcut.*;
import java.util.List;
import org.vaultage.core.VaultageMessage;

public class ConfirmContactRequestHandler extends ConfirmContactRequestBaseHandler {

	@Override
	public boolean run(VaultageMessage senderMessage) throws Exception {	
		return (boolean) result;
		
	}
}
/**** protected region ConfirmContactRequestHandler end ****/