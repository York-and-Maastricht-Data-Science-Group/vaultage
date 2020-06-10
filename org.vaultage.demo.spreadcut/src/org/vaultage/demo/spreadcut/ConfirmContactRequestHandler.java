package org.vaultage.demo.spreadcut;

// import org.vaultage.demo.spreadcut.*;
import java.util.List;
import org.vaultage.core.VaultageMessage;

public class ConfirmContactRequestHandler extends ConfirmContactRequestBaseHandler {

	@Override
	public boolean run(VaultageMessage message, String timestamp) throws Exception {	
		return (boolean) result;
		
	}
}