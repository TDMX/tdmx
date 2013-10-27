package org.tdmx.console.application.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.tdmx.console.application.service.ProxyService;
import org.tdmx.console.application.util.ValidationUtils;


/**
 * An outgoing HTTP proxy.
 * 
 * @author Peter
 *
 */
public class HttpProxyDO extends AbstractDO {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	public static List<String> proxyTypes;
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private String hostname;
	private Integer port;
	private String type; //HTTP or SOCKS
	private String username; // if need Basic ProxyAuthorization
	private String password;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------
	
	static {
		List<String> pt = new ArrayList<>();
		pt.add("HTTP");
		pt.add("SOCKS");
		proxyTypes = Collections.unmodifiableList(pt);
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	public void merge( HttpProxyDO other ) {
		setHostname(other.getHostname());
		setPort(other.getPort());
		setType(other.getType());
		setUsername(other.getUsername());
		setPassword(other.getPassword());
	}
	
	public List<ProxyService.ERROR> validate() {
		List<ProxyService.ERROR> errors = new ArrayList<>();
		
		ValidationUtils.mandatoryTextField(getHostname(), ProxyService.ERROR.HOSTNAME_MISSING, errors);
		ValidationUtils.optionalHostnameField(getHostname(), ProxyService.ERROR.HOSTNAME_INVALID, errors);
		ValidationUtils.mandatoryNumberField(getPort(), ProxyService.ERROR.PORT_MISSING, errors);
		ValidationUtils.optionalPortField(getPort(), ProxyService.ERROR.PORT_INVALID, errors);
		
		ValidationUtils.mandatoryTextField(getType(), ProxyService.ERROR.TYPE_MISSING, errors);
		ValidationUtils.optionalEnumeratedTextField(getType(), proxyTypes, ProxyService.ERROR.TYPE_INVALID, errors);
		
		ValidationUtils.optionalTextFieldGroup(new String[] { getUsername(),  getPassword()}, ProxyService.ERROR.USERNAME_OR_PASSWORD_MISSING, errors);

		return errors;
	}
	
    //-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
}
