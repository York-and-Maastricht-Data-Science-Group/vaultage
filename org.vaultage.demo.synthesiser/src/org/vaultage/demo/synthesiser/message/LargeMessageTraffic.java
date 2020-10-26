package org.vaultage.demo.synthesiser.message;

import java.io.File;
import java.io.PrintStream;
import java.text.MessageFormat;

import org.vaultage.core.VaultageServer;
import org.vaultage.demo.synthesiser.SynthesiserBroker;
import org.vaultage.demo.synthesiser.Worker;

/**
 * LargeMessage: Sending large message from a worker to another worker
 *
 * @author Alfonso de la Vega, Alfa Yohannis
 */
public class LargeMessageTraffic {

	protected long latestRunTime;
	protected int numWorkers;
	protected int numBytes;
	protected boolean isEncrypted;

	public static void main(String[] args) throws Exception {

		int numReps = 5;
		int numWorkers = 2;
		int[] numberOfBytes = { 9000, 10000, 20000, 30000 };

		PrintStream profilingStream = new PrintStream(new File("largeMessageResults.csv"));
		profilingStream.println("IsEncrypted,MessageBytes,TotalTimeMillis");

		boolean isEncrypted = false;
		for (int numBytes : numberOfBytes) {
			LargeMessageTraffic trafficSimulation = new LargeMessageTraffic(numWorkers, numBytes, isEncrypted);
			for (int rep = 0; rep < numReps; rep++) {
				trafficSimulation.run();
				System.out.println(trafficSimulation.getLatestRunDetails());
				profilingStream.println(
						String.format("%b,%s,%d", isEncrypted, numBytes, trafficSimulation.getLatestRunTime()));
			}
		}

		isEncrypted = true;
		for (int numBytes : numberOfBytes) {
			LargeMessageTraffic trafficSimulation = new LargeMessageTraffic(numWorkers, numBytes, isEncrypted);
			for (int rep = 0; rep < numReps; rep++) {
				trafficSimulation.run();
				System.out.println(trafficSimulation.getLatestRunDetails());
				profilingStream.println(
						String.format("%b,%s,%d", isEncrypted, numBytes, trafficSimulation.getLatestRunTime()));
			}
		}
		profilingStream.close();
	}

	public LargeMessageTraffic(int numWorkers, int numBytes, boolean isEncrypted) {
		this.numWorkers = numWorkers;
		this.numBytes = numBytes;
		this.isEncrypted = isEncrypted;
	}

	public void run() throws Exception {
		Worker[] workers = new Worker[numWorkers];

		SynthesiserBroker broker = new SynthesiserBroker();
		broker.start(SynthesiserBroker.BROKER_ADDRESS);
		VaultageServer server = new VaultageServer(SynthesiserBroker.BROKER_ADDRESS);

		for (int i = 0; i < numWorkers; i++) {
			workers[i] = new Worker();
			workers[i].setId("" + i);
			workers[i].setGetTextSizeResponseHandler(new SynchronisedGetTextSizeResponseHandler());
			workers[i].register(server);
		}

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < numBytes; i++) {
			sb.append("a");
		}

		Thread threads[] = new Thread[numWorkers];
		threads[0] = startWork(workers[0], workers[1].getPublicKey(), sb.toString(), isEncrypted);

		long start = System.currentTimeMillis();

		threads[0].start();
		threads[0].join();

		long end = System.currentTimeMillis();

		latestRunTime = end - start;

		// appropriately dispose broker
		for (int i = 0; i < numWorkers; i++) {
			workers[i].unregister();
		}
		broker.stop();
	}

	public int getNumBytes() {
		return numBytes;
	}

	public long getLatestRunTime() {
		return latestRunTime;
	}

	public String getLatestRunDetails() {
		return MessageFormat.format("Num workers: {0}, NumOfBytes: {1}, Total time: {2} ms", numWorkers, numBytes,
				latestRunTime);
	}

	private static Thread startWork(Worker worker, String remoteWorkerKey, String text, boolean isEncrypted) {
		Thread t = new Thread() {
			public void run() {
				try {
					int size = worker.getTextSize(remoteWorkerKey, text, isEncrypted);
					System.out.println("Text size = " + size + " bytes");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		return t;
	}

}
