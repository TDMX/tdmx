package org.tdmx.console.domain;

import java.io.Serializable;
import java.util.List;

import org.tdmx.console.application.domain.CertificateAuthorityDO;
import org.tdmx.console.application.search.SearchableObjectField;

public class CertificateAuthority implements Serializable {
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------

	private String id;
	private String name;
	private Boolean active;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	public CertificateAuthority( CertificateAuthorityDO o, List<SearchableObjectField> searchFields) {
		setId(o.getId());
		//TODO name mapped from searchFields
		setActive(o.isActive());
	}
	
	public CertificateAuthorityDO domain() {
		CertificateAuthorityDO o = new CertificateAuthorityDO();
		o.setId(getId());
		o.setActive(getActive());
		return o;
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

}
