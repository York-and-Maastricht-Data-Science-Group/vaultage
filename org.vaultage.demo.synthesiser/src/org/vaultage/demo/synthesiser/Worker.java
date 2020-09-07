package org.vaultage.demo.synthesiser;

public class Worker extends WorkerBase {

	private int currentValue;
	private int completedValue;

	public Worker() throws Exception {
		super();
		currentValue = 0;
	}

	public void sendOperation(String workerPublicKey) throws Exception {
		RemoteWorker worker = new RemoteWorker(this, workerPublicKey);
		synchronized (this.getIncrementResponseHandler()) {
			worker.increment(currentValue);
			this.getIncrementResponseHandler().wait();
		}
	}

	@Override
	public void increment(String requesterPublicKey, String requestToken, Integer number) throws Exception {
		RemoteWorker remote = new RemoteWorker(this, requesterPublicKey);
		remote.respondToIncrement(number + 1, requestToken);
	}

	public boolean isWorkComplete() {
		return currentValue >= completedValue;
	}

	public void setCurrentValue(int currentValue) {
		this.currentValue = currentValue;
	}

	public void setCompletedValue(int completedValue) {
		this.completedValue = completedValue;
	}

}