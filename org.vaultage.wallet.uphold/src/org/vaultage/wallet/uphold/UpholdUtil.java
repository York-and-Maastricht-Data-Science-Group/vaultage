package org.vaultage.wallet.uphold;

import java.util.Base64;
import java.util.Random;

public class UpholdUtil {

	public static String credential(String clientId, String clientSecret) {
		String concat = clientId + ":" + clientSecret;
		String encoded = Base64.getEncoder().encodeToString(concat.getBytes());
		return encoded;
	}

	public static String randomBytes() {

		Random rd = new Random();
		byte[] arr = new byte[8];
		rd.nextBytes(arr);
		StringBuilder result = new StringBuilder();
		for (byte aByte : arr) {
			result.append(String.format("%02x", aByte));
		}
		return result.toString();
	}
}
