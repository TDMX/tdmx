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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;

public class DnsDomainZoneFacade {

	public static DnsDomainZone createDnsDomainZone(String domainName, String zoneApex, String scsHostname, Date from,
			Date to) throws MalformedURLException {

		DnsDomainZone s = new DnsDomainZone();
		s.setDomainName(domainName);
		s.setZoneApex(zoneApex);
		s.setScsUrl(new URL("https://" + scsHostname + "/scs/v1.0/"));
		s.setZacFingerprint("abcdef1234567890");
		s.setValidFromTime(from);
		s.setValidUntilTime(to);
		s.setNameServerAddresses(Arrays.asList("n1.ns.com", "n2.ns.com", "n3.ns.com"));
		return s;
	}

}
