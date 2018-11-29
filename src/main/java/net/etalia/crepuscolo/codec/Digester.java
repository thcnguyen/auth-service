package net.etalia.crepuscolo.codec;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static javax.crypto.Cipher.DECRYPT_MODE;
import static javax.crypto.Cipher.ENCRYPT_MODE;

public class Digester {

	private static final Base62 base62 = new Base62();
	private static final String BLOWFISH = "Blowfish";
	private static final String SHA1 = "SHA1";
	private static final String AES = "AES";
	private static final String MD5 = "MD5";
	private static final String HMAC_SHA256 = "HmacSHA256";
	private SecretKeySpec aes128SecretKey;
	private SecretKeySpec sha256SecretKey;
	private SecretKeySpec blowfish128SecretKey;

	public Charset charset = Charset.forName("UTF-8");

	public Digest hmacSHA256(String value) throws NoSuchAlgorithmException, InvalidKeyException {
		Mac mac = Mac.getInstance(HMAC_SHA256);
		mac.init(sha256SecretKey);
		return new Digest(mac.doFinal(stringToBytes(value)));
	}

	public String hash(String seed, int nchars) {
		return sha1(seed).toBase64UrlSafeNoPad().substring(0, nchars);
	}

	public String hash(byte[] seed, int nchars) {
		return sha1(seed).toBase64UrlSafeNoPad().substring(0, nchars);
	}

	public Digest md5(String seed) {
		try {
			return md5(stringToBytes(seed));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public Digest md5(byte[] seed) {
		try {
			MessageDigest md = MessageDigest.getInstance(MD5);
			md.update(seed);
			return new Digest(md.digest());
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	public Digest encryptAes128(byte[] clearMessage) {
		try {
			Cipher cipher = Cipher.getInstance(AES);
			cipher.init(ENCRYPT_MODE, aes128SecretKey);
			byte[] encrypted = cipher.doFinal(clearMessage);
			return new Digest(encrypted);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		} catch (NoSuchPaddingException e) {
			throw new IllegalStateException(e);
		} catch (InvalidKeyException e) {
			throw new IllegalStateException(e);
		} catch (IllegalBlockSizeException e) {
			throw new IllegalStateException(e);
		} catch (BadPaddingException e) {
			throw new IllegalStateException(e);
		}
	}

	public byte[] decryptAes128(byte[] encrypted) {
		try {
			Cipher cipher = Cipher.getInstance(AES);
			cipher.init(DECRYPT_MODE, aes128SecretKey);
			return cipher.doFinal(encrypted);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		} catch (NoSuchPaddingException e) {
			throw new IllegalStateException(e);
		} catch (InvalidKeyException e) {
			throw new IllegalStateException(e);
		} catch (IllegalBlockSizeException e) {
			throw new IllegalStateException(e);
		} catch (BadPaddingException e) {
			throw new IllegalStateException(e);
		}
	}

	public Digest encryptBlowfish128(byte[] clearMessage) {
		try {
			Cipher cipher = Cipher.getInstance(BLOWFISH);
			cipher.init(ENCRYPT_MODE, blowfish128SecretKey);
			byte[] encrypted = cipher.doFinal(clearMessage);
			return new Digest(encrypted);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		} catch (NoSuchPaddingException e) {
			throw new IllegalStateException(e);
		} catch (InvalidKeyException e) {
			throw new IllegalStateException(e);
		} catch (IllegalBlockSizeException e) {
			throw new IllegalStateException(e);
		} catch (BadPaddingException e) {
			throw new IllegalStateException(e);
		}
	}

	public byte[] decryptBlowfish128(byte[] encrypted) {
		try {
			Cipher cipher = Cipher.getInstance(BLOWFISH);
			cipher.init(DECRYPT_MODE, blowfish128SecretKey);
			return cipher.doFinal(encrypted);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		} catch (NoSuchPaddingException e) {
			throw new IllegalStateException(e);
		} catch (InvalidKeyException e) {
			throw new IllegalStateException(e);
		} catch (IllegalBlockSizeException e) {
			throw new IllegalStateException(e);
		} catch (BadPaddingException e) {
			throw new IllegalStateException(e);
		}
	}

	public Digest sha1(byte[] seed) {
		try {
			MessageDigest md = MessageDigest.getInstance(SHA1);
			md.update(seed);
			return new Digest(md.digest());
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public Digest sha1(String seed) {
		try {
			return sha1(stringToBytes(seed));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private byte[] stringToBytes(String seed) {
		return seed.getBytes(charset);
	}

	public Digester withCharset(String cs) {
		setCharset(cs);
		return this;
	}

	public void setCharset(String cs) {
		charset = Charset.forName(cs);
	}

	public Digester withSecretKeySpec(String secretKey) {
		setSecretKeySpec(secretKey);
		return this;
	}

	public void setSecretKeySpec(String secretKey) {
		byte[] b = stringToBytes(secretKey);
		this.aes128SecretKey = new SecretKeySpec(Arrays.copyOf(b, 16), AES);
		this.sha256SecretKey = new SecretKeySpec(Arrays.copyOf(b, 32), HMAC_SHA256);
		this.blowfish128SecretKey = new SecretKeySpec(Arrays.copyOf(b, 16), BLOWFISH);
	}

	public static class Digest {
		private byte[] b;
		private static final Base64Codec base64 = new Base64Codec();

		public Digest(byte[] digest) {
			b = digest;
		}

		public String toBase64Classic() {
			return base64.encodeClassic(b);
		}

		public String toBase64GzipClassic() {
			return base64.gzipEncodeClassic(b);
		}

		public String toBase64UrlSafeNoPad() {
			return base64.encodeUrlSafeNoPad(b);
		}

		public String toBase62() {
			return base62.encode(b);
		}

		public String toBase64GzipUrlSafeNoPad() {
			return base64.gzipEncodeUrlSafeNoPad(b);
		}

		public String toHex() {
			return Hex.encode(b);
		}

		public byte[] unwrap() {
			return b;
		}
	}

}