package org.rdbd.demo.fairnet.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class EncryptionUtil {

	public static final String ALGORITHM = "RSA";
	public static final int MAXIMUM_PLAIN_MESSAGE_LENGTH = 53;
	public static final int MAXIMUM_ENCRYPTED_MESSAGE_LENGTH = 64;
	public static final int KEY_LENGTH = 512;

	public static void main(String[] args)
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, IllegalBlockSizeException,
			BadPaddingException, NoSuchPaddingException, IOException {

		String message = "Not all sequences of bytes can be mapped to characters in UTF-16. "
				+ "Not all sequences of bytes can be mapped to characters in UTF-16. "
				+ "Not all sequences of bytes can be mapped to characters in UTF-16. "
				+ "Not all sequences of bytes can be mapped to characters in UTF-16. "
				+ "Not all sequences of bytes can be mapped to characters in UTF-16. "
				+ "Not all sequences of bytes can be mapped to characters in UTF-16. "
				+ "Not all sequences of bytes can be mapped to characters in UTF-16. "
				+ "Not all sequences of bytes can be mapped to characters in UTF-16. "
				+ "Not all sequences of bytes can be mapped to characters in UTF-16. "
				+ "Not all sequences of bytes can be mapped to characters in UTF-16. ";
		byte[] input = (message).getBytes();
		System.out.println(input.length);
		int length = KEY_LENGTH;

		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
		keyPairGen.initialize(length);

		KeyPair pair = keyPairGen.generateKeyPair();
		PublicKey publicKey = pair.getPublic();
		PrivateKey privateKey = pair.getPrivate();

		String privateKeyString = Base64.getEncoder().encodeToString(privateKey.getEncoded());
		String publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());

		// original
		System.out.println("Private key: " + privateKeyString);
		System.out.println("Public key: " + publicKeyString);
		System.out.println("Original Message: " + message);

		// encrypt with private key
		String encryptedMessage = encrypt(message, privateKey);
		System.out.println("Encrypted Message: " + encryptedMessage);

		// decrypt with public key
		String decryptedMessage = decrypt(encryptedMessage, publicKey);
		System.out.println("Decrypted Message: " + decryptedMessage);

		// encrypt with public key
		String encryptedMessage2 = encrypt(message, publicKey);
		System.out.println("Encrypted Message: " + encryptedMessage2);

		// decrypt with private key
		String decryptedMessage2 = decrypt(encryptedMessage2, privateKey);
		System.out.println("Decrypted Message: " + decryptedMessage2);

	}

	/***
	 * A method to encrypt a message using receiver public key.
	 * 
	 * @param plainMessage
	 * @param receiverPublicKey
	 * @return
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws IOException
	 */
	public static String encrypt(String plainMessage, PublicKey receiverPublicKey)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException,
			NoSuchAlgorithmException, NoSuchPaddingException, IOException {
		return encrypt(plainMessage, (Key) receiverPublicKey);
	}

	/***
	 * A method to encrypt a message using sender private key.
	 * 
	 * @param plainMessage
	 * @param senverPrivateKey
	 * @return
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws IOException
	 */
	public static String encrypt(String plainMessage, PrivateKey senverPrivateKey)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException,
			NoSuchAlgorithmException, NoSuchPaddingException, IOException {
		return encrypt(plainMessage, (Key) senverPrivateKey);
	}

	/***
	 * A method to double encrypt a message using receiver public key and sender
	 * private key.
	 * 
	 * @param plainMessage
	 * @param receiverPublicKey
	 * @param senderPrivateKey
	 * @return
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws IOException
	 */
	public static String doubleEncrypt(String plainMessage, PublicKey receiverPublicKey, PrivateKey senderPrivateKey)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException,
			NoSuchAlgorithmException, NoSuchPaddingException, IOException {
		String encryptedMessage1 = encrypt(plainMessage, (Key) receiverPublicKey);
		return encrypt(encryptedMessage1, (Key) senderPrivateKey);
	}

	/***
	 * A method to encrypt a message using receiver public key or sender private
	 * key.
	 * 
	 * @param plainMessage
	 * @param key
	 * @return
	 * @throws InvalidKeyException
	 * @throws IOException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 */
	public static String encrypt(String plainMessage, Key key)
			throws InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException,
			UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException {

		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, key);
//		StringBuilder sb = new StringBuilder();
		ByteArrayInputStream in = new ByteArrayInputStream(plainMessage.getBytes(StandardCharsets.UTF_8));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[MAXIMUM_PLAIN_MESSAGE_LENGTH];
		int len;
		while ((len = in.read(buffer)) > 0) {
			byte[] cipherText = cipher.doFinal(buffer, 0, len);
			out.write(cipherText);
		}
//		return out.toByteArray();
		return Base64.getEncoder().encodeToString(out.toByteArray());

	}

	/***
	 * A method to decrypt an encrypted messaged using receiver receiver private
	 * key.
	 * 
	 * @param encryptedMessage
	 * @param receiverPrivateKey
	 * @return
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws IOException
	 */
	public static String decrypt(String encryptedMessage, PrivateKey receiverPrivateKey)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException,
			NoSuchAlgorithmException, NoSuchPaddingException, IOException {
		return decrypt(encryptedMessage, (Key) receiverPrivateKey);
	}

	/***
	 * A method to decrypt an encrypted message using receiver sender public key.
	 * 
	 * @param encryptedMessage
	 * @param senderPublicKey
	 * @return
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws IOException
	 */
	public static String decrypt(String encryptedMessage, PublicKey senderPublicKey)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException,
			NoSuchAlgorithmException, NoSuchPaddingException, IOException {
		return decrypt(encryptedMessage, (Key) senderPublicKey);
	}

	/***
	 * A method to double decrypt an encrypted message using sender public key and
	 * receiver private key.
	 * 
	 * @param encryptedMessage
	 * @param senderPublicKey
	 * @param receiverPrivateKey
	 * @return
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws IOException
	 */
	public static String doubleDecrypt(String encryptedMessage, PublicKey senderPublicKey,
			PrivateKey receiverPrivateKey) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
			UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, IOException {
		String decryptedMessage1 = decrypt(encryptedMessage, (Key) senderPublicKey);
		return decrypt(decryptedMessage1, (Key) receiverPrivateKey);
	}

	/***
	 * A method to decrypt an encrypted message using sender public key or receiver
	 * private key.
	 * 
	 * @param encryptedMessage
	 * @param key
	 * @return
	 * @throws InvalidKeyException
	 * @throws IOException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 */
	public static String decrypt(String encryptedMessage, Key key)
			throws InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException,
			UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException {

		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, key);
		StringBuilder sb = new StringBuilder();
		ByteArrayInputStream in = new ByteArrayInputStream(Base64.getDecoder().decode(encryptedMessage));
		byte[] buffer = new byte[MAXIMUM_ENCRYPTED_MESSAGE_LENGTH];
//		int len;
//		int count = 1;
//		while ((len = in.read(buffer)) > 0) {
		while (in.read(buffer) > 0) {
			byte[] cipherText = cipher.doFinal(buffer);
			String temp = new String(cipherText, StandardCharsets.UTF_8);
			sb.append(temp);
//			System.out.println("Message Part-" + count + ": " + temp);
//			count++;
		}
		return sb.toString();

	}

}
