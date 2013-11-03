package org.tdmx.console.domain;

import java.io.Serializable;

import org.tdmx.console.application.service.ProxyService;

public class Proxy implements Serializable {
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------

	private String id;
	
	private String description; // a textual representation of the proxy
	private boolean deleteWarning; // if deleting the proxy will remove the proxy setting from service providers
	
	private String hostname;
	private Integer port;
	private String type;
	private String username;
	private String password;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	public Proxy( org.tdmx.console.application.domain.HttpProxyDO o, ProxyService service ) {
		this.id = o.getId();
		
		this.hostname = o.getHostname();
		this.port = o.getPort();
		this.type = o.getType();
		this.username = o.getUsername();
		this.password = o.getPassword();
		
		this.description = o.getDescription();
		
		this.deleteWarning = service.isDeleteWarning(o);
	}

	public Proxy() {
	}
	
	public org.tdmx.console.application.domain.HttpProxyDO domain() {
		org.tdmx.console.application.domain.HttpProxyDO o = new org.tdmx.console.application.domain.HttpProxyDO();
		o.setId(getId());
		o.setHostname(getHostname());
		o.setPort(getPort());
		o.setType(getType());
		o.setUsername(getUsername());
		o.setPassword(getPassword());
		return o;
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

	public String getId() {
		return id;
	}

	public boolean isDeleteWarning() {
		return deleteWarning;
	}

	public String getDescription() {
		return description;
	}

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
