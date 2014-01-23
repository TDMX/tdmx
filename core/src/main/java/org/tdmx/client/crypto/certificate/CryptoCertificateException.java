package org.tdmx.client.crypto.certificate;

public class CryptoCertificateException extends Exception {

	private static final long serialVersionUID = -3340667312593427822L;

	private CertificateResultCode rc;
	
	public CryptoCertificateException(CertificateResultCode rc) {
		this.rc = rc;
	}
	
	public CryptoCertificateException(CertificateResultCode rc, Throwable cause) {
		super(cause);
		this.rc = rc;
	}

	/**
	 * @return the rc
	 */
	public CertificateResultCode getRc() {
		return rc;
	}

}
