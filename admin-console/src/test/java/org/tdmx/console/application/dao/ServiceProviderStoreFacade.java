package org.tdmx.console.application.dao;

import org.tdmx.console.application.domain.AbstractDO;


public class ServiceProviderStoreFacade {

	public static ServiceProvider getServiceProvider( int num ) {
		String sp = "serviceprovider"+num+".com";
		String company = "company"+num;
		
		ServiceProvider s = new ServiceProvider();
		s.setId(AbstractDO.getNextObjectId());
		s.setSubjectIdentity("cn="+sp+" ou=it o="+company+" c=CH");
		s.setApiVersion(1);
		EndPoint mas = new EndPoint();
		mas.setHostname("mas."+sp);
		mas.setPort(443);
		s.setMas(mas);
		
		//TODO mos, mds, mrs missing
		return s;
	}
}
