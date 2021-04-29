package org.vaultage.demo.fairnet;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

class AudioStreamReceiver extends Thread {

	private boolean isListening = false;
	private String localAddress;
	private int port;
	private DatagramSocket receiverDatagramSocket;
	private SourceDataLine sourceDataLine;

	public AudioStreamReceiver(String localAddress, int port) {
		this.localAddress = localAddress;
		this.port = port;
	}

	@Override
	public void run() {

		try {
			System.out.println("Start listening ...");

			int sampleRate = 44100;

			receiverDatagramSocket = new DatagramSocket(new InetSocketAddress(localAddress, port));
			receiverDatagramSocket.setSoTimeout(2000);

			/** 1024, 1280 for 16 000Hz and 3584 for 44 100Hz */
			/** experiment with the size of the buffer to get the best result */
			byte[] receivedData = new byte[1280];

			/** create the datagram packet for the receiveData */
			DatagramPacket receivePacket = new DatagramPacket(receivedData, receivedData.length);

			// PCM_SIGNED 44100.0 Hz, 16 bit, stereo (2 channels), 4 bytes/frame,
			// little-endian (not big-endian)
			AudioFormat format = new AudioFormat(sampleRate, 16, 2, true, false);

			// get the dataline info for that format
			DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);

			sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);

			// open source data line for that format
			sourceDataLine.open(format);

			// start using the source data line
			isListening = true;
			sourceDataLine.start();

			// while listening socket is not terminated, do
			while (isListening) {
				// receive the datagram packet from the client into the receive packet
				receiverDatagramSocket.receive(receivePacket);

				// write the data get from the received packet to the source data line to play
				// the audio stream
				sourceDataLine.write(receivePacket.getData(), 0, receivePacket.getData().length);
			}
		} catch (SocketTimeoutException e) {
			System.out.println("No more incoming stream");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			isListening = false;
			if (sourceDataLine != null) {
				// remove the remaining data left in the source data line
				sourceDataLine.drain();
				// stop using the source data line
				sourceDataLine.close();
			}
			if (receiverDatagramSocket != null) {
				/** close socket */
				receiverDatagramSocket.close();
			}
		}

		System.out.println("Listening terminated!");
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

}
