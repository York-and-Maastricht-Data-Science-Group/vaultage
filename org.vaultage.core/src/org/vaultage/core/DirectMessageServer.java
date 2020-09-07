package org.vaultage.core;

import java.io.IOException;

/***
 * The interface for different implementations of direct message server.
 * 
 * @author Alfa Yohannis
 *
 */
public interface DirectMessageServer extends Runnable {

	/***
	 * Get the ip address or hostname of the direct message server.
	 * 
	 * @return ip or hostname
	 */
	public String getLocalAddress();

	/***
	 * Get the port of the direct message server.
	 * 
	 * @return port number
	 */
	public int getLocalPort();

	/***
	 * To start the direct message server.
	 */
	public void start();

	/***
	 * To shutdown the direct message server.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void shutdown() throws IOException, InterruptedException;

	/***
	 * To set the private key of the direct message server. It is used to decrypt
	 * received messages.
	 * 
	 * @param privateKey
	 */
	public void setPrivateKey(String privateKey);

}
