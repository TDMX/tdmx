package org.tdmx.console.application.service;

import java.util.List;

import org.tdmx.console.application.domain.HttpProxyDO;
import org.tdmx.console.application.domain.ServiceProviderDO;
import org.tdmx.console.application.service.ObjectRegistryImpl.OBJECT_OPERATION;


public class ProxyServiceImpl implements ProxyService {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private ObjectRegistry objectRegistry;

	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@Override
	public boolean isDeleteWarning(HttpProxyDO proxy) {
		boolean found = false;
		for( ServiceProviderDO sp : objectRegistry.getServiceProviders() ) {
			if ( sp.getMasProxy() == proxy || sp.getMrsProxy() == proxy || sp.getMosProxy() == proxy || sp.getMdsProxy() == proxy ) {
				found = true;
				break;
			}
		}
		
		return !found;
	}
	
	@Override
	public List<ERROR> create(HttpProxyDO proxy) {
		// TODO field validation
		
		objectRegistry.notifyObject(OBJECT_OPERATION.Add, proxy);
		return null;
	}

	@Override
	public List<ERROR> modify(HttpProxyDO proxy) {
		// TODO field validation

		objectRegistry.notifyObject(OBJECT_OPERATION.Modify, proxy);
		return null;
	}

	@Override
	public void delete(HttpProxyDO proxy) {
		for( ServiceProviderDO sp : objectRegistry.getServiceProviders() ) {
			boolean changedSp = false;
			if ( sp.getMasProxy() == proxy ) {
				changedSp = true;
				sp.setMasProxy(null);
			}
			if ( sp.getMrsProxy() == proxy ) {
				changedSp = true;
				sp.setMrsProxy(null);
			}
			if ( sp.getMosProxy() == proxy ) {
				changedSp = true;
				sp.setMosProxy(null);
			}
			if ( sp.getMdsProxy() == proxy ) {
				changedSp = true;
				sp.setMdsProxy(null);
			}
			if ( changedSp ) {
				objectRegistry.notifyObject(OBJECT_OPERATION.Modify, sp);
			}
		}
		objectRegistry.notifyObject(OBJECT_OPERATION.Remove, proxy);
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

	public ObjectRegistry getObjectRegistry() {
		return objectRegistry;
	}

	public void setObjectRegistry(ObjectRegistry objectRegistry) {
		this.objectRegistry = objectRegistry;
	}

}
