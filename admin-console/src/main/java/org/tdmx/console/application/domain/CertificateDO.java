package org.tdmx.console.application.domain;

import java.security.cert.X509Certificate;


/**
 * An CertificateChain.
 * 
 * @author Peter
 *
 */
public class CertificateDO implements DomainObject {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private String id;
	
	private X509Certificate[] certificateChain;

	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------
	
	public CertificateDO( X509Certificate[] certificateChain ) {
		//TODO create a HEX of the SHA2 of the combined encoded byte arrays.
		this.certificateChain = certificateChain;
	}
	
	@Override
	public <E extends DomainObject> E copy() {
		// TODO Auto-generated method stub
		return null;
	}

	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@Override
	public <E extends DomainObject> DomainObjectFieldChanges merge(E other) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getId() {
		return id;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		CertificateDO other = (CertificateDO) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
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

}
