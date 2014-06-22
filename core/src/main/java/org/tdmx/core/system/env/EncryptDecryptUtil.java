/*
 * TDMX - Trusted Domain Messaging eXchange
 * 
 * Enterprise B2B messaging between separate corporations via interoperable cloud service providers.
 * 
 * Copyright (C) 2014 Peter Klauser (http://tdmx.org)
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses/.
 */
package org.tdmx.core.system.env;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncryptDecryptUtil {

	private static Logger log = LoggerFactory.getLogger(EncryptDecryptUtil.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			log.warn("Missing argument.");
			return;
		}

		String arg = args[0];

		StringEncrypter e = ObfuscationSupport.getInstance().getEncrypter();
		String decryptedValue = e.decrypt(arg);

		String encryptedValue = e.encrypt(arg);
		if (decryptedValue != null) {
			log.info("decrypted=>" + decryptedValue);
		} else {
			log.info("encrypted=>" + encryptedValue);
		}
	}

}
