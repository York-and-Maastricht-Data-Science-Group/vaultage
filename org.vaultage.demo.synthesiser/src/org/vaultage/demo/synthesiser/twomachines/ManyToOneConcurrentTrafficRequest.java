package org.vaultage.demo.synthesiser.twomachines;

import java.io.File;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Random;

import org.vaultage.core.Vaultage;
import org.vaultage.core.VaultageServer;
import org.vaultage.demo.synthesiser.Worker;
import org.vaultage.demo.synthesiser.concurrency.ManyToOneConcurrentTraffic.SendOperationThread;
import org.vaultage.demo.synthesiser.traffic.SynchronisedIncrementResponseHandler;

/**
 * Stress-testing for Vaultage. Run worker service first to prepare the workers
 * before running this class.
 *
 * @author Alfonso de la Vega, Alfa Yohannis
 */
public class ManyToOneConcurrentTrafficRequest {

	private static String SHARED_REQUESTER_DIRECTORY;
	private static String SHARED_WORKER_DIRECTORY;
	private static String LOCAL_IP;
	private static String REMOTE_IP;

	protected long latestWaitTime;
	protected long latestTotalTime;
	protected boolean brokered;
	protected boolean encrypted;
	protected int numRequesters;
	protected int numOperations;
	private Random random = new Random();

	public static void main(String[] args) throws Exception {

		String hostname = InetAddress.getLocalHost().getHostName();
		if (hostname.equals("DESKTOP-S9QN639")) {
			SHARED_REQUESTER_DIRECTORY = "Z:\\requesters\\";
			SHARED_WORKER_DIRECTORY = "Z:\\workers\\";
			LOCAL_IP = "192.168.0.2";
			REMOTE_IP = "192.168.0.2";
		} else if (hostname.equals("wv9011")) {
			SHARED_REQUESTER_DIRECTORY = "/home/ryan/share/requesters/";
			SHARED_WORKER_DIRECTORY = "/home/ryan/share/workers/";
			LOCAL_IP = "192.168.0.4";
			REMOTE_IP = "192.168.0.2";
		}

		int numReps = 5;
		int numOperations = 1;
		int[] numRequesters = { 9, 10, 15, 20, 25, 30 };

		PrintStream profilingStream = new PrintStream(new File("ManyToOneConcurrentNetResults.csv"));
		profilingStream.println("Mode,Encryption,NumTasks,WaitTimeMillis");

		// brokered and encrypted
		for (int numReq : numRequesters) {
			ManyToOneConcurrentTrafficRequest trafficSimulation = new ManyToOneConcurrentTrafficRequest(numReq,
					numOperations, true, true);
			for (int rep = 0; rep < numReps; rep++) {
				trafficSimulation.run();
				System.out.println(trafficSimulation.getLatestRunDetails());
				profilingStream.println(String.format("%s,%s,%s,%d", "brokered", "encrypted", numReq,
						trafficSimulation.getLatestWaitTime()));
			}
		}

		// direct and encrypted
		for (int numReq : numRequesters) {
			ManyToOneConcurrentTrafficRequest trafficSimulation = new ManyToOneConcurrentTrafficRequest(numReq,
					numOperations, false, true);
			for (int rep = 0; rep < numReps; rep++) {
				trafficSimulation.run();
				System.out.println(trafficSimulation.getLatestRunDetails());
				profilingStream.println(String.format("%s,%s,%s,%d", "direct", "encrypted", numReq,
						trafficSimulation.getLatestWaitTime()));
			}
		}

		// brokered and un-encrypted
		for (int nReq : numRequesters) {
			ManyToOneConcurrentTrafficRequest trafficSimulation = new ManyToOneConcurrentTrafficRequest(nReq,
					numOperations, true, false);
			for (int rep = 0; rep < numReps; rep++) {
				trafficSimulation.run();
				System.out.println(trafficSimulation.getLatestRunDetails());
				profilingStream.println(
						String.format("%s,%s,%s,%d", "brokered", "plain", nReq, trafficSimulation.getLatestWaitTime()));
			}
		}

		// direct and un-encrypted
		for (int numReq : numRequesters) {
			ManyToOneConcurrentTrafficRequest trafficSimulation = new ManyToOneConcurrentTrafficRequest(numReq,
					numOperations, false, false);
			for (int rep = 0; rep < numReps; rep++) {
				trafficSimulation.run();
				System.out.println(trafficSimulation.getLatestRunDetails());
				profilingStream.println(
						String.format("%s,%s,%s,%d", "direct", "plain", numReq, trafficSimulation.getLatestWaitTime()));
			}
		}
		profilingStream.close();
		System.out.println("Finished!");
		System.exit(0);
	}

