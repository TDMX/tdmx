package org.tdmx.console.application.service;

import java.util.ArrayList;
import java.util.List;

import org.tdmx.console.application.dao.ServiceProvider;
import org.tdmx.console.application.dao.ServiceProviderStorage;
import org.tdmx.console.domain.Domain;

public class ObjectRegistryImpl implements ObjectRegistry {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private Object syncObj = new Object();
	private boolean dirty = false;
	private ObjectRegistryChangeListener changeListener;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	public void initContent( ServiceProviderStorage content ) {
		synchronized( syncObj ) {
			//TODO
			dirty = false;
		}
		
	}
	
	public ServiceProviderStorage getContentIfDirty() {
		synchronized( syncObj ) {
			if ( dirty ) {
				//TODO
				ServiceProviderStorage s = new ServiceProviderStorage();
				ServiceProvider sp =  new ServiceProvider();
				sp.setDomainname(""+System.currentTimeMillis());
				s.getServiceprovider().add(sp);
				dirty = false;
				return s;
			}
			return null;
		}
	}
	
	
	@Override
	public List<Domain> getDomains() {
    	List<Domain> domainList = new ArrayList<Domain>();
    	domainList.add(new Domain("Domain A"));
    	domainList.add(new Domain("Domain B"));
    	domainList.add(new Domain("Domain C"));
    	domainList.add(new Domain("Domain D"));

    	// structural changes need to be made under syncObj
    	// and set dirty to true
		synchronized( syncObj ) {
			dirty = true;
		}
    	notifyChanged();
		return domainList; 
	}

    //-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------

	private void notifyChanged() {
		ObjectRegistryChangeListener l = getChangeListener();
		if ( l != null ) {
			l.notifyObjectRegistryChanged();
		}
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

	public ObjectRegistryChangeListener getChangeListener() {
		return changeListener;
	}

	public void setChangeListener(ObjectRegistryChangeListener changeListener) {
		this.changeListener = changeListener;
	}

}
