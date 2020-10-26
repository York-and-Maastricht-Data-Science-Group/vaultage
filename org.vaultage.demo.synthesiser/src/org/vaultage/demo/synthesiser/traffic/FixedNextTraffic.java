package org.vaultage.demo.synthesiser.traffic;

import java.io.File;
import java.io.PrintStream;
import java.text.MessageFormat;

import org.vaultage.core.VaultageServer;
import org.vaultage.demo.synthesiser.SynthesiserBroker;
import org.vaultage.demo.synthesiser.Worker;

/**
 * FixedNext: All work operations to the same worker: the next in the list
 *
 * @author Alfonso de la Vega, Alfa Yohannis
 */
public class FixedNextTraffic {

	protected long latestRunTime;
	protected int numWorkers;
	protected int numOperations;

	public static void main(String[] args) throws Exception {
		int numReps = 5;
		int numWorkers = 3;
		int[] numOperations = { 25, 50, 100, 150, 200 };

		PrintStream profilingStream = new PrintStream(new File("fixedNetResults.csv"));
		profilingStream.println("NumTasks,TotalTimeMillis");

		for (int numOp : numOperations) {
			FixedNextTraffic trafficSimulation = new FixedNextTraffic(numWorkers, numOp);
			for (int rep = 0; rep < numReps; rep++) {
				trafficSimulation.run();
				System.out.println(trafficSimulation.getLatestRunDetails());
				profilingStream.println(String.format("%s,%d", numOp, trafficSimulation.getLatestRunTime()));
			}
		}
		profilingStream.close();
	}

	public FixedNextTraffic(int numWorkers, int numOperations) {
		this.numWorkers = numWorkers;
		this.numOperations = numOperations;
	}

	public void run() throws Exception {
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

		latestRunTime = end - start;

		// appropriately dispose broker
		for (int i = 0; i < numWorkers; i++) {
			workers[i].unregister();
		}
		broker.stop();
	}

	public int getNumOperations() {
		return numOperations;
	}

	public long getLatestRunTime() {
		return latestRunTime;
	}

	public String getLatestRunDetails() {
		return MessageFormat.format(
				"Num workers: {0}, NumOps/worker: {1}, Total time: {2} ms",
				numWorkers, numOperations, latestRunTime);
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
