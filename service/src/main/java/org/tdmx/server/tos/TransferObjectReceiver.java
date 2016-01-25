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
package org.tdmx.server.tos;

import java.util.Map;

import org.tdmx.server.pcs.protobuf.Common.AttributeValue.AttributeId;
import org.tdmx.server.pcs.protobuf.Common.ObjectType;
import org.tdmx.server.ws.session.WebServiceApiName;

/**
 * A receiver of object references passed between services.
 * 
 * TODO #93: MRS -(MSG)-> MDS
 *
 * TODO #93: MRS -(DR)-> MOS
 * 
 * TODO #93: use relay service from MRS to transfer relayed-in FC-open to ROS(sender side)
 * 
 * TODO #93: fast inform of MOS sender that a CDS has changed. Flowquota to include DS id.
 * 
 * @author Peter
 *
 */
public interface TransferObjectReceiver {

	/**
	 * Inbound transfer of an object to a session.
	 * 
	 * @param sessionKey
	 * @param api
	 * @param type
	 * @param attributes
	 * @return
	 */
	public boolean transferObject(String sessionId, WebServiceApiName api, ObjectType type,
			Map<AttributeId, Long> attributes);

}
