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
package org.tdmx.core.cli.display;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;

import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.client.crypto.certificate.TrustStoreEntry;
import org.tdmx.client.crypto.converters.ByteArray;
import org.tdmx.core.system.lang.CalendarUtils;
import org.tdmx.core.system.lang.EnumUtils;

/**
 * ApiToPrintableObjectMapper maps the representation of the core API xml classes to PrintableObjects for the CLI.
 * 
 * @author Peter
 *
 */
public class CorePrintableObjectMapperImpl implements PrintableObjectMapper {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public CorePrintableObjectMapperImpl() {
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------
	@Override
	public Object map(Object object, boolean verbose) {
		if (object instanceof Exception) {
			return toLog((Exception) object);
		} else if (object instanceof org.tdmx.core.api.v01.common.Error) {
			return toLog((org.tdmx.core.api.v01.common.Error) object);
		} else if (object instanceof org.tdmx.core.api.v01.msg.User) {
			org.tdmx.core.api.v01.msg.User u = (org.tdmx.core.api.v01.msg.User) object;
			return verbose ? toVerbose(u) : toLog(u);
		} else if (object instanceof org.tdmx.core.api.v01.msg.UserIdentity) {
			return toLog((org.tdmx.core.api.v01.msg.UserIdentity) object);
		} else if (object instanceof org.tdmx.core.api.v01.msg.Administrator) {
			org.tdmx.core.api.v01.msg.Administrator admin = (org.tdmx.core.api.v01.msg.Administrator) object;
			return verbose ? toVerbose(admin) : toLog(admin);
		} else if (object instanceof org.tdmx.core.api.v01.msg.AdministratorIdentity) {
			org.tdmx.core.api.v01.msg.AdministratorIdentity ai = (org.tdmx.core.api.v01.msg.AdministratorIdentity) object;
			return verbose ? toVerbose(ai) : toLog(ai);
		} else if (object instanceof org.tdmx.core.api.v01.msg.Channelinfo) {
			return toLog((org.tdmx.core.api.v01.msg.Channelinfo) object);
		} else if (object instanceof org.tdmx.core.api.v01.msg.Channelauthorization) {
			return toLog((org.tdmx.core.api.v01.msg.Channelauthorization) object);
		} else if (object instanceof org.tdmx.core.api.v01.msg.Sessioninfo) {
			return toLog((org.tdmx.core.api.v01.msg.Sessioninfo) object);
		} else if (object instanceof org.tdmx.core.api.v01.msg.FlowStatus) {
			return toLog((org.tdmx.core.api.v01.msg.FlowStatus) object);
		} else if (object instanceof org.tdmx.core.api.v01.common.Ps) {
			return toLog((org.tdmx.core.api.v01.common.Ps) object);
		} else if (object instanceof org.tdmx.core.api.v01.msg.Limit) {
			return toLog((org.tdmx.core.api.v01.msg.Limit) object);
		} else if (object instanceof org.tdmx.core.api.v01.msg.Destinationsession) {
			return toLog((org.tdmx.core.api.v01.msg.Destinationsession) object);
		} else if (object instanceof org.tdmx.core.api.v01.msg.Permission) {
			return toLog((org.tdmx.core.api.v01.msg.Permission) object);
		} else if (object instanceof org.tdmx.core.api.v01.msg.Signaturevalue) {
			return toLog((org.tdmx.core.api.v01.msg.Signaturevalue) object);
		} else if (object instanceof org.tdmx.core.api.v01.msg.Administratorsignature) {
			return toLog((org.tdmx.core.api.v01.msg.Administratorsignature) object);
		} else if (object instanceof org.tdmx.core.api.v01.msg.Usersignature) {
			return toLog((org.tdmx.core.api.v01.msg.Usersignature) object);
		} else if (object instanceof org.tdmx.core.api.v01.msg.Currentchannelauthorization) {
			return toLog((org.tdmx.core.api.v01.msg.Currentchannelauthorization) object);
		} else if (object instanceof org.tdmx.core.api.v01.msg.RequestedChannelAuthorization) {
			return toLog((org.tdmx.core.api.v01.msg.RequestedChannelAuthorization) object);
		} else if (object instanceof TrustStoreEntry) {
			return toLog((TrustStoreEntry) object);
		} else if (object instanceof PKIXCertificate) {
			return toLog((PKIXCertificate) object);
		} else if (object instanceof Calendar) {
			return CalendarUtils.toString((Calendar) object);
		} else if (object instanceof org.tdmx.core.api.v01.msg.Service) {
			return toString((org.tdmx.core.api.v01.msg.Service) object);
		} else if (object instanceof org.tdmx.core.api.v01.msg.Address) {
			return toString((org.tdmx.core.api.v01.msg.Address) object);
		} else if (object instanceof org.tdmx.core.api.v01.msg.Channel) {
			return toString((org.tdmx.core.api.v01.msg.Channel) object);
		}

		return null;
	}

