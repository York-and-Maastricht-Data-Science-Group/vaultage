package org.vaultage.core;

import java.io.IOException;
import java.net.InetSocketAddress;

/***
 * The interface for diffferent implementations of direct message client.
 * 
 * @author Alfa Yohannis
 *
 */
public interface DirectMessageClient {

	/***
	 * To connect to a direct message server using its ip/hostname and port.
	 * 
	 * @param serverName the direct message server's ip or hostname
	 * @param port       the direct message server's port
	 * @return 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public boolean connect(String serverName, int port) throws IOException, InterruptedException;

	/***
	 * To connect to a direct message server using its ip/hostname and port if the
	 * ip/hostname and port have been already defined.
	 * @return 
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public boolean connect() throws IOException, InterruptedException;

	/***
	 * To re-connect to a direct message server with the ip/hostname and port.
	 * 
	 * @param serverName the direct message server's ip or hostname
	 * @param port       the direct message server's port
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void reconnect(String serverName, int port) throws IOException, InterruptedException;

	/***
	 * To connect to a direct message server with the ip/hostname and port if the
	 * ip/hostname and port have been already defined.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void reconnect() throws IOException, InterruptedException;

	/***
	 * To disconnect from a direct message server.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void disconnect() throws IOException, InterruptedException;

	/***
	 * To send a message to the direct message server
	 * 
	 * @param message the message
	 * @throws IOException
	 */
	public void sendMessage(String message) throws IOException;

	/***
	 * Close all connections and shutdown the direct message client
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void shutdown() throws IOException, InterruptedException;

	/***
	 * To check if the client has successfully connected to the direct message
	 * server.
	 * 
	 * @return true if the connection is active, false if it's inactive
	 */
	public boolean isActive();

	/***
	 * To get the address of the direct message server
	 * 
	 * @return the ip/hostname and port
	 */
	public InetSocketAddress getRemoteAddress();

	/***
	 * To get the local address of the client
	 * 
	 * @return the ip/hostname and port
	 */
	public InetSocketAddress getLocalAddress();

}
