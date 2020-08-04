package org.vaultage.core;

import java.io.IOException;

public interface DirectMessageServer extends Runnable {

	public String getLocalAddress();

	public int getLocalPort();

	public void start();

	public void shutdown() throws IOException;

}