	public PrintableObject toLog(Exception e) {
		PrintableObject result = new PrintableObject(e.getClass().getName());
		result.add("message", e.getMessage());

		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		result.addVerbose("stacktrace", sw.toString());
		return result;
	}

	public PrintableObject toLog(org.tdmx.core.api.v01.common.Error error) {
		return new PrintableObject("Error").add("code", error.getCode()).add("description", error.getDescription());
	}

	public PrintableObject toLog(org.tdmx.core.api.v01.msg.Channelinfo ci) {
		PrintableObject result = new PrintableObject("ChannelInfo");
		result.add("authorization", ci.getChannelauthorization());
		result.add("session", ci.getSessioninfo() != null ? ci.getSessioninfo() : "none");
		result.add("flowstatus", ci.getStatus());
		return result;
	}

	public PrintableObject toLog(org.tdmx.core.api.v01.msg.Channelauthorization ca) {
		PrintableObject result = new PrintableObject("ChannelAuthorization");
		result.add("domain", ca.getDomain());
		result.add("channel", ca.getChannel());
		if (ca.getCurrent() != null) {
			result.add("send-permission", ca.getCurrent().getOriginPermission());
		}
		if (ca.getUnconfirmed() != null) {
			result.add("requested-send", ca.getUnconfirmed().getOriginPermission());
		}
		if (ca.getCurrent() != null) {
			result.add("recv-permission", ca.getCurrent().getDestinationPermission());
		}
		if (ca.getUnconfirmed() != null) {
			result.add("requested-recv", ca.getUnconfirmed().getDestinationPermission());
		}
		if (ca.getCurrent() != null) {
			result.add("limit", ca.getCurrent().getLimit());
			result.add("signature", ca.getCurrent().getAdministratorsignature());
		}
		result.add("status", ca.getPs());

		return result;
	}

	public PrintableObject toLog(org.tdmx.core.api.v01.msg.Sessioninfo si) {
		PrintableObject result = new PrintableObject("Session");
		if (si.getDestinationsession() != null) {
			result.add("destination", si.getDestinationsession());
		} else {
			result.add("destination", "none");
		}
		if (si.getPs() != null) {
			result.add("status", si.getPs());
		} else {
			result.add("destination", "none");
		}
		return result;
	}

	public PrintableObject toLog(org.tdmx.core.api.v01.msg.FlowStatus fs) {
		return new PrintableObject("FlowStatus").add("relayStatus", fs.getRelayStatus())
				.add("flowstatus", fs.getFlowStatus()).add("usedbytes", fs.getUsedBytes());
	}

	public PrintableObject toLog(org.tdmx.core.api.v01.common.Ps ps) {
		return new PrintableObject("ProcessingState").add("error", ps.getError()).add("status", ps.getStatus())
				.add("timestamp", ps.getTimestamp());
	}

