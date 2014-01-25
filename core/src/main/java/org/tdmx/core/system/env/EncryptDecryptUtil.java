package org.tdmx.core.system.env;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EncryptDecryptUtil {

    private static Logger log = LoggerFactory.getLogger(EncryptDecryptUtil.class);
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if ( args.length != 1 ) {
			log.warn("Missing argument.");
			return;
		}

		String arg = args[0];
		
		StringEncrypter e = ObfuscationSupport.getInstance().getEncrypter();
		String decryptedValue = e.decrypt(arg);
		
		String encryptedValue = e.encrypt(arg);
		if ( decryptedValue != null ) {
			log.info("decrypted=>"+decryptedValue);
		} else {
			log.info("encrypted=>"+encryptedValue);
		}
	}

}
