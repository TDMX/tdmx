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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.PKIXCertificate;

public class DelegatingTrustedCertificateProvider implements TrustedServerCertificateProvider {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(DelegatingTrustedCertificateProvider.class);

	private List<TrustedServerCertificateProvider> delegateProviders;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public PKIXCertificate[] getTrustedCertificates() {
		if (delegateProviders == null) {
			return new PKIXCertificate[0];
		}
		List<PKIXCertificate> allResults = new ArrayList<>();
		for (int i = 0; i < delegateProviders.size(); i++) {
			PKIXCertificate[] result = delegateProviders.get(i).getTrustedCertificates();
			for (PKIXCertificate r : result) {
				allResults.add(r);
			}
		}
		return allResults.toArray(new PKIXCertificate[0]);
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

	public List<TrustedServerCertificateProvider> getDelegateProviders() {
		return delegateProviders;
	}

	public void setDelegateProviders(List<TrustedServerCertificateProvider> delegateProviders) {
		this.delegateProviders = delegateProviders;
	}

}
