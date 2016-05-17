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
package org.tdmx.server.ws.security;

import java.io.IOException;
import java.security.cert.X509Certificate;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.server.ws.security.service.AuthenticatedClientService;

public class ClientAuthenticationFilter implements Filter {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(ClientAuthenticationFilter.class);

	private static String CLIENT_CERTIFICATE = "javax.servlet.request.X509Certificate";

	private AuthenticatedClientService authenticatedClientService;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		log.debug("init");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
			throw new ServletException("ClientAuthenticationFilter just supports HTTP requests");
		}
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		log.debug("doFilter");

		X509Certificate[] certs = (X509Certificate[]) httpRequest.getAttribute(CLIENT_CERTIFICATE);

		PKIXCertificate[] pkixCerts = null;
		try {
			pkixCerts = CertificateIOUtils.convert(certs);

		} catch (CryptoCertificateException e) {
			log.warn("Unable to convert X509 certificate.", e);
			// 401 - access denied
			httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Bad Certificate.");
			return;
		}

		try {
			getAuthenticatedClientService().setAuthenticatedClient(pkixCerts[0]);
			// the AuthenticatedClientLookupService will give the client identity further down the chain.
			chain.doFilter(request, response);

		} catch (AuthorizationException e) {
			log.info("Unauthorized access.", e); // TODO future raise incident!
			// 401 - access denied
			httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not Authorized.");
			return;

		} finally {
			getAuthenticatedClientService().clearAuthenticatedClient();
		}

	}

	@Override
	public void destroy() {
		log.debug("destroy");
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

	public AuthenticatedClientService getAuthenticatedClientService() {
		return authenticatedClientService;
	}

	public void setAuthenticatedClientService(AuthenticatedClientService authenticatedClientService) {
		this.authenticatedClientService = authenticatedClientService;
	}

}
