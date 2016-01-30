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
package org.tdmx.server.runtime;

/**
 * The name of an independent services.
 * 
 * @author Peter
 * 
 */
public enum ServiceName {

	PCC, // PartitionControlService client ( prerequisite of all except PCS )

	TOS, // TransferObjectService ( prerequisite of WS [MRS], )
	ROC, // RelayOutboundService client ( prerequisite of WS [MOS,MDS,ZAS] )

	WS, // Web(SOAP) Services - MOS,MDS,MRS,ZAS APIs
	RS, // RESTful Services
	SCS, // SessionControlService
	PCS, // PartitionControlService
	ROS, // RelayOutboundService
	JOB; // Background Jobs

}
