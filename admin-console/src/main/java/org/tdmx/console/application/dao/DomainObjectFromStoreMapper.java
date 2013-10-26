package org.tdmx.console.application.dao;

import org.tdmx.console.application.domain.HttpProxyDO;
import org.tdmx.console.application.domain.ServiceProviderDO;
import org.tdmx.console.application.service.ObjectRegistry;

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
	
	public HttpProxyDO map( Proxy other ) {
		HttpProxyDO o = new HttpProxyDO();
		o.setId(other.getId());
		o.setHostname(other.getHostname());
		o.setPort(other.getPort());
		o.setType(other.getType());
		o.setUsername(other.getUsername());
		o.setPassword(new String(other.getEncryptedPassword())); //TODO encrypt
		return o;
	}
	
	public ServiceProviderDO map( ServiceProvider other, ObjectRegistry reg ) {
		ServiceProviderDO o = new ServiceProviderDO();
		o.setId(other.getId());
		
		o.setSubjectIdentifier(other.getSubjectIdentity());
		o.setVersion(other.getApiVersion());
		
		if ( other.getMas() != null) {
			o.setMasHostname(other.getMas().getHostname());
			o.setMasPort(other.getMas().getPort());
			o.setMasProxy(reg.getProxy(other.getMas().getProxyId()));
		}
		o.setMasStatus(null);
		
		if ( other.getMrs() != null) {
			o.setMrsHostname(other.getMrs().getHostname());
			o.setMrsPort(other.getMrs().getPort());
			o.setMrsProxy(reg.getProxy(other.getMrs().getProxyId()));
		}
		o.setMrsStatus(null);
		
		if ( other.getMos() != null) {
			o.setMosHostname(other.getMos().getHostname());
			o.setMosPort(other.getMos().getPort());
			o.setMosProxy(reg.getProxy(other.getMos().getProxyId()));
		}
		o.setMosStatus(null);
		
		if ( other.getMds() != null) {
			o.setMdsHostname(other.getMds().getHostname());
			o.setMdsPort(other.getMds().getPort());
			o.setMdsProxy(reg.getProxy(other.getMds().getProxyId()));
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