	public String toLog(org.tdmx.core.api.v01.msg.Administrator admin) {
		PKIXCertificate pk = CertificateIOUtils.safeDecodeX509(admin.getAdministratorIdentity().getDomaincertificate());

		return pk.getCommonName() + " (" + admin.getStatus() + ")";
	}

	public PrintableObject toVerbose(org.tdmx.core.api.v01.msg.Administrator admin) {
		PrintableObject result = new PrintableObject("Administrator");

		PKIXCertificate pk = CertificateIOUtils.safeDecodeX509(admin.getAdministratorIdentity().getDomaincertificate());

		result.add("name", pk.getCommonName());
		result.add("status", admin.getStatus());
		result.addVerbose("identity", admin.getAdministratorIdentity());
		return result;
	}

	public String toLog(org.tdmx.core.api.v01.msg.AdministratorIdentity admin) {
		PKIXCertificate dc = CertificateIOUtils.safeDecodeX509(admin.getDomaincertificate());

		return dc.getCommonName() + " (" + dc.getFingerprint() + ")";
	}

	public PrintableObject toVerbose(org.tdmx.core.api.v01.msg.AdministratorIdentity admin) {
		PrintableObject result = new PrintableObject("AdministratorIdentity");

		PKIXCertificate dc = CertificateIOUtils.safeDecodeX509(admin.getDomaincertificate());
		result.addVerbose("domainCertificate", dc);
		PKIXCertificate zr = CertificateIOUtils.safeDecodeX509(admin.getRootcertificate());
		result.addVerbose("zoneCertificate", zr);
		return result;
	}

	public PrintableObject toLog(org.tdmx.core.api.v01.msg.UserIdentity u) {
		PrintableObject result = new PrintableObject("UserIdentity");

		PKIXCertificate uc = CertificateIOUtils.safeDecodeX509(u.getUsercertificate());
		result.add("userCertificate", uc);
		PKIXCertificate dc = CertificateIOUtils.safeDecodeX509(u.getDomaincertificate());
		result.addVerbose("domainCertificate", dc);
		PKIXCertificate zr = CertificateIOUtils.safeDecodeX509(u.getRootcertificate());
		result.addVerbose("zoneCertificate", zr);
		return result;
	}

	public String toLog(org.tdmx.core.api.v01.msg.User u) {
		PKIXCertificate pk = CertificateIOUtils.safeDecodeX509(u.getUserIdentity().getUsercertificate());

		return pk.getCommonName() + " (" + u.getStatus() + ")";
	}

	public PrintableObject toVerbose(org.tdmx.core.api.v01.msg.User u) {
		PrintableObject result = new PrintableObject("User");

		PKIXCertificate pk = CertificateIOUtils.safeDecodeX509(u.getUserIdentity().getUsercertificate());

		result.add("name", pk.getCommonName());
		result.add("status", u.getStatus());
		result.addVerbose("identity", u.getUserIdentity());
		return result;
	}

	public PrintableObject toLog(org.tdmx.core.api.v01.msg.Currentchannelauthorization cca) {
		PrintableObject result = new PrintableObject("Current");

		result.add("originPermission", cca.getOriginPermission() != null ? cca.getOriginPermission() : "none");
		result.add("destinationPermission",
				cca.getDestinationPermission() != null ? cca.getDestinationPermission() : "none");
		return result;
	}

	public PrintableObject toLog(org.tdmx.core.api.v01.msg.RequestedChannelAuthorization rca) {
		PrintableObject result = new PrintableObject("Requested");

		result.add("originPermission", rca.getOriginPermission() != null ? rca.getOriginPermission() : "none");
		result.add("destinationPermission",
				rca.getDestinationPermission() != null ? rca.getDestinationPermission() : "none");
		return result;
	}

