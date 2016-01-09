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

import java.util.Map;

import org.tdmx.server.pcs.protobuf.Common.AttributeValue.AttributeId;
import org.tdmx.server.pcs.protobuf.ROSClient.RelayStatistic;

public interface RelayOutboundServiceController {

	/**
	 * Creates a new relay session for a channel.
	 * 
	 * @param channelKey
	 * @param attributes
	 *            providing objectIds for channel objects.
	 * @param mrsSessionId
	 * 
	 * @return the relay server load statistics or null if this failed.
	 */
	public RelayStatistic createRelaySession(String channelKey, Map<AttributeId, Long> seedAttributes,
			String mrsSessionId);

	/**
	 * Return the server's load statistics.
	 * 
	 * @return the server's load statistics or null if the operation failed.
	 */
	public RelayStatistic getStatistics();
}
