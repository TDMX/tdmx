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
package org.tdmx.client.cli;

import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.PKIXCertificate;

/**
 * Utilities for logging for Client CLI commands.
 * 
 * @author Peter
 *
 */
public class ClientCliLoggingUtils {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	private ClientCliLoggingUtils() {
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS - Zone Descriptor
	// -------------------------------------------------------------------------

	public static String toString(org.tdmx.core.api.v01.common.Error error) {
		return "Error [" + error.getCode() + "] " + error.getDescription();
	}

	public static String toString(org.tdmx.core.api.v01.msg.Administrator admin) {
		PKIXCertificate pk = CertificateIOUtils.safeDecodeX509(admin.getAdministratorIdentity().getDomaincertificate());

		StringBuilder sb = new StringBuilder();
		sb.append("Administrator [");
		sb.append(" domain=").append(pk.getTdmxDomainName());
		sb.append(" status=").append(admin.getStatus());
		sb.append(" identity=")
				.append(CertificateIOUtils.safeX509certsToPem(admin.getAdministratorIdentity().getDomaincertificate(),
						admin.getAdministratorIdentity().getRootcertificate()));
		sb.append("]");
		return sb.toString();
	}

	public static String truncatedMessage() {
		return "More results may exist. Use the pageNumber and pageSize parameters to get the next page of results.";
	}
}
