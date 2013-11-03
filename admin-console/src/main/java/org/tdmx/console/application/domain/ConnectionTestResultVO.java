package org.tdmx.console.application.domain;

import java.security.cert.X509Certificate;
import java.util.Date;


/**
 * A Result of a ConnectionTest of a ServiceProvider's interface.
 * 
 * @author Peter
 *
 */
public class ConnectionTestResultVO implements ValueObject {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	
	private String url;
	private X509Certificate[] serverCertificateChain;
	private CertificateStatus trustStatus;
	private ConnectionStatus status;
	private Date testedDate;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
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
	public X509Certificate[] getServerCertificateChain() {
		return serverCertificateChain;
	}
	public void setServerCertificateChain(X509Certificate[] serverCertificateChain) {
		this.serverCertificateChain = serverCertificateChain;
	}
	public CertificateStatus getTrustStatus() {
		return trustStatus;
	}
	public void setTrustStatus(CertificateStatus trustStatus) {
		this.trustStatus = trustStatus;
	}
	public ConnectionStatus getStatus() {
		return status;
	}
	public void setStatus(ConnectionStatus status) {
		this.status = status;
	}
	public Date getTestedDate() {
		return testedDate;
	}
	public void setTestedDate(Date testedDate) {
		this.testedDate = testedDate;
	}
}
