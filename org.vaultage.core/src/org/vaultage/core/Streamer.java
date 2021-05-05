package org.vaultage.core;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.vaultage.util.VaultageEncryption;

public class Streamer extends Thread {

	private InetSocketAddress receiverAddress;
	private String senderPrivateKey;
	private String receiverPublicKey;
	private byte[] data;
	private OutputStream outputStream;
	private Socket streamerSocket;
	private boolean isEncrypted = false;

	public Streamer(InetSocketAddress receiverAddress, byte[] data, String senderPrivateKey, String receiverPublicKey) {
		this.receiverAddress = receiverAddress;
		this.data = data;
		this.senderPrivateKey = senderPrivateKey;
		this.receiverPublicKey = receiverPublicKey;
	}

	@Override
	public void run() {

		try {
			streamerSocket = new Socket();
			streamerSocket.connect(this.receiverAddress);

			outputStream = streamerSocket.getOutputStream();

			if (isEncrypted) {
				byte[] encryptedBufferedData = VaultageEncryption.doubleEncrypt(data, receiverPublicKey,
						senderPrivateKey);
				outputStream.write(encryptedBufferedData);
			} else {
				outputStream.write(data);
			}

			// double EOTs as a marker to end socket listening at the receiver
			byte[] temp = new byte[] { (byte) (char) 4, (byte) (char) 4 };
			outputStream.write(temp);
			outputStream.flush();

			outputStream.close();
			streamerSocket.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void startStreaming() {
		this.start();
	}

	public void stopStreaming() throws IOException {
		if (outputStream != null)
			outputStream.close();
		if (streamerSocket != null && !streamerSocket.isClosed())
			streamerSocket.close();

		System.out.println("Streaming ended!");
	}

	public boolean isEncrypted() {
		return isEncrypted;
	}

	public void setEncrypted(boolean isEncrypted) {
		this.isEncrypted = isEncrypted;
	}
}
