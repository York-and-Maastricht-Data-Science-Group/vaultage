package org.vaultage.demo.fairnet;

import java.util.List;

import org.vaultage.core.OperationResponseHandler;
import org.vaultage.core.Vault;

public abstract class StreamFileResponseHandler extends OperationResponseHandler {

	/* local vault's type is different from the remote vault's type */
	public abstract void run(Vault localVault, RemoteFairnetVault remoteVault, String responseToken, java.io.File result) throws Exception;
	
	/* local vault has the same type as the remote vault */ 
	public abstract void run(FairnetVault localVault, RemoteFairnetVault remoteVault, String responseToken, java.io.File result) throws Exception;
}
