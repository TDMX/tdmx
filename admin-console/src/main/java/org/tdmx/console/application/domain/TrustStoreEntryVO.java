package org.tdmx.console.application.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.tdmx.core.system.lang.StringUtils;



/**
 * An object representing the a certificate in a RootCA list.
 * 
 * @author Peter
 *
 */
public class TrustStoreEntryVO implements ValueObject {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private String friendlyName;
	private String x509certificateId;
	private String comment;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------
	
	public TrustStoreEntryVO() {
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@SuppressWarnings("unchecked")
	@Override
	public TrustStoreEntryVO copy() {
		TrustStoreEntryVO o = new TrustStoreEntryVO();
		o.setComment(getComment());
		o.setFriendlyName(getFriendlyName());
		o.setX509certificateId(getX509certificateId());
		return o;
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

	public String getFriendlyName() {
		return friendlyName;
	}

	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}

	public String getX509certificateId() {
		return x509certificateId;
	}

	public void setX509certificateId(String x509certificateId) {
		this.x509certificateId = x509certificateId;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

}
