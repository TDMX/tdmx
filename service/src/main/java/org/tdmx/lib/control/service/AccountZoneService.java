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
package org.tdmx.lib.control.service;

import java.util.List;

import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.domain.AccountZoneSearchCriteria;
import org.tdmx.lib.control.domain.Segment;

/**
 * AccountZoneService provides access to the control information about a Zone in the ControlDB.
 * 
 * @author Peter
 * 
 */
public interface AccountZoneService {

	public enum ZoneCheckStatus {
		DNS_TXT_RECORD_MISSING,
		DNS_ZONEAPEX_WRONG,
		DNS_SCS_URL_MISSING,
		DNS_SCS_URL_WRONG, // doesn't match the segment
		ZONE_EXISTS,
		OK;
	}

	/**
	 * Checks performed before a zone can be "claimed".
	 * 
	 * @param zoneApex
	 * @param the
	 *            target segment
	 * @return null if success, else the reason for not allowing creation of the Zone.
	 */
	public ZoneCheckStatus check(String zoneApex, Segment segment);

	public void createOrUpdate(AccountZone accountZone);

	public AccountZone findById(Long id);

	public AccountZone findByZoneApex(String zoneApex);

	public void delete(AccountZone accountZone);

	public List<AccountZone> search(AccountZoneSearchCriteria criteria);

}
