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
package org.tdmx.server.pcs;

import java.util.ArrayList;
import java.util.List;

import org.tdmx.server.ws.session.WebServiceApiName;

public class ServerServiceStatistics {
	private final List<ServiceStatistic> stats = new ArrayList<ServiceStatistic>(WebServiceApiName.values().length);

	public void addStatistic(ServiceStatistic s) {
		stats.add(s);
	}

	public List<ServiceStatistic> getStatistics() {
		return stats;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("Statistics [");
		for (ServiceStatistic stat : stats) {
			b.append(stat).append(", ");
		}
		b.append("]");
		return b.toString();
	}
}