	public PrintableObject toLog(org.tdmx.core.api.v01.msg.Permission p) {
		PrintableObject result = new PrintableObject("Permission");
		result.add("grant", EnumUtils.mapToString(p.getPermission()));
		result.add("maxPlaintextSizeBytes", p.getMaxPlaintextSizeBytes());
		result.addVerbose("signature", p.getAdministratorsignature());
		return result;
	}

	public PrintableObject toLog(org.tdmx.core.api.v01.msg.Signaturevalue sv) {
		PrintableObject result = new PrintableObject("SignatureValue");
		result.add("timestamp", sv.getTimestamp());
		result.add("algorithm", sv.getSignatureAlgorithm());
		result.add("signature", sv.getSignature());
		return result;
	}

	public PrintableObject toLog(org.tdmx.core.api.v01.msg.Administratorsignature sig) {
		PrintableObject result = new PrintableObject("AdministratorSignature");
		result.add("identity", sig.getAdministratorIdentity());
		result.add("signature", sig.getSignaturevalue());
		return result;
	}

	public PrintableObject toLog(org.tdmx.core.api.v01.msg.Usersignature sig) {
		PrintableObject result = new PrintableObject("UserSignature");
		result.add("identity", sig.getUserIdentity());
		result.add("signature", sig.getSignaturevalue());
		return result;
	}

	private PrintableObject toLog(org.tdmx.core.api.v01.msg.Limit li) {
		return new PrintableObject("Limit").add("high", li.getHighBytes()).add("low", li.getLowBytes());
	}

	public static PrintableObject toLog(org.tdmx.core.api.v01.msg.Destinationsession ds) {
		PrintableObject result = new PrintableObject("DestinationSession");
		result.add("contextId", ds.getEncryptionContextId());
		result.add("scheme", ds.getScheme());
		result.add("sessionKey", ByteArray.asHex(ds.getSessionKey()));
		result.addVerbose("usersignature", ds.getUsersignature());
		return result;
	}

	public PrintableObject toLog(PKIXCertificate pk) {
		PrintableObject result = null;
		if (pk.isTdmxZoneAdminCertificate()) {
			result = new PrintableObject("ZoneAdministrator");
			result.add("zone", pk.getTdmxZoneInfo().getZoneRoot());
			result.add("email", pk.getEmailAddress());
			result.add("name", pk.getCommonName());
			result.add("tel", pk.getTelephoneNumber());
			result.add("location", pk.getLocation());
			result.add("country", pk.getCountry());
		} else if (pk.isTdmxDomainAdminCertificate()) {
			result = new PrintableObject("DomainAdministrator");
			result.add("domain", pk.getTdmxDomainName());

		} else if (pk.isTdmxUserCertificate()) {
			result = new PrintableObject("User");
			result.add("name", pk.getTdmxUserName());

		} else {
			result = new PrintableObject("X509Certificate");
		}
		result.add("serialNumber", pk.getSerialNumber());
		result.add("fingerprint", pk.getFingerprint());
		result.addVerbose("pem", CertificateIOUtils.safeX509certsToPem(new PKIXCertificate[] { pk }));
		return result;
	}

	public PrintableObject toLog(TrustStoreEntry entry) {
		return new PrintableObject("TrustStoreEntry").add("friendlyName", entry.getFriendlyName())
				.add("certificate", entry.getCertificate()).add("comment", entry.getComment());
	}

	public String toString(org.tdmx.core.api.v01.msg.Service service) {
		return service.getDomain() + "#" + service.getServicename();
	}

	public String toString(org.tdmx.core.api.v01.msg.Address address) {
		return address.getLocalname() + "@" + address.getDomain();
	}

	public String toString(org.tdmx.core.api.v01.msg.Channel channel) {
		return channel.getOrigin().getLocalname() + "@" + channel.getOrigin().getDomain() + "->"
				+ channel.getDestination().getLocalname() + "@" + channel.getDestination().getDomain() + "#"
				+ channel.getDestination().getServicename();
	}

}
