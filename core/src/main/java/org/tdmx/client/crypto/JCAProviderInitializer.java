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
package org.tdmx.client.crypto;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * The JCAProviderInitializer establishes BouncyCastle as a JCA provider using dynamic initialization method.
 * 
 * This may be prohibited depending on the security policy of the local JVM. If this mechanism doesn't work then the BC
 * provider must be setup statically.
 * 
 * @see http://www.bouncycastle.org/wiki/display/JA1/Provider+Installation
 * 
 * @author Peter
 * 
 */
public class JCAProviderInitializer {

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	private static JCAProviderInitializer staticInstance = new JCAProviderInitializer();

	private JCAProviderInitializer() {
	}

	public static JCAProviderInitializer init() {
		return staticInstance;
	}
}
