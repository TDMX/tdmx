package org.tdmx.server.ws.security;

import java.io.IOException;
import java.security.cert.X509Certificate;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequireClientCertificateFilter implements Filter {

	private static Logger log = LoggerFactory.getLogger(RequireClientCertificateFilter.class);

	private static String CLIENT_CERTIFICATE = "javax.servlet.request.X509Certificate";
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		log.debug("init");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
				log.debug("doFilter");
    	HttpServletResponse res = (HttpServletResponse)response;
		
		X509Certificate[] certs = (X509Certificate[]) request.getAttribute(CLIENT_CERTIFICATE);
	    if ( certs != null && certs.length > 0 ) {
			chain.doFilter(request, response);
	    } else {
	    	res.sendError(HttpServletResponse.SC_UNAUTHORIZED,"A client certificate must be provided to access this path.");
	    }
	}

	@Override
	public void destroy() {
		log.debug("destroy");
	}

}
