package org.tdmx.console.application.service;

import org.tdmx.console.application.dao.ServiceProviderStorage;

public interface ObjectRegistrySPI {

	public void initContent( ServiceProviderStorage content );

	public ServiceProviderStorage getContentIfDirty();

	public ObjectRegistryChangeListener getChangeListener();

	public void setChangeListener(ObjectRegistryChangeListener changeListener);

}
