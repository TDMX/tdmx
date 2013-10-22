package org.tdmx.console.application.dao;

import javax.xml.ws.Endpoint;

import org.tdmx.console.application.domain.ServiceProviderDO;

public class DomainObjectToStoreMapper {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	public ServiceProvider map( ServiceProviderDO other ) {
		ServiceProvider o = new ServiceProvider();
		o.setSubjectIdentity(other.getSubjectIdentifier());
		o.setApiVersion(other.getVersion());
		
		EndPoint masEp = new EndPoint();
		masEp.setHostname(other.getMasHostname());
		masEp.setPort(other.getMasPort());
		o.setMas(masEp);
		
		EndPoint mrsEp = new EndPoint();
		mrsEp.setHostname(other.getMrsHostname());
		mrsEp.setPort(other.getMrsPort());
		o.setMrs(mrsEp);
		
		EndPoint mosEp = new EndPoint();
		mosEp.setHostname(other.getMosHostname());
		mosEp.setPort(other.getMosPort());
		o.setMos(mosEp);
		
		EndPoint mdsEp = new EndPoint();
		mdsEp.setHostname(other.getMdsHostname());
		mdsEp.setPort(other.getMdsPort());
		o.setMds(mdsEp);
		
		//TODO domain
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

}
