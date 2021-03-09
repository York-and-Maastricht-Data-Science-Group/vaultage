package org.vaultage.demo.synthesiser.twomachines;

import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

import org.vaultage.core.Vaultage;
import org.vaultage.core.VaultageServer;
import org.vaultage.demo.synthesiser.Worker;
import org.vaultage.demo.synthesiser.traffic.SynchronisedIncrementResponseHandler;

/**
 * Service for generating workers
 *
 * @author Alfa Yohannis
 */
public class WorkerService {

	private static String SHARED_WORKER_DIRECTORY = "Z:\\workers\\";
	private static String LOCAL_IP = "192.168.0.2";
	private static final int NUM_WORKERS = 1;
	private static Worker[] workers;

	public static void main(String[] args) throws Exception {
		try {
			System.out.println("Running worker service ...");

			String hostname = InetAddress.getLocalHost().getHostName();
			if (hostname.equals("DESKTOP-S9QN639")) {
				SHARED_WORKER_DIRECTORY = "Z:\\workers\\";
				LOCAL_IP = "192.168.0.2";
			} else if (hostname.equals("wv9011")) {
				SHARED_WORKER_DIRECTORY = "/home/ryan/share/workers/";
				LOCAL_IP = "192.168.0.4";
			}

//		VaultageServer server = new VaultageServer("tcp://localhost:61616");
			VaultageServer server = new VaultageServer("tcp://139.162.228.32:61616");

			int port = Vaultage.DEFAULT_SERVER_PORT + 100;

			workers = new Worker[NUM_WORKERS];

			for (int i = 0; i < NUM_WORKERS; i++) {
				workers[i] = new Worker();
				// I added 100 so that the key files can be ordered perfectly when
				// listing the files
				workers[i].setId("Worker-" + (100 + i));
				workers[i].addOperationResponseHandler(new SynchronisedIncrementResponseHandler());
				workers[i].register(server);
				workers[i].startServer(LOCAL_IP, port++);
				Files.write(Paths.get(SHARED_WORKER_DIRECTORY + workers[i].getId() + ".txt"),
						workers[i].getPublicKey().getBytes(), StandardOpenOption.CREATE);
				System.out.println(workers[i].getId() + " created");
			}

			Scanner scanner = new Scanner(System.in);
			String key = "";
			while (!key.equals("Q") && !key.equals("q")) {
				System.out.println("Enter 'Q' or 'q' to stop: ");
				key = scanner.next().trim();
			}
			scanner.close();
			System.out.println("Shutting down ...");
			// remove connections
			for (int i = 0; i < NUM_WORKERS; i++) {
				workers[i].unregister();
				workers[i].shutdownServer();
			}
			System.out.println("Terminated!");
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			if (workers != null)
				for (Worker worker : workers) {
					worker.unregister();
					worker.shutdownServer();
				}
			System.out.println("Terminated!");
			System.exit(0);
		}
	}

}
