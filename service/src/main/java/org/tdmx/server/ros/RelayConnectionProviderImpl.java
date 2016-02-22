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

import javax.net.ssl.X509TrustManager;
import javax.xml.ws.WebServiceException;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.adapter.ClientCredentialProvider;
import org.tdmx.client.adapter.ClientKeyManagerFactoryImpl;
import org.tdmx.client.adapter.ServerTrustManagerFactory;
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
import org.tdmx.core.system.lang.StringUtils;
import org.tdmx.lib.control.domain.DomainZoneApexInfo;
import org.tdmx.lib.zone.domain.Channel;
import org.tdmx.server.runtime.DomainZoneResolutionService;
import org.tdmx.server.scs.SessionDataService;
import org.tdmx.server.ws.DomainToApiMapper;
import org.tdmx.server.ws.ErrorCode;

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
	private static final Logger log = LoggerFactory.getLogger(RelayConnectionProviderImpl.class);

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

	private X509TrustManager trustManager;
	private DomainZoneResolutionService domainZoneResolver;
	private SessionDataService sessionDataService;

	private final DomainToApiMapper d2a = new DomainToApiMapper();

	// internal
	private ClientKeyManagerFactoryImpl kmf;
	private SCS scsClient;
	private String segmentScsUrl;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void setSegmentScsUrl(String segmentScsUrl) {
		// we give a shortcut MRS when relaying "within" our own segment.
		this.segmentScsUrl = segmentScsUrl;
	}

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

		if (trustManager == null) {
			SystemDefaultTrustedCertificateProvider sdtcp = new SystemDefaultTrustedCertificateProvider();
			ServerTrustManagerFactoryImpl stfm = new ServerTrustManagerFactoryImpl();
			stfm.setCertificateProvider(sdtcp);
			log.warn("No explicit X509TrustManager declared.");
			trustManager = stfm.getTrustManager();
		}

		SoapClientFactory<SCS> factory = new SoapClientFactory<>();
		factory.setUrl("https://scs-url-defined-per-request");
		factory.setConnectionTimeoutMillis(CONNECTION_TIMEOUT_MS);
		factory.setKeepAlive(true);
		factory.setClazz(SCS.class);
		factory.setReceiveTimeoutMillis(READ_TIMEOUT_MS);
		factory.setDisableCNCheck(true); // FIXME
		factory.setKeyManagerFactory(kmf);
		factory.setTrustManagerFactory(new ServerTrustManagerFactory() {
			@Override
			public X509TrustManager getTrustManager() {
				return trustManager;
			}
		});
		factory.setTlsProtocolVersion(TLS_VERSION);

		factory.setEnabledCipherSuites(STRONG_CIPHERS);

		scsClient = factory.createClient();
	}

	@Override
	public MRSSessionHolder getMRS(Channel channel, RelayDirection direction) {

		// the URL of the SCS is mapped from the Doamin we are trying to relay to in DNS.
		String otherDomain = direction == RelayDirection.Fowards ? channel.getDestination().getDomainName()
				: channel.getOrigin().getDomainName();
		DomainZoneApexInfo apexInfo = domainZoneResolver.resolveDomain(otherDomain);
		if (apexInfo == null) {
			return MRSSessionHolder.error(ErrorCode.DnsZoneApexMissing.getErrorCode(),
					ErrorCode.DnsZoneApexMissing.getErrorDescription(otherDomain));
		}
		String url = apexInfo.getScsUrl().toString();

		if (segmentScsUrl.equalsIgnoreCase(url)) {
			if (log.isDebugEnabled()) {
				log.debug("Shortcut (same segment) relay.");
			}
			return null;// TODO #93: getShortcutRelayClient() sessionDataService
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Resolved SCS url of " + otherDomain + " to be " + url);
			}
			return getRelayClient(url, channel);
		}
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private MRSSessionHolder getRelayClient(String url, Channel channel) {
		// set the URL on a per request basis.
		Client proxy = ClientProxy.getClient(scsClient);
		proxy.getRequestContext().put(Message.ENDPOINT_ADDRESS, url);

		GetMRSSession sessionRequest = new GetMRSSession();
		sessionRequest.setChannel(d2a.mapChannel(channel));
		GetMRSSessionResponse sessionResponse = null;
		try {
			sessionResponse = scsClient.getMRSSession(sessionRequest);
			if (!sessionResponse.isSuccess()) {
				return MRSSessionHolder.error(sessionResponse.getError().getCode(),
						sessionResponse.getError().getDescription());
			}
		} catch (WebServiceException wse) {
			// runtime error handling
			if (log.isDebugEnabled()) {
				log.debug("SCS call failed", wse);
			}
			String errorInfo = StringUtils.getExceptionSummary(wse);
			log.info("MRS relay SCS call to remote failed " + errorInfo);
			return MRSSessionHolder.error(ErrorCode.RelayGetSessionFault.getErrorCode(),
					ErrorCode.RelayGetSessionFault.getErrorDescription(errorInfo));
		}

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

		MRS mrsClient = null;
		try {
			mrsClient = mrsFactory.createClient();

			return MRSSessionHolder.success(mrsClient, sessionResponse.getSession().getSessionId());
		} catch (WebServiceException wse) {
			// runtime error handling
			if (log.isDebugEnabled()) {
				log.debug("SCS call failed", wse);
			}
			String errorInfo = StringUtils.getExceptionSummary(wse);
			log.info("MRS relay SCS call to remote failed " + errorInfo);
			return MRSSessionHolder.error(ErrorCode.RelayClientConstructionFailed.getErrorCode(),
					ErrorCode.RelayClientConstructionFailed.getErrorDescription(errorInfo));
		}
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public DomainZoneResolutionService getDomainZoneResolver() {
		return domainZoneResolver;
	}

	public void setDomainZoneResolver(DomainZoneResolutionService domainZoneResolver) {
		this.domainZoneResolver = domainZoneResolver;
	}

	public SessionDataService getSessionDataService() {
		return sessionDataService;
	}

	public void setSessionDataService(SessionDataService sessionDataService) {
		this.sessionDataService = sessionDataService;
	}

	public X509TrustManager getTrustManager() {
		return trustManager;
	}

	public void setTrustManager(X509TrustManager trustManager) {
		this.trustManager = trustManager;
	}

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
