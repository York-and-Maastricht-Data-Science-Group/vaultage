package org.vaultage.demo.synthesiser.traffic;

import java.io.File;
import java.io.PrintStream;
import java.text.MessageFormat;

import org.vaultage.core.Vaultage;
import org.vaultage.core.VaultageServer;
import org.vaultage.demo.synthesiser.SynthesiserBroker;
import org.vaultage.demo.synthesiser.Worker;

/**
 * FixedNext: All work operations to the same worker: the next in the list
 *
 * @author Alfonso de la Vega
 */
public class DirectMessageFixedNextTraffic {

	protected long latestRunTime;
	protected int numWorkers;
	protected int numOperations;

	public static void main(String[] args) throws Exception {
		int numReps = 5;
		int numWorkers = 3;
		int[] numOperations = { 25, 50, 100, 150, 200 };

		PrintStream profilingStream = new PrintStream(new File("directFixedNetResults.csv"));
		profilingStream.println("NumTasks,TotalTimeMillis");

		for (int numOp : numOperations) {
			DirectMessageFixedNextTraffic trafficSimulation = new DirectMessageFixedNextTraffic(numWorkers, numOp);
			for (int rep = 0; rep < numReps; rep++) {
				trafficSimulation.run();
				System.out.println(trafficSimulation.getLatestRunDetails());
				profilingStream.println(String.format("%s,%d", numOp, trafficSimulation.getLatestRunTime()));
			}
		}
		profilingStream.close();
	}

	public DirectMessageFixedNextTraffic(int numWorkers, int numOperations) {
		this.numWorkers = numWorkers;
		this.numOperations = numOperations;
	}

	public void run() throws Exception {
		Worker[] workers = new Worker[numWorkers];

//		SynthesiserBroker broker = new SynthesiserBroker();
//		broker.start(SynthesiserBroker.BROKER_ADDRESS);
//		VaultageServer server = new VaultageServer(SynthesiserBroker.BROKER_ADDRESS);
		VaultageServer server = new VaultageServer("tcp://localhost:61616");

		int port = Vaultage.DEFAULT_SERVER_PORT;
		
		for (int i = 0; i < numWorkers; i++) {
			workers[i] = new Worker();
			workers[i].setId("" + i);
			workers[i].setCompletedValue(numOperations);
			workers[i].addOperationResponseHandler(new SynchronisedIncrementResponseHandler());
			workers[i].register(server);
			workers[i].startServer("127.0.0.1", port++);
		}
		
		// setting up workers to trust each other so no need to communicate via broker
		for (Worker worker1 : workers) {
			for (Worker worker2 : workers) {
				if (!worker1.equals(worker2)) {
					worker1.getVaultage().getPublicKeyToRemoteAddress().put(worker2.getPublicKey(),
							worker2.getVaultage().getDirectMessageServerAddress());
				}
			}
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
			workers[i].shutdownServer();
		}
//		broker.stop();
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
//						worker.sendOperation(remoteWorkerKey);
						worker.sendOperation(remoteWorkerKey, false);
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
