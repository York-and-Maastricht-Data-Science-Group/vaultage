package org.vaultage.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

import org.fusesource.hawtbuf.ByteArrayInputStream;
import org.vaultage.util.VaultageEncryption;

public class Streamer extends Thread {

	/** experiment with the size of the buffer to get the best result */
	// 1024, 1280, 3584, 44100,
	// 88200, etc.

	private InetSocketAddress receiverAddress;
	private String senderPrivateKey;
	private String receiverPublicKey;
	private InputStream data;
	private InputStream inputStream;
	private OutputStream outputStream;
	private Socket streamerSocket;
	private boolean isEncrypted = false;

	public Streamer(InetSocketAddress receiverAddress, InputStream data, String senderPrivateKey, String receiverPublicKey) {
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
			inputStream = data;

			byte[] bufferedData = new byte[VaultageEncryption.MAXIMUM_PLAIN_MESSAGE_LENGTH];
			byte[] encryptedBufferedData = new byte[VaultageEncryption.MAXIMUM_ENCRYPTED_MESSAGE_LENGTH];
			byte[] plainData;

			outputStream = streamerSocket.getOutputStream();
			int length = 0;
//			int count = 0;
			if (isEncrypted) {
				while ((length = inputStream.read(bufferedData)) > -1) {
					if (length < bufferedData.length) {
						plainData = Arrays.copyOf(bufferedData, length);
					} else {
						plainData = bufferedData;
					}
					encryptedBufferedData = VaultageEncryption.doubleEncrypt(plainData, receiverPublicKey,
							senderPrivateKey);
////					if (count == 0) {
//					System.out.println("Send 1 = " + plainData.length + " = " + new String(plainData));
//					System.out.println(
//							"Send 2 = " + encryptedBufferedData.length + " = " + new String(encryptedBufferedData));
////					}
////					count++;
					outputStream.write(encryptedBufferedData, 0, encryptedBufferedData.length);
					outputStream.flush();
				}
			} else {
				while ((length = inputStream.read(bufferedData)) > -1) {
					outputStream.write(bufferedData, 0, length);
					outputStream.flush();
				}
			}

			// double EOTs as a marker to end socket listening at the receiver
			byte[] temp = new byte[] { (byte) (char) 4, (byte) (char) 4 };
			outputStream.write(temp);
			outputStream.flush();

			outputStream.close();
			inputStream.close();
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
		if (inputStream != null)
			inputStream.close();
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
