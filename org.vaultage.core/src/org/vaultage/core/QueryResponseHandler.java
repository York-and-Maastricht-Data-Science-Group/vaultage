package org.vaultage.core;


public abstract class QueryResponseHandler extends OperationResponseHandler {

	/* local vault's type is different from the remote vault's type */
	public abstract void run(Vault localVault, RemoteVault remoteVault, String responseToken, Object result) throws Exception;
	
}
