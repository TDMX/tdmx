package org.tdmx.console.application.dao;

import java.util.ArrayList;
import java.util.List;

import org.tdmx.console.application.dao.EndPoint;
import org.tdmx.console.application.dao.ServiceProvider;
import org.tdmx.console.application.dao.ServiceProviderStorage;
import org.tdmx.console.application.domain.ServiceProviderDO;
import org.tdmx.console.domain.Domain;

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
			o.setMasStatus(ServiceProviderDO.STATUS_UNCHECKED);
		} else {
			o.setMasStatus(ServiceProviderDO.STATUS_IRRELEVANT);
		}
		
		if ( other.getMrs() != null) {
			o.setMrsHostname(other.getMrs().getHostname());
			o.setMrsPort(other.getMrs().getPort());
			o.setMrsStatus(ServiceProviderDO.STATUS_UNCHECKED);
		} else {
			o.setMrsStatus(ServiceProviderDO.STATUS_IRRELEVANT);
		}
		
		if ( other.getMos() != null) {
			o.setMosHostname(other.getMos().getHostname());
			o.setMosPort(other.getMos().getPort());
			o.setMosStatus(ServiceProviderDO.STATUS_UNCHECKED);
		} else {
			o.setMosStatus(ServiceProviderDO.STATUS_IRRELEVANT);
		}
		
		if ( other.getMds() != null) {
			o.setMdsHostname(other.getMds().getHostname());
			o.setMdsPort(other.getMds().getPort());
			o.setMdsStatus(ServiceProviderDO.STATUS_UNCHECKED);
		} else {
			o.setMdsStatus(ServiceProviderDO.STATUS_IRRELEVANT);
		}
		
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
