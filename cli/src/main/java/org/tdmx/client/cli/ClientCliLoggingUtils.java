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
	private static final String LINEFEED = System.getProperty("line.separator", "\n");

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	private ClientCliLoggingUtils() {
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public static String toString(org.tdmx.core.api.v01.common.Error error) {
		return "Error [" + error.getCode() + "] " + error.getDescription();
	}

	public static String toString(org.tdmx.core.api.v01.msg.Administrator admin) {
		PKIXCertificate pk = CertificateIOUtils.safeDecodeX509(admin.getAdministratorIdentity().getDomaincertificate());

		StringBuilder sb = new StringBuilder();
		sb.append("Administrator[ ").append(pk.getTdmxDomainName());
		sb.append(" serialNumber=" + pk.getSerialNumber());
		sb.append(" fingerprint=").append(pk.getFingerprint());
		sb.append(" status=").append(admin.getStatus());
		sb.append(" identity=")
				.append(CertificateIOUtils.safeX509certsToPem(admin.getAdministratorIdentity().getDomaincertificate(),
						admin.getAdministratorIdentity().getRootcertificate()));
		sb.append("]");
		return sb.toString();
	}

	public static String toString(org.tdmx.core.api.v01.msg.Service service) {
		return "Service [" + service.getDomain() + "#" + service.getServicename() + "]";
	}

	public static String toString(org.tdmx.core.api.v01.msg.Address address) {
		return "Address [" + address.getLocalname() + "@" + address.getDomain() + "]";
	}

	public static String toString(org.tdmx.core.api.v01.msg.Channel channel) {
		return "Channel [" + channel.getOrigin().getLocalname() + "@" + channel.getOrigin().getDomain() + "->"
				+ channel.getDestination().getLocalname() + "@" + channel.getDestination().getDomain() + "#"
				+ channel.getDestination().getServicename() + "]";
	}

	public static String toString(org.tdmx.core.api.v01.msg.Channelinfo ci) {
		StringBuilder buf = new StringBuilder();
		buf.append("Channel Authorization [").append(toString(ci.getChannelauthorization())).append(LINEFEED);
		if (ci.getSessioninfo() != null) {
			buf.append(" Session [").append(toString(ci.getSessioninfo())).append("]").append(LINEFEED);
		} else {
			buf.append(" No Session");
		}
		buf.append("FlowStatus [").append(toString(ci.getStatus())).append("]").append(LINEFEED);
		if (ci.getLimit() != null) {
			buf.append(", Limit [").append(toString(ci.getLimit())).append("]").append(LINEFEED);
		} else {
			buf.append(", No FlowControlLimit");
		}
		if (ci.getLevel() != null) {
			buf.append(", Level [").append(toString(ci.getLevel())).append("]").append(LINEFEED);
		} else {
			buf.append(", No FlowControlLevel");
		}
		return buf.toString();
	}

	public static String toString(org.tdmx.core.api.v01.msg.Channelauthorization ca) {
		StringBuilder buf = new StringBuilder();
		buf.append("Domain [").append(ca.getDomain()).append("]").append(LINEFEED);
		buf.append("Current Authorization [").append(toString(ca.getCurrent())).append("]").append(LINEFEED);
		buf.append("Requested Authorization [").append(toString(ca.getUnconfirmed())).append("]").append(LINEFEED);
		return buf.toString();
	}

	public static String toString(org.tdmx.core.api.v01.msg.Currentchannelauthorization cca) {
		StringBuilder buf = new StringBuilder();
		buf.append(toString(cca.getChannel())).append(LINEFEED);
		if (cca.getOriginPermission() != null) {
			buf.append("Origin [").append(toString(cca.getOriginPermission())).append("]").append(LINEFEED);
		} else {
			buf.append("No Origin Permission");
		}
		if (cca.getDestinationPermission() != null) {
			buf.append("Destination [").append(toString(cca.getDestinationPermission())).append("]").append(LINEFEED);
		} else {
			buf.append("No Destination Permission");
		}
		return buf.toString();
	}

	public static String toString(org.tdmx.core.api.v01.msg.RequestedChannelAuthorization rca) {
		StringBuilder buf = new StringBuilder();
		if (rca.getOriginPermission() != null) {
			buf.append("Origin [").append(toString(rca.getOriginPermission())).append("]").append(LINEFEED);
		} else {
			buf.append("No Origin Permission");
		}
		if (rca.getDestinationPermission() != null) {
			buf.append("Destination [").append(toString(rca.getDestinationPermission())).append("]").append(LINEFEED);
		} else {
			buf.append("No Destination Permission");
		}
		return buf.toString();
	}

	public static String toString(org.tdmx.core.api.v01.msg.Permission p) {
		StringBuilder buf = new StringBuilder();
		buf.append(p.getPermission());
		buf.append(" Size=").append(p.getMaxPlaintextSizeBytes());
		buf.append(" Signature [").append(toString(p.getAdministratorsignature())).append("]");
		return buf.toString();
	}

	public static String toString(org.tdmx.core.api.v01.msg.Administratorsignature sig) {
		return "TODO"; // TODO
	}

	public static String toString(org.tdmx.core.api.v01.msg.Sessioninfo si) {
		StringBuilder buf = new StringBuilder();
		if (si.getDestinationsession() != null) {
			buf.append("Destination Session [").append(toString(si.getDestinationsession())).append("]");
		} else {
			buf.append("No Destination Session");
		}
		if (si.getPs() != null) {
			buf.append("Processing Status [").append(toString(si.getPs())).append("]");
		} else {
			buf.append("No Processing Status");
		}
		return buf.toString();
	}

	public static String toString(org.tdmx.core.api.v01.msg.FlowStatus fs) {
		StringBuilder buf = new StringBuilder();
		buf.append("receive=").append(fs.getReceiverStatus());
		buf.append(" send=").append(fs.getSenderStatus());

		return buf.toString();
	}

	public static String toString(org.tdmx.core.api.v01.msg.FlowControlLevel fl) {
		StringBuilder buf = new StringBuilder();
		buf.append("Level [");
		buf.append(" Undelivered=").append(fl.getUndeliveredBuffer());
		buf.append(", Unsent=").append(fl.getUnsentBuffer());
		buf.append("]");
		return buf.toString();
	}

	public static String toString(org.tdmx.core.api.v01.msg.FlowControlLimit li) {
		StringBuilder buf = new StringBuilder();
		if (li.getUnsentBuffer() != null) {
			buf.append("Unsent [").append(toString(li.getUnsentBuffer())).append("]");
		} else {
			buf.append("No Unsent Limit");
		}
		if (li.getUndeliveredBuffer() != null) {
			buf.append(", Undelivered [").append(toString(li.getUndeliveredBuffer())).append("]");
		} else {
			buf.append(", No Undelivered Limit");
		}
		return buf.toString();
	}

	public static String toString(org.tdmx.core.api.v01.msg.Limit li) {
		StringBuilder buf = new StringBuilder();
		buf.append("High=").append(li.getHighBytes());
		buf.append(" Low=").append(li.getLowBytes());
		return buf.toString();
	}

	public static String toString(org.tdmx.core.api.v01.msg.Destinationsession ds) {
		return "TODO";// TODO
	}

	public static String toString(org.tdmx.core.api.v01.common.Ps ps) {
		return "TODO";// TODO
	}

	public static String truncatedMessage() {
		return "More results may exist. Use the pageNumber and pageSize parameters to get the next page of results.";
	}
}