	public ManyToOneConcurrentTrafficRequest(int numRequesters, int numOperations, boolean brokered,
			boolean encrypted) {
		this.numRequesters = numRequesters;
		this.brokered = brokered;
		this.encrypted = encrypted;
		this.numOperations = numOperations;
	}

	public void run() throws Exception {
		Worker[] requesters = new Worker[numRequesters];

//		VaultageServer server = new VaultageServer("tcp://localhost:61616");
		VaultageServer server = new VaultageServer("tcp://139.162.228.32:61616");

		// loading workers public keys
		String[] workerPKs = new String[1];
		File directoryPath = new File(SHARED_WORKER_DIRECTORY);
		File[] files = directoryPath.listFiles();
		Arrays.sort(files);
//		for (int i = 0; i < files.length; i++) {
		String workerPK = new String(Files.readAllBytes(Paths.get(files[0].getAbsolutePath())));
		workerPKs[0] = workerPK;
//		}

		int port = Vaultage.DEFAULT_SERVER_PORT + 200;
		for (int i = 0; i < numRequesters; i++) {
			requesters[i] = new Worker();
			requesters[i].setId("Requester-" + i);
			requesters[i].setCompletedValue(numOperations);
			requesters[i].addOperationResponseHandler(new SynchronisedIncrementResponseHandler());
			requesters[i].register(server);
			if (!brokered) {
				requesters[i].startServer(LOCAL_IP, port++);
				requesters[i].getVaultage().getPublicKeyToRemoteAddress().put(workerPKs[0],
						new InetSocketAddress(REMOTE_IP, Vaultage.DEFAULT_SERVER_PORT + 100));
			} else {
				requesters[i].getVaultage().forceBrokeredMessaging(false);
			}
			Files.write(Paths.get(SHARED_REQUESTER_DIRECTORY + requesters[i].getId() + ".txt"),
					requesters[i].getPublicKey().getBytes(), StandardOpenOption.CREATE);
//			System.out.println(requesters[i].getId() + " created");
		}

		Thread threads[] = new Thread[numRequesters];
		for (int i = 0; i < numRequesters; i++) {
			String remoteWorkerKey = workerPKs[0];
			threads[i] = initThread(requesters[i], remoteWorkerKey, encrypted);
		}

		long start = System.currentTimeMillis();

		// start all threads
		for (int i = 0; i < numRequesters; i++) {
			threads[i].start();
		}

		// wait for workers to finish
		for (int i = 0; i < numRequesters; i++) {
			threads[i].join();
		}

		long end = System.currentTimeMillis();

		// get average waiting time
		long average = 0;
		for (int i = 0; i < numRequesters; i++) {	
				average = average + ((SendOperationThread) threads[i]).getExecutionTime();
		}
		average = average / numRequesters;
		latestWaitTime = average;

		//total time 
		latestTotalTime = end - start;
		
		if (!brokered) {
			for (Worker requester : requesters) {
				requester.shutdownServer();
			}
		}

	}

	public int getNumOperations() {
		return numOperations;
	}

	public long getLatestWaitTime() {
		return latestWaitTime;
	}

	public String getLatestRunDetails() {
		return MessageFormat.format(
				"Brokered: {0}, Encrypted: {1}, Num workers: {2}, NumOps/worker: {3}, Wait time: {4} ms", brokered,
				encrypted, numRequesters, numOperations, latestWaitTime);
	}

	private Thread initThread(Worker worker, String remoteWorkerKey, boolean encrypted2) {
		Thread t = new SendOperationThread(worker, remoteWorkerKey, encrypted);
//		t.start();
		return t;
	}

	public class SendOperationThread extends Thread {
		private final Worker worker;
		private final String remoteWorkerKey;
		private long executionTime;
		private boolean encrypted;

		public SendOperationThread(Worker worker, String remoteWorkerKey, boolean encrypted) {
			this.setName(worker.getId());
			this.executionTime = 0;
			this.worker = worker;
			this.remoteWorkerKey = remoteWorkerKey;
			this.encrypted = encrypted;
		}

		public void run() {
//			System.out.println("Requester " + worker.getId() + " start...");
			long start = System.currentTimeMillis();
			while (!worker.isWorkComplete()) {
				try {
					worker.sendOperation(remoteWorkerKey, encrypted);
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
