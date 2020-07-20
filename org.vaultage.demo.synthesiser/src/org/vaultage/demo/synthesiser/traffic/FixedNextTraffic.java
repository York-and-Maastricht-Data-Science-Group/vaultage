package org.vaultage.demo.synthesiser.traffic;

import java.text.MessageFormat;

import org.vaultage.core.VaultageServer;
import org.vaultage.demo.synthesiser.SynthesiserBroker;
import org.vaultage.demo.synthesiser.Worker;

/**
 * FixedNext: All work operations to the same worker: the next in the list
 *
 * @author Alfonso de la Vega
 */
public class FixedNextTraffic {

	public static void main(String[] args) throws Exception {
		int numWorkers = 3;
		int numOperations = 100;
		Worker[] workers = new Worker[numWorkers];

		SynthesiserBroker broker = new SynthesiserBroker();
		broker.start(SynthesiserBroker.BROKER_ADDRESS);
		VaultageServer server = new VaultageServer(SynthesiserBroker.BROKER_ADDRESS);

		for (int i = 0; i < numWorkers; i++) {
			workers[i] = new Worker();
			workers[i].setId("" + i);
			workers[i].setCompletedValue(numOperations);
			workers[i].setIncrementResponseHandler(new SynchronisedIncrementResponseHandler());
			workers[i].register(server);
		}
		
		long start = System.currentTimeMillis();

		Thread threads[] = new Thread[numWorkers];
		for (int i = 0; i < numWorkers; i++) {
			int remoteWorkerIndex = (i + 1) % numWorkers;
			String remoteWorkerKey = workers[remoteWorkerIndex].getPublicKey();
			threads[i] = startWork(workers[i], remoteWorkerKey);
		}

		// wait for workers to finish
		for (int i = 0; i < numWorkers; i++) {
			threads[i].join();
		}

		long end = System.currentTimeMillis();
		System.out.println(MessageFormat.format(
				"Num workers: {0}, NumOps/worker: {1}, Total time: {2} ms",
				numWorkers, numOperations, end - start));

		// appropriately dispose broker
		for (int i = 0; i < numWorkers; i++) {
			workers[i].unregister();
		}
		broker.stop();
	}

	private static Thread startWork(Worker worker, String remoteWorkerKey) {
		Thread t = new Thread() {
			public void run() {
				while (!worker.isWorkComplete()) {
					try {
						worker.sendOperation(remoteWorkerKey);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
		t.start();
		return t;
	}

}
