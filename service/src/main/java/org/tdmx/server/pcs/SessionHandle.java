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
package org.tdmx.server.pcs;

import java.util.Map;

import org.tdmx.server.ws.session.WebServiceApiName;
import org.tdmx.server.ws.session.WebServiceSessionFactory.SeedAttribute;

/**
 * A ServerHandle is a value type holding a SessionKey and the information describing the session.
 * 
 * @author Peter
 *
 */
public class SessionHandle {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private final String segment;
	private final WebServiceApiName api;
	private final String sessionKey;
	private final Map<SeedAttribute, Long> seedAttributes;

	/**
	 * The sessionId is determined later after instantiation.
	 */
	private String sessionId;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public SessionHandle(String segment, WebServiceApiName api, String sessionKey,
			Map<SeedAttribute, Long> seedAttributes) {
		this.segment = segment;
		this.api = api;
		this.sessionKey = sessionKey;
		this.seedAttributes = seedAttributes;
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SessionHandle [sessionKey=");
		builder.append(sessionKey);
		builder.append(", api=");
		builder.append(api);
		builder.append(", segment=");
		builder.append(segment);
		builder.append(", seedAttributes=");
		builder.append(seedAttributes);
		builder.append("]");
		return builder.toString();
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

	public String getSegment() {
		return segment;
	}

	public WebServiceApiName getApi() {
		return api;
	}

	public String getSessionKey() {
		return sessionKey;
	}

	public Map<SeedAttribute, Long> getSeedAttributes() {
		return seedAttributes;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

}
