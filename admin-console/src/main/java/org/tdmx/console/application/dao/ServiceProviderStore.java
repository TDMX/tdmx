package org.tdmx.console.application.dao;

import java.io.IOException;

import javax.xml.bind.JAXBException;


public interface ServiceProviderStore {

	public ServiceProviderStorage load() throws IOException, JAXBException;
	
	public void save( ServiceProviderStorage content )  throws IOException, JAXBException;
	
}
