package org.tdmx.console.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.JAXBException;

import org.apache.wicket.Application;
import org.apache.wicket.IInitializer;
import org.tdmx.console.AdminApplication;
import org.tdmx.console.application.dao.ServiceProvider;
import org.tdmx.console.application.dao.ServiceProviderStorage;
import org.tdmx.console.application.dao.ServiceProviderStoreImpl;
import org.tdmx.console.application.domain.Problem;
import org.tdmx.console.application.domain.Problem.ProblemCode;
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
	private ProblemRegistryImpl problemRegistry = new ProblemRegistryImpl();
	private ServiceProviderStoreImpl store = new ServiceProviderStoreImpl();
	private ScheduledExecutorService scheduler = null;
	private AtomicInteger processingId = new AtomicInteger();
	private int busyId = 0;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	@Override
	public int getBusyId() {
		return busyId;
	}

	
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
		
		scheduler = Executors.newSingleThreadScheduledExecutor();
		registry.setChangeListener(this);
		
		store.setFilename(configFilePath);
		try {
			ServiceProviderStorage content = store.load();
			registry.initContent(content);
		} catch ( IOException e ) {
			Problem p = new Problem(ProblemCode.CONFIGURATION_FILE_READ_IO, e);
			problemRegistry.addProblem(p);
		} catch ( JAXBException e) {
			Problem p = new Problem(ProblemCode.CONFIGURATION_FILE_PARSE, e);
			problemRegistry.addProblem(p);
		}
	}

	@Override
	public void destroy(Application application) {
		if ( scheduler != null ) {
			scheduler.shutdown();
			try {
				scheduler.awaitTermination(60, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// ignore.
			}
		}
		scheduler = null;
		((AdminApplication)application).setAdministration(null);
	}


	@Override
	public void notifyObjectRegistryChanged() {
		flushStorage();
	}


    //-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------

	private void flushStorage()  {
		// start a thread off which will periodically save the app state.
		Runnable r = new Runnable() {

			@Override
			public void run() {
				busyId = processingId.getAndIncrement();
				
				try {
					ServiceProviderStorage s = registry.getContentIfDirty();
					if ( s != null ) {
						store.save(s);
					}
				} catch (IOException e) {
					Problem p = new Problem(ProblemCode.CONFIGURATION_FILE_WRITE_IO, e);
					problemRegistry.addProblem(p);
				} catch (JAXBException e) {
					Problem p = new Problem(ProblemCode.CONFIGURATION_FILE_MARSHAL, e);
					problemRegistry.addProblem(p);
				} finally {
					busyId = 0;
				}
			}
			
		};
		if ( scheduler != null && !scheduler.isShutdown()) {
			scheduler.schedule(r, 10, TimeUnit.SECONDS);
		}
	}

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

}
