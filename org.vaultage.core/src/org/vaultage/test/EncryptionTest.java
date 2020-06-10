package org.vaultage.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.junit.Test;
import org.vaultage.util.VaultageEncryption;

/***
 * A class to test the encryption and decryption of EncryptionUtil.java
 * 
 * @author Alfa Yohannis
 *
 */
public class EncryptionTest {

	private KeyPair receiverKeyPair;
	private KeyPair senderKeyPair;
	private KeyFactory keyFactory;

	public EncryptionTest() throws NoSuchAlgorithmException {
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(VaultageEncryption.KEY_GENERATOR_ALGORITHM);
		keyFactory = KeyFactory.getInstance(VaultageEncryption.KEY_GENERATOR_ALGORITHM);
		keyPairGen.initialize(VaultageEncryption.KEY_LENGTH);

		receiverKeyPair = keyPairGen.generateKeyPair();
		senderKeyPair = keyPairGen.generateKeyPair();
	}

	/***
	 * Test encryption and decryption with private and public keys are firstly saved
	 * to text files and then re-loaded to perform encryption and decryption.
	 * 
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws NoSuchPaddingException
	 */
	@Test
	public void testDecryptionWithKeysFromFiles() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException {
		String message = "01234567890123456789012345678901234567890123456789"
				+ "01234567890123456789012345678901234567890123456789"
				+ "01234567890123456789012345678901234567890123456789"
				+ "01234567890123456789012345678901234567890123456789";

		PublicKey publicKey = receiverKeyPair.getPublic();
		PrivateKey privateKey = receiverKeyPair.getPrivate();

		String privateKeyString = Base64.getEncoder().encodeToString(privateKey.getEncoded());
		String publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());

		String privateKeyPath = "keys" + File.separator + "private.key";
		String publicKeyPath = "keys" + File.separator + "public.key";
		Files.write(Paths.get(privateKeyPath), privateKeyString.getBytes());
		Files.write(Paths.get(publicKeyPath), publicKeyString.getBytes());

		String loadedPrivateKeyString = new String(Files.readAllBytes(Paths.get(privateKeyPath)));
		String loadedPublicKeyString = new String(Files.readAllBytes(Paths.get(publicKeyPath)));

		byte[] privateKeyBytes = Base64.getDecoder().decode(loadedPrivateKeyString);
		PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
		PrivateKey loadedPrivateKey = keyFactory.generatePrivate(privateKeySpec);

		byte[] publicKeyBytes = Base64.getDecoder().decode(loadedPublicKeyString);
		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
		PublicKey loadedPublicKey = keyFactory.generatePublic(publicKeySpec);

		String encryptedMessage = VaultageEncryption.encrypt(message, loadedPublicKey);
		String decryptedMessage = VaultageEncryption.decrypt(encryptedMessage, loadedPrivateKey);

