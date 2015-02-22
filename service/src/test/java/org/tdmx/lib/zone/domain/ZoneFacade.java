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
package org.tdmx.lib.zone.domain;

import org.tdmx.lib.common.domain.ZoneReference;

public class ZoneFacade {

	public static Zone createZone(ZoneReference zone) throws Exception {
		Zone z = new Zone(zone);
		return z;
	}

	public static Domain createDomain(ZoneReference zone, String domainName) throws Exception {
		Domain d = new Domain(zone);
		d.setDomainName(domainName);
		return d;
	}

	public static Address createAddress(AddressID id) throws Exception {
		Address a = new Address(id);
		return a;
	}

	public static Service createService(ZoneReference zone, String domainName, String serviceName, int concurrencyLimit)
			throws Exception {
		Service s = new Service(zone);
		s.setDomainName(domainName);
		s.setServiceName(serviceName);
		s.setConcurrencyLimit(concurrencyLimit);
		return s;
	}
}
