package org.tdmx.console.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.wicket.Application;
import org.apache.wicket.IInitializer;
import org.tdmx.console.AdminApplication;
import org.tdmx.console.application.dao.ServiceProviderStorage;
import org.tdmx.console.application.dao.ServiceProviderStoreImpl;
import org.tdmx.console.application.domain.ProblemDO;
import org.tdmx.console.application.domain.ProblemDO.ProblemCode;
import org.tdmx.console.application.job.BackgroundJob;
import org.tdmx.console.application.job.StateStorageJob;
import org.tdmx.console.application.service.ObjectRegistry;
import org.tdmx.console.application.service.ObjectRegistryChangeListener;
import org.tdmx.console.application.service.ObjectRegistryImpl;
import org.tdmx.console.application.service.ProblemRegistry;
import org.tdmx.console.application.service.ProblemRegistryImpl;

public class AdministrationImpl implements Administration, ObjectRegistryChangeListener, IInitializer {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	public static final String CONFIG_PROPERTY = "org.tdmx.console.config";
	public static final String PASSPHRASE_PROPERTY = "org.tdmx.console.passphrase";
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private String configFilePath = null;
	private String passPhrase = null;
	
	private ObjectRegistryImpl registry = new ObjectRegistryImpl();
	private ProblemRegistry problemRegistry = new ProblemRegistryImpl();
	private ServiceProviderStoreImpl store = new ServiceProviderStoreImpl();
	private StateStorageJob storageJob = null;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	@Override
	public void init(Application application) {
		((AdminApplication)application).setAdministration(this);
		
		configFilePath = System.getProperty(CONFIG_PROPERTY);
		if ( configFilePath == null ) {
	    	throw new RuntimeException("Missing System property " + CONFIG_PROPERTY);
		}
		passPhrase = System.getProperty(PASSPHRASE_PROPERTY);
		if ( passPhrase == null ) {
			System.out.println("Enter passphrase:");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			try {
				passPhrase = br.readLine();
		    } catch (IOException ioe) {
		    	throw new RuntimeException("Unable to read passphrase from stdin.");
		    }
		}
		
		registry.setChangeListener(this);
		
		store.setFilename(configFilePath);
		try {
			ServiceProviderStorage content = store.load();
			registry.initContent(content);
		} catch ( IOException e ) {
			ProblemDO p = new ProblemDO(ProblemCode.CONFIGURATION_FILE_READ_IO, e);
			problemRegistry.addProblem(p);
		} catch ( JAXBException e) {
			ProblemDO p = new ProblemDO(ProblemCode.CONFIGURATION_FILE_PARSE, e);
			problemRegistry.addProblem(p);
		}
		
		storageJob = new StateStorageJob();
		storageJob.setName("StateStorage");
		storageJob.setProblemRegistry(problemRegistry);
		storageJob.setRegistry(registry);
		storageJob.setStore(store);
		storageJob.init();
	}

	@Override
	public void destroy(Application application) {
		if ( storageJob != null ) {
			storageJob.shutdown();
		}
		storageJob = null;
		((AdminApplication)application).setAdministration(null);
	}


	@Override
	public void notifyObjectRegistryChanged() {
		if ( storageJob != null ) {
			storageJob.flushStorage();
		}
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


	@Override
	public ObjectRegistry getObjectRegistry() {
		return registry;
	}

	@Override
	public ProblemRegistry getProblemRegistry() {
		return problemRegistry;
	}

	@Override
	public List<BackgroundJob> getBackgroundJobs() {
		List<BackgroundJob> jobList = new ArrayList<>();
		if ( storageJob != null ) {
			jobList.add(storageJob);
		}
		return jobList;
	}


}
