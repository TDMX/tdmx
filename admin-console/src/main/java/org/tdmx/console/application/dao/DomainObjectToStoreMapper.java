package org.tdmx.console.application.dao;

import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.console.application.domain.DnsResolverListDO;
import org.tdmx.console.application.domain.ServiceProviderDO;
import org.tdmx.console.application.domain.SystemProxyDO;
import org.tdmx.console.application.domain.X509CertificateDO;

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

	public PKIXCertificate map( X509CertificateDO other ) throws CryptoCertificateException {
		PKIXCertificate o = new PKIXCertificate();
		o.setId(other.getId());
		o.setPemValue(CertificateIOUtils.x509certToPem(other.getCertificate()));
		return o;
	}
	
	public ProxySettings map( SystemProxyDO other ) {
		ProxySettings o = new ProxySettings();
		o.setHttpsProxy(other.getHttpsProxy());
		o.setHttpsNonProxyHosts(other.getHttpsNonProxyHosts());
		o.setSocksProxy(other.getSocksProxy());
		return o;
	}
	
	public DNSResolverList map( DnsResolverListDO other ) {
		DNSResolverList o = new DNSResolverList();
		o.setId(other.getId());
		o.setActive(other.isActive());
		o.setName(other.getName());
		if ( other.getHostnames() != null ) {
			o.getResolverIp().addAll(other.getHostnames());
		}
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
