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
package org.tdmx.lib.common.domain;

import java.util.Objects;

/**
 * An ZoneReference is the information you need to reference a Zone in the ZoneDB.
 * 
 * @author Peter Klauser
 * 
 */
public class ZoneReference {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	/**
	 * The tenantId is the entityID of the AccountZone in ControlDB.
	 */
	private final Long tenantId;

	private final String zoneApex;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public ZoneReference(Long tenantId, String zoneApex) {
		this.tenantId = tenantId;
		this.zoneApex = zoneApex;
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public int hashCode() {
		return Objects.hash(tenantId, zoneApex);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ZoneReference) {
			ZoneReference other = (ZoneReference) obj;
			return Objects.equals(tenantId, other.getTenantId()) && Objects.equals(zoneApex, other.getZoneApex());
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ZoneRef [zoneApex=");
		builder.append(zoneApex);
		builder.append(", tenantId=");
		builder.append(tenantId);
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

	public Long getTenantId() {
		return tenantId;
	}

	public String getZoneApex() {
		return zoneApex;
	}

}
