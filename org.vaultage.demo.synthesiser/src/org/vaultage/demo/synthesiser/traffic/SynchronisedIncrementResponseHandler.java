package org.vaultage.demo.synthesiser.traffic;

import java.text.MessageFormat;

import org.vaultage.demo.synthesiser.IncrementResponseHandler;
import org.vaultage.demo.synthesiser.RemoteWorker;
import org.vaultage.demo.synthesiser.Worker;

public class SynchronisedIncrementResponseHandler implements IncrementResponseHandler {

	@Override
	public void run(Worker me, RemoteWorker other,
			String responseToken, Integer result) throws Exception {
		synchronized (this) {
			System.out.println(MessageFormat.format(
					"Worker: {0}, currentValue: {1}", me.getId(), result));
			me.setCurrentValue(result);
			this.notify();
		}
	}
}
