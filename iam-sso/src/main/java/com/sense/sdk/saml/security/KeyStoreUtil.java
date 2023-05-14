package com.sense.sdk.saml.security;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;

/**
 * Simple utility for managing common KeyStore tasks.
 *
 * @author LiangRiLu
 * @Time 2019年12月14日下午1:42:33
 */
public class KeyStoreUtil {
	/**
	 * Get a KeyStore object given the keystore filename and password.
	 */
	public KeyStore getKeyStore(String filename, String password) throws KeyStoreException {
		KeyStore result = KeyStore.getInstance(KeyStore.getDefaultType());

		try {
			InputStream is = this.getClass().getResourceAsStream(filename);
			result.load(is, password.toCharArray());
			is.close();
		} catch (Exception ex) {
			System.out.println("Failed to read keystore:");
			ex.printStackTrace();
		}

		return result;
	}

	/**
	 * Helper method that converts a single byte to a hex string representation.
	 *
	 * @param b
	 *            byte Byte to convert
	 * @return StringBuffer with the two-digit hex string
	 */
	public static void appendHexValue(StringBuffer buffer, byte b) {
		int[] digits = { (b >>> 4) & 0x0F, b & 0x0F };
		for (int d = 0; d < digits.length; ++d) {
			int increment = (int) ((digits[d] < 10) ? '0' : ('a' - 10));
			buffer.append((char) (digits[d] + increment));
		}
	}

	/**
	 * Helper that appends a hex representation of a byte array to an existing
	 * StringBuffer.
	 */
	public static void appendHexValue(StringBuffer buffer, byte[] bytes) {
		for (int i = 0; i < bytes.length; ++i)
			appendHexValue(buffer, bytes[i]);
	}

}
