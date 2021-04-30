package org.vaultage.demo.synthesiser;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.fusesource.hawtbuf.ByteArrayInputStream;
import org.vaultage.core.StreamReceiver;
import org.vaultage.core.Vault;
import org.vaultage.demo.synthesiser.message.SynchronisedGetTextSizeResponseHandler;

public class Worker extends WorkerBase {

	private int currentValue;
	private int completedValue;
	private Map<Integer, String> dataMap;

	public Worker() throws Exception {
		super();
		currentValue = 0;
		
	}

	public void sendOperation(String workerPublicKey) throws Exception {
		this.sendOperation(workerPublicKey, true);
	}

	public void sendOperation(String workerPublicKey, boolean isEncrypted) throws Exception {
		RemoteWorker worker = new RemoteWorker(this, workerPublicKey);
		synchronized (this.getOperationResponseHandler(IncrementResponseHandler.class)) {
			worker.increment(currentValue, isEncrypted);
			this.getOperationResponseHandler(IncrementResponseHandler.class).wait();
		}
	}

	@Override
	public void increment(String requesterPublicKey, String requestToken, Integer number) throws Exception {
		RemoteWorker remote = new RemoteWorker(this, requesterPublicKey);
		remote.respondToIncrement(number + 1, requestToken);
	}

	public boolean isWorkComplete() {
		System.out.println("current value: " + this.currentValue);
		return currentValue >= completedValue;
	}

	public void setCurrentValue(int currentValue) {
		this.currentValue = currentValue;
	}

	public void setCompletedValue(int completedValue) {
		this.completedValue = completedValue;
	}

	public int getTextSize(String workerPublicKey, String text) throws Exception {
		return this.getTextSize(workerPublicKey, text, true);
	}

	public int getTextSize(String workerPublicKey, String text, boolean isEncrypted) throws Exception {
		RemoteWorker worker = new RemoteWorker(this, workerPublicKey);
		synchronized (this.getOperationResponseHandler(GetTextSizeResponseHandler.class)) {
			worker.getTextSize(text, isEncrypted);
			this.getOperationResponseHandler(GetTextSizeResponseHandler.class).wait();
		}
		return ((SynchronisedGetTextSizeResponseHandler) this
				.getOperationResponseHandler(GetTextSizeResponseHandler.class)).getTextSize();
	}

	@Override
	public void getTextSize(String requesterPublicKey, String requestToken, String text) throws Exception {
		this.getTextSize(requesterPublicKey, requestToken, text, true);
	}

	public void getTextSize(String requesterPublicKey, String requestToken, String text, boolean isEncrypted)
			throws Exception {
		RemoteWorker requester = new RemoteWorker(this, requesterPublicKey);
		isEncrypted = this.getVaultage().isEncrypted();
		requester.respondToGetTextSize(text.getBytes().length, requestToken, isEncrypted);
	}

	@Override
	public void streamData(String requesterPublicKey, String requestToken, InetSocketAddress receiverSocketAddress,
			Integer size) throws Exception {
		String data = dataMap.get(size);
		RemoteWorker remoteRequester = new RemoteWorker(this, requesterPublicKey, receiverSocketAddress);
		boolean isEncrypted = this.getVaultage().isEncrypted();
		ByteArrayInputStream dataInputStream = new ByteArrayInputStream(data.getBytes());
		remoteRequester.respondToStreamData(dataInputStream, requestToken, isEncrypted);
	}

	public void requestDataStream(String requesterPublicKey, boolean encrypted, int size) throws Exception {

		// setup localIpAddress and port to receive stream
		String receiverAddress = InetAddress.getLoopbackAddress().getHostAddress();
		int receiverPort = 54322;

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		RemoteWorker remotePeer = new RemoteWorker(this, requesterPublicKey);
		
		addOperationResponseHandler(new StreamDataResponseHandler() {
			
			@Override
			public void run(Worker localVault, RemoteWorker remoteVault, String responseToken, String result) throws Exception {
//				System.out.println("AA");
			}	
			@Override
			public void run(Vault localVault, RemoteWorker remoteVault, String responseToken, String result) throws Exception {
//				System.out.println("MessageSize = " + result.length());
			}
		});
		
		StreamReceiver streamReceiver = remotePeer.streamData(outputStream, receiverAddress, receiverPort, size, encrypted);
		synchronized (streamReceiver) {
			streamReceiver.wait();
		}

//		System.out.println("Finished!");
	}
	
	public Map<Integer, String> getDataMap() {
		return dataMap;
	}

	public void setDataMap(Map<Integer, String> dataMap) {
		this.dataMap = dataMap;
	}

}