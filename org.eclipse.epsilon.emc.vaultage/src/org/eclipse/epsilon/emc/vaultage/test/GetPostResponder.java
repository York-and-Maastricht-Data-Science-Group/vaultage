package org.eclipse.epsilon.emc.vaultage;

import org.vaultage.core.Vault;
import org.vaultage.demo.fairnet.FairnetVault;
import org.vaultage.demo.fairnet.GetPostResponseHandler;
import org.vaultage.demo.fairnet.Post;
import org.vaultage.demo.fairnet.RemoteFairnetVault;

public class GetPostResponder extends GetPostResponseHandler {

	@Override
	public void run(Vault localVault, RemoteFairnetVault remoteVault, String responseToken, Post result)
			throws Exception {

		synchronized (this) {
			this.notify();
		}
	}

	@Override
	public void run(FairnetVault localVault, RemoteFairnetVault remoteVault, String responseToken, Post result)
			throws Exception {
		synchronized (this) {
			this.notify();
		}
	}

}