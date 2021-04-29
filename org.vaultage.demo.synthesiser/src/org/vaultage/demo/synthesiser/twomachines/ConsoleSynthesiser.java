package org.vaultage.demo.synthesiser.twomachines;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.vaultage.core.VaultageServer;
import org.vaultage.demo.synthesiser.Worker;
import org.vaultage.demo.synthesiser.traffic.SynchronisedIncrementResponseHandler;
import org.vaultage.demo.synthesiser.twomachines.OneToOneTrafficRequest.SendOperationThread;

public class ConsoleSynthesiser {

//	private static String SHARED_WORKER_DIRECTORY = "";
	private static String SHARED_WORKER_DIRECTORY = "workers" + File.separator;
	private static String RESULT_FILE;

	private static final String TYPE_WORKER = "Worker";
	private static final String TYPE_REQUESTER = "Requester";
	private static String type;
	private static String localIp;
	private static int localPort;
	private static String remoteIp;
	private static int remotePort;
	private static int numOfWork;
	private static String localVaultName;
	private static String brokerAddress;
	private static boolean isRunning = false;
	private static boolean isEncrypted = false;
	private static boolean isBrokered = false;

	private static String remoteVaultName;
	private static String header;

	// synthesiser [brokered(true/false)] [encrypted(true/false)]
	// [type(Worker/Requester)]
	// [brokerAddress(ip:port)]
	// [localVaultName] [localAddress(ip:port)]
	// [remoteVaultName] [remoteAddress(ip:port)] [numOfWork]
	// synthesiser true true Worker 139.162.228.32:61616 worker-001 127.0.0.1:50101
	// synthesiser true true Requester 139.162.228.32:61616 requester-001
	// 127.0.0.1:50201
	// worker-001 127.0.0.1:50101 3
	// 127.0.0.1-50101
	// 127.0.0.1-50002
	public static void main(String[] args) throws Exception {

		isBrokered = Boolean.valueOf(args[0]);
		isEncrypted = Boolean.valueOf(args[1]);

		type = args[2];
		brokerAddress = "tcp://" + args[3];

		localVaultName = args[4];

		String[] localAddress = args[5].split(":");
		localIp = localAddress[0];
		localPort = Integer.valueOf(localAddress[1]);

		if (args.length >= 7) {
			remoteVaultName = args[6];
		}

		if (args.length >= 8) {
			String[] remoteAddress = args[7].split(":");
			remoteIp = remoteAddress[0];
			remotePort = Integer.valueOf(remoteAddress[1]);
		}

		if (args.length >= 9) {
			numOfWork = Integer.valueOf(args[8]);
		}

		// SETUP directory
		String hostname = InetAddress.getLocalHost().getHostName();
		if (hostname.equals("DESKTOP-S9QN639")) {
//			SHARED_WORKER_DIRECTORY = "Z:\\workers\\";
		} else if (hostname.equals("wv9011")) {
//			SHARED_WORKER_DIRECTORY = "/home/ryan/share/workers/";
		}

		//
		VaultageServer server = new VaultageServer(brokerAddress);

		System.out.println("Running " + type + " " + localVaultName + " " + localIp + ":" + localPort + " ...");

		if (type.equals(TYPE_WORKER)) {
			startWorker(server);
		} else if (type.equals(TYPE_REQUESTER)) {

			RESULT_FILE = "result_" + isBrokered + "_" + isEncrypted + /*"_" + numOfWork +*/ ".csv";
			header = "name,brokered,encrypted,num_of_work,wait_time" + System.lineSeparator();
//			Files.deleteIfExists(Paths.get(RESULT_FILE));
			while (Files.notExists(Paths.get(RESULT_FILE), LinkOption.NOFOLLOW_LINKS)) {
				Files.write(Paths.get(RESULT_FILE), header.getBytes(), StandardOpenOption.CREATE);
			}

			System.out.println("Sending work to " + remoteVaultName + " " + remoteIp + ":" + remotePort + " ...");

			startRequester(server);
		}

		System.exit(0);
	}

	/***
	 * Start a requester.
	 * 
	 * @param server
	 * @throws Exception
	 */
	private static void startRequester(VaultageServer server) throws Exception {
		Worker requester = new Worker();
		requester.setId(localVaultName);
		requester.setCompletedValue(numOfWork);
		requester.addOperationResponseHandler(new SynchronisedIncrementResponseHandler());
		if (isBrokered) {
			requester.register(server);
			requester.getVaultage().forceBrokeredMessaging(true);
			System.out.println("Connected to a broker. ");
		} else {
			requester.startServer(localIp, localPort);
			System.out.println("Direct server started.");
		}

		File workerPKfile = new File(SHARED_WORKER_DIRECTORY + remoteVaultName + ".txt");
		String remoteWorkerKey = new String(Files.readAllBytes(Paths.get(workerPKfile.getAbsolutePath())));

		requester.getVaultage().getPublicKeyToRemoteAddress().put(remoteWorkerKey,
				new InetSocketAddress(remoteIp, remotePort));

		Thread thread = new SendOperationThread(requester, remoteWorkerKey, true);
		long start = System.currentTimeMillis();
		thread.start();
		thread.join();
		long end = System.currentTimeMillis();
		long executionTime = end - start;
		System.out.println("Total Time: " + executionTime + " ms");
		String result = localVaultName + "," + isBrokered + "," + isEncrypted + "," + numOfWork + "," + executionTime
				+ System.lineSeparator();

		while (Files.notExists(Paths.get(RESULT_FILE), LinkOption.NOFOLLOW_LINKS)) {
			Files.createFile(Paths.get(RESULT_FILE));
			Files.write(Paths.get(RESULT_FILE), header.getBytes(), StandardOpenOption.APPEND);
		}
		Files.write(Paths.get(RESULT_FILE), result.getBytes(), StandardOpenOption.APPEND);

		try {
			requester.unregister();
			requester.shutdownServer();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println(type + " " + requester.getId() + " " + localIp + ":" + localPort + " finished");
	}

	/***
	 * Start a worker.
	 * 
	 * @param server
	 * @throws Exception
	 */
	private static void startWorker(VaultageServer server) throws Exception {
		Worker worker = new Worker();
		worker.setId(localVaultName);
		worker.addOperationResponseHandler(new SynchronisedIncrementResponseHandler());
		if (isBrokered) {
			worker.register(server);
			worker.getVaultage().forceBrokeredMessaging(true);
			System.out.println("Connected to a broker. ");
		} else {
			worker.startServer(localIp, localPort);
			System.out.println("Direct server started.");
		}

		Files.write(Paths.get(SHARED_WORKER_DIRECTORY + worker.getId() + ".txt"), worker.getPublicKey().getBytes(),
				StandardOpenOption.CREATE);

		Scanner scanner = new Scanner(System.in);
		String key = "";
		while (!"Q".equals(key) && !"q".equals(key)) {
			System.out.println("Enter 'Q' or 'q' to stop: ");
			key = scanner.next();
			try {
				key = (key != null) ? key.trim() : "";
			} catch (Exception e) {
			}
		}
		scanner.close();

		try {
			worker.unregister();
			worker.shutdownServer();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println(type + " " + worker.getId() + " " + localIp + ":" + localPort + " finished");
	}

	public static class SendOperationThread extends Thread {
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