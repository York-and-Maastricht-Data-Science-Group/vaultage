package org.vaultage.demo.synthesiser.concurrency;

import java.io.File;
import java.io.PrintStream;
import java.text.MessageFormat;

import org.vaultage.core.VaultageServer;
import org.vaultage.demo.synthesiser.SynthesiserBroker;
import org.vaultage.demo.synthesiser.Worker;
import org.vaultage.demo.synthesiser.traffic.SynchronisedIncrementResponseHandler;

/**
 * ManyToOne Concurrent Traffic: All requesters send work operations to the same
 * worker concurrently
 *
 * @author Alfonso de la Vega, Alfa Yohannis
 */
public class ManyToOneConcurrentTraffic {

	protected long latestWaitTime;
	protected int numRequester;
	protected int numOperations;

	public static void main(String[] args) throws Exception {

		int numReps = 1;
		int[] numRequesters = {30};
		int numOperations = 1;

		PrintStream profilingStream = new PrintStream(new File("manyToOneConcurrentTrafficResults.csv"));
		profilingStream.println("NumRequesters,TotalTimeMillis");

		for (int numRequester : numRequesters) {
			ManyToOneConcurrentTraffic trafficSimulation = new ManyToOneConcurrentTraffic(numRequester, numOperations);
			for (int rep = 0; rep < numReps; rep++) {
				trafficSimulation.run();
				System.out.println(trafficSimulation.getLatestRunDetails());
				profilingStream.println(String.format("%s,%d", numRequester, trafficSimulation.getLatestWaitTime()));
			}
		}
		profilingStream.close();
		System.out.println("Finished!");
	}

	public ManyToOneConcurrentTraffic(int numRequester, int numOperations) {
		this.numRequester = numRequester;
		this.numOperations = numOperations;
	}

	public void run() throws Exception {
		Worker worker = new Worker();
		Worker[] requesters = new Worker[numRequester];

		SynthesiserBroker broker = new SynthesiserBroker();
		broker.start(SynthesiserBroker.BROKER_ADDRESS);
		VaultageServer server = new VaultageServer(SynthesiserBroker.BROKER_ADDRESS);
//		VaultageServer server = new VaultageServer("tcp://178.79.178.61:61616");

		worker = new Worker();
		worker.setId("" + numRequester);
		worker.setCompletedValue(numOperations);
		worker.setIncrementResponseHandler(new SynchronisedIncrementResponseHandler());
		worker.register(server);

		for (int i = 0; i < numRequester; i++) {
			requesters[i] = new Worker();
			requesters[i].setId("" + i);
			requesters[i].setCompletedValue(numOperations);
			requesters[i].setIncrementResponseHandler(new SynchronisedIncrementResponseHandler());
			requesters[i].register(server);
		}

		// initialise all threads first
		Thread threads[] = new Thread[numRequester];
		for (int i = 0; i < numRequester; i++) {
			String remoteWorkerKey = worker.getPublicKey();
			threads[i] = initThread(requesters[i], remoteWorkerKey);
		}

		long start = System.currentTimeMillis();
		
		// start all threads at once
		for (int i = 0; i < numRequester; i++) {
			threads[i].start();
		}

		// wait for all requesters to finish
		for (int i = 0; i < numRequester; i++) {
			threads[i].join();
		}

		long end = System.currentTimeMillis();
		System.out.println("Total Time = " + (end - start));
		
		// get maximum waiting time
		long max = 0;
		for (int i = 0; i < numRequester; i++) {
			if (((SendOperationThread) threads[i]).getExecutionTime() > max) {
				max = ((SendOperationThread) threads[i]).getExecutionTime();
			}
		}
		
		latestWaitTime = max;

		// appropriately dispose broker
		for (int i = 0; i < numRequester; i++) {
			requesters[i].unregister();
		}
		broker.stop();
	}

	public int getNumOperations() {
		return numOperations;
	}

	public long getLatestWaitTime() {
		return latestWaitTime;
	}

	public String getLatestRunDetails() {
		return MessageFormat.format("Num requesters: {0}, NumOps/worker: {1}, Max waiting time: {2} ms", numRequester,
				numOperations, latestWaitTime);
	}

	private static Thread initThread(Worker worker, String remoteWorkerKey) {
		Thread t = new SendOperationThread(worker, remoteWorkerKey);
//		t.start();
		return t;
	}

	public static class SendOperationThread extends Thread {
		private final Worker worker;
		private final String remoteWorkerKey;
		private long executionTime;

		public SendOperationThread(Worker worker, String remoteWorkerKey) {
			this.executionTime = 0;
			this.worker = worker;
			this.remoteWorkerKey = remoteWorkerKey;
		}

		public void run() {
			System.out.println("Requester " + worker.getId() + " start...");
			long start = System.currentTimeMillis();
			while (!worker.isWorkComplete()) {
				try {
					worker.sendOperation(remoteWorkerKey);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			long end = System.currentTimeMillis();
			executionTime = end - start;
			System.out.println("Requester " + worker.getId() + " ended = " + executionTime);
		}

		public long getExecutionTime() {
			return executionTime;
		}
	}

}
