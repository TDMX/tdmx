package org.tdmx.console.application.dao;

import org.tdmx.console.application.domain.ServiceProviderDO;

public class DomainObjectFromStoreMapper {

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
	
	public ServiceProviderDO map( ServiceProvider other ) {
		ServiceProviderDO o = new ServiceProviderDO();
		o.setSubjectIdentifier(other.getSubjectIdentity());
		o.setVersion(other.getApiVersion());
		
		if ( other.getMas() != null) {
			o.setMasHostname(other.getMas().getHostname());
			o.setMasPort(other.getMas().getPort());
		}
		o.setMasStatus(null);
		
		if ( other.getMrs() != null) {
			o.setMrsHostname(other.getMrs().getHostname());
			o.setMrsPort(other.getMrs().getPort());
		}
		o.setMrsStatus(null);
		
		if ( other.getMos() != null) {
			o.setMosHostname(other.getMos().getHostname());
			o.setMosPort(other.getMos().getPort());
		}
		o.setMosStatus(null);
		
		if ( other.getMds() != null) {
			o.setMdsHostname(other.getMds().getHostname());
			o.setMdsPort(other.getMds().getPort());
		}
		o.setMdsStatus(null);
		
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
