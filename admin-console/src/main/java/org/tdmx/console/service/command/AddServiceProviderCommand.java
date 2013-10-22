package org.tdmx.console.service.command;

import java.io.Serializable;

public class AddServiceProviderCommand extends AbstractCommand implements Serializable {

	//-------------------------------------------------------------------------
	//ERROR CODES
	//-------------------------------------------------------------------------
	
	public static final int ERROR_UNKNOWN_HOST = 1;
	
	//-------------------------------------------------------------------------
	//INPUT FIELDS
	//-------------------------------------------------------------------------
	
	private String hostname;
	private int port;

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
	
	//-------------------------------------------------------------------------
	//OUTPUT FIELDS
	//-------------------------------------------------------------------------
	
	private String subjectIdentifier;

	public String getSubjectIdentifier() {
		return subjectIdentifier;
	}
	public void setSubjectIdentifier(String subjectIdentifier) {
		this.subjectIdentifier = subjectIdentifier;
	}
	
	
}
