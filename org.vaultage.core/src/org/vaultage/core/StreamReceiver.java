package org.vaultage.core;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

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
	private byte[] data;

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
			byte[] receivedData = new byte[20480];

			isListening = true;
			while (isListening) {
				Socket socket = receiverSocket.accept();
				InputStream is = socket.getInputStream();
				int length = 0;

				
				while ((length = is.read(receivedData)) > -1) {
					if (receivedData[length - 2] == (byte) (char) 4
							&& receivedData[length - 1] == (byte) (char) 4) {
						isListening = false;
						
						byte[] copy = Arrays.copyOf(receivedData, length - 2); 
						outputStream.write(receivedData, 0, copy.length);
						outputStream.flush();
						
						break;
					}

					outputStream.write(receivedData, 0, length);
					outputStream.flush();
				}
				
				if (isEncrypted) {
					data = VaultageEncryption.doubleDecrypt(outputStream.toByteArray(), senderPublicKey,
							receiverPrivateKey);
				} else {
					data = outputStream.toByteArray();
				}
				
				is.close();
			}
			outputStream.close();
			receiverSocket.close();

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
//			Object outputValue = bytesToOutputType.convert(outputStream);
			Object outputValue = bytesToOutputType.convert(data);
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
