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

import org.tdmx.client.crypto.algorithm.DigestAlgorithm;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.client.crypto.certificate.StringSigningUtils;
import org.tdmx.client.crypto.converters.ByteArray;
import org.tdmx.client.crypto.converters.StringToUtf8;
import org.tdmx.client.crypto.scheme.CryptoException;
import org.tdmx.client.crypto.scheme.IntegratedCryptoScheme;
import org.tdmx.core.api.v01.common.Error;
import org.tdmx.core.api.v01.msg.AdministratorIdentity;
import org.tdmx.core.api.v01.msg.Administratorsignature;
import org.tdmx.core.api.v01.msg.Channel;
import org.tdmx.core.api.v01.msg.Chunk;
import org.tdmx.core.api.v01.msg.Currentchannelauthorization;
import org.tdmx.core.api.v01.msg.Destinationsession;
import org.tdmx.core.api.v01.msg.Dr;
import org.tdmx.core.api.v01.msg.Header;
import org.tdmx.core.api.v01.msg.Msgreference;
import org.tdmx.core.api.v01.msg.Payload;
import org.tdmx.core.api.v01.msg.Permission;
import org.tdmx.core.api.v01.msg.Signaturevalue;
import org.tdmx.core.api.v01.msg.UserIdentity;
import org.tdmx.core.api.v01.msg.UserSignature;
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
	public static boolean checkMsgId(Header header, Payload payload, Calendar sentTS) {
		return header != null && sentTS != null && header.getMsgId() != null
				&& header.getMsgId().equals(calculateMsgId(header, payload, sentTS));
	}

	public static void setMsgId(Header header, Payload payload, Calendar sentTS) {
		if (header != null && payload != null && sentTS != null) {
			header.setMsgId(calculateMsgId(header, payload, sentTS));
		}
	}

	public static boolean checkMessageSignature(Header header, Payload payload) {
		PKIXCertificate signingPublicCert = CertificateIOUtils
				.safeDecodeX509(header.getUsersignature().getUserIdentity().getUsercertificate());
		SignatureAlgorithm alg = SignatureAlgorithm
				.getByAlgorithmName(header.getUsersignature().getSignaturevalue().getSignatureAlgorithm().value());

		String valueToSign = getValueToSign(header, payload);
		String signatureHex = header.getUsersignature().getSignaturevalue().getSignature();

		return StringSigningUtils.checkHexSignature(signingPublicCert.getCertificate().getPublicKey(), alg, valueToSign,
				signatureHex);
	}

	public static boolean checkChunkMac(Chunk chunk, IntegratedCryptoScheme ies) {
		DigestAlgorithm da = ies.getChunkMACAlgorithm();
		try {
			byte[] mac = da.kdf(chunk.getData());
			String macHex = ByteArray.asHex(mac);
			return macHex.equalsIgnoreCase(chunk.getMac());
		} catch (CryptoException e) {
			throw new IllegalStateException(e);
		}
	}

	public static boolean checkDeliveryReceiptSignature(Dr dr) {
		PKIXCertificate signingPublicCert = CertificateIOUtils
				.safeDecodeX509(dr.getReceiptsignature().getUserIdentity().getUsercertificate());
		SignatureAlgorithm alg = SignatureAlgorithm
				.getByAlgorithmName(dr.getReceiptsignature().getSignaturevalue().getSignatureAlgorithm().value());

		String valueToSign = getValueToSign(dr);
		String signatureHex = dr.getReceiptsignature().getSignaturevalue().getSignature();

		return StringSigningUtils.checkHexSignature(signingPublicCert.getCertificate().getPublicKey(), alg, valueToSign,
				signatureHex);
	}

	public static void createDeliveryReceiptSignature(PKIXCredential credential, SignatureAlgorithm alg,
			Date signatureDate, Dr dr) {
		UserSignature us = new UserSignature();

		UserIdentity id = new UserIdentity();
		id.setUsercertificate(credential.getPublicCert().getX509Encoded());
		id.setDomaincertificate(credential.getIssuerPublicCert().getX509Encoded());
		id.setRootcertificate(credential.getZoneRootPublicCert().getX509Encoded());

		Signaturevalue sig = new Signaturevalue();
		sig.setTimestamp(CalendarUtils.getTimestamp(signatureDate));
		sig.setSignatureAlgorithm(org.tdmx.core.api.v01.msg.SignatureAlgorithm.fromValue(alg.getAlgorithm()));

		us.setSignaturevalue(sig);
		us.setUserIdentity(id);
		dr.setReceiptsignature(us);

		String valueToSignHeader = getValueToSign(dr);
		sig.setSignature(StringSigningUtils.getHexSignature(credential.getPrivateKey(), alg, valueToSignHeader));
	}

	public static void createMessageSignature(PKIXCredential credential, SignatureAlgorithm alg, Calendar signatureDate,
			Header header, Payload payload) {
		UserSignature us = new UserSignature();

		UserIdentity id = new UserIdentity();
		id.setUsercertificate(credential.getPublicCert().getX509Encoded());
		id.setDomaincertificate(credential.getIssuerPublicCert().getX509Encoded());
		id.setRootcertificate(credential.getZoneRootPublicCert().getX509Encoded());

		Signaturevalue sig = new Signaturevalue();
		sig.setTimestamp(signatureDate);
		sig.setSignatureAlgorithm(org.tdmx.core.api.v01.msg.SignatureAlgorithm.fromValue(alg.getAlgorithm()));

		us.setSignaturevalue(sig);
		us.setUserIdentity(id);
		header.setUsersignature(us);

		String valueToSignHeader = getValueToSign(header, payload);
		sig.setSignature(StringSigningUtils.getHexSignature(credential.getPrivateKey(), alg, valueToSignHeader));
		// sig is in the header
	}

	public static boolean checkEndpointPermissionSignature(Channel channel, Permission perm) {
		PKIXCertificate publicCert = CertificateIOUtils
				.safeDecodeX509(perm.getAdministratorsignature().getAdministratorIdentity().getDomaincertificate());
		SignatureAlgorithm alg = SignatureAlgorithm.getByAlgorithmName(
				perm.getAdministratorsignature().getSignaturevalue().getSignatureAlgorithm().value());

		return CalendarUtils.isInPast(perm.getAdministratorsignature().getSignaturevalue().getTimestamp())
				&& checkEndpointPermissionSignature(publicCert, alg, channel, perm);
	}

	public static void createEndpointPermissionSignature(PKIXCredential credential, SignatureAlgorithm alg,
			Date signatureDate, Channel channel, Permission perm) {
		AdministratorIdentity id = new AdministratorIdentity();
		id.setDomaincertificate(credential.getPublicCert().getX509Encoded());
		id.setRootcertificate(credential.getZoneRootPublicCert().getX509Encoded());

		Signaturevalue sig = new Signaturevalue();
		sig.setTimestamp(CalendarUtils.getTimestamp(signatureDate));
		sig.setSignatureAlgorithm(org.tdmx.core.api.v01.msg.SignatureAlgorithm.fromValue(alg.getAlgorithm()));

		Administratorsignature signature = new Administratorsignature();
		signature.setAdministratorIdentity(id);
		signature.setSignaturevalue(sig);
		perm.setAdministratorsignature(signature);

		String valueToSign = getValueToSign(channel, perm);
		sig.setSignature(StringSigningUtils.getHexSignature(credential.getPrivateKey(), alg, valueToSign));
	}

	public static boolean checkChannelAuthorizationSignature(Channel c, Currentchannelauthorization ca) {
		PKIXCertificate publicCert = CertificateIOUtils
				.safeDecodeX509(ca.getAdministratorsignature().getAdministratorIdentity().getDomaincertificate());
		SignatureAlgorithm alg = SignatureAlgorithm
				.getByAlgorithmName(ca.getAdministratorsignature().getSignaturevalue().getSignatureAlgorithm().value());

		return CalendarUtils.isInPast(ca.getAdministratorsignature().getSignaturevalue().getTimestamp())
				&& checkEndpointPermissionSignature(publicCert, alg, c, ca);
	}

	public static void createChannelAuthorizationSignature(PKIXCredential credential, SignatureAlgorithm alg,
			Date signatureDate, Channel c, Currentchannelauthorization ca) {
		AdministratorIdentity id = new AdministratorIdentity();
		id.setDomaincertificate(credential.getPublicCert().getX509Encoded());
		id.setRootcertificate(credential.getZoneRootPublicCert().getX509Encoded());

		Signaturevalue sig = new Signaturevalue();
		sig.setTimestamp(CalendarUtils.getTimestamp(signatureDate));
		sig.setSignatureAlgorithm(org.tdmx.core.api.v01.msg.SignatureAlgorithm.fromValue(alg.getAlgorithm()));

		Administratorsignature signature = new Administratorsignature();
		signature.setAdministratorIdentity(id);
		signature.setSignaturevalue(sig);
		ca.setAdministratorsignature(signature);

		String valueToSign = getValueToSign(c, ca);
		sig.setSignature(StringSigningUtils.getHexSignature(credential.getPrivateKey(), alg, valueToSign));
	}

	public static void createDestinationSessionSignature(PKIXCredential credential, SignatureAlgorithm alg,
			Date signatureDate, String serviceName, Destinationsession ds) {

		UserIdentity id = new UserIdentity();
		id.setUsercertificate(credential.getPublicCert().getX509Encoded());
		id.setDomaincertificate(credential.getIssuerPublicCert().getX509Encoded());
		id.setRootcertificate(credential.getZoneRootPublicCert().getX509Encoded());

		Signaturevalue sig = new Signaturevalue();
		sig.setTimestamp(CalendarUtils.getTimestamp(signatureDate));
		sig.setSignatureAlgorithm(org.tdmx.core.api.v01.msg.SignatureAlgorithm.fromValue(alg.getAlgorithm()));

		UserSignature signature = new UserSignature();
		signature.setUserIdentity(id);
		signature.setSignaturevalue(sig);
		ds.setUsersignature(signature);

		String valueToSign = getValueToSign(serviceName, ds);
		sig.setSignature(StringSigningUtils.getHexSignature(credential.getPrivateKey(), alg, valueToSign));
	}

	public static boolean checkDestinationSessionSignature(String serviceName, Destinationsession ds) {
		PKIXCertificate publicCert = CertificateIOUtils
				.safeDecodeX509(ds.getUsersignature().getUserIdentity().getUsercertificate());
		SignatureAlgorithm alg = SignatureAlgorithm
				.getByAlgorithmName(ds.getUsersignature().getSignaturevalue().getSignatureAlgorithm().value());

		return CalendarUtils.isInPast(ds.getUsersignature().getSignaturevalue().getTimestamp())
				&& checkDestinationSessionSignature(publicCert, alg, serviceName, ds);
	}

	public static String createContinuationId(int chunkPos, byte[] entropy, String msgId, int len) {
		StringBuffer sb = new StringBuffer();
		sb.append(toValue(msgId));
		sb.append(toValue(chunkPos));
		sb.append(toValue(entropy));
		String valueToHash = sb.toString();

		DigestAlgorithm alg = DigestAlgorithm.SHA_256;
		byte[] hashedByes;
		try {
			hashedByes = alg.kdf(StringToUtf8.toBytes(valueToHash));
		} catch (CryptoException e) {
			return null;
		}
		return ByteArray.asHex(hashedByes).substring(0, len);
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private static String calculateMsgId(Header header, Payload payload, Calendar sentTS) {
		StringBuffer sb = new StringBuffer();
		// channel
		sb.append(toValue(header.getChannel().getOrigin().getLocalname()));
		sb.append(toValue(header.getChannel().getOrigin().getDomain()));
		sb.append(toValue(header.getChannel().getDestination().getLocalname()));
		sb.append(toValue(header.getChannel().getDestination().getDomain()));
		sb.append(toValue(header.getChannel().getDestination().getServicename()));
		// timestamp
		sb.append(toValue(sentTS));
		// payload signature with sentTS makes the msgId value unique per payload
		sb.append(toValue(payload.getMACofMACs()));
		String valueToHash = sb.toString();

		DigestAlgorithm alg = DigestAlgorithm.SHA_256;
		byte[] hashedByes;
		try {
			hashedByes = alg.kdf(StringToUtf8.toBytes(valueToHash));
		} catch (CryptoException e) {
			return null;
		}
		return ByteArray.asHex(hashedByes);
	}

	private static String getValueToSign(Dr dr) {
		StringBuilder value = new StringBuilder();

		appendValueToSign(value, dr.getMsgreference());
		if (dr.getError() != null) {
			appendValueToSign(value, dr.getError());
		}
		value.append(toValue(dr.getReceiptsignature().getSignaturevalue().getTimestamp()));

		return value.toString();
	}

	private static String getValueToSign(Header header, Payload payload) {
		StringBuilder value = new StringBuilder();

		value.append(toValue(header.getMsgId()));
		value.append(toValue(header.getUsersignature().getSignaturevalue().getTimestamp()));
		value.append(toValue(header.getTtl()));
		appendValueToSign(value, header.getChannel());
		appendValueToSign(value, header.getTo());
		value.append(toValue(header.getEncryptionContextId()));
		value.append(toValue(header.getScheme()));

		value.append(toValue(payload.getLength()));
		value.append(toValue(payload.getEncryptionContext()));
		value.append(toValue(payload.getPlaintextLength()));
		value.append(toValue(payload.getMACofMACs()));
		value.append(toValue(header.getExternalReference()));

		return value.toString();
	}

	private static void appendValueToSign(StringBuilder value, Error error) {
		value.append(toValue(error.getCode()));
		value.append(toValue(error.getDescription()));
	}

	private static void appendValueToSign(StringBuilder value, Msgreference mr) {
		value.append(toValue(mr.getMsgId()));
		value.append(toValue(mr.getExternalReference()));
		value.append(toValue(mr.getSignature()));
	}

	private static void appendValueToSign(StringBuilder value, UserIdentity uc) {
		value.append(toValue(uc.getUsercertificate()));
		value.append(toValue(uc.getDomaincertificate()));
		value.append(toValue(uc.getRootcertificate()));
	}

	private static void appendValueToSign(StringBuilder value, AdministratorIdentity dac) {
		value.append(toValue(dac.getDomaincertificate()));
		value.append(toValue(dac.getRootcertificate()));
	}

	private static void appendValueToSign(StringBuilder value, Channel channel) {
		// channel origin -> destination + service
		value.append(toValue(channel.getOrigin().getLocalname()));
		value.append(toValue(channel.getOrigin().getDomain()));
		value.append(toValue(channel.getDestination().getLocalname()));
		value.append(toValue(channel.getDestination().getDomain()));
		value.append(toValue(channel.getDestination().getServicename()));
	}

	private static String getValueToSign(Channel channel, Permission perm) {
		StringBuilder value = new StringBuilder();
		appendValueToSign(value, channel);
		// permission data
		value.append(toValue(perm.getPermission()));
		value.append(toValue(perm.getMaxPlaintextSizeBytes()));
		// signer
		appendValueToSign(value, perm.getAdministratorsignature().getAdministratorIdentity());
		// signature details
		value.append(toValue(perm.getAdministratorsignature().getSignaturevalue().getTimestamp()));
		value.append(toValue(perm.getAdministratorsignature().getSignaturevalue().getSignatureAlgorithm()));

		return value.toString();
	}

	private static boolean checkDestinationSessionSignature(PKIXCertificate signingPublicCert, SignatureAlgorithm alg,
			String serviceName, Destinationsession fts) {
		String valueToSign = getValueToSign(serviceName, fts);
		String signatureHex = fts.getUsersignature().getSignaturevalue().getSignature();

		return StringSigningUtils.checkHexSignature(signingPublicCert.getCertificate().getPublicKey(), alg, valueToSign,
				signatureHex);
	}

	private static String getValueToSign(String serviceName, Destinationsession ds) {
		StringBuilder value = new StringBuilder();
		// serviceName
		value.append(toValue(serviceName));

		// session
		value.append(toValue(ds.getEncryptionContextId()));
		value.append(toValue(ds.getScheme()));
		value.append(toValue(ds.getSessionKey()));

		// signer
		appendValueToSign(value, ds.getUsersignature().getUserIdentity());
		// signature details
		value.append(toValue(ds.getUsersignature().getSignaturevalue().getTimestamp()));
		value.append(toValue(ds.getUsersignature().getSignaturevalue().getSignatureAlgorithm()));
		// permission data
		return value.toString();
	}

	private static String getValueToSign(Channel c, Currentchannelauthorization ca) {
		StringBuilder value = new StringBuilder();
		// channel origin -> destination + service
		value.append(toValue(c.getOrigin().getLocalname()));
		value.append(toValue(c.getOrigin().getDomain()));
		value.append(toValue(c.getDestination().getLocalname()));
		value.append(toValue(c.getDestination().getDomain()));
		value.append(toValue(c.getDestination().getServicename()));
		// send and receive permissions
		if (ca.getOriginPermission() != null) {
			appendValueToSign(value, ca.getOriginPermission());
		} else {
			value.append(MISSING);
		}
		if (ca.getDestinationPermission() != null) {
			appendValueToSign(value, ca.getDestinationPermission());
		} else {
			value.append(MISSING);
		}
		// flowcontrol limits
		if (ca.getLimit() != null) {
			appendValueToSign(value, ca.getLimit());
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

	private static void appendValueToSign(StringBuilder value, org.tdmx.core.api.v01.msg.Limit limit) {
		value.append(limit.getHighBytes());
		value.append(limit.getLowBytes());
	}

	private static void appendValueToSign(StringBuilder value, Permission perm) {
		value.append(toValue(perm.getPermission()));
		value.append(toValue(perm.getMaxPlaintextSizeBytes()));
		// signer
		value.append(toValue(perm.getAdministratorsignature().getAdministratorIdentity().getDomaincertificate()));
		value.append(toValue(perm.getAdministratorsignature().getAdministratorIdentity().getRootcertificate()));
		// signature details
		value.append(toValue(perm.getAdministratorsignature().getSignaturevalue().getTimestamp()));
		value.append(toValue(perm.getAdministratorsignature().getSignaturevalue().getSignatureAlgorithm()));
		value.append(toValue(perm.getAdministratorsignature().getSignaturevalue().getSignature()));
	}

	private static boolean checkEndpointPermissionSignature(PKIXCertificate signingPublicCert, SignatureAlgorithm alg,
			Channel c, Currentchannelauthorization ca) {
		String valueToSign = getValueToSign(c, ca);
		String signatureHex = ca.getAdministratorsignature().getSignaturevalue().getSignature();

		return StringSigningUtils.checkHexSignature(signingPublicCert.getCertificate().getPublicKey(), alg, valueToSign,
				signatureHex);
	}

	private static boolean checkEndpointPermissionSignature(PKIXCertificate signingPublicCert, SignatureAlgorithm alg,
			Channel channel, Permission perm) {
		String valueToSign = getValueToSign(channel, perm);
		String signatureHex = perm.getAdministratorsignature().getSignaturevalue().getSignature();

		return StringSigningUtils.checkHexSignature(signingPublicCert.getCertificate().getPublicKey(), alg, valueToSign,
				signatureHex);
	}

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
		return alg.value();
	}

	private static String toValue(org.tdmx.core.api.v01.msg.Grant grant) {
		if (grant == null) {
			return MISSING;
		}
		return grant.value();
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

	private static String toValue(long longValue) {
		return "" + longValue;
	}

	private static String toValue(int intValue) {
		return "" + intValue;
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
