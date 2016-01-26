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
package org.tdmx.server.ros;

import org.tdmx.client.adapter.ClientCredentialProvider;
import org.tdmx.client.adapter.ClientKeyManagerFactoryImpl;
import org.tdmx.client.adapter.ServerTrustManagerFactoryImpl;
import org.tdmx.client.adapter.SingleTrustedCertificateProvider;
import org.tdmx.client.adapter.SoapClientFactory;
import org.tdmx.client.adapter.SystemDefaultTrustedCertificateProvider;
import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.core.api.v01.mrs.ws.MRS;
import org.tdmx.core.api.v01.scs.Endpoint;
import org.tdmx.core.api.v01.scs.GetMRSSession;
import org.tdmx.core.api.v01.scs.GetMRSSessionResponse;
import org.tdmx.core.api.v01.scs.ws.SCS;

/**
 * Relay Connection Provider
 * 
 * @author Peter
 *
 */
public class RelayConnectionProviderImpl {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	//@formatter:off
	private final String[] STRONG_CIPHERS = new String[] {
			"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
			"TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384",
			"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
			"TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA",
			"TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384",
			"TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384",
	};
	//@formatter:on
	private String TLS_VERSION = "TLSv1.2";
	private int CONNECTION_TIMEOUT_MS = 10000;
	private int READ_TIMEOUT_MS = 60000;

	// TODO #93: dependency inject server's client certificate

	// TODO #93: dep inject TrustedServerCertificateProvider

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public MRSSessionHolder getMRS(String channelKey) {
		PKIXCredential credential = null; // TODO #93: get from "ros-client" keystore.
		String url = null; // TODO #93: map from DnsService

		ClientCredentialProvider cp = new ClientCredentialProvider() {

			@Override
			public PKIXCredential getCredential() {
				return credential;
			}

		};
		ClientKeyManagerFactoryImpl kmf = new ClientKeyManagerFactoryImpl();
		kmf.setCredentialProvider(cp);

		SystemDefaultTrustedCertificateProvider sdtcp = new SystemDefaultTrustedCertificateProvider();

		ServerTrustManagerFactoryImpl stfm = new ServerTrustManagerFactoryImpl();
		stfm.setCertificateProvider(sdtcp);

		SoapClientFactory<SCS> factory = new SoapClientFactory<>();
		factory.setUrl(url);
		factory.setConnectionTimeoutMillis(CONNECTION_TIMEOUT_MS);
		factory.setKeepAlive(true);
		factory.setClazz(SCS.class);
		factory.setReceiveTimeoutMillis(READ_TIMEOUT_MS);
		factory.setDisableCNCheck(true); // FIXME
		factory.setKeyManagerFactory(kmf);
		factory.setTrustManagerFactory(stfm);
		factory.setTlsProtocolVersion(TLS_VERSION);

		factory.setEnabledCipherSuites(STRONG_CIPHERS);

		SCS scsClient = factory.createClient();

		GetMRSSession sessionRequest = new GetMRSSession(); // TODO #93
		GetMRSSessionResponse sessionResponse = scsClient.getMRSSession(sessionRequest);
		if (!sessionResponse.isSuccess()) {
			return MRSSessionHolder.error(sessionResponse.getError().getCode(),
					sessionResponse.getError().getDescription());
		}

		// TODO #93 catch some WS runtime exception

		Endpoint endpoint = sessionResponse.getEndpoint();
		PKIXCertificate mrsServerCert = CertificateIOUtils.safeDecodeX509(endpoint.getTlsCertificate());

		// we only trust the one certificate which the MRS gave to us!
		SingleTrustedCertificateProvider tcp = new SingleTrustedCertificateProvider(mrsServerCert);
		ServerTrustManagerFactoryImpl rtfm = new ServerTrustManagerFactoryImpl();
		stfm.setCertificateProvider(tcp);

		SoapClientFactory<MRS> mrsFactory = new SoapClientFactory<>();
		mrsFactory.setUrl(endpoint.getUrl());
		mrsFactory.setConnectionTimeoutMillis(10000);
		mrsFactory.setKeepAlive(true);
		mrsFactory.setClazz(MRS.class);
		mrsFactory.setReceiveTimeoutMillis(READ_TIMEOUT_MS);
		mrsFactory.setDisableCNCheck(false);
		mrsFactory.setKeyManagerFactory(kmf);
		mrsFactory.setTrustManagerFactory(rtfm);
		mrsFactory.setTlsProtocolVersion(TLS_VERSION);

		mrsFactory.setEnabledCipherSuites(STRONG_CIPHERS);

		MRS mrsClient = mrsFactory.createClient();

		return MRSSessionHolder.success(mrsClient, sessionResponse.getSession().getSessionId());
	}

}
