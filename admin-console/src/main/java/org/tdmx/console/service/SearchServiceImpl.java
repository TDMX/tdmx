package org.tdmx.console.service;

import java.util.List;

import org.tdmx.console.application.ObjectRegistry;
import org.tdmx.console.domain.Domain;

public class SearchServiceImpl implements SearchService {

	private ObjectRegistry objectRegistry;
	
	/* (non-Javadoc)
	 * @see org.tdmx.console.service.SearchService#listDomains()
	 */
	@Override
	public List<Domain> listDomains( ) {
		return objectRegistry.getDomains();
	}

	/**
	 * @return the objectRegistry
	 */
	public ObjectRegistry getObjectRegistry() {
		return objectRegistry;
	}

	/**
	 * @param objectRegistry the objectRegistry to set
	 */
	public void setObjectRegistry(ObjectRegistry objectRegistry) {
		this.objectRegistry = objectRegistry;
	}
}
