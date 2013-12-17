package org.tdmx.console.application.dao;

import org.tdmx.client.crypto.certificate.CertificateIOUtils;
import org.tdmx.client.crypto.certificate.CertificateResultCode;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.console.application.domain.DnsResolverListDO;
import org.tdmx.console.application.domain.ServiceProviderDO;
import org.tdmx.console.application.domain.SystemPropertiesVO;
import org.tdmx.console.application.domain.X509CertificateDO;

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
	public X509CertificateDO map( X509Certificate other ) throws CryptoCertificateException {
		String id = other.getId();
		
		PKIXCertificate cert = CertificateIOUtils.pemToX509cert(other.getPemValue());
		
		X509CertificateDO o = new X509CertificateDO(cert);
		if ( !o.getId().equals(id)) {
			throw new CryptoCertificateException(CertificateResultCode.ERROR_FINGERPRINT_TAMPERING);
		}
		return o;
	}


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

	public SystemPropertiesVO map( SystemPropertyList other ) {
		if ( other == null ) {
			return null;
		}
		SystemPropertiesVO o = new SystemPropertiesVO();
		for( Property p : other.getProperty()) {
			o.add(p.getName(), p.getValue());
		}
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
