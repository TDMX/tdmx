package org.tdmx.console.application.dao;

import org.tdmx.console.application.domain.HttpProxyDO;
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
	
	public Proxy map( HttpProxyDO other ) {
		Proxy o = new Proxy();
		o.setId(other.getId());
		o.setHostname(other.getHostname());
		o.setPort(other.getPort());
		o.setType(other.getType());
		o.setUsername(other.getUsername());
		o.setEncryptedPassword(other.getPassword().getBytes()); //TODO
		return o;
	}
	
	public ServiceProvider map( ServiceProviderDO other ) {
		ServiceProvider o = new ServiceProvider();
		o.setId(other.getId());
		
		o.setSubjectIdentity(other.getSubjectIdentifier());
		o.setApiVersion(other.getVersion());
		
		EndPoint masEp = new EndPoint();
		masEp.setHostname(other.getMasHostname());
		masEp.setPort(other.getMasPort());
		if ( other.getMasProxy() != null ) {
			masEp.setProxyId(other.getMasProxy().getId());
		}
		o.setMas(masEp);
		
		EndPoint mrsEp = new EndPoint();
		mrsEp.setHostname(other.getMrsHostname());
		mrsEp.setPort(other.getMrsPort());
		if ( other.getMrsProxy() != null ) {
			mrsEp.setProxyId(other.getMrsProxy().getId());
		}
		o.setMrs(mrsEp);
		
		EndPoint mosEp = new EndPoint();
		mosEp.setHostname(other.getMosHostname());
		mosEp.setPort(other.getMosPort());
		if ( other.getMosProxy() != null ) {
			mosEp.setProxyId(other.getMosProxy().getId());
		}
		o.setMos(mosEp);
		
		EndPoint mdsEp = new EndPoint();
		mdsEp.setHostname(other.getMdsHostname());
		mdsEp.setPort(other.getMdsPort());
		if ( other.getMdsProxy() != null ) {
			mdsEp.setProxyId(other.getMdsProxy().getId());
		}
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
