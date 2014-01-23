package org.tdmx.client.crypto.scheme;

public class CryptoException extends Exception {

	private static final long serialVersionUID = -3340667312593427822L;

	private CryptoResultCode rc;
	
	public CryptoException(CryptoResultCode rc) {
		this.rc = rc;
	}
	
	public CryptoException(CryptoResultCode rc, Throwable cause) {
		super(cause);
		this.rc = rc;
	}

	/**
	 * @return the rc
	 */
	public CryptoResultCode getRc() {
		return rc;
	}

}
