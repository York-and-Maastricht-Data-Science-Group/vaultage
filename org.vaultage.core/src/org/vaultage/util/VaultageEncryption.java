package org.vaultage.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/***
 * A class that is responsible for encryption and decryption in Vaultage. This
 * class use RSA for generating keys and RSA/ECB/OAEPWithSHA-256AndMGF1Padding
 * for cipher. Since the cipher's default implementation in JRE can only encrypt
 * using public keys (cannot be done using private keys), double encryption
 * cannot be implemented. Therefore, Bouncy Castle library is used as the
 * provider for ciphering. Apparently, the library works well for encryption and
 * decryption using private keys and public keys.
 * 
 * @author Alfa Yohannis
 *
 */

public class VaultageEncryption {

	private static Provider bouncyCastleProvider = initBouncyCastleProvider();

	public static final String KEY_GENERATOR_ALGORITHM = "RSA";

//	public static final String CIPHER_ALGORITHM = "RSA/ECB/PKCS1Padding";
//	public static final String CIPHER_ALGORITHM = "RSA/ECB/OAEPWithSHA-1AndMGF1Padding";
	public static final String CIPHER_ALGORITHM = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
//	public static final String CIPHER_ALGORITHM = "RSA";
	public static final int MAXIMUM_PLAIN_MESSAGE_LENGTH = 190;
	public static final int MAXIMUM_ENCRYPTED_MESSAGE_LENGTH = 256;
	public static final int KEY_LENGTH = 2048;

	/***
	 * A test/demo for encryption and decryption
	 * 
	 * @param args
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws NoSuchPaddingException
	 * @throws IOException
	 */
	public static void main(String[] args)
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, IllegalBlockSizeException,
			BadPaddingException, NoSuchPaddingException, IOException {

		String message = "01234567890123456789012345678901234567890123456789"
				+ "01234567890123456789012345678901234567890123456789"
				+ "01234567890123456789012345678901234567890123456789" + "01234567890123456789012345678901234567890";

		byte[] input = (message).getBytes();
		System.out.println("Plain text length: " + input.length);
		int length = KEY_LENGTH;

		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(VaultageEncryption.KEY_GENERATOR_ALGORITHM);
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

//		// encrypt with private key
//		String encryptedMessage = encryptNoLoop(message, privateKey);
//		System.out.println("Encrypted Message: " + encryptedMessage);
//
//		System.out.println("Encrypted text length: " + encryptedMessage.length());
//		// decrypt with public key
//		String decryptedMessage = decryptNoLoop(encryptedMessage, publicKey);
//		System.out.println("Decrypted Message: " + decryptedMessage);

		// encrypt with private key
		String encryptedMessage3 = encrypt(message, privateKey);
		System.out.println("Encrypted Message: " + encryptedMessage3);

		System.out.println("Encrypted text length: " + encryptedMessage3.length());
		// decrypt with public key
		String decryptedMessage3 = decrypt(encryptedMessage3, publicKey);
		System.out.println("Decrypted Message: " + decryptedMessage3);

		System.out.println();

		// encrypt with public key
		String encryptedMessage2 = encrypt(message, publicKey);
		System.out.println("Encrypted Message: " + encryptedMessage2);

		System.out.println("Encrypted text length: " + encryptedMessage3.length());
		// decrypt with private key
		String decryptedMessage2 = decrypt(encryptedMessage2, privateKey);
		System.out.println("Decrypted Message: " + decryptedMessage2);

	}

	/***
	 * initialisation to use Bouncy Castle as the provider for en(de)cryption
	 * 
	 * @return
	 */
	public static Provider initBouncyCastleProvider() {
		Provider p = bouncyCastleProvider;
		if (Security.getProvider("BC") == null) {
			p = new org.bouncycastle.jce.provider.BouncyCastleProvider();
			Security.addProvider(p);
		}
		return p;
	}

	/***
	 * A method to generate key pair
	 * 
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static KeyPair generateKeys() throws NoSuchAlgorithmException {
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(VaultageEncryption.CIPHER_ALGORITHM);
		keyPairGen.initialize(VaultageEncryption.KEY_LENGTH);
		return keyPairGen.generateKeyPair();
	}

	/***
	 * Get the Base64 String private key from a key pair
	 * 
	 * @param keyPair
	 * @return
	 */
	public static String getPrivateKey(KeyPair keyPair) {
		return Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
	}

