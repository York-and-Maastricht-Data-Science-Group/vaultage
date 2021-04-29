package org.vaultage.core;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.vaultage.util.VaultageEncryption;

public class StreamReceiver extends Thread {

	private boolean isListening = false;
	private String localAddress;
	private int port;
	private ServerSocket receiverSocket;
	private String senderPublicKey;
	private String receiverPrivateKey;
	private boolean isEncrypted;
	private String token;
	private OnStreamingFinishedHandler onFinishedStreamingHandler;
	private Object returnValue;

	private BytesToOutputTypeConverter bytesToOutputType;
	private ByteArrayOutputStream outputStream;

	public StreamReceiver(String localAddress, int port, String senderPublicKey, String receiverPrivateKey,
			ByteArrayOutputStream outputStream) {
		this.localAddress = localAddress;
		this.port = port;
		this.senderPublicKey = senderPublicKey;
		this.receiverPrivateKey = receiverPrivateKey;
		this.outputStream = outputStream;
	}

	@Override
	public void run() {
		try {
			receiverSocket = new ServerSocket(port);
			byte[] receivedData = new byte[VaultageEncryption.MAXIMUM_DOUBLE_ENCRYPTED_MESSAGE_LENGTH];
			byte[] decryptedData = new byte[VaultageEncryption.MAXIMUM_PLAIN_MESSAGE_LENGTH];

			isListening = true;
			while (isListening) {
				Socket socket = receiverSocket.accept();
				InputStream is = socket.getInputStream();
				int length = 0;

				if (isEncrypted) {
					while ((length = is.read(receivedData)) > -1) {

						if (receivedData[length - 2] == (byte) (char) 4
								&& receivedData[length - 1] == (byte) (char) 4) {
							isListening = false;
							break;
						}

//						System.out.println("Receive 1 = " + receivedData.length + " = " + new String(receivedData));
						decryptedData = VaultageEncryption.doubleDecrypt(receivedData, senderPublicKey,
								receiverPrivateKey);
//						System.out.println("Receive 2 = " + decryptedData.length + " = " + new String(decryptedData));
						outputStream.write(decryptedData);
//						fos.write(decryptedData, 0, VaultageEncryption.MAXIMUM_PLAIN_MESSAGE_LENGTH);
						outputStream.flush();
					}
				} else {
					while ((length = is.read(receivedData)) > -1) {
						if (receivedData[length - 2] == (byte) (char) 4
								&& receivedData[length - 1] == (byte) (char) 4) {
							isListening = false;
							break;
						}

						outputStream.write(receivedData, 0, length);
						outputStream.flush();
					}
				}

				is.close();
			}
			outputStream.close();

			onFinishedStreaming();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void stopListening() {
		isListening = false;
	}

	public boolean isListening() {
		return isListening;
	}

	public void startListening() {
		this.start();
	}

	/**
	 * @return the isEncrypted
	 */
	public boolean isEncrypted() {
		return isEncrypted;
	}

	/**
	 * @param isEncrypted the isEncrypted to set
	 */
	public void setEncrypted(boolean isEncrypted) {
		this.isEncrypted = isEncrypted;
	}

	/**
	 * @return the token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * @param token the token to set
	 */
	public void setToken(String token) {
		this.token = token;
	}

	private void onFinishedStreaming() {
		if (bytesToOutputType != null) {
			Object outputValue = bytesToOutputType.convert(outputStream);
			this.onFinishedStreamingHandler.onStreamingFinished(outputValue);
		}
	}

	public void setBytesToOutputTypeConverter(BytesToOutputTypeConverter bytesToOutputType) {
		this.bytesToOutputType = bytesToOutputType;
	}
	
	public void setOnStreamingFinishedHandler(OnStreamingFinishedHandler onFinishedStreamingHandler) {
		this.onFinishedStreamingHandler = onFinishedStreamingHandler;
	}

	/**
	 * @return the returnValue
	 */
	public Object getReturnValue() {
		return returnValue;
	}

	/**
	 * @return the localAddress
	 */
	public String getLocalAddress() {
		return localAddress;
	}

}
