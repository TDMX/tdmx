package org.tdmx.client.crypto.converters;

import java.io.UnsupportedEncodingException;

import org.tdmx.client.crypto.scheme.CryptoException;
import org.tdmx.client.crypto.scheme.CryptoResultCode;

public class StringToUtf8 {

	public static byte[] toBytes(String passphrase) throws CryptoException {
		try {
			return passphrase.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new CryptoException( CryptoResultCode.ERROR_ENCODING_MISSING, e );
		}
	}

}
