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
package org.tdmx.client.adapter;

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509KeyManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.PKIXCredential;

class ClientCredentialKeyManager implements X509KeyManager {

	private static final Logger log = LoggerFactory.getLogger(ClientCredentialKeyManager.class);

	// a fake alias for a pre-loaded credential
	private static final String CLIENT_ALIAS = "identity";

	private final PKIXCredential credential;

	public ClientCredentialKeyManager(PKIXCredential credential) {
		this.credential = credential;
	}

	@Override
	public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
		return CLIENT_ALIAS;
	}

	@Override
	public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
		return null;
	}

	@Override
	public X509Certificate[] getCertificateChain(String alias) {
		log.debug("getCertificateChain alias{" + alias + "}");
		// we don't provide the "full" chain to the TDMX zone root issuer since
		// the service providers always know our certificates exactly, and the
		// "certification" chain is not relevant for TLS trust - since the SP "knows"
		// each identity.
		return new X509Certificate[] { credential.getPublicCert().getCertificate() };
	}

	@Override
	public String[] getClientAliases(String keyType, Principal[] issuers) {
		return null;
	}

	@Override
	public PrivateKey getPrivateKey(String alias) {
		log.debug("getPrivateKey " + alias);
		return credential.getPrivateKey();
	}

	@Override
	public String[] getServerAliases(String keyType, Principal[] issuers) {
		return null;
	}

}