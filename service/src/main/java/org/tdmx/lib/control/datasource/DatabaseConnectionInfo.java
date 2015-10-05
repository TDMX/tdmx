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
package org.tdmx.lib.control.datasource;

import java.util.Objects;

/**
 * 
 * A ValueObject representing a Database Schema and Login.
 * 
 */
public class DatabaseConnectionInfo {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	private final String username;
	private final String password;
	private final String url;
	private final String driverClassname;

	// TODO LATER: extend to other properties of org.apache.commons.dbcp.BasicDataSource
	// like maxSize etc.

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public DatabaseConnectionInfo(String username, String password, String url, String driverClassname) {
		if (username == null) {
			throw new IllegalArgumentException("username");
		}
		if (password == null) {
			throw new IllegalArgumentException("password");
		}
		if (url == null) {
			throw new IllegalArgumentException("url");
		}
		if (driverClassname == null) {
			throw new IllegalArgumentException("driverClassname");
		}
		this.url = url;
		this.username = username;
		this.password = password;
		this.driverClassname = driverClassname;
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public int hashCode() {
		return Objects.hash(url, username, password);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DatabaseConnectionInfo) {
			DatabaseConnectionInfo other = (DatabaseConnectionInfo) obj;
			return Objects.equals(url, other.getUrl()) && Objects.equals(username, other.getUsername())
					&& Objects.equals(password, other.getPassword());
		} else {
			return false;
		}
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

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @return the driverclassname
	 */
	public String getDriverClassname() {
		return driverClassname;
	}

}
