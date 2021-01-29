package org.vaultage.demo.synthesiser.concurrency;

import java.io.File;
import java.io.PrintStream;
import java.text.MessageFormat;

import org.vaultage.core.VaultageServer;
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

		int numReps = 10;
		int[] numRequesters = { 1, 20, 30, 50 };
		int numOperations = 1;

		PrintStream profilingStream = new PrintStream(new File("manyToOneConcurrentTrafficResults.csv"));
		profilingStream.println("Mode,NumRequesters,TotalTimeMillis");

		// brokered messaging
		for (int numRequester : numRequesters) {
			ManyToOneConcurrentTraffic brokeredTrafficSimulation = new ManyToOneConcurrentTraffic(numRequester,
					numOperations);
			for (int rep = 0; rep < numReps; rep++) {
				brokeredTrafficSimulation.runBrokeredMessaging();
				System.out.println(brokeredTrafficSimulation.getLatestRunDetails());
				profilingStream.println(String.format("%s,%s,%d", "brokered", numRequester,
						brokeredTrafficSimulation.getLatestWaitTime()));
			}
		}

		// direct messaging
		for (int numRequester : numRequesters) {
			ManyToOneConcurrentTraffic directTrafficSimulation = new ManyToOneConcurrentTraffic(numRequester,
					numOperations);
			for (int rep = 0; rep < numReps; rep++) {
				directTrafficSimulation.runDirectMessaging();
				System.out.println(directTrafficSimulation.getLatestRunDetails());
				profilingStream.println(
						String.format("%s,%s,%d", "direct", numRequester, directTrafficSimulation.getLatestWaitTime()));
			}
		}

		profilingStream.close();
		System.out.println("Finished!");
	}

	public ManyToOneConcurrentTraffic(int numRequester, int numOperations) {
		this.numRequester = numRequester;
		this.numOperations = numOperations;
	}

	/** RUN BROKERED MESSAGING **/
	public void runBrokeredMessaging() throws Exception {
		Worker worker = new Worker();
		Worker[] requesters = new Worker[numRequester];

//		SynthesiserBroker broker = new SynthesiserBroker();
//		broker.start(SynthesiserBroker.BROKER_ADDRESS);
//		VaultageServer server = new VaultageServer(SynthesiserBroker.BROKER_ADDRESS);
//		VaultageServer server = new VaultageServer("tcp://178.79.178.61:61616");
		VaultageServer server = new VaultageServer("tcp://localhost:61616");

		worker = new Worker();
		worker.setId("Worker-" + numRequester);
		worker.setCompletedValue(numOperations);
		worker.setIncrementResponseHandler(new SynchronisedIncrementResponseHandler());
		worker.register(server);

		for (int i = 0; i < numRequester; i++) {
			requesters[i] = new Worker();
			requesters[i].setId("Requester-" + i);
			requesters[i].setCompletedValue(numOperations);
			requesters[i].setIncrementResponseHandler(new SynchronisedIncrementResponseHandler());
			requesters[i].register(server);
		}

		// initialise all threads first
		Thread threads[] = new Thread[numRequester];
		for (int i = 0; i < numRequester; i++) {
			threads[i] = initThread(requesters[i], worker.getPublicKey());
		}

		long start = System.currentTimeMillis();

		// start all threads at once
		for (int i = 0; i < numRequester; i++) {
			threads[i].start();
//			threads[i].join();
		}

		// wait for all requesters to finish
		for (int i = 0; i < numRequester; i++) {
			threads[i].join();
		}

		long end = System.currentTimeMillis();
//		System.out.println("Total Time = " + (end - start));

		// get maximum waiting time
		long max = 0;
		for (int i = 0; i < numRequester; i++) {
			if (((SendOperationThread) threads[i]).getExecutionTime() > max) {
				max = ((SendOperationThread) threads[i]).getExecutionTime();
			}
		}

		latestWaitTime = max;

		// appropriately dispose broker
		worker.unregister();
		for (int i = 0; i < numRequester; i++) {
			requesters[i].unregister();
		}
//		broker.stop();
	}

	/** RUN DIRECT MESSAGING **/
	public void runDirectMessaging() throws Exception {

		Worker worker = new Worker();
		Worker[] requesters = new Worker[numRequester];

		int port = 61000;

		// through a broker
		worker = new Worker();
		worker.setId("Worker-" + numRequester);
		worker.setCompletedValue(numOperations);
		worker.setIncrementResponseHandler(new SynchronisedIncrementResponseHandler());
		worker.startServer("127.0.0.1", port++);

		for (int i = 0; i < numRequester; i++) {
			requesters[i] = new Worker();
			requesters[i].setId("Requester-" + i);
			requesters[i].setCompletedValue(numOperations);
			requesters[i].setIncrementResponseHandler(new SynchronisedIncrementResponseHandler());
			requesters[i].startServer("127.0.0.1", port++);
		}

		// setting up workers to trust each other so no need to communicate via broker,
		// only if the the communication mode is direct
		for (int i = 0; i < numRequester; i++) {
			Worker requester = requesters[i];
			worker.getVaultage().getPublicKeyToRemoteAddress().put(requester.getPublicKey(),
					requester.getVaultage().getDirectMessageServerAddress());
			requester.getVaultage().getPublicKeyToRemoteAddress().put(worker.getPublicKey(),
					worker.getVaultage().getDirectMessageServerAddress());
		}

		// initialise all threads first
		Thread threads[] = new Thread[numRequester];
		for (int i = 0; i < numRequester; i++) {
			threads[i] = initThread(requesters[i], worker.getPublicKey());
		}

		long start = System.currentTimeMillis();

		// start all threads at once
		for (int i = 0; i < numRequester; i++) {
			threads[i].start();
//			threads[i].join();
		}

		System.console();

		// wait for all requesters to finish
		for (int i = 0; i < numRequester; i++) {
			threads[i].join();
		}

		long end = System.currentTimeMillis();
//		System.out.println("Total Time = " + (end - start));

		// get maximum waiting time
		long max = 0;
		for (int i = 0; i < numRequester; i++) {
			if (((SendOperationThread) threads[i]).getExecutionTime() > max) {
				max = ((SendOperationThread) threads[i]).getExecutionTime();
			}
		}

		latestWaitTime = max;

		// appropriately dispose broker
		worker.shutdownServer();
		for (int i = 0; i < numRequester; i++) {
			requesters[i].shutdownServer();
		}

		System.console();
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

	private Thread initThread(Worker worker, String remoteWorkerKey) {
		Thread t = new SendOperationThread(worker, remoteWorkerKey);
//		t.start();
		return t;
	}

	public class SendOperationThread extends Thread {
		private final Worker worker;
		private final String remoteWorkerKey;
		private long executionTime;

		public SendOperationThread(Worker worker, String remoteWorkerKey) {
			this.setName(worker.getId());
			this.executionTime = 0;
			this.worker = worker;
			this.remoteWorkerKey = remoteWorkerKey;
		}

		public void run() {
//			System.out.println("Requester " + worker.getId() + " start...");
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
//			System.out.println("Requester " + worker.getId() + " ended = " + executionTime);
		}

		public long getExecutionTime() {
			return executionTime;
		}
	}

}
