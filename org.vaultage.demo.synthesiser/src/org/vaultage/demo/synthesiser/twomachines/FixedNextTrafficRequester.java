package org.vaultage.demo.synthesiser.twomachines;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.Random;

import org.vaultage.core.Vaultage;
import org.vaultage.core.VaultageServer;
import org.vaultage.demo.synthesiser.Worker;
import org.vaultage.demo.synthesiser.traffic.SynchronisedIncrementResponseHandler;

/**
 * Stress-testing for Vaultage. Run worker service first to prepare the workers
 * before running this class. 
 *
 * @author Alfonso de la Vega, Alfa Yohannis
 */
public class FixedNextTrafficRequester {

	private static final String SHARED_REQUESTER_DIRECTORY = "Z:\\requesters\\";
	private static final String SHARED_WORKER_DIRECTORY = "Z:\\workers\\";
	private static final String LOCAL_IP = "192.168.0.2";
	protected long latestRunTime;
	protected boolean brokered;
	protected boolean encrypted;
	protected int numWorkers;
	protected int numOperations;
	private Random random = new Random();

	public static void main(String[] args) throws Exception {
		int numReps = 5;
		int numWorkers = 3;
//		int[] numOperations = { 10 };
		int[] numOperations = { 5, 10, 15, 20, 25 };

		PrintStream profilingStream = new PrintStream(new File("fixedNetResults.csv"));
		profilingStream.println("Mode,Encryption,NumTasks,TotalTimeMillis");

		// brokered and encrypted
		for (int numOp : numOperations) {
			FixedNextTrafficRequester trafficSimulation = new FixedNextTrafficRequester(numWorkers, numOp, true, true);
			for (int rep = 0; rep < numReps; rep++) {
				trafficSimulation.run();
				System.out.println(trafficSimulation.getLatestRunDetails());
				profilingStream.println(String.format("%s,%s,%s,%d", "brokered", "encrypted", numOp,
						trafficSimulation.getLatestRunTime()));
			}
		}

		// direct and encrypted
		for (int numOp : numOperations) {
			FixedNextTrafficRequester trafficSimulation = new FixedNextTrafficRequester(numWorkers, numOp, false, true);
			for (int rep = 0; rep < numReps; rep++) {
				trafficSimulation.run();
				System.out.println(trafficSimulation.getLatestRunDetails());
				profilingStream.println(String.format("%s,%s,%s,%d", "direct", "encrypted", numOp,
						trafficSimulation.getLatestRunTime()));
			}
		}

		// brokered and un-encrypted
		for (int numOp : numOperations) {
			FixedNextTrafficRequester trafficSimulation = new FixedNextTrafficRequester(numWorkers, numOp, true, false);
			for (int rep = 0; rep < numReps; rep++) {
				trafficSimulation.run();
				System.out.println(trafficSimulation.getLatestRunDetails());
				profilingStream.println(
						String.format("%s,%s,%s,%d", "brokered", "plain", numOp, trafficSimulation.getLatestRunTime()));
			}
		}

		// direct and un-encrypted
		for (int numOp : numOperations) {
			FixedNextTrafficRequester trafficSimulation = new FixedNextTrafficRequester(numWorkers, numOp, false,
					false);
			for (int rep = 0; rep < numReps; rep++) {
				trafficSimulation.run();
				System.out.println(trafficSimulation.getLatestRunDetails());
				profilingStream.println(
						String.format("%s,%s,%s,%d", "direct", "plain", numOp, trafficSimulation.getLatestRunTime()));
			}
		}
		profilingStream.close();
		System.out.println("Finished!");
		System.exit(0);
	}

	public FixedNextTrafficRequester(int numWorkers, int numOperations, boolean brokered, boolean encrypted) {
		this.numWorkers = numWorkers;
		this.numOperations = numOperations;
		this.brokered = brokered;
		this.encrypted = encrypted;
	}

	public void run() throws Exception {
		Worker[] requesters = new Worker[numWorkers];

//		VaultageServer server = new VaultageServer("tcp://localhost:61616");
		VaultageServer server = new VaultageServer("tcp://139.162.228.32:61616");

		// loading workers public keys
		String[] workerPKs = new String[numWorkers];
		File directoryPath = new File(SHARED_WORKER_DIRECTORY);
		File[] files = directoryPath.listFiles();
		for (int i = 0; i < files.length; i++) {
			String workerPK = new String(Files.readAllBytes(Paths.get(files[i].getAbsolutePath())));
			workerPKs[i] = workerPK;
		}

		int port = Vaultage.DEFAULT_SERVER_PORT + 100;
		for (int i = 0; i < numWorkers; i++) {
			requesters[i] = new Worker();
			requesters[i].setId("Requester-" + i);
			requesters[i].setCompletedValue(numOperations);
			requesters[i].addOperationResponseHandler(new SynchronisedIncrementResponseHandler());
			requesters[i].register(server);
			if (!brokered) {
				requesters[i].startServer(LOCAL_IP, port++);
			} else {
				requesters[i].getVaultage().forceBrokeredMessaging(false);
			}
			Files.write(Paths.get(SHARED_REQUESTER_DIRECTORY + requesters[i].getId() + ".txt"),
					requesters[i].getPublicKey().getBytes(), StandardOpenOption.CREATE);
//			System.out.println(requesters[i].getId() + " created");
		}

		Thread threads[] = new Thread[numWorkers];
		for (int i = 0; i < numWorkers; i++) {
//			int remoteWorkerIndex = (i + 1) % numWorkers;
			int remoteWorkerIndex = random.nextInt(numWorkers);
			String remoteWorkerKey = workerPKs[remoteWorkerIndex];
			threads[i] = startWork(requesters[i], remoteWorkerKey, encrypted);
		}

		long start = System.currentTimeMillis();
		
		// start all threads
		for (int i = 0; i < numWorkers; i++) {
			threads[i].start();
		}
		
		// wait for workers to finish
		for (int i = 0; i < numWorkers; i++) {
			threads[i].join();
		}

		long end = System.currentTimeMillis();

		latestRunTime = end - start;

		if (!brokered) {
			for (Worker requester : requesters) {
				requester.shutdownServer();
			}
		}

	}

	public int getNumOperations() {
		return numOperations;
	}

	public long getLatestRunTime() {
		return latestRunTime;
	}

	public String getLatestRunDetails() {
		return MessageFormat.format(
				"Brokered: {0}, Encrypted: {1}, Num workers: {2}, NumOps/worker: {3}, Total time: {4} ms", brokered,
				encrypted, numWorkers, numOperations, latestRunTime);
	}

	private static Thread startWork(Worker worker, String remoteWorkerKey, boolean encrypted) {
		Thread t = new Thread() {
			public void run() {
				while (!worker.isWorkComplete()) {
					try {
						worker.sendOperation(remoteWorkerKey, encrypted);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
//		t.start();
		return t;
	}

}
