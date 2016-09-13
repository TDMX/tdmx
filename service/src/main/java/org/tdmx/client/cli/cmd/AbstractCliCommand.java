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
package org.tdmx.client.cli.cmd;

import org.tdmx.client.cli.service.ClientUIKeystoreService;
import org.tdmx.client.cli.service.ClientUITruststoreService;
import org.tdmx.client.cli.service.ZoneAdministrationCredentialService;
import org.tdmx.core.cli.runtime.CommandExecutable;

public abstract class AbstractCliCommand implements CommandExecutable {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	protected static final int PAGE_SIZE = 10;
	protected static final int SUCCESS = 200;

	private ZoneAdministrationCredentialService zacService;
	private ClientUIKeystoreService uiKeystoreService;
	private ClientUITruststoreService uiTruststoreService;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public ClientUITruststoreService getUiTruststoreService() {
		return uiTruststoreService;
	}

	public void setUiTruststoreService(ClientUITruststoreService uiTruststoreService) {
		this.uiTruststoreService = uiTruststoreService;
	}

	public ClientUIKeystoreService getUiKeystoreService() {
		return uiKeystoreService;
	}

	public void setUiKeystoreService(ClientUIKeystoreService uiKeystoreService) {
		this.uiKeystoreService = uiKeystoreService;
	}

	public ZoneAdministrationCredentialService getZacService() {
		return zacService;
	}

	public void setZacService(ZoneAdministrationCredentialService zacService) {
		this.zacService = zacService;
	}

}
