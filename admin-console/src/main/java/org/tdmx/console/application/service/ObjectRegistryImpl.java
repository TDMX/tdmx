package org.tdmx.console.application.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.tdmx.console.application.dao.DomainObjectFromStoreMapper;
import org.tdmx.console.application.dao.DomainObjectToStoreMapper;
import org.tdmx.console.application.dao.Proxy;
import org.tdmx.console.application.dao.ServiceProvider;
import org.tdmx.console.application.dao.ServiceProviderStorage;
import org.tdmx.console.application.domain.DomainObject;
import org.tdmx.console.application.domain.HttpProxyDO;
import org.tdmx.console.application.domain.ServiceProviderDO;
import org.tdmx.console.domain.Domain;

public class ObjectRegistryImpl implements ObjectRegistry, ObjectRegistrySPI {

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
	
	private Map<String, DomainObject> objects = new TreeMap<>();
	private List<ServiceProviderDO> serviceproviderList = new LinkedList<>();
	private List<HttpProxyDO> proxyList = new LinkedList<>();
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@Override
	public void initContent( ServiceProviderStorage content ) {
		synchronized( syncObj ) {
			serviceproviderList.clear();
			for( Proxy p : content.getProxy() ) {
				HttpProxyDO h = domMapper.map(p);
				proxyList.add(h);
				objects.put(h.getId(), h);
			}
			for( ServiceProvider sp : content.getServiceprovider() ) {
				ServiceProviderDO s = domMapper.map(sp, this);
				serviceproviderList.add(s);
			}
			dirty = false;
		}
	}
	
	@Override
	public ServiceProviderStorage getContentIfDirty() {
		synchronized( syncObj ) {
			if ( dirty ) {
				ServiceProviderStorage store = new ServiceProviderStorage();
				for( HttpProxyDO hp : proxyList ) {
					Proxy p = storeMapper.map(hp);
					store.getProxy().add(p);
				}
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
	public HttpProxyDO getProxy(String id) {
		if ( id == null ) {
			return null;
		}
		DomainObject dom = objects.get(id);
		if ( dom instanceof HttpProxyDO ) {
			return (HttpProxyDO)dom;
		}
		//TODO warn
		return null;
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

	@Override
	public List<HttpProxyDO> getHttpProxies() {
		return Collections.unmodifiableList(proxyList);
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

	@Override
	public ObjectRegistryChangeListener getChangeListener() {
		return changeListener;
	}

	@Override
	public void setChangeListener(ObjectRegistryChangeListener changeListener) {
		this.changeListener = changeListener;
	}

}
