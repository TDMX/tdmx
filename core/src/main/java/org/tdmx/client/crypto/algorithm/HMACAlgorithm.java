package org.tdmx.client.crypto.algorithm;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.tdmx.client.crypto.converters.ByteArray;
import org.tdmx.client.crypto.scheme.CryptoException;
import org.tdmx.client.crypto.scheme.CryptoResultCode;

public enum HMACAlgorithm {

	HMAC_SHA_1("HmacSHA1", PKCSObjectIdentifiers.id_hmacWithSHA1),
	HMAC_SHA_256("HmacSHA256", PKCSObjectIdentifiers.id_hmacWithSHA256),
	HMAC_SHA_384("HmacSHA384", PKCSObjectIdentifiers.id_hmacWithSHA384),
	HMAC_SHA_512("HmacSHA512", PKCSObjectIdentifiers.id_hmacWithSHA512),
	;
	
	private String algorithm;
	private ASN1ObjectIdentifier asn1oid;
	
	private HMACAlgorithm(String algorithm, ASN1ObjectIdentifier asn1oid) {
		this.algorithm = algorithm;
		this.asn1oid = asn1oid;
	}

	public String getAlgorithm() {
		return this.algorithm;
	}

	public String hexHMACfromUTF8( String data, String key ) throws CryptoException {
		try {
			byte[] rawHmac=hmac(data.getBytes("UTF-8"), key.getBytes("UTF-8"));
			return ByteArray.asHex(rawHmac);
		} catch (UnsupportedEncodingException e) {
			throw new CryptoException(CryptoResultCode.ERROR_ENCODING_MISSING, e);
		}
	}
	
	public boolean verify( byte[] data, byte[] key, byte[] expected ) throws CryptoException {
		byte[] result = hmac(data,key);
		return ByteArray.equals(result,expected);
	}
	
	public ASN1ObjectIdentifier getAsn1oid() {
		return asn1oid;
	}

	public byte[] hmac(byte[] data, byte[] key) throws CryptoException {
		try {
			// get an hmac_sha256 key from the raw key bytes
			SecretKeySpec signingKey = new SecretKeySpec(key, algorithm);

			// get an hmac_sha256 Mac instance and initialize with the signing
			// key
			Mac mac = Mac.getInstance(algorithm);
			mac.init(signingKey);

			// compute the hmac256 on input data bytes
			byte[] rawHmac = mac.doFinal(data);
			return rawHmac;
			
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException(CryptoResultCode.ERROR_HMAC_ALGORITHM_MISSING, e);

		} catch (InvalidKeyException e) {
			throw new CryptoException(CryptoResultCode.ERROR_HMAC_KEY_INVALID, e);
		}
	}
	
}
