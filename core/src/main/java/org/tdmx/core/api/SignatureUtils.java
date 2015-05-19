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
package org.tdmx.core.api;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;

import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.client.crypto.certificate.StringSigningUtils;
import org.tdmx.client.crypto.converters.ByteArray;
import org.tdmx.core.api.v01.msg.AdministratorIdentity;
import org.tdmx.core.api.v01.msg.Administratorsignature;
import org.tdmx.core.api.v01.msg.Channel;
import org.tdmx.core.api.v01.msg.Currentchannelauthorization;
import org.tdmx.core.api.v01.msg.EndpointPermission;
import org.tdmx.core.api.v01.msg.Signaturevalue;
import org.tdmx.core.system.lang.CalendarUtils;
import org.tdmx.core.system.lang.StringUtils;

public class SignatureUtils {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final String MISSING = "-";

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
	private SignatureUtils() {
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public static boolean checkEndpointPermissionSignature(Channel channel, EndpointPermission perm,
			boolean checkValidUntil) {
		PKIXCertificate publicCert = CertificateIOUtils.safeDecodeX509(perm.getAdministratorsignature()
				.getAdministratorIdentity().getDomaincertificate());
		SignatureAlgorithm alg = SignatureAlgorithm.valueOf(perm.getAdministratorsignature().getSignaturevalue()
				.getSignatureAlgorithm().toString());

		return CalendarUtils.isInPast(perm.getAdministratorsignature().getSignaturevalue().getTimestamp())
				&& checkEndpointPermissionSignature(publicCert, alg, channel, perm)
				&& (!checkValidUntil || CalendarUtils.isInFuture(perm.getValidUntil()));
	}

	private static boolean checkEndpointPermissionSignature(PKIXCertificate signingPublicCert, SignatureAlgorithm alg,
			Channel channel, EndpointPermission perm) {
		String valueToSign = getValueToSign(channel, perm);
		String signatureHex = perm.getAdministratorsignature().getSignaturevalue().getSignature();

		return StringSigningUtils.checkHexSignature(signingPublicCert.getCertificate().getPublicKey(), alg,
				valueToSign, signatureHex);
	}

	public static void createEndpointPermissionSignature(PKIXCredential credential, SignatureAlgorithm alg,
			Date signatureDate, Channel channel, EndpointPermission perm) {
		AdministratorIdentity id = new AdministratorIdentity();
		id.setDomaincertificate(credential.getPublicCert().getX509Encoded());
		id.setRootcertificate(credential.getZoneRootPublicCert().getX509Encoded());

		Signaturevalue sig = new Signaturevalue();
		sig.setTimestamp(CalendarUtils.getDate(signatureDate));
		sig.setSignatureAlgorithm(org.tdmx.core.api.v01.msg.SignatureAlgorithm.fromValue(alg.getAlgorithm()));

		Administratorsignature signature = new Administratorsignature();
		signature.setAdministratorIdentity(id);
		signature.setSignaturevalue(sig);
		perm.setAdministratorsignature(signature);

		String valueToSign = getValueToSign(channel, perm);
		sig.setSignature(StringSigningUtils.getHexSignature(credential.getPrivateKey(), alg, valueToSign));
	}

	public static boolean checkChannelAuthorizationSignature(Currentchannelauthorization ca) {
		PKIXCertificate publicCert = CertificateIOUtils.safeDecodeX509(ca.getAdministratorsignature()
				.getAdministratorIdentity().getDomaincertificate());
		SignatureAlgorithm alg = SignatureAlgorithm.valueOf(ca.getAdministratorsignature().getSignaturevalue()
				.getSignatureAlgorithm().toString());

		return CalendarUtils.isInPast(ca.getAdministratorsignature().getSignaturevalue().getTimestamp())
				&& checkEndpointPermissionSignature(publicCert, alg, ca);
	}

	private static boolean checkEndpointPermissionSignature(PKIXCertificate signingPublicCert, SignatureAlgorithm alg,
			Currentchannelauthorization ca) {
		String valueToSign = getValueToSign(ca);
		String signatureHex = ca.getAdministratorsignature().getSignaturevalue().getSignature();

		return StringSigningUtils.checkHexSignature(signingPublicCert.getCertificate().getPublicKey(), alg,
				valueToSign, signatureHex);
	}

	public static void createChannelAuthorizationSignature(PKIXCredential credential, SignatureAlgorithm alg,
			Date signatureDate, Currentchannelauthorization ca) {
		AdministratorIdentity id = new AdministratorIdentity();
		id.setDomaincertificate(credential.getPublicCert().getX509Encoded());
		id.setRootcertificate(credential.getZoneRootPublicCert().getX509Encoded());

		Signaturevalue sig = new Signaturevalue();
		sig.setTimestamp(CalendarUtils.getDate(signatureDate));
		sig.setSignatureAlgorithm(org.tdmx.core.api.v01.msg.SignatureAlgorithm.fromValue(alg.getAlgorithm()));

		Administratorsignature signature = new Administratorsignature();
		signature.setAdministratorIdentity(id);
		signature.setSignaturevalue(sig);
		ca.setAdministratorsignature(signature);

		String valueToSign = getValueToSign(ca);
		sig.setSignature(StringSigningUtils.getHexSignature(credential.getPrivateKey(), alg, valueToSign));
	}

	public static String getValueToSign(Channel channel, EndpointPermission perm) {
		StringBuilder value = new StringBuilder();
		// channel origin -> destination + service
		value.append(toValue(channel.getOrigin().getLocalname()));
		value.append(toValue(channel.getOrigin().getDomain()));
		value.append(toValue(channel.getOrigin().getServiceprovider()));
		value.append(toValue(channel.getDestination().getLocalname()));
		value.append(toValue(channel.getDestination().getDomain()));
		value.append(toValue(channel.getDestination().getServiceprovider()));
		value.append(toValue(channel.getDestination().getServicename()));
		// permission data
		value.append(toValue(perm.getPermission()));
		value.append(toValue(perm.getValidUntil()));
		value.append(toValue(perm.getMaxPlaintextSizeBytes()));
		// signer
		value.append(toValue(perm.getAdministratorsignature().getAdministratorIdentity().getDomaincertificate()));
		value.append(toValue(perm.getAdministratorsignature().getAdministratorIdentity().getRootcertificate()));
		// signature details
		value.append(toValue(perm.getAdministratorsignature().getSignaturevalue().getTimestamp()));
		value.append(toValue(perm.getAdministratorsignature().getSignaturevalue().getSignatureAlgorithm()));

		return value.toString();
	}

	public static String getValueToSign(Currentchannelauthorization ca) {
		StringBuilder value = new StringBuilder();
		// channel origin -> destination + service
		value.append(toValue(ca.getChannel().getOrigin().getLocalname()));
		value.append(toValue(ca.getChannel().getOrigin().getDomain()));
		value.append(toValue(ca.getChannel().getOrigin().getServiceprovider()));
		value.append(toValue(ca.getChannel().getDestination().getLocalname()));
		value.append(toValue(ca.getChannel().getDestination().getDomain()));
		value.append(toValue(ca.getChannel().getDestination().getServiceprovider()));
		value.append(toValue(ca.getChannel().getDestination().getServicename()));
		// send and receive permissions
		if (ca.getOrigin() != null) {
			appendValueToSign(value, ca.getOrigin());
		} else {
			value.append(MISSING);
		}
		if (ca.getDestination() != null) {
			appendValueToSign(value, ca.getDestination());
		} else {
			value.append(MISSING);
		}
		// flowcontrol limits
		if (ca.getLimit().getUnsentBuffer() != null) {
			appendValueToSign(value, ca.getLimit().getUnsentBuffer());
		} else {
			value.append(MISSING);
		}
		if (ca.getLimit().getUndeliveredBuffer() != null) {
			appendValueToSign(value, ca.getLimit().getUndeliveredBuffer());
		} else {
			value.append(MISSING);
		}
		// signer
		value.append(toValue(ca.getAdministratorsignature().getAdministratorIdentity().getDomaincertificate()));
		value.append(toValue(ca.getAdministratorsignature().getAdministratorIdentity().getRootcertificate()));
		// signature details
		value.append(toValue(ca.getAdministratorsignature().getSignaturevalue().getTimestamp()));
		value.append(toValue(ca.getAdministratorsignature().getSignaturevalue().getSignatureAlgorithm()));
		// permission data
		return value.toString();
	}

	public static void appendValueToSign(StringBuilder value, org.tdmx.core.api.v01.msg.Limit limit) {
		value.append(limit.getHighBytes());
		value.append(limit.getLowBytes());
	}

	public static void appendValueToSign(StringBuilder value, EndpointPermission perm) {
		value.append(toValue(perm.getPermission()));
		value.append(toValue(perm.getValidUntil()));
		value.append(toValue(perm.getMaxPlaintextSizeBytes()));
		// signer
		value.append(toValue(perm.getAdministratorsignature().getAdministratorIdentity().getDomaincertificate()));
		value.append(toValue(perm.getAdministratorsignature().getAdministratorIdentity().getRootcertificate()));
		// signature details
		value.append(toValue(perm.getAdministratorsignature().getSignaturevalue().getTimestamp()));
		value.append(toValue(perm.getAdministratorsignature().getSignaturevalue().getSignatureAlgorithm()));
		value.append(toValue(perm.getAdministratorsignature().getSignaturevalue().getSignature()));
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private static String toValue(byte[] b) {
		if (b == null || b.length == 0) {
			return MISSING;
		}
		return ByteArray.asHex(b);
	}

	private static String toValue(org.tdmx.core.api.v01.msg.SignatureAlgorithm alg) {
		if (alg == null) {
			return MISSING;
		}
		return alg.toString();
	}

	private static String toValue(org.tdmx.core.api.v01.msg.Permission perm) {
		if (perm == null) {
			return MISSING;
		}
		return perm.toString();
	}

	private static String toValue(String str) {
		if (!StringUtils.hasText(str)) {
			return MISSING;
		}
		return str;
	}

	private static String toValue(BigInteger bigint) {
		if (bigint == null) {
			return MISSING;
		}
		return bigint.toString();
	}

	/**
	 * The current time as UTC milliseconds from the epoch as a string.
	 * 
	 * @param cal
	 * @return the current time as UTC milliseconds from the epoch as a string
	 */
	private static String toValue(Calendar cal) {
		if (cal == null) {
			return MISSING;
		}
		return "" + cal.getTimeInMillis();
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

}