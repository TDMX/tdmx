package org.tdmx.console.application.dao;

import java.io.IOException;

import javax.xml.bind.JAXBException;

public class ServiceProviderStoreMockImpl implements ServiceProviderStore {

	private int scale = 10;
	private ServiceProviderStorage state = null;
	
	public ServiceProviderStoreMockImpl( int scale ) {
		this.scale = scale;

		ServiceProviderStorage s = new ServiceProviderStorage();
		for( int i = 0; i < scale; i++ ) {
			s.getServiceprovider().add(ServiceProviderStoreFacade.getServiceProvider(i));
		}
		state = s;
	}
	
	@Override
	public ServiceProviderStorage load() throws IOException, JAXBException {
		return this.state;
	}

	@Override
	public void save(ServiceProviderStorage content) throws IOException,
			JAXBException {
		this.state = content;
	}

	public int getScale() {
		return scale;
	}

}
