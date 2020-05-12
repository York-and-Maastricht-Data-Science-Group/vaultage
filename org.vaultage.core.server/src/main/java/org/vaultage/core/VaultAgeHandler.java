package org.vaultage.core;

public abstract class VaultAgeHandler {

	private static long COUNTER = 0;
	protected String queueId;
	protected String senderPublicKey;
	protected VaultAgeMessage message;
	protected Object owner;
	protected String name;
	protected Thread thread;

	public VaultAgeHandler() {
		name = this.getClass().getSimpleName() + "-" + COUNTER;
		COUNTER++;
	}

	public void execute(String queueId, VaultAgeMessage message) {
		this.message = message;
		this.start();
	}

	public void start() {

		thread = new Thread(VaultAgeHandler.this.getName()) {
			@Override
			public void run() {
				VaultAgeHandler.this.run();
			}
		};
		thread.run();
	}

	public abstract void run();

	public String getSenderPublicKey() {
		return senderPublicKey;
	}

	public void setSenderPublicKey(String senderPublicKey) {
		this.senderPublicKey = senderPublicKey;
	}

	public String getName() {
		return name;

	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getOwner() {
		return owner;
	}

	public void setOwner(Object owner) {
		this.owner = owner;
	}

	public VaultAgeMessage getMessage() {
		return message;
	}

	protected boolean isAlive() {
		if (this.thread != null) {
			return this.thread.isAlive();
		} else {
			return false;
		}
	}

	protected void interrupt() {
		if (this.thread != null) {
			this.thread.interrupt();
		}
	}

}
