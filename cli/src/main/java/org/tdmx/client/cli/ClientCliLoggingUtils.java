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

import java.text.DateFormat;
import java.util.Calendar;

import org.tdmx.client.cli.ClientCliUtils.ZoneDescriptor;
import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.client.crypto.certificate.TrustStoreEntry;

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
	private static final String TAB = "\t";

	// TODO better rep.

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

	public static String toString(ZoneDescriptor zd) {
		StringBuilder sb = new StringBuilder();
		sb.append("zone=").append(zd.getZoneApex()).append(LINEFEED);
		sb.append("scsUrl=").append(zd.getScsUrl()).append(LINEFEED);
		sb.append("version=").append(zd.getVersion()).append(LINEFEED);
		return sb.toString();
	}

	public static String toString(TrustStoreEntry entry) {
		StringBuilder sb = new StringBuilder();
		sb.append("Friendly Name=").append(entry.getFriendlyName()).append(LINEFEED);
		sb.append(entry.getCertificate()).append(LINEFEED);
		sb.append(entry.getComment()).append(LINEFEED);
		return sb.toString();
	}

	public static String toString(PKIXCertificate pk) {
		StringBuilder sb = new StringBuilder();
		if (pk.isTdmxZoneAdminCertificate()) {
			sb.append("Zone Administrator[ ").append(pk.getTdmxZoneInfo().getZoneRoot());
			sb.append(" Subject=" + pk.getSubject());
			// TODO split
		} else if (pk.isTdmxDomainAdminCertificate()) {
			sb.append("Domain Administrator[ ").append(pk.getTdmxDomainName());

		} else if (pk.isTdmxUserCertificate()) {
			sb.append("User[ ").append(pk.getTdmxUserName());

		} else {
			sb.append("Non TDMX cert [");
		}
		sb.append(" SerialNumber=" + pk.getSerialNumber());
		sb.append(" Fingerprint=").append(pk.getFingerprint());
		sb.append(" PEM=").append(CertificateIOUtils.safeX509certsToPem(new PKIXCertificate[] { pk }));
		sb.append("]");
		return sb.toString();
	}

	public static String toString(org.tdmx.core.api.v01.msg.Administrator admin) {
		PKIXCertificate pk = CertificateIOUtils.safeDecodeX509(admin.getAdministratorIdentity().getDomaincertificate());

		StringBuilder sb = new StringBuilder();
		sb.append("Administrator[ ").append(pk.getTdmxDomainName());
		sb.append(" SerialNumber=" + pk.getSerialNumber());
		sb.append(" Fingerprint=").append(pk.getFingerprint());
		sb.append(" Status=").append(admin.getStatus());
		sb.append(toString(admin.getAdministratorIdentity()));
		sb.append("]");
		return sb.toString();
	}

	public static String toString(org.tdmx.core.api.v01.msg.User u) {
		PKIXCertificate pk = CertificateIOUtils.safeDecodeX509(u.getUserIdentity().getUsercertificate());

		StringBuilder sb = new StringBuilder();
		sb.append("User[ ").append(pk.getCommonName());
		sb.append(" SerialNumber=" + pk.getSerialNumber());
		sb.append(" Fingerprint=").append(pk.getFingerprint());
		sb.append(" Status=").append(u.getStatus());
		sb.append(toString(u.getUserIdentity()));
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
		buf.append("Channel Info [").append(toString(ci.getChannelauthorization())).append(LINEFEED);
		if (ci.getSessioninfo() != null) {
			buf.append("Session [").append(toString(ci.getSessioninfo())).append("]").append(LINEFEED);
		} else {
			buf.append("No Session").append(LINEFEED);
		}
		buf.append("FlowStatus [").append(toString(ci.getStatus())).append("]").append(LINEFEED);
		buf.append("]");
		return buf.toString();
	}

	public static String toString(org.tdmx.core.api.v01.msg.Channelauthorization ca) {
		StringBuilder buf = new StringBuilder();
		buf.append("Domain [").append(ca.getDomain()).append("]").append(LINEFEED);
		buf.append("Channel [").append(toString(ca.getChannel())).append("]").append(LINEFEED);
		if (ca.getCurrent() != null) {
			buf.append("Current Authorization [").append(toString(ca.getCurrent())).append("]").append(LINEFEED);
		} else {
			buf.append("No Current Authorization").append(LINEFEED);
		}
		if (ca.getUnconfirmed() != null) {
			buf.append("Requested Authorization [").append(toString(ca.getUnconfirmed())).append("]").append(LINEFEED);
		} else {
			buf.append("No Requested Authorization").append(LINEFEED);
		}
		buf.append(toString(ca.getPs()));
		return buf.toString();
	}

	public static String toString(org.tdmx.core.api.v01.msg.Currentchannelauthorization cca) {
		StringBuilder buf = new StringBuilder();
		if (cca.getOriginPermission() != null) {
			buf.append("Origin Permission [").append(toString(cca.getOriginPermission())).append("]").append(LINEFEED);
		} else {
			buf.append("No Origin Permission").append(LINEFEED);
		}
		if (cca.getDestinationPermission() != null) {
			buf.append("Destination Permission [").append(toString(cca.getDestinationPermission())).append("]")
					.append(LINEFEED);
		} else {
			buf.append("No Destination Permission").append(LINEFEED);
		}
		return buf.toString();
	}

	public static String toString(org.tdmx.core.api.v01.msg.RequestedChannelAuthorization rca) {
		StringBuilder buf = new StringBuilder();
		if (rca.getOriginPermission() != null) {
			buf.append("Origin Permission [").append(toString(rca.getOriginPermission())).append("]").append(LINEFEED);
		} else {
			buf.append("No Origin Permission").append(LINEFEED);
		}
		if (rca.getDestinationPermission() != null) {
			buf.append("Destination Permission[").append(toString(rca.getDestinationPermission())).append("]")
					.append(LINEFEED);
		} else {
			buf.append("No Destination Permission").append(LINEFEED);
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

	public static String toString(org.tdmx.core.api.v01.msg.UserIdentity u) {
		StringBuilder buf = new StringBuilder();
		buf.append(" User Public Key=").append(CertificateIOUtils.safeX509certsToPem(u.getUsercertificate()))
				.append(LINEFEED);
		// buf.append(" Administrator Public
		// Key=").append(CertificateIOUtils.safeX509certsToPem(u.getDomaincertificate())).append(LINEFEED);
		// buf.append(" Zone Root Public
		// Key=").append(CertificateIOUtils.safeX509certsToPem(u.getRootcertificate())).append(LINEFEED);
		return buf.toString();
	}

	public static String toString(org.tdmx.core.api.v01.msg.AdministratorIdentity a) {
		StringBuilder buf = new StringBuilder();
		buf.append(" Administrator Public Key=").append(CertificateIOUtils.safeX509certsToPem(a.getDomaincertificate()))
				.append(LINEFEED);
		// buf.append(" Zone Root Public
		// Key=").append(CertificateIOUtils.safeX509certsToPem(a.getRootcertificate())).append(LINEFEED);
		return buf.toString();
	}

	public static String toString(org.tdmx.core.api.v01.msg.Signaturevalue sv) {
		StringBuilder buf = new StringBuilder();
		buf.append(" SignatureValue [");
		buf.append(" Timestamp=").append(toString(sv.getTimestamp()));
		buf.append(" Algorithm=").append(sv.getSignatureAlgorithm());
		buf.append(" Signature=").append(sv.getSignature());
		buf.append("]");
		return buf.toString();
	}

	public static String toString(org.tdmx.core.api.v01.msg.Administratorsignature sig) {
		StringBuilder buf = new StringBuilder();
		buf.append("Administrator Signature [");
		buf.append(toString(sig.getAdministratorIdentity()));
		buf.append(toString(sig.getSignaturevalue()));
		buf.append("]");
		return buf.toString();
	}

	public static String toString(org.tdmx.core.api.v01.msg.UserSignature sig) {
		StringBuilder buf = new StringBuilder();
		buf.append("User Signature [");
		buf.append(toString(sig.getUserIdentity()));
		buf.append(toString(sig.getSignaturevalue()));
		buf.append("]");
		return buf.toString();
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
		buf.append(" RelayStatus=").append(fs.getRelayStatus());
		buf.append(" FlowStatus=").append(fs.getFlowStatus());
		buf.append(" UsedBytes=").append(fs.getUsedBytes());
		return buf.toString();
	}

	public static String toString(org.tdmx.core.api.v01.msg.Limit li) {
		return "High=" + li.getHighBytes() + " Low=" + li.getLowBytes();
	}

	public static String toString(org.tdmx.core.api.v01.msg.Destinationsession ds) {
		StringBuilder buf = new StringBuilder();
		buf.append("Destination Session [");
		buf.append(" ContextId=").append(ds.getEncryptionContextId());
		buf.append(" Scheme=").append(ds.getScheme());
		buf.append(" SessionKey=").append(ds.getSessionKey());
		buf.append(toString(ds.getUsersignature()));
		buf.append("]");
		return buf.toString();
	}

	public static String toString(org.tdmx.core.api.v01.common.Ps ps) {
		StringBuilder buf = new StringBuilder();
		buf.append("Processing State [");
		if (ps.getError() != null) {
			buf.append(toString(ps.getError()));
		}
		if (ps.getStatus() != null) {
			buf.append(" Status=").append(ps.getStatus());
		}
		if (ps.getTimestamp() != null) {
			buf.append(" Timestamp=").append(toString(ps.getTimestamp()));
		}
		buf.append("]");
		return buf.toString();
	}

	public static String toString(Calendar cal) {
		if (cal == null) {
			return null;
		}
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
		return df.format(cal.getTime());
	}

	public static String truncatedMessage() {
		return "More results may exist. Use the pageNumber and pageSize parameters to get the next page of results.";
	}
}
