package org.vaultage.core;

import java.io.IOException;
import java.net.InetSocketAddress;

public interface DirectMessageClient {

	public void connect(String serverName, int port) throws IOException, InterruptedException;

	public void connect() throws IOException, InterruptedException;

	public void reconnect(String serverName, int port) throws IOException, InterruptedException;

	public void reconnect() throws IOException, InterruptedException;

	public void disconnect() throws IOException, InterruptedException;

	public void sendMessage(String message) throws IOException;

	public void shutdown() throws IOException, InterruptedException;

	public boolean isActive();

	public InetSocketAddress getRemoteAddress();

	public InetSocketAddress getLocalAddress();

}
