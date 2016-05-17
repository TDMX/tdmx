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

package org.tdmx.server.ws.security.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.server.ws.session.WebServiceSession;

/**
 * AuthorizedSessionLookupService holds thread bound information about the logged in authorized Agent.
 * 
 * @author Peter Klauser
 * 
 */
public class AuthorizedSessionServiceImpl<E extends WebServiceSession> implements AuthorizedSessionService<E> {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(AuthorizedSessionServiceImpl.class);

	private final ThreadLocal<E> authStore = new ThreadLocal<E>();

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public E getAuthorizedSession() {
		return authStore.get();
	}

	@Override
	public void setAuthorizedSession(E session) {
		if (authStore.get() != null) {
			log.warn("SECURITY WARNING: ThreadLocal not cleared when being set.");
			clearAuthorizedSession();
		}
		authStore.set(session);
	}

	@Override
	public void clearAuthorizedSession() {
		authStore.remove();
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

}
