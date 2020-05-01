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

	public static String encrypt(String plainMessage, PublicKey publicKey)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException,
			NoSuchAlgorithmException, NoSuchPaddingException, IOException {
		return encrypt(plainMessage, (Key) publicKey);
	}

	public static String encrypt(String plainMessage, PrivateKey privateKey)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException,
			NoSuchAlgorithmException, NoSuchPaddingException, IOException {
		return encrypt(plainMessage, (Key) privateKey);
	}

	public static String encrypt(String plainMessage, Key privateKey)
			throws InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException,
			UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException {

		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, privateKey);
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

	public static String decrypt(String encryptedMessage, PrivateKey privateKey)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException,
			NoSuchAlgorithmException, NoSuchPaddingException, IOException {
		return decrypt(encryptedMessage, (Key) privateKey);
	}

	public static String decrypt(String encryptedMessage, PublicKey publicKey)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException,
			NoSuchAlgorithmException, NoSuchPaddingException, IOException {
		return decrypt(encryptedMessage, (Key) publicKey);
	}

	public static String decrypt(String encryptedMessage, Key publicKey)
			throws InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException,
			UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException {

		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, publicKey);
		StringBuilder sb = new StringBuilder();
		ByteArrayInputStream in = new ByteArrayInputStream(Base64.getDecoder().decode(encryptedMessage));
		byte[] buffer = new byte[MAXIMUM_ENCRYPTED_MESSAGE_LENGTH];
		int len;
		int count = 1;
		while ((len = in.read(buffer)) > 0) {
			byte[] cipherText = cipher.doFinal(buffer);
			String temp = new String(cipherText, StandardCharsets.UTF_8);
			sb.append(temp);
			System.out.println("Message Part-" + count + ": " + temp);
			count++;
		}
		return sb.toString();

	}

}
