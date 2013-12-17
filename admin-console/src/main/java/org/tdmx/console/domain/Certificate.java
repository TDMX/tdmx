package org.tdmx.console.domain;

import java.io.Serializable;
import java.util.Date;

import org.tdmx.console.application.util.CalendarUtils;

public class Certificate implements Serializable {
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------

	private String id;
	private String text;
	private Date validFrom;
	private Date validTo;

	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	public Certificate( org.tdmx.console.application.domain.X509CertificateDO p ) {
		this.id = p.getId();
		this.text = p.getCertificate().getInfo();
		this.validFrom = CalendarUtils.getDate(p.getCertificate().getNotBefore());
		this.validTo = CalendarUtils.getDate(p.getCertificate().getNotAfter());
	}
	
	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

	public String getId() {
		return id;
	}

	public String getText() {
		return text;
	}

	public Date getValidFrom() {
		return validFrom;
	}

	public Date getValidTo() {
		return validTo;
	}

}
