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
import org.vaultage.demo.synthesiser.RemoteWorker;
import org.vaultage.demo.synthesiser.Worker;
import org.vaultage.demo.synthesiser.concurrency.ManyToOneConcurrentTraffic.SendOperationThread;
import org.vaultage.demo.synthesiser.traffic.SynchronisedIncrementResponseHandler;

/**
 * 1-to-1 test to check the penalty of using a brokered server. Run worker
 * service first to prepare the workers before running this class.
 *
 * @author Alfonso de la Vega, Alfa Yohannis
 */
public class OneToOneTrafficRequest {

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
			REMOTE_IP = "192.168.0.4";
		} else if (hostname.equals("wv9011")) {
			SHARED_REQUESTER_DIRECTORY = "/home/ryan/share/requesters/";
			SHARED_WORKER_DIRECTORY = "/home/ryan/share/workers/";
			LOCAL_IP = "192.168.0.4";
			REMOTE_IP = "192.168.0.2";
		} else if (hostname.equals("research1")) {
			SHARED_WORKER_DIRECTORY = "/tmp/ary506/workers/";
			SHARED_REQUESTER_DIRECTORY = "/tmp/ary506/requesters/";
//			LOCAL_IP = "144.32.196.129";
			LOCAL_IP = "127.0.0.1";
			REMOTE_IP = "127.0.0.1";
		}

		int numReps = 10;
		int[] numOps = { 200, 160, 120, 80, 40 };

		PrintStream profilingStream = new PrintStream(new File("06-1-to-1-test-results.csv"));
		profilingStream.println("Mode,Encryption,NumTasks,WaitTimeMillis");

		// brokered and encrypted

		for (int numOp : numOps) {
			OneToOneTrafficRequest trafficSimulation = new OneToOneTrafficRequest(1, numOp, true, true);
			for (int rep = 0; rep < numReps; rep++) {
				trafficSimulation.run();
				System.out.println(trafficSimulation.getLatestRunDetails());
				profilingStream.println(String.format("%s,%s,%s,%d", "brokered", "encrypted", numOp,
						trafficSimulation.getLatestWaitTime()));
			}
		}

		// direct and encrypted
		for (int numOp : numOps) {
			OneToOneTrafficRequest trafficSimulation2 = new OneToOneTrafficRequest(1, numOp, false, true);
			for (int rep = 0; rep < numReps; rep++) {
				trafficSimulation2.run();
				System.out.println(trafficSimulation2.getLatestRunDetails());
				profilingStream.println(
						String.format("%s,%s,%s,%d", "direct", "encrypted", numOp, trafficSimulation2.getLatestWaitTime()));
			}
		}

		// brokered and un-encrypted
		for (int numOp : numOps) {
			OneToOneTrafficRequest trafficSimulation3 = new OneToOneTrafficRequest(1, numOp, true, false);
			for (int rep = 0; rep < numReps; rep++) {
				trafficSimulation3.run();
				System.out.println(trafficSimulation3.getLatestRunDetails());
				profilingStream.println(
						String.format("%s,%s,%s,%d", "brokered", "plain", numOp, trafficSimulation3.getLatestWaitTime()));
			}
		}

		// direct and un-encrypted
		for (int numOp : numOps) {
			OneToOneTrafficRequest trafficSimulation4 = new OneToOneTrafficRequest(1, numOp, false, false);
			for (int rep = 0; rep < numReps; rep++) {
				trafficSimulation4.run();
				System.out.println(trafficSimulation4.getLatestRunDetails());
				profilingStream.println(
						String.format("%s,%s,%s,%d", "direct", "plain", numOp, trafficSimulation4.getLatestWaitTime()));
			}
		}

		profilingStream.close();
		System.out.println("Finished!");
		System.exit(0);
	}

	public OneToOneTrafficRequest(int numRequesters, int numOp, boolean brokered, boolean encrypted) {
		this.numRequesters = numRequesters;
		this.brokered = brokered;
		this.encrypted = encrypted;
		this.numOperations = numOp;
	}

	public void run() throws Exception {

//		VaultageServer server = new VaultageServer("tcp://localhost:61616");
		VaultageServer server = new VaultageServer("tcp://139.162.228.32:61616");

		// loading workers public keys
		File directoryPath = new File(SHARED_WORKER_DIRECTORY);
		File[] files = directoryPath.listFiles();
		Arrays.sort(files);
		String workerPK = new String(Files.readAllBytes(Paths.get(files[0].getAbsolutePath())));

		int port = Vaultage.DEFAULT_SERVER_PORT + 100;
		Worker requester = new Worker();
		requester.setId("Requester-0");
		requester.setCompletedValue(numOperations);
		requester.addOperationResponseHandler(new SynchronisedIncrementResponseHandler());
		requester.register(server);
		if (!brokered) {
			requester.startServer(LOCAL_IP, port++);
			requester.getVaultage().getPublicKeyToRemoteAddress().put(workerPK,
					new InetSocketAddress(REMOTE_IP, Vaultage.DEFAULT_SERVER_PORT + 100));
		} else {
			requester.getVaultage().forceBrokeredMessaging(true);
		}
		Files.write(Paths.get(SHARED_REQUESTER_DIRECTORY + requester.getId() + ".txt"),
				requester.getPublicKey().getBytes(), StandardOpenOption.CREATE);

		// set the remote worker(s) into encrypted/plain and direct/brokered modes
		RemoteWorker remoteWorker = new RemoteWorker(requester, workerPK);
		remoteWorker.forceBrokeredMessaging(brokered, false);
		remoteWorker.setEncrypted(encrypted, false);
		Thread.sleep(1000);

		Thread thread = initThread(requester, workerPK, encrypted);

		long start = System.currentTimeMillis();
		thread.start();
		thread.join();
		long end = System.currentTimeMillis();

		// get average waiting time
		long average = ((SendOperationThread) thread).getExecutionTime();
		latestWaitTime = average;

		// total time
		latestTotalTime = end - start;

		requester.unregister();
		requester.shutdownServer();
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
