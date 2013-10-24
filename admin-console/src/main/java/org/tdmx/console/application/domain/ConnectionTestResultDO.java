package org.tdmx.console.application.domain;

import java.security.cert.X509Certificate;
import java.util.Date;


/**
 * A Result of a ConnectionTest to .
 * 
 * @author Peter
 *
 */
public class ConnectionTestResultDO {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	public static final int STATUS_IRRELEVANT = -2;
	public static final int STATUS_UNCHECKED = -1;
	public static final int STATUS_OK = 0;
	public static final int STATUS_UNKNOWN_HOST = 1;
	public static final int STATUS_UNREACHABLE_HOST = 2;
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	
	private String url;
	private X509Certificate serverCertificate;
	private int status;
	private String subject;
	private Date testedDate;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConnectionTestResultDO other = (ConnectionTestResultDO) obj;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
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

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public X509Certificate getServerCertificate() {
		return serverCertificate;
	}
	public void setServerCertificate(X509Certificate serverCertificate) {
		this.serverCertificate = serverCertificate;
	}
	public Date getTestedDate() {
		return testedDate;
	}
	public void setTestedDate(Date testedDate) {
		this.testedDate = testedDate;
	}
}
