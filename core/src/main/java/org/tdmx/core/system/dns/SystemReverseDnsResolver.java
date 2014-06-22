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
package org.tdmx.core.system.dns;

import java.io.IOException;
import java.net.UnknownHostException;

import org.xbill.DNS.DClass;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.ReverseMap;
import org.xbill.DNS.Section;
import org.xbill.DNS.Type;

public class SystemReverseDnsResolver {

	private final String hostIp;

	public SystemReverseDnsResolver(String hostIp) {
		this.hostIp = hostIp;
	}

	public String reverseDns() {
		Resolver res = getResolver();

		Name name;
		try {
			name = ReverseMap.fromAddress(hostIp);
		} catch (UnknownHostException e) {
			return hostIp;
		}
		int type = Type.PTR;
		int dclass = DClass.IN;
		Record rec = Record.newRecord(name, type, dclass);
		Message query = Message.newQuery(rec);
		Message response;
		try {
			response = res.send(query);
		} catch (IOException e) {
			return hostIp;
		}

		Record[] answers = response.getSectionArray(Section.ANSWER);
		if (answers.length == 0) {
			return hostIp;
		} else {
			return answers[0].rdataToString();
		}
	}

	private Resolver getResolver() {
		Resolver dr = Lookup.getDefaultResolver();
		dr.setTCP(true);
		return dr;
	}
}
