package org.rdbd.core.server;

public abstract class RDBDHandler extends Thread {

	protected String queueId;
	protected RDBDMessage message;
	protected Object owner;

	public void execute(String queueId, RDBDMessage message) {
		this.message = message;
		this.start();
	}

	public Object getOwner() {
		return owner;
	}

	public void setOwner(Object owner) {
		this.owner = owner;
	}

	public RDBDMessage getMessage() {
		return message;
	}

}
