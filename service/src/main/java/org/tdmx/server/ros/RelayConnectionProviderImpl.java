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

import java.io.IOException;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.message.Message;
import org.tdmx.client.adapter.ClientCredentialProvider;
import org.tdmx.client.adapter.ClientKeyManagerFactoryImpl;
import org.tdmx.client.adapter.ServerTrustManagerFactoryImpl;
import org.tdmx.client.adapter.SingleTrustedCertificateProvider;
import org.tdmx.client.adapter.SoapClientFactory;
import org.tdmx.client.adapter.SystemDefaultTrustedCertificateProvider;
import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.KeyStoreUtils;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.client.crypto.certificate.PKIXCredential;
import org.tdmx.core.api.v01.mrs.ws.MRS;
import org.tdmx.core.api.v01.scs.Endpoint;
import org.tdmx.core.api.v01.scs.GetMRSSession;
import org.tdmx.core.api.v01.scs.GetMRSSessionResponse;
import org.tdmx.core.api.v01.scs.ws.SCS;
import org.tdmx.core.system.lang.FileUtils;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.server.ws.DomainToApiMapper;

/**
 * Relay Connection Provider
 * 
 * @author Peter
 *
 */
public class RelayConnectionProviderImpl implements RelayConnectionProvider {

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
	private static final String TLS_VERSION = "TLSv1.2";
	private static final int CONNECTION_TIMEOUT_MS = 10000;
	private static final int READ_TIMEOUT_MS = 60000;

	private String keyStoreFile;
	private String keyStoreType;
	private String keyStorePassword;
	private String keyStoreAlias;

	// TODO #93: dep inject TrustedServerCertificateProvider
	private final DomainToApiMapper d2a = new DomainToApiMapper();

	// internal
	private ClientKeyManagerFactoryImpl kmf;
	private SCS scsClient;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public void init() {
		String errorMsg = "Unable to load client keystore " + keyStoreFile;
		final PKIXCredential clientCredential;
		try {
			byte[] keystoreContents = FileUtils.getFileContents(keyStoreFile);
			if (keystoreContents == null) {
				throw new IllegalStateException(errorMsg);
			}
			clientCredential = KeyStoreUtils.getPrivateCredential(keystoreContents, keyStoreType, keyStorePassword,
					keyStoreAlias);
		} catch (IOException e) {
			throw new IllegalStateException(errorMsg, e);
		} catch (CryptoCertificateException e) {
			throw new IllegalStateException(errorMsg, e);
		}

		ClientCredentialProvider cp = new ClientCredentialProvider() {

			@Override
			public PKIXCredential getCredential() {
				return clientCredential;
			}

		};
		kmf = new ClientKeyManagerFactoryImpl();
		kmf.setCredentialProvider(cp);

		SystemDefaultTrustedCertificateProvider sdtcp = new SystemDefaultTrustedCertificateProvider();

		ServerTrustManagerFactoryImpl stfm = new ServerTrustManagerFactoryImpl();
		stfm.setCertificateProvider(sdtcp);

		SoapClientFactory<SCS> factory = new SoapClientFactory<>();
		factory.setUrl("https://scs-url-defined-per-request");
		factory.setConnectionTimeoutMillis(CONNECTION_TIMEOUT_MS);
		factory.setKeepAlive(true);
		factory.setClazz(SCS.class);
		factory.setReceiveTimeoutMillis(READ_TIMEOUT_MS);
		factory.setDisableCNCheck(true); // FIXME
		factory.setKeyManagerFactory(kmf);
		factory.setTrustManagerFactory(stfm);
		factory.setTlsProtocolVersion(TLS_VERSION);

		factory.setEnabledCipherSuites(STRONG_CIPHERS);

		scsClient = factory.createClient();
	}

	@Override
	public MRSSessionHolder getMRS(Channel channel, RelayDirection direction) {

		String url = null; // TODO #93: map from DnsService

		// set the URL on a per request basis.
		Client proxy = ClientProxy.getClient(scsClient);
		proxy.getRequestContext().put(Message.ENDPOINT_ADDRESS, url);

		GetMRSSession sessionRequest = new GetMRSSession();
		sessionRequest.setChannel(d2a.mapChannel(channel));
		GetMRSSessionResponse sessionResponse = scsClient.getMRSSession(sessionRequest);
		if (!sessionResponse.isSuccess()) {
			return MRSSessionHolder.error(sessionResponse.getError().getCode(),
					sessionResponse.getError().getDescription());
		}

		// TODO #93 catch some WS runtime exception

		Endpoint endpoint = sessionResponse.getEndpoint();
		PKIXCertificate mrsServerCert = CertificateIOUtils.safeDecodeX509(endpoint.getTlsCertificate());

		// we only trust the one certificate which the MRS gave to us!
		SingleTrustedCertificateProvider stcp = new SingleTrustedCertificateProvider(mrsServerCert);
		ServerTrustManagerFactoryImpl rtfm = new ServerTrustManagerFactoryImpl();
		rtfm.setCertificateProvider(stcp);

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

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public String getKeyStoreFile() {
		return keyStoreFile;
	}

	public void setKeyStoreFile(String keyStoreFile) {
		this.keyStoreFile = keyStoreFile;
	}

	public String getKeyStoreType() {
		return keyStoreType;
	}

	public void setKeyStoreType(String keyStoreType) {
		this.keyStoreType = keyStoreType;
	}

	public String getKeyStorePassword() {
		return keyStorePassword;
	}

	public void setKeyStorePassword(String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
	}

	public String getKeyStoreAlias() {
		return keyStoreAlias;
	}

	public void setKeyStoreAlias(String keyStoreAlias) {
		this.keyStoreAlias = keyStoreAlias;
	}
}