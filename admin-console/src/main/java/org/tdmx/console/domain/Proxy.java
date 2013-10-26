package org.tdmx.console.domain;

import java.io.Serializable;

import org.tdmx.console.application.service.ProxyService;

public class Proxy implements Serializable {
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------

	private String id;
	private String description;
	private boolean deleteEnabled;
	
	private String hostname;
	private int port;
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
		
		this.deleteEnabled = service.isDeleteable(o);
	}

	public Proxy() {
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

	public String getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public boolean isDeleteEnabled() {
		return deleteEnabled;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
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
