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

/**
 * Creates a static encryption utility.
 */
public class ObfuscationSupport {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	public static final String CONFIGURATION_PASSPHRASE_PROPERTY = "org.tdmx.core.system.env.obfuscation.passphrase";
	public static final String STANDARD_PASSPHRASE = "Man this obfuscated stuff is just hiding in plain sight!";

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static Logger log = LoggerFactory.getLogger(ObfuscationSupport.class);

	private static ObfuscationSupport instance;

	private StringEncrypter encrypter;

	public StringEncrypter getEncrypter() {
		return encrypter;
	}

	public static final String ENCRYPTED_TAG = "!!!ENCRYPTED!!!";

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	private ObfuscationSupport() {
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public static final synchronized ObfuscationSupport getInstance() throws RuntimeException {
		if (instance == null) {
			String passphrase = EnvironmentSupport.getProperty(CONFIGURATION_PASSPHRASE_PROPERTY);
			if (passphrase == null) {
				log.warn("Creating Encrypter with standard passphrase.");
				passphrase = STANDARD_PASSPHRASE;
			} else {
				log.info("Creating Encrypter with environment variable " + CONFIGURATION_PASSPHRASE_PROPERTY
						+ " passphrase.");
			}
			StringEncrypter enc = new StringEncrypter(passphrase);
			instance = new ObfuscationSupport(enc);
		}
		return instance;
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private ObfuscationSupport(StringEncrypter e) {
		encrypter = e;
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

}
