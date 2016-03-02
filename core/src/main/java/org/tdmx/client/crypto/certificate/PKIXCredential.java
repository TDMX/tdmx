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
package org.tdmx.client.crypto.certificate;

import java.security.KeyPair;
import java.security.PrivateKey;

public class PKIXCredential {
	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private final PKIXCertificate[] certificateChain;

	private final PrivateKey privateKey;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	public PKIXCredential(PKIXCertificate publicCert, PKIXCertificate[] issuerCertChain, PrivateKey key)
			throws CryptoCertificateException {
		this.certificateChain = new PKIXCertificate[issuerCertChain.length + 1];
		certificateChain[0] = publicCert;
		for (int i = 0; i < issuerCertChain.length; i++) {
			certificateChain[i + 1] = issuerCertChain[i];
		}
		this.privateKey = key;
	}

	public PKIXCredential(PKIXCertificate[] certChain, PrivateKey key) throws CryptoCertificateException {
		this.certificateChain = certChain;
		this.privateKey = key;
	}

	public PKIXCredential(PKIXCertificate publicCert, PKIXCertificate issuerCert, PrivateKey key)
			throws CryptoCertificateException {
		this.certificateChain = new PKIXCertificate[] { publicCert, issuerCert };
		this.privateKey = key;
	}

	public PKIXCredential(PKIXCertificate selfsignedCert, PrivateKey key) throws CryptoCertificateException {
		this.certificateChain = new PKIXCertificate[] { selfsignedCert };
		this.privateKey = key;
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public PKIXCertificate getPublicCert() {
		return certificateChain != null && certificateChain.length > 0 ? certificateChain[0] : null;
	}

	public PKIXCertificate getIssuerPublicCert() {
		return certificateChain != null && certificateChain.length > 1 ? certificateChain[1] : null;
	}

	public PKIXCertificate getZoneRootPublicCert() {
		return certificateChain != null && certificateChain.length > 2 ? certificateChain[2]
				: certificateChain.length > 1 ? certificateChain[1] : certificateChain[0];
	}

	/**
	 * Get the KeyPair corresponding to this credential's PublicKey and PrivateKey.
	 * 
	 * @return the KeyPair corresponding to this credential's PublicKey and PrivateKey.
	 */
	public KeyPair getKeyPair() {
		return new KeyPair(getPublicCert().getCertificate().getPublicKey(), getPrivateKey());
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public PKIXCertificate[] getCertificateChain() {
		return certificateChain;
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}
}
