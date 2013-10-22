package org.tdmx.console.application.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.tdmx.console.application.dao.DomainObjectFromStoreMapper;
import org.tdmx.console.application.dao.DomainObjectToStoreMapper;
import org.tdmx.console.application.dao.ServiceProvider;
import org.tdmx.console.application.dao.ServiceProviderStorage;
import org.tdmx.console.application.domain.ServiceProviderDO;
import org.tdmx.console.domain.Domain;

public class ObjectRegistryImpl implements ObjectRegistry {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------
	public static enum OBJECT_OPERATION {
		Add, Modify, Remove
	};
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private Object syncObj = new Object();
	private boolean dirty = false;
	private ObjectRegistryChangeListener changeListener;
	
	private DomainObjectFromStoreMapper domMapper = new DomainObjectFromStoreMapper();
	private DomainObjectToStoreMapper storeMapper = new DomainObjectToStoreMapper();
	
	private List<ServiceProviderDO> serviceproviderList = new LinkedList<>();
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	public void initContent( ServiceProviderStorage content ) {
		synchronized( syncObj ) {
			serviceproviderList.clear();
			for( ServiceProvider sp : content.getServiceprovider() ) {
				ServiceProviderDO s = domMapper.map(sp);
				serviceproviderList.add(s);
			}
			dirty = false;
		}
	}
	
	public ServiceProviderStorage getContentIfDirty() {
		synchronized( syncObj ) {
			if ( dirty ) {
				ServiceProviderStorage store = new ServiceProviderStorage();
				for( ServiceProviderDO sp : serviceproviderList ) {
					ServiceProvider s = storeMapper.map(sp);
					store.getServiceprovider().add(s);
				}
				dirty = false;
				return store;
			}
			return null;
		}
	}
	
	public void notifyServiceProvider( OBJECT_OPERATION op, ServiceProviderDO obj ) {
		synchronized( syncObj ) {
			switch (op) {
			case Add:
				if( !serviceproviderList.contains(obj)) {
					serviceproviderList.add(obj);
				}
				break;
			case Modify:
				break;
			case Remove:
				serviceproviderList.remove(obj);
				break;
			}
			dirty = true;
		}
		notifyChangedListener();
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
    	notifyChangedListener();
		return domainList; 
	}

    //-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------

	private void notifyChangedListener() {
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