		assertEquals(privateKeyString, loadedPrivateKeyString);
		assertEquals(publicKeyString, loadedPublicKeyString);
		assertEquals(message, decryptedMessage);
	}
	
	@Test
	public void testDoubleEncryptionWithKeysFromFiles()
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException {
		String message = "01234567890123456789012345678901234567890123456789"
				+ "01234567890123456789012345678901234567890123456789"
				+ "01234567890123456789012345678901234567890123456789"
				+ "01234567890123456789012345678901234567890123456789";

		// RECEIVER
		PublicKey receiverPublicKey = receiverKeyPair.getPublic();
		PrivateKey receiverPrivateKey = receiverKeyPair.getPrivate();

		String receiverPrivateKeyString = Base64.getEncoder().encodeToString(receiverPrivateKey.getEncoded());
		String receiverPublicKeyString = Base64.getEncoder().encodeToString(receiverPublicKey.getEncoded());

		String receiverPrivateKeyPath = "keys" + File.separator + "receiver.private.key";
		String receiverPublicKeyPath = "keys" + File.separator + "receiver.public.key";
		Files.write(Paths.get(receiverPrivateKeyPath), receiverPrivateKeyString.getBytes());
		Files.write(Paths.get(receiverPublicKeyPath), receiverPublicKeyString.getBytes());

		String loadedReceiverPrivateKeyString = new String(Files.readAllBytes(Paths.get(receiverPrivateKeyPath)));
		String loadedReceiverPublicKeyString = new String(Files.readAllBytes(Paths.get(receiverPublicKeyPath)));

		byte[] receiverPrivateKeyBytes = Base64.getDecoder().decode(loadedReceiverPrivateKeyString);
		PKCS8EncodedKeySpec receiverPrivateKeySpec = new PKCS8EncodedKeySpec(receiverPrivateKeyBytes);
		PrivateKey receiverLoadedPrivateKey = keyFactory.generatePrivate(receiverPrivateKeySpec);

		byte[] receiverPublicKeyBytes = Base64.getDecoder().decode(loadedReceiverPublicKeyString);
		X509EncodedKeySpec receiverPublicKeySpec = new X509EncodedKeySpec(receiverPublicKeyBytes);
		PublicKey receiverLoadedPublicKey = keyFactory.generatePublic(receiverPublicKeySpec);

		// SENDER
		PublicKey senderPublicKey = senderKeyPair.getPublic();
		PrivateKey senderPrivateKey = senderKeyPair.getPrivate();

		String senderPrivateKeyString = Base64.getEncoder().encodeToString(senderPrivateKey.getEncoded());
		String senderPublicKeyString = Base64.getEncoder().encodeToString(senderPublicKey.getEncoded());

		String senderPrivateKeyPath = "keys" + File.separator + "sender.private.key";
		String senderPublicKeyPath = "keys" + File.separator + "sender.public.key";
		Files.write(Paths.get(senderPrivateKeyPath), senderPrivateKeyString.getBytes());
		Files.write(Paths.get(senderPublicKeyPath), senderPublicKeyString.getBytes());

		String senderLoadedPrivateKeyString = new String(Files.readAllBytes(Paths.get(senderPrivateKeyPath)));
		String senderLoadedPublicKeyString = new String(Files.readAllBytes(Paths.get(senderPublicKeyPath)));

		byte[] senderPrivateKeyBytes = Base64.getDecoder().decode(senderLoadedPrivateKeyString);
		PKCS8EncodedKeySpec senderPrivateKeySpec = new PKCS8EncodedKeySpec(senderPrivateKeyBytes);
		PrivateKey senderLoadedPrivateKey = keyFactory.generatePrivate(senderPrivateKeySpec);

		byte[] senderPublicKeyBytes = Base64.getDecoder().decode(senderLoadedPublicKeyString);
		X509EncodedKeySpec senderPublicKeySpec = new X509EncodedKeySpec(senderPublicKeyBytes);
		PublicKey senderLoadedPublicKey = keyFactory.generatePublic(senderPublicKeySpec);

		// ENCRYPT / DECRYPT
		System.out.println("Original Message: " + message);
		String encryptedMessage = VaultageEncryption.doubleEncrypt(message, receiverLoadedPublicKey,
				senderLoadedPrivateKey);
		System.out.println("Double Encrypted Message: " + encryptedMessage);
		String decryptedMessage = VaultageEncryption.doubleDecrypt(encryptedMessage, senderLoadedPublicKey,
				receiverLoadedPrivateKey);
		System.out.println("Double Decrypted Message: " + decryptedMessage);

		assertEquals(message, decryptedMessage);
	}

	/***
	 * Test encryption and decryption with a short message input
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchPaddingException
	 * @throws IOException
	 */
	@Test
	public void testShortTextDecryption()
			throws NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
			UnsupportedEncodingException, NoSuchPaddingException, IOException {

		String message = "Foo Bar!";

		PublicKey publicKey = receiverKeyPair.getPublic();
		PrivateKey privateKey = receiverKeyPair.getPrivate();

		String privateKeyString = Base64.getEncoder().encodeToString(privateKey.getEncoded());
		String publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());

		// original
		System.out.println("Private key: " + privateKeyString);
		System.out.println("Public key: " + publicKeyString);
		System.out.println("Original Message: " + message);

		// encrypt with private key
		String encryptedMessage = VaultageEncryption.encrypt(message, publicKey);
		System.out.println("Encrypted Message: " + encryptedMessage);

		// decrypt with public key
		String decryptedMessage = VaultageEncryption.decrypt(encryptedMessage, privateKey);
		System.out.println("Decrypted Message: " + decryptedMessage);

		assertEquals(message, decryptedMessage);
	}

	/***
	 * Test encryption and decryption with a long message input. Message bytes are
	 * streamed and then decrypted per 64 bytes.
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchPaddingException
	 * @throws IOException
	 */
	@Test
	public void testLongDecryption() throws NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException, UnsupportedEncodingException, NoSuchPaddingException, IOException {

		String message = "01234567890123456789012345678901234567890123456789"
				+ "01234567890123456789012345678901234567890123456789"
				+ "01234567890123456789012345678901234567890123456789"
				+ "01234567890123456789012345678901234567890123456789";

		PublicKey publicKey = receiverKeyPair.getPublic();
		PrivateKey privateKey = receiverKeyPair.getPrivate();

		String privateKeyString = Base64.getEncoder().encodeToString(privateKey.getEncoded());
		String publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());

		// original
		System.out.println("Private key: " + privateKeyString);
		System.out.println("Public key: " + publicKeyString);
		System.out.println("Original Message: " + message);

		// encrypt with private key
		String encryptedMessage = VaultageEncryption.encrypt(message, publicKey);
		System.out.println("Encrypted Message: " + encryptedMessage);

		// decrypt with public key
		String decryptedMessage = VaultageEncryption.decrypt(encryptedMessage, privateKey);
		System.out.println("Decrypted Message: " + decryptedMessage);

		assertEquals(message, decryptedMessage);
	}
}
