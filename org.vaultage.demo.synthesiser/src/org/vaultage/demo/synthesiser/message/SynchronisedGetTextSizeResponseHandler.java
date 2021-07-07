package org.vaultage.demo.synthesiser.message;

import org.vaultage.core.Vault;
import org.vaultage.demo.synthesiser.GetTextSizeResponseHandler;
import org.vaultage.demo.synthesiser.IncrementResponseHandler;
import org.vaultage.demo.synthesiser.RemoteWorker;
import org.vaultage.demo.synthesiser.Worker;

public class SynchronisedGetTextSizeResponseHandler extends GetTextSizeResponseHandler {
	
	int textSize = 0;
	
	public int getTextSize() {
		return textSize;
	}

	@Override
	public void run(Worker me, RemoteWorker other, String responseToken, Integer result) throws Exception {
		synchronized (this) {
			this.textSize = result;
			this.notify();
		}
	}

	@Override
	public void run(Vault localVault, RemoteWorker remoteVault, String responseToken, Integer result) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
