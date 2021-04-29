package org.vaultage.demo.fairnet;

import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

/***
 * Thanks to Sirkoto51 for the Audio file
 * '349179__sirkoto51__rpg-town-loop-2.wav' taken from
 * https://freesound.org/people/Sirkoto51/sounds/349179/
 */

public class AudioStreamer extends Thread {

	/** experiment with the size of the buffer to get the best result */
	public static final int BUFFER_SIZE = 1280; // 1024, 1280, 3584, 44100, 88200, etc.
	private DatagramSocket streamerDatagramSocket;
	private SourceDataLine sourceDataLine;
	private String filename;
	private InetSocketAddress receiverAddress;

	public AudioStreamer(InetSocketAddress receiverAddress, String filename) {
		this.receiverAddress =receiverAddress;
		this.filename = filename;
	}
	
	@Override
	public void run() {
		
		try {

			// get the file from the filename
			File soundFile = new File(filename);

			// get the audio stream of the file
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);

			// read the audio format
			AudioFormat audioFormat = audioInputStream.getFormat();

			// get data line info for playing the format
			DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);

			sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);

			// open the source data line for that format
			sourceDataLine.open(audioFormat);

			// set volume to minimum so that we can only hear it on the server, not locally
			FloatControl volumeControl = (FloatControl) sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
//			float volume = volumeControl.getMinimum()
//					+ (0.6f * (volumeControl.getMaximum() - volumeControl.getMinimum()));
			float volume = volumeControl.getMinimum();
			volumeControl.setValue(volume);

			/** Initialise socket connection */
			// set ip address and port
			streamerDatagramSocket = new DatagramSocket();
			DatagramPacket sendPacket;
			/****/

			// start using the source data line
			System.out.println("Start streaming ...");
			sourceDataLine.start();

			int bytesRead = 0;
			byte[] bufferedPlayData = new byte[BUFFER_SIZE];

			// while there is bytes to read from the audio stream
			while ((bytesRead = audioInputStream.read(bufferedPlayData, 0, bufferedPlayData.length)) > -1) {

				// write it to the source data line to play the stream
				sourceDataLine.write(bufferedPlayData, 0, bytesRead);

				/** also stream it to the server */
				sendPacket = new DatagramPacket(bufferedPlayData, bufferedPlayData.length, this.receiverAddress);
				streamerDatagramSocket.send(sendPacket);
				/***/

//				System.out.println(sendPacket.getData().length + ": " + Arrays.toString(sendPacket.getData()));
//				System.console();
			}
			
			this.stopStreaming();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void startStreaming() {
		this.start();
	}
	
	public void stopStreaming() {
		/** Close sockets */
		streamerDatagramSocket.close();
		/***/

		// remove the remaining data left in the source data line
		sourceDataLine.drain();

		// stop using the source data line
		sourceDataLine.close();

		System.out.println("Streaming ended!");
	}
}
