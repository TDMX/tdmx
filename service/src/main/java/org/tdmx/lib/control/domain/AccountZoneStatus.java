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
package org.tdmx.lib.control.domain;

/**
 * The AccountZoneStatus represents the state of a Zone as defined by the Account. The Account is free to toggle the
 * AccountZone's status at any time. The AccountZoneStatus applies to all Agents of the Zone.
 * 
 * The AccountZoneStatus is not specified by the TDMX specification. How it is changed is defined by the
 * ServiceProvider.
 * 
 * @author Peter
 * 
 */
public enum AccountZoneStatus {

	/**
	 * The AccountZone is active so that Agents associated with the Zone may interact with the ServiceProvider.
	 */
	ACTIVE,

	/**
	 * The AccountZone is closed for maintenance so that Agents associated with the Zone may not interact with the
	 * ServiceProvider. //TODO zone transfer job - state must keep record of whether blocked or active
	 */
	MAINTENANCE,

	/**
	 * The AccountZone is blocked so that Agents associated with the Zone may not interact with the ServiceProvider.
	 */
	BLOCKED, ;

	public static final int MAX_ACCOUNTZONESTATUS_LEN = 16;
}
