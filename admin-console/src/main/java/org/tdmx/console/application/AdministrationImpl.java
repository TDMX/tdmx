package org.tdmx.console.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.xml.bind.JAXBException;

import org.apache.wicket.Application;
import org.apache.wicket.IInitializer;
import org.tdmx.console.AdminApplication;
import org.tdmx.console.application.dao.PrivateKeyStoreImpl;
import org.tdmx.console.application.dao.ServiceProviderStorage;
import org.tdmx.console.application.dao.ServiceProviderStoreImpl;
import org.tdmx.console.application.dao.SystemTrustStore;
import org.tdmx.console.application.dao.SystemTrustStoreImpl;
import org.tdmx.console.application.domain.ProblemDO;
import org.tdmx.console.application.domain.ProblemDO.ProblemCode;
import org.tdmx.console.application.job.BackgroundJobRegistry;
import org.tdmx.console.application.job.BackgroundJobRegistryImpl;
import org.tdmx.console.application.job.StateStorageJob;
import org.tdmx.console.application.job.SystemTrustStoreUpdateJob;
import org.tdmx.console.application.search.SearchServiceImpl;
import org.tdmx.console.application.service.ObjectRegistry;
import org.tdmx.console.application.service.ObjectRegistryImpl;
import org.tdmx.console.application.service.ProblemRegistry;
import org.tdmx.console.application.service.ProblemRegistryImpl;
import org.tdmx.console.application.service.ProxyService;
import org.tdmx.console.application.service.ProxyServiceImpl;

public class AdministrationImpl implements Administration, IInitializer {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	public static final String CONFIGFILE_PROPERTY = "org.tdmx.console.config";
	public static final String CERTFILE_PROPERTY = "org.tdmx.console.certfile";
	public static final String PASSPHRASE_PROPERTY = "org.tdmx.console.passphrase";
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private String configFilePath = null;
	private String certFilePath = null;
	
	private ObjectRegistryImpl registry = new ObjectRegistryImpl();
	private BackgroundJobRegistryImpl jobRegistry = new BackgroundJobRegistryImpl();
	private SystemTrustStoreImpl trustStore = new SystemTrustStoreImpl();
	private PrivateKeyStoreImpl keyStore = new PrivateKeyStoreImpl();
	private ProblemRegistry problemRegistry = new ProblemRegistryImpl();
	private ProxyServiceImpl proxyService = new ProxyServiceImpl();
	
	private ServiceProviderStoreImpl store = new ServiceProviderStoreImpl();
	private SearchServiceImpl searchService = new SearchServiceImpl();
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	@Override
	public void init(Application application) {
		((AdminApplication)application).setAdministration(this);
		
		configFilePath = System.getProperty(CONFIGFILE_PROPERTY,"config.xml");
		certFilePath = System.getProperty(CERTFILE_PROPERTY,"certs.pkcs12");
		String passPhrase = System.getProperty(PASSPHRASE_PROPERTY);
		if ( passPhrase == null ) {
			System.out.println("Enter passphrase:");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			try {
				passPhrase = br.readLine();
		    } catch (IOException ioe) {
		    	throw new RuntimeException("Unable to read passphrase from stdin.");
		    }
		}

		trustStore = new SystemTrustStoreImpl();
		
		keyStore.setFilename(certFilePath);
		keyStore.setKeystoreType("pkcs12");
		keyStore.setPassphrase(passPhrase);
		try {
			keyStore.load();
		} catch (NoSuchAlgorithmException e) {
			ProblemDO p = new ProblemDO(ProblemCode.CERTIFICATE_STORE_ALGORITHM, e);
			problemRegistry.addProblem(p);
		} catch (CertificateException e) {
			ProblemDO p = new ProblemDO(ProblemCode.CERTIFICATE_STORE_EXCEPTION, e);
			problemRegistry.addProblem(p);
		} catch (KeyStoreException e) {
			ProblemDO p = new ProblemDO(ProblemCode.CERTIFICATE_STORE_KEYSTORE_EXCEPTION, e);
			problemRegistry.addProblem(p);
		} catch (IOException e) {
			ProblemDO p = new ProblemDO(ProblemCode.CERTIFICATE_STORE_IO_EXCEPTION, e);
			problemRegistry.addProblem(p);
		}
		
		store.setFilename(configFilePath);
		ServiceProviderStorage content = null;
		try {
			content = store.load();
		} catch ( IOException e ) {
			ProblemDO p = new ProblemDO(ProblemCode.CONFIGURATION_FILE_READ_IO, e);
			problemRegistry.addProblem(p);
		} catch ( JAXBException e) {
			ProblemDO p = new ProblemDO(ProblemCode.CONFIGURATION_FILE_PARSE, e);
			problemRegistry.addProblem(p);
		}
		registry.initContent(content);

		StateStorageJob storageJob = new StateStorageJob();
		storageJob.setName("StateStorage");
		storageJob.setProblemRegistry(problemRegistry);
		storageJob.setRegistry(registry);
		storageJob.setStore(store);
		storageJob.init();
		registry.setChangeListener(storageJob);
		jobRegistry.addBackgroundJob(storageJob);
		
		SystemTrustStoreUpdateJob trustStoreJob = new SystemTrustStoreUpdateJob();
		trustStoreJob.setProblemRegistry(problemRegistry);
		trustStoreJob.setTrustStore(trustStore);
		trustStoreJob.setName("TrustStore");
		trustStoreJob.init();
		jobRegistry.addBackgroundJob(trustStoreJob);
		
		searchService.setObjectRegistry(registry);
		searchService.initialize();
		
		proxyService.setObjectRegistry(registry);
		proxyService.setSearchService(searchService);

	}

	@Override
	public void destroy(Application application) {
		jobRegistry.shutdownAndClear();
		((AdminApplication)application).setAdministration(null);
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
	public ProxyService getProxyService() {
		return proxyService;
	}

	@Override
	public SystemTrustStore getTrustStore() {
		return trustStore;
	}

	@Override
	public BackgroundJobRegistry getBackgroundJobRegistry() {
		return jobRegistry;
	}


}
