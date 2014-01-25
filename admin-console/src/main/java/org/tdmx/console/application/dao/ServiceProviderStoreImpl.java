package org.tdmx.console.application.dao;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.tdmx.core.system.lang.FileUtils;
import org.tdmx.core.system.lang.JaxbMarshaller;

public class ServiceProviderStoreImpl implements ServiceProviderStore {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private String filename;
	private AtomicInteger suffixId = new AtomicInteger();
	
	private JaxbMarshaller<ServiceProviderStorage> marshaller = new JaxbMarshaller<>(ServiceProviderStorage.class, new QName("urn:dao.application.console.tdmx.org", "service-provider-storage") );

	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	public ServiceProviderStoreImpl() {
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@Override
	public synchronized ServiceProviderStorage load() throws IOException, JAXBException {
		byte[] fileContents = FileUtils.getFileContents(getFilename());
		if ( fileContents != null ) {
			ServiceProviderStorage content = marshaller.unmarshal(fileContents);
			return content;
		}
		return new ServiceProviderStorage();
	}

	@Override
	public synchronized void save(ServiceProviderStorage content) throws IOException, JAXBException {
		byte[] bytes = marshaller.marshal(content);
		FileUtils.storeFileContents(getFilename(), bytes, "."+suffixId.getAndIncrement());
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

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

}
