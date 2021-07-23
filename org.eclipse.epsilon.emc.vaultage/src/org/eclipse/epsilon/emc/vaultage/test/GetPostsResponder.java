package org.eclipse.epsilon.emc.vaultage;

import java.util.List;

import org.vaultage.core.Vault;
import org.vaultage.demo.fairnet.FairnetVault;
import org.vaultage.demo.fairnet.GetPostsResponseHandler;
import org.vaultage.demo.fairnet.RemoteFairnetVault;

public class GetPostsResponder extends GetPostsResponseHandler {

	@Override
	public void run(Vault localVault, RemoteFairnetVault remoteVault, String responseToken, List<String> result)
			throws Exception {
		synchronized (this) {
			this.notify();
		}
	}

	@Override
	public void run(FairnetVault localVault, RemoteFairnetVault remoteVault, String responseToken,
			List<String> result) throws Exception {
		synchronized (this) {
			this.notify();
		}
	}
}