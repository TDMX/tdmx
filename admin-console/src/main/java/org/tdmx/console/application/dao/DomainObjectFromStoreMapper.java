package org.tdmx.console.application.dao;

import org.tdmx.console.application.domain.DnsResolverListDO;
import org.tdmx.console.application.domain.ServiceProviderDO;
import org.tdmx.console.application.domain.SystemProxyDO;

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
	public DnsResolverListDO map( DNSResolverList other ) {
		DnsResolverListDO o = new DnsResolverListDO();
		if ( other.getId() != null ) {
			o.setId(other.getId());
		}
		
		o.setName(other.getName());
		o.setHostnames(other.getResolverIp());
		o.setActive(other.isActive());
		return o;
	}

	public SystemProxyDO map( ProxySettings other ) {
		SystemProxyDO o = new SystemProxyDO();
		o.setHttpsProxy(other.getHttpsProxy());
		o.setHttpsNonProxyHosts(other.getHttpsNonProxyHosts());
		o.setSocksProxy(other.getSocksProxy());
		return o;
	}
	
	public ServiceProviderDO map( ServiceProvider other ) {
		ServiceProviderDO o = new ServiceProviderDO();
		if ( other.getId() != null ) {
			o.setId(other.getId());
		}
		
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
