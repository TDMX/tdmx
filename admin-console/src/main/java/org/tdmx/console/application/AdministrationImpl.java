package org.tdmx.console.application;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.tdmx.console.application.dao.ServiceProviderStorage;
import org.tdmx.console.application.domain.Problem;
import org.tdmx.console.application.domain.Problem.ProblemCode;
import org.tdmx.console.application.service.ProblemRegistry;
import org.tdmx.console.application.service.ProblemRegistryImpl;
import org.tdmx.console.application.util.FileUtils;
import org.tdmx.console.application.util.JaxbMarshaller;

public class AdministrationImpl implements Administration, AdministrationLifecycle {

	private boolean initialized = false;
	private ObjectRegistry registry;
	private ProblemRegistry problemRegistry = new ProblemRegistryImpl();
	
	private JaxbMarshaller<ServiceProviderStorage> marshaller = new JaxbMarshaller<>(ServiceProviderStorage.class);
	private ServiceProviderStorage configuration = null;
	
	@Override
	public void initialize( String configFilePath, String passphrase) {
		try {
			byte[] configFileContents = FileUtils.getFileContents(configFilePath);
			
			configuration = marshaller.unmarshal(configFileContents);
			//TODO move down to ServiceProviderStore
		} catch ( IOException e ) {
			Problem p = new Problem(ProblemCode.CONFIGURATION_FILE_IO, e);
			problemRegistry.addProblem(p);
		} catch (JAXBException e) {
			Problem p = new Problem(ProblemCode.CONFIGURATION_FILE_PARSE, e);
			problemRegistry.addProblem(p);
		}
		registry = new ObjectRegistryImpl();
		initialized = true;
	}
	
	@Override
	public ObjectRegistry getObjectRegistry() {
		enforceInitialized();
		return registry;
	}

	@Override
	public ProblemRegistry getProblemRegistry() {
		return problemRegistry;
	}

	private void enforceInitialized() {
		if ( !initialized ) {
			throw new RuntimeException("Not initialized.");
		}
	}

}
