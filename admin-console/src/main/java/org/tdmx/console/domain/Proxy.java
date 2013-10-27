package org.tdmx.console.domain;

import java.io.Serializable;

import org.tdmx.console.application.service.ProxyService;

public class Proxy implements Serializable {
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------

	private String id;
	
	private String description;
	private boolean deleteWarning;
	
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
		
		StringBuilder sb = new StringBuilder();
		if ( username != null ) {
			sb.append(username).append("@");
		}
		sb.append(hostname).append(":").append(port);
		sb.append("/").append(type);
		this.description = sb.toString();
		
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
