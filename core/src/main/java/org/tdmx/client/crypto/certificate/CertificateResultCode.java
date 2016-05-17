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
package org.tdmx.client.crypto.certificate;

public enum CertificateResultCode {

	OK,

	ERROR_CA_KEYPAIR_GENERATION,
	ERROR_CA_CERT_GENERATION,

	ERROR_MISSING_CERTS,
	ERROR_TOO_MANY_CERTS,
	ERROR_FINGERPRINT_TAMPERING,

	ERROR_INVALID_KEY_SPEC,

	ERROR_MISSING_ALGORITHM,
	ERROR_MISSING_PROVIDER,

	ERROR_SYSTEM_TRUSTSTORE_EXCEPTION,
	ERROR_KEYSTORE_EXCEPTION,

	ERROR_INVALID_OU,

	ERROR_EXCEPTION,
	ERROR_ENCODING,
	ERROR_IO,
}
