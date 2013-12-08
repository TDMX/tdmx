package org.tdmx.console.application.service;

import org.tdmx.console.application.dao.ServiceProviderStorage;

public interface ObjectRegistrySPI {

	public void initContent( ServiceProviderStorage content ) throws Exception;

	public ServiceProviderStorage getContentIfDirty() throws Exception;

	public ObjectRegistryChangeListener getChangeListener();

	public void setChangeListener(ObjectRegistryChangeListener changeListener);

}
