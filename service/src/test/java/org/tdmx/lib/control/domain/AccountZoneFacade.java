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

import org.tdmx.lib.control.domain.AccountZone;
import org.tdmx.lib.control.domain.AccountZoneStatus;

public class AccountZoneFacade {

	public static AccountZone createAccountZone(String accountId, String zoneApex, String segment,
			String zonePartitionId) throws Exception {
		AccountZone az = new AccountZone();

		az.setAccountId(accountId);
		az.setZoneApex(zoneApex);
		az.setStatus(AccountZoneStatus.ACTIVE);
		az.setSegment(segment);
		az.setZonePartitionId(zonePartitionId);
		return az;
	}

}
