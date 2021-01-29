package org.vaultage.core;

import org.apache.activemq.broker.BrokerService;

public class BaseBroker {
	private BrokerService broker;

	public void start(String address) throws Exception {
		broker = new BrokerService();
		broker.setUseJmx(true);
		broker.addConnector(address);
		broker.start();
	}

	public void stop() throws Exception {
		if (broker != null) {
			broker.stop();
		}
	}
	
	public BrokerService getBroker() {
		return broker;
	}
}
