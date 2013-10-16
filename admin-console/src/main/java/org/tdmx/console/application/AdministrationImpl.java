package org.tdmx.console.application;

public class AdministrationImpl implements Administration, AdministrationLifecycle {

	private boolean initialized = false;
	private ObjectRegistry registry;
	
	@Override
	public void initialize( String configFilePath, String passphrase) {
		registry = new ObjectRegistryImpl();
		initialized = true;
	}
	
	@Override
	public ObjectRegistry getObjectRegistry() {
		enforceInitialized();
		return registry;
	}
	
	private void enforceInitialized() {
		if ( !initialized ) {
			throw new RuntimeException("Not initialized.");
		}
	}

}
