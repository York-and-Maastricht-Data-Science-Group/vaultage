package org.vaultage.demo.pollen;

// import org.vaultage.demo.pollen.*;
import java.util.List;
import org.vaultage.core.VaultageMessage;

public class SendMultivaluedPollRequestHandler extends SendMultivaluedPollRequestBaseHandler {

	@Override
	public List<Integer> run(VaultageMessage message, MultivaluedPoll poll) throws Exception {	
		return (List<Integer>) result;
		
	}
}