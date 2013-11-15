package org.tdmx.console.application.dao;

import org.tdmx.console.application.domain.AbstractDO;


public class ServiceProviderStoreFacade {

	public static Proxy getProxy( int num ) {
		Proxy p = new Proxy();
		p.setId(AbstractDO.getNextObjectId());
		p.setHostname("proxyhost"+num);
		p.setPort(8080);
		p.setUsername("username"+num);
		p.setEncryptedPassword("pwd".getBytes());
		return p;
	}
	
	public static ServiceProvider getServiceProvider( int num, String proxyId ) {
		String sp = "serviceprovider"+num+".com";
		
		ServiceProvider s = new ServiceProvider();
		s.setId(AbstractDO.getNextObjectId());
		s.setSubjectIdentity("cn="+sp+" ou=it o=company c=CH");
		s.setApiVersion(1);
		EndPoint mas = new EndPoint();
		mas.setHostname("mas."+sp);
		mas.setPort(443);
		mas.setProxyId(proxyId);
		s.setMas(mas);
		
		//TODO mos, mds, mrs missing
		return s;
	}
}