	/***
	 * Get the Base64 String public key from a key pair
	 * 
	 * @param keyPair
	 * @return
	 */
	public static String getPublicKey(KeyPair keyPair) {
		return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
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
	 * A method to double encrypt a message using Strings of receiver public key and
	 * sender private key.
	 * 
	 * @param plainMessage
	 * @param receiverPublicKeyString
	 * @param senderPrivateKeyString
	 * @return
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws IOException
	 * @throws InvalidKeySpecException
	 */
	public static String doubleEncrypt(String plainMessage, String receiverPublicKeyString,
			String senderPrivateKeyString)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException,
			NoSuchAlgorithmException, NoSuchPaddingException, IOException, InvalidKeySpecException {

		KeyFactory keyFactory = KeyFactory.getInstance(VaultageEncryption.CIPHER_ALGORITHM);
		byte[] privateKeyBytes = Base64.getDecoder().decode(senderPrivateKeyString);
		PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
		PrivateKey senderPrivateKey = keyFactory.generatePrivate(privateKeySpec);

		byte[] publicKeyBytes = Base64.getDecoder().decode(receiverPublicKeyString);
		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
		PublicKey receiverPublicKey = keyFactory.generatePublic(publicKeySpec);

		return doubleEncrypt(plainMessage, receiverPublicKey, senderPrivateKey);
	}

	/***
	 * A method to double encrypt a message using sender private key and receiver
	 * public key.
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
		String encryptedMessage1 = encrypt(plainMessage, (Key) senderPrivateKey);
//		System.out.println("intermediateEncryptedMessage: " + encryptedMessage1);
		return encrypt(encryptedMessage1, (Key) receiverPublicKey);
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

		Cipher cipher = Cipher.getInstance(VaultageEncryption.CIPHER_ALGORITHM);
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

	public static String doubleDecrypt(String encryptedMessage, String senderPublicKeyString,
			String receiverPrivateKeyString)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException,
			NoSuchAlgorithmException, NoSuchPaddingException, IOException, InvalidKeySpecException {

		KeyFactory keyFactory = KeyFactory.getInstance(VaultageEncryption.CIPHER_ALGORITHM);
		byte[] privateKeyBytes = Base64.getDecoder().decode(receiverPrivateKeyString);
		PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
		PrivateKey receiverPrivateKey = keyFactory.generatePrivate(privateKeySpec);

		byte[] publicKeyBytes = Base64.getDecoder().decode(senderPublicKeyString);
		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
		PublicKey senderPublicKey = keyFactory.generatePublic(publicKeySpec);

		return doubleDecrypt(encryptedMessage, senderPublicKey, receiverPrivateKey);
	}

	/***
	 * A method to double decrypt an encrypted message using receiver private key
	 * and sender public key .
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
		String decryptedMessage1 = decrypt(encryptedMessage, (Key) receiverPrivateKey);
//		System.out.println("intermediateDecryptedMessage: " + decryptedMessage1);
		return decrypt(decryptedMessage1, (Key) senderPublicKey);
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

		Cipher cipher = Cipher.getInstance(VaultageEncryption.CIPHER_ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, key);
		StringBuilder sb = new StringBuilder();
		ByteArrayInputStream in = new ByteArrayInputStream(Base64.getDecoder().decode(encryptedMessage));
		byte[] buffer = new byte[MAXIMUM_ENCRYPTED_MESSAGE_LENGTH];
		int len;
//		int count = 1;
		while ((len = in.read(buffer)) > 0) {
			byte[] cipherText = cipher.doFinal(buffer, 0, len);
			String temp = new String(cipherText, StandardCharsets.UTF_8);
			sb.append(temp);
//			System.out.println("Message Part-" + count + ": " + temp);
//			count++;
		}
		return sb.toString();

	}

	/***
	 * This method is identify the maximum length of a message that can be encrypted
	 * without breaking the message into smaller chunks
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
	public static String encryptNoLoop(String plainMessage, Key key)
			throws InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException,
			UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException {

		Cipher cipher = Cipher.getInstance(VaultageEncryption.CIPHER_ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] cipherText = cipher.doFinal(plainMessage.getBytes());
		out.write(cipherText);
		return Base64.getEncoder().encodeToString(out.toByteArray());
	}

	/***
	 * This method is identify the maximum length of an encrypted message that can
	 * be decrypted without breaking the message into smaller chunks
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
	public static String decryptNoLoop(String encryptedMessage, Key key)
			throws InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException,
			UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException {

		Cipher cipher = Cipher.getInstance(VaultageEncryption.CIPHER_ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, key);
		StringBuilder sb = new StringBuilder();
		byte[] input = Base64.getDecoder().decode(encryptedMessage);
		byte[] cipherText = cipher.doFinal(input);
		String temp = new String(cipherText, StandardCharsets.UTF_8);
		sb.append(temp);
		return sb.toString();

	}
}
