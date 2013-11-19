package org.tdmx.client.crypto;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * The JCAProviderInitializer establishes BouncyCastle as a JCA provider using
 * dynamic initialization method.
 * 
 * This may be prohibited depending on the security policy of the local JVM. 
 * If this mechanism doesn't work then the BC provider must be setup statically.
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
	
	private JCAProviderInitializer() {}
	
	
	public static JCAProviderInitializer init() {
		return staticInstance;
	}
}
