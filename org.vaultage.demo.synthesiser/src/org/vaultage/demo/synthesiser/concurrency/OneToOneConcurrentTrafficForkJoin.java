package org.vaultage.demo.synthesiser.concurrency;

import java.io.File;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

import org.vaultage.core.VaultageServer;
import org.vaultage.demo.synthesiser.SynthesiserBroker;
import org.vaultage.demo.synthesiser.Worker;
import org.vaultage.demo.synthesiser.traffic.SynchronisedIncrementResponseHandler;

/**
 * OneToOne Concurrent Traffic: Each requester send a work operation to a unique
 * worker worker concurrently
 *
 * @author Alfonso de la Vega, Alfa Yohannis
 */
public class OneToOneConcurrentTrafficForkJoin {

	private static final int NUM_OF_PROCESSORS = 3;
	protected long latestWaitTime;
	protected int numRequester;
	protected int numOperations;
	protected long totalTime;

	public static void main(String[] args) throws Exception {

		int numReps = 10;
//		int numReps = 3;
		int[] numRequesters = { 1, 2, 3, 4 };
//		int[] numRequesters = {4};
		int numOperations = 100;

		PrintStream profilingStream = new PrintStream(new File("oneToOneConcurrentTrafficResults.csv"));
		profilingStream.println("Mode,NumRequesters,TotalTimeMillis");

		// brokered messaging
		System.out.println("==brokered==");
		for (int numRequester : numRequesters) {
			OneToOneConcurrentTrafficForkJoin brokeredTrafficSimulation = new OneToOneConcurrentTrafficForkJoin(numRequester,
					numOperations);
			for (int rep = 0; rep < numReps; rep++) {
				brokeredTrafficSimulation.runBrokeredMessaging();
				System.out.println(brokeredTrafficSimulation.getLatestRunDetails());
				profilingStream.println(String.format("%s,%s,%d,d%", "brokered", numRequester,
						brokeredTrafficSimulation.getLatestWaitTime(), brokeredTrafficSimulation.getTotalTime()));
			}
		}

		// direct messaging
		System.out.println("==DIRECT==");
		for (int numRequester : numRequesters) {
			OneToOneConcurrentTrafficForkJoin directTrafficSimulation = new OneToOneConcurrentTrafficForkJoin(numRequester,
					numOperations);
			for (int rep = 0; rep < numReps; rep++) {
				directTrafficSimulation.runDirectMessaging();
				System.out.println(directTrafficSimulation.getLatestRunDetails());
				profilingStream.println(String.format("%s,%s,%d,%d", "direct", numRequester,
						directTrafficSimulation.getLatestWaitTime(), directTrafficSimulation.getTotalTime()));
			}
		}

		profilingStream.close();
		System.out.println("Finished!");
	}

	public OneToOneConcurrentTrafficForkJoin(int numRequester, int numOperations) {
		this.numRequester = numRequester;
		this.numOperations = numOperations;
	}

	/** RUN brokered MESSAGING **/
	public void runBrokeredMessaging() throws Exception {
		Worker[] workers = new Worker[numRequester];
		Worker[] requesters = new Worker[numRequester];

		SynthesiserBroker broker = new SynthesiserBroker();
//		broker.start(SynthesiserBroker.BROKER_ADDRESS);
//		VaultageServer server = new VaultageServer(SynthesiserBroker.BROKER_ADDRESS);
//		VaultageServer server = new VaultageServer("tcp://178.79.178.61:61616");
		VaultageServer server = new VaultageServer("tcp://localhost:61616");

		// through a broker
		for (int i = 0; i < numRequester; i++) {
			workers[i] = new Worker();
			workers[i].setId("Worker-" + numRequester + i);
			workers[i].setCompletedValue(numOperations);
			workers[i].addOperationResponseHandler(new SynchronisedIncrementResponseHandler());
			workers[i].register(server);
		}

		for (int i = 0; i < numRequester; i++) {
			requesters[i] = new Worker();
			requesters[i].setId("Requester-" + i);
			requesters[i].setCompletedValue(numOperations);
			requesters[i].addOperationResponseHandler(new SynchronisedIncrementResponseHandler());
			requesters[i].register(server);
		}

		// initialise all threads first
		Thread threads[] = new Thread[numRequester];
		for (int i = 0; i < numRequester; i++) {
			String remoteWorkerKey = workers[i].getPublicKey();
			threads[i] = initThread(requesters[i], remoteWorkerKey);
		}

		
		ForkRequest forkRequest = new ForkRequest(requesters, workers, 0, numRequester - 1);
		ForkJoinPool pool = new ForkJoinPool(NUM_OF_PROCESSORS);

		long start = System.currentTimeMillis();
		long max = pool.invoke(forkRequest);

//		long start = System.currentTimeMillis();
//
//		// start all threads at once
//		for (int i = 0; i < numRequester; i++) {
//			threads[i].start();
////			threads[i].join();
//		}
//
//		// wait for all requesters to finish
//		for (int i = 0; i < numRequester; i++) {
//			threads[i].join();
//		}

		long end = System.currentTimeMillis();
		totalTime = end - start;
//		System.out.println();
//		System.out.println("Total Time = " + (end - start));

//		// get maximum waiting time
//		long max = 0;
//		for (int i = 0; i < numRequester; i++) {
//			if (((SendOperationThread) threads[i]).getExecutionTime() > max) {
//				max = ((SendOperationThread) threads[i]).getExecutionTime();
//			}
//		}

		latestWaitTime = max;

		// appropriately dispose broker
		for (int i = 0; i < numRequester; i++) {
			workers[i].unregister();
			requesters[i].unregister();
		}
//		broker.stop();
	}

