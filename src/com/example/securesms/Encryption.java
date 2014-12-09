package com.example.securesms;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import android.util.Base64;
import android.util.Log;

public class Encryption {

	private static final String TAG = Encryption.class.getSimpleName();
	private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
	private static String DELIMITER = "]";
	private static int KEY_LENGTH = 256;
	private static int ITERATION_COUNT = 1000;
	private static final int PKCS5_SALT_LENGTH = 8;
	private static SecureRandom random = new SecureRandom();

	protected Encryption() {
	}

	public static SecretKey deriveKeyPad(String password) {
		try {
			long start = System.currentTimeMillis();
			byte[] keyBytes = new byte[KEY_LENGTH / 8];
			Arrays.fill(keyBytes, (byte) 0x0);
			byte[] passwordBytes = password.getBytes("UTF-8");
			int length = passwordBytes.length < keyBytes.length ? passwordBytes.length
					: keyBytes.length;
			System.arraycopy(passwordBytes, 0, keyBytes, 0, length);

			SecretKey result = new SecretKeySpec(keyBytes, "AES");
			long elapsed = System.currentTimeMillis() - start;
			Log.d(TAG, String.format("Padding key derivation took %d [ms].",
					elapsed));

			return result;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] generateIv(int length) {
		byte[] b = new byte[length];
		random.nextBytes(b);

		return b;
	}

	public static byte[] generateSalt() {
		byte[] b = new byte[PKCS5_SALT_LENGTH];
		random.nextBytes(b);

		return b;
	}

	public static String encryptPkcs12(String plaintext, SecretKey key,
			byte[] salt) {
		try {
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);

			PBEParameterSpec pbeSpec = new PBEParameterSpec(salt,
					ITERATION_COUNT);
			cipher.init(Cipher.ENCRYPT_MODE, key, pbeSpec);
			Log.d(TAG, "Cipher IV: " + toHex(cipher.getIV()));
			byte[] cipherText = cipher.doFinal(plaintext.getBytes("UTF-8"));

			return String.format("%s%s%s", toBase64(salt), DELIMITER,
					toBase64(cipherText));
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String encrypt(String plaintext, SecretKey key, byte[] salt) {
		try {
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);

			byte[] iv = generateIv(cipher.getBlockSize());

			Log.d(TAG, "IV: " + toHex(iv));
			IvParameterSpec ivParams = new IvParameterSpec(iv);
			cipher.init(Cipher.ENCRYPT_MODE, key, ivParams);
			Log.d(TAG, "Cipher IV: "
					+ (cipher.getIV() == null ? null : toHex(cipher.getIV())));

			byte[] cipherText = cipher.doFinal(plaintext.getBytes("UTF-8"));

			if (salt != null) {
				return String.format("%s%s%s%s%s", toBase64(salt), DELIMITER,
						toBase64(iv), DELIMITER, toBase64(cipherText));
			}
			String encryptedText = String.format("%s%s%s", toBase64(iv),
					DELIMITER, toBase64(cipherText));
			return encryptedText;
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String toHex(byte[] bytes) {
		StringBuffer buff = new StringBuffer();
		for (byte b : bytes) {
			buff.append(String.format("%02X", b));
		}

		return buff.toString();
	}

	public static String toBase64(byte[] bytes) {
		return Base64.encodeToString(bytes, Base64.NO_WRAP);
	}

	public static byte[] fromBase64(String base64) {
		return Base64.decode(base64, Base64.NO_WRAP);
	}

	public static String decrypt(byte[] cipherBytes, SecretKey key, byte[] iv) {
		try {
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
			IvParameterSpec ivParams = new IvParameterSpec(iv);
			cipher.init(Cipher.DECRYPT_MODE, key, ivParams);
			Log.d(TAG, "Cipher IV: " + toHex(cipher.getIV()));
			byte[] plaintext = cipher.doFinal(cipherBytes);
			String plainrStr = new String(plaintext, "UTF-8");

			return plainrStr;
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String decryptNoSalt(String ciphertext, SecretKey key) {
		String[] fields = ciphertext.split(DELIMITER);
		if (fields.length != 2) {
			throw new IllegalArgumentException("Invalid encypted text format");
		}
		byte[] iv = fromBase64(fields[0]);
		byte[] cipherBytes = fromBase64(fields[1]);

		return decrypt(cipherBytes, key, iv);
	}

}
