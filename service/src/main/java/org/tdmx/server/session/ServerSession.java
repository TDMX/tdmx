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
package org.tdmx.server.session;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tdmx.client.crypto.certificate.PKIXCertificate;

public abstract class ServerSession {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------
	public static final String CREATED_TIMESTAMP = "CREATED_TIMESTAMP";
	public static final String LAST_USED_TIMESTAMP = "LAST_USED_TIMESTAMP";

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	protected final Map<String, Object> attributeMap = new HashMap<>();

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	public ServerSession() {
		setAttribute(CREATED_TIMESTAMP, new Date());
	}

	class ServerSessionCertificateHolder {
		private final PKIXCertificate cert;

		public ServerSessionCertificateHolder(PKIXCertificate cert) {
			this.cert = cert;
		}

		public PKIXCertificate getCert() {
			return cert;
		}

	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------
	public Object getAttribute(String name) {
		return attributeMap.get(name);
	}

	public void setAttribute(String name, Object object) {
		attributeMap.put(name, object);
	}

	public void addAuthorizedCertificate(PKIXCertificate authorizedCert) {
		attributeMap.put(authorizedCert.getFingerprint(), new ServerSessionCertificateHolder(authorizedCert));
	}

	public void removeAuthorizedCertificate(PKIXCertificate authorizedCert) {
		attributeMap.remove(authorizedCert.getFingerprint());
	}

	/**
	 * Find all PKIXCertificates which are authorized in the session.
	 * 
	 * @return
	 */
	public List<PKIXCertificate> getAuthorizedCertificates() {
		List<PKIXCertificate> certificates = new ArrayList<>();
		for (Map.Entry<String, Object> es : attributeMap.entrySet()) {
			if (es.getValue() instanceof ServerSessionCertificateHolder) {
				ServerSessionCertificateHolder ssv = (ServerSessionCertificateHolder) es.getValue();
				certificates.add(ssv.getCert());
			}
		}
		return certificates;
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

	public Date getLastUsedTimestamp() {
		Object lut = getAttribute(LAST_USED_TIMESTAMP);
		return lut != null ? (Date) lut : null;
	}

	public void setLastUsedTimestamp(Date lut) {
		setAttribute(LAST_USED_TIMESTAMP, lut);
	}

}
