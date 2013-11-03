package org.tdmx.console.domain;

import java.io.Serializable;
import java.util.Date;

import org.tdmx.console.application.domain.CertificateStatus;
import org.tdmx.console.application.domain.ConnectionStatus;

public class ConnectionTestResult implements Serializable {
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------

	private String url;
	private String certificateChainId;
	private CertificateStatus trustStatus;
	private ConnectionStatus connectionStatus;
	private String subject;
	private Date testedDate;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	public ConnectionTestResult( org.tdmx.console.application.domain.ConnectionTestResultVO o ) {
	}

	public ConnectionTestResult() {
	}
	
	public org.tdmx.console.application.domain.ConnectionTestResultVO domain() {
		org.tdmx.console.application.domain.ConnectionTestResultVO o = new org.tdmx.console.application.domain.ConnectionTestResultVO();
		return o;
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

}
