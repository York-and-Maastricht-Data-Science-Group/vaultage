package org.eclipse.epsilon.emc.vaultage.test;

import org.vaultage.core.QueryResponseHandler;
import org.vaultage.core.RemoteVault;
import org.vaultage.core.Vault;


public class QueryResponder extends QueryResponseHandler{

	@Override
	public void run(Vault localVault, RemoteVault remoteVault, String responseToken, Object result) throws Exception {
		synchronized (this) {
			this.notify();
		}
	}

}