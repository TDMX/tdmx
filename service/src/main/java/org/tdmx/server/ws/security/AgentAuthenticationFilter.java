package org.tdmx.server.ws.security;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.server.ws.security.service.AgentAuthorizationService.AuthorizationFailureCode;

public class AgentAuthenticationFilter implements Filter {

	private static Logger log = LoggerFactory.getLogger(AgentAuthenticationFilter.class);

	private static String CLIENT_CERTIFICATE = "javax.servlet.request.X509Certificate";
	private static Map<AuthorizationFailureCode,String> errorMessageMap = new HashMap<>();
	static {
		errorMessageMap.put(AuthorizationFailureCode.UNKNOWN_AGENT, "Unknown Agent.");
		errorMessageMap.put(AuthorizationFailureCode.AGENT_SUSPENDED, "Suspended.");
		errorMessageMap.put(AuthorizationFailureCode.INVALID_API_USAGE_ATTEMPT, "Wrong API usage attempt.");
		errorMessageMap.put(AuthorizationFailureCode.NON_WHITELISTED_IPADDRESS, "Connection from non whitelisted IP address.");
	}

	//TODO wire agentAuthorizationService
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		log.debug("init");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
	    log.debug("doFilter");
		
		X509Certificate[] certs = (X509Certificate[]) request.getAttribute(CLIENT_CERTIFICATE);
	    if ( certs != null && certs.length > 0 ) {
	    	log.info("Client Cert: "+certs[0]);
	    } else {
	    	log.info("No client cert.");
	    }
	    try {
	    	//TODO setAuthorizedAgent
			chain.doFilter(request, response);
	    	
	    } finally {
	    	//TODO clearAUthorizedAgent
	    }
	}

	@Override
	public void destroy() {
		log.debug("destroy");
	}

}
