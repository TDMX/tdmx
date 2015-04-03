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

import java.util.Date;

import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.client.crypto.certificate.StringSigningUtils;
import org.tdmx.core.api.v01.msg.AdministratorIdentity;
import org.tdmx.core.api.v01.msg.Administratorsignature;
import org.tdmx.core.api.v01.msg.Channel;
import org.tdmx.core.api.v01.msg.EndpointPermission;
import org.tdmx.core.api.v01.msg.Signaturevalue;
import org.tdmx.core.system.lang.CalendarUtils;

public class SignatureUtils {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public static boolean checkEndpointPermissionSignature(PKIXCertificate signingPublicCert, SignatureAlgorithm alg,
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
		sig.setSignature(null); // filled in by createEndpointPermissionSignature
		sig.setTimestamp(CalendarUtils.getDate(signatureDate));
		sig.setSignatureAlgorithm(org.tdmx.core.api.v01.msg.SignatureAlgorithm.fromValue(alg.getAlgorithm()));

		Administratorsignature signature = new Administratorsignature();
		signature.setAdministratorIdentity(id);
		signature.setSignaturevalue(sig);
		perm.setAdministratorsignature(signature);

		String valueToSign = getValueToSign(channel, perm);
		sig.setSignature(StringSigningUtils.getHexSignature(credential.getPrivateKey(), alg, valueToSign));
	}

	public static String getValueToSign(Channel channel, EndpointPermission perm) {
		// TODO
		return "";
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

}
