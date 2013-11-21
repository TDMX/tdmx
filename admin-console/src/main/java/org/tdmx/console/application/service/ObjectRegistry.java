package org.tdmx.console.application.service;

import java.util.List;

import org.tdmx.console.application.domain.DomainObject;
import org.tdmx.console.application.domain.DomainObjectChangesHolder;
import org.tdmx.console.application.domain.DomainObjectFieldChanges;
import org.tdmx.console.application.domain.HttpProxyDO;
import org.tdmx.console.application.domain.ServiceProviderDO;
import org.tdmx.console.domain.Domain;

public interface ObjectRegistry {

	/**
	 * Inform the ObjectRegistry that a DomainObject has been removed.
	 * 
	 * The DomainObjectChangesHolder is updated to know the change.
	 * 
	 * @param obj the DomainObject removed
	 * @param holder
	 */
	public void notifyRemove( DomainObject obj, DomainObjectChangesHolder holder );
	/**
	 * Inform the ObjectRegistry that a DomainObject has been added.
	 * 
	 * The DomainObjectChangesHolder is updated to know the change.
	 * 
	 * @param obj the DomainObject added
	 * @param holder
	 */
	public void notifyAdd( DomainObject obj, DomainObjectChangesHolder holder );
	/**
	 * Inform the ObjectRegistry that a DomainObject has been changed.
	 * 
	 * The DomainObjectChangesHolder is updated to know the change.
	 * 
	 * @param obj the DomainObject changed
	 * @param holder
	 */
	public void notifyModify( DomainObjectFieldChanges changes, DomainObjectChangesHolder holder );
	
	/**
	 * @return the list of all Domain DomainObjects.
	 */
	public List<Domain> getDomains();

	/**
	 * @return the list of all ServiceProvider DomainObjects.
	 */
	public List<ServiceProviderDO> getServiceProviders();
	
	/**
	 * @return the list of all HttpProxy DomainObjects.
	 */
	public List<HttpProxyDO> getHttpProxies();
	
	/**
	 * @param id
	 * @return the HttpProxy DomainObject with the id, or null if none exists.
	 */
	public HttpProxyDO getProxy( String id );
	
}