	/** RUN DIRECT MESSAGING **/
	public void runDirectMessaging() throws Exception {

		Worker[] workers = new Worker[numRequester];
		Worker[] requesters = new Worker[numRequester];

		int port = 61000;

		// through a broker
		for (int i = 0; i < numRequester; i++) {
			workers[i] = new Worker();
			workers[i].setId("Worker-" + i);
			workers[i].setCompletedValue(numOperations);
			workers[i].addOperationResponseHandler(new SynchronisedIncrementResponseHandler());
			workers[i].startServer("127.0.0.1", port++);
		}

		for (int i = 0; i < numRequester; i++) {
			requesters[i] = new Worker();
			requesters[i].setId("Requester-" + i);
			requesters[i].setCompletedValue(numOperations);
			requesters[i].addOperationResponseHandler(new SynchronisedIncrementResponseHandler());
			requesters[i].startServer("127.0.0.1", port++);
		}

		// setting up workers to trust each other so no need to communicate via broker,
		// only if the the communication mode is direct
		for (int i = 0; i < numRequester; i++) {
			Worker worker = workers[i];
			Worker requester = requesters[i];
			worker.getVaultage().getPublicKeyToRemoteAddress().put(requester.getPublicKey(),
					requester.getVaultage().getDirectMessageServerAddress());
			requester.getVaultage().getPublicKeyToRemoteAddress().put(worker.getPublicKey(),
					worker.getVaultage().getDirectMessageServerAddress());
		}

//		// initialise all threads first
//		Thread threads[] = new Thread[numRequester];
//		for (int i = 0; i < numRequester; i++) {
//			threads[i] = initThread(requesters[i], workers[i].getPublicKey());
//		}
//

		ForkRequest forkRequest = new ForkRequest(requesters, workers, 0, numRequester - 1);
		ForkJoinPool pool = new ForkJoinPool(NUM_OF_PROCESSORS);

		long start = System.currentTimeMillis();
		long max = pool.invoke(forkRequest);

//		// start all threads at once
//		for (int i = 0; i < numRequester; i++) {
//			threads[i].start();
////			threads[i].join();
//		}
//
////		System.console();
//
//		// wait for all requesters to finish
//		for (int i = 0; i < numRequester; i++) {
//			threads[i].join();
//		}

		long end = System.currentTimeMillis();
		totalTime = end - start;
//		System.out.println();
//		System.out.println("Total Time = " + (end - start));

//		// get maximum waiting time
//		long max = 0;
//		for (int i = 0; i < numRequester; i++) {
//			if (((SendOperationThread) threads[i]).getExecutionTime() > max) {
//				max = ((SendOperationThread) threads[i]).getExecutionTime();
//			}
//		}

		latestWaitTime = max;

		// appropriately dispose broker
		for (int i = 0; i < numRequester; i++) {
			workers[i].shutdownServer();
			requesters[i].shutdownServer();
		}

//		System.console();
	}

	public int getNumOperations() {
		return numOperations;
	}

	public long getLatestWaitTime() {
		return latestWaitTime;
	}

	public long getTotalTime() {
		return totalTime;
	}

	public String getLatestRunDetails() {
		return MessageFormat.format(
				"Num requesters: {0}, NumOps/worker: {1}, Max waiting time: {2} ms, Total time: {3} ms", numRequester,
				numOperations, latestWaitTime, totalTime);
	}

	private static Thread initThread(Worker worker, String remoteWorkerKey) {
		Thread t = new SendOperationThread(worker, remoteWorkerKey);
//		t.start();
		return t;
	}

	public class ForkRequest extends RecursiveTask<Long> {

		Worker[] requesters;
		Worker[] workers;
		int start;
		int end;

		public ForkRequest(Worker[] requesters, Worker[] workers, int start, int end) {
			this.requesters = requesters;
			this.workers = workers;
			this.start = start;
			this.end = end;
		}

		@Override
		protected Long compute() {

			if (start == end) {
				long temp = sendRequest(requesters[end], workers[end]);
				return temp;
			}

			int mid = Math.floorDiv(start + end, 2);

			ForkRequest left = new ForkRequest(requesters, workers, start, mid);
			ForkRequest right = new ForkRequest(requesters, workers, mid + 1, end);
			left.fork();
			long rightResult = right.compute();
			long leftResult = left.join();
			return (leftResult > rightResult) ? leftResult : rightResult;
		}

		public long sendRequest(Worker requester, Worker worker) {
//			System.out.println("Requester " + worker.getId() + " start...");
			long start = System.currentTimeMillis();
			while (!requester.isWorkComplete()) {
				try {
					requester.sendOperation(worker.getPublicKey(), false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			long end = System.currentTimeMillis();
			long executionTime = end - start;
//			System.out.println("Requester " + worker.getId() + " ended = " + executionTime);
			return executionTime;
		}

	}

	public static class SendOperationThread extends Thread {
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
//			while (!worker.isWorkComplete()) {
				try {
					worker.sendOperation(remoteWorkerKey, false);
				} catch (Exception e) {
					e.printStackTrace();
				}
//			}
			long end = System.currentTimeMillis();
			executionTime = end - start;
//			System.out.println("Requester " + worker.getId() + " ended = " + executionTime);
		}

		public long getExecutionTime() {
			return executionTime;
		}
	}

}
