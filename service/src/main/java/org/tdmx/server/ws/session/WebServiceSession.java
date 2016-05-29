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
package org.tdmx.server.ws.session;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.server.pcs.protobuf.Common.AttributeValue.AttributeId;
import org.tdmx.server.pcs.protobuf.Common.ObjectType;

public abstract class WebServiceSession {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------
	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	protected final Map<String, Object> attributeMap = new ConcurrentHashMap<>();

	protected static final String CREATED_TIMESTAMP = "CREATED_TIMESTAMP";
	protected static final String LAST_USED_TIMESTAMP = "LAST_USED_TIMESTAMP";

	protected static final String SESSION_ID = "SESSION_ID";
	protected static final String CONTROLLER_ID = "CONTROLLER_ID";

	protected static final String ACCOUNT_ZONE = "ACCOUNT_ZONE";
	protected static final String ZONE = "ZONE";
	protected static final String DOMAIN = "DOMAIN";
	protected static final String CHANNEL = "CHANNEL";
	protected static final String TEMP_CHANNEL = "TEMP_CHANNEL";
	protected static final String ORIGIN_ADDRESS = "ORIGIN_ADDRESS";
	protected static final String DESTINATION_ADDRESS = "DESTINATION_ADDRESS";
	protected static final String SERVICE = "SERVICE";

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	public WebServiceSession(String sessionId) {
		setAttribute(CREATED_TIMESTAMP, new Date());
		setAttribute(SESSION_ID, sessionId);
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

	/**
	 * Whether the session has NOT been used since the lastCutoffDate, and can be discarded because no resources are
	 * currently held by the session.
	 * 
	 * @param lastCutoffDate
	 * @return true if the session has NOT been used since the lastCutoffDate.
	 */
	public boolean isIdle(Date lastCutoffDate) {
		return lastCutoffDate.after(getLastUsedTimestamp());
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

	/**
	 * An object is transferred to this session.
	 * 
	 * @param type
	 * @param attributes
	 * @return true if the object is sucessfully handled.
	 */
	public abstract boolean transferObject(ObjectType type, Map<AttributeId, Long> attributes);

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	@SuppressWarnings("unchecked")
	protected <E extends Object> E getAttribute(String name) {
		return (E) attributeMap.get(name);
	}

	@SuppressWarnings("unchecked")
	protected <E extends Object> E removeAttribute(String name) {
		return (E) attributeMap.remove(name);
	}

	protected <E extends Object> void setAttribute(String name, E object) {
		if (object == null) {
			removeAttribute(name);
		} else {
			attributeMap.put(name, object);
		}
	}

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public Date getLastUsedTimestamp() {
		return getAttribute(LAST_USED_TIMESTAMP);
	}

	public void setLastUsedTimestamp(Date lut) {
		setAttribute(LAST_USED_TIMESTAMP, lut);
	}

	public Date getCreationTimestamp() {
		return getAttribute(CREATED_TIMESTAMP);
	}

	public String getSessionId() {
		return getAttribute(SESSION_ID);
	}

	public void setControllerId(String controllerId) {
		setAttribute(CONTROLLER_ID, controllerId);
	}

	public String getControllerId() {
		return getAttribute(CONTROLLER_ID);
	}
}
