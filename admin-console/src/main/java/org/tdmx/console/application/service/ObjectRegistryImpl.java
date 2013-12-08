package org.tdmx.console.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.tdmx.console.application.dao.DNSResolverList;
import org.tdmx.console.application.dao.DomainObjectFromStoreMapper;
import org.tdmx.console.application.dao.DomainObjectToStoreMapper;
import org.tdmx.console.application.dao.PKIXCertificate;
import org.tdmx.console.application.dao.ProxySettings;
import org.tdmx.console.application.dao.ServiceProvider;
import org.tdmx.console.application.dao.ServiceProviderStorage;
import org.tdmx.console.application.domain.DnsResolverListDO;
import org.tdmx.console.application.domain.DomainObject;
import org.tdmx.console.application.domain.DomainObjectChangesHolder;
import org.tdmx.console.application.domain.DomainObjectFieldChanges;
import org.tdmx.console.application.domain.ServiceProviderDO;
import org.tdmx.console.application.domain.SystemProxyDO;
import org.tdmx.console.application.domain.X509CertificateDO;
import org.tdmx.console.domain.Domain;

public class ObjectRegistryImpl implements ObjectRegistry, ObjectRegistrySPI {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------
	private static enum OBJECT_OPERATION {
		Add, Modify, Remove
	};
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private Object syncObj = new Object();
	private boolean dirty = false;
	private boolean cleanLoad = false;

	private ObjectRegistryChangeListener changeListener;
	
	private DomainObjectFromStoreMapper domMapper = new DomainObjectFromStoreMapper();
	private DomainObjectToStoreMapper storeMapper = new DomainObjectToStoreMapper();
	
	private Map<String, DomainObject> objects = new ConcurrentSkipListMap<>();
	private Map<String, DomainObjectContainer<? extends DomainObject>> classMap = new TreeMap<>();
	private SystemProxyDO proxySettings;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------
	public ObjectRegistryImpl() {
		init();
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------

	@Override
	public void initContent( ServiceProviderStorage content ) throws Exception {
		if ( content == null ) {
			return;
		}
		synchronized( syncObj ) {
			init();
			if ( content.getProxy() != null ) {
				proxySettings = domMapper.map(content.getProxy());
			} else {
				proxySettings = new SystemProxyDO();
			}
			proxySettings.check();
			for ( PKIXCertificate pkcert : content.getX509Certificate()) {
				X509CertificateDO cert = domMapper.map(pkcert);
				cert.check();
				add(cert);
			}
			//TODO rootcalist

			for( DNSResolverList dnslist : content.getDnsresolverList() ) {
				DnsResolverListDO d = domMapper.map(dnslist);
				d.check();
				add(d);
			}
			for( ServiceProvider sp : content.getServiceprovider() ) {
				ServiceProviderDO s = domMapper.map(sp);
				s.check();
				add(s);
			}
			dirty = false;
			cleanLoad = true;
		}
	}
	
	@Override
	public ServiceProviderStorage getContentIfDirty() throws Exception {
		if ( !cleanLoad ) {
			throw new Exception("Storage was not loaded cleanly.");
		}
		synchronized( syncObj ) {
			if ( dirty ) {
				ServiceProviderStorage store = new ServiceProviderStorage();
				ProxySettings proxy = storeMapper.map(getSystemProxy());
				store.setProxy(proxy);

				for( X509CertificateDO c : getX509Certificates()) {
					PKIXCertificate cert = storeMapper.map(c);
					store.getX509Certificate().add(cert);
				}
				
				//TODO rootcalist
				for( DnsResolverListDO d : getDnsResolverLists() ) {
					DNSResolverList rl = storeMapper.map(d);
					store.getDnsresolverList().add(rl);
				}
				
				for( ServiceProviderDO sp : getServiceProviders() ) {
					ServiceProvider s = storeMapper.map(sp);
					store.getServiceprovider().add(s);
				}
				dirty = false;
				return store;
			}
			return null;
		}
	}
	
	@Override
	public void notifyRemove(DomainObject obj, DomainObjectChangesHolder holder) {
		notifyObject(OBJECT_OPERATION.Remove, obj, null, holder);
	}

	@Override
	public void notifyAdd(DomainObject obj, DomainObjectChangesHolder holder) {
		notifyObject(OBJECT_OPERATION.Add, obj, null, holder);
	}

	@Override
	public void notifyModify(DomainObjectFieldChanges changes,
			DomainObjectChangesHolder holder) {
		notifyObject(OBJECT_OPERATION.Modify, changes.getObject(), changes, holder);
	}

	private void notifyObject( OBJECT_OPERATION op, DomainObject obj, DomainObjectFieldChanges changes, DomainObjectChangesHolder holder ) {
		synchronized( syncObj ) {
			switch (op) {
			case Add:
				add(obj);
				holder.registerNew(obj);
				break;
			case Modify:
				holder.registerModified(changes);
				break;
			case Remove:
				remove(obj);
				holder.registerDeleted(obj);
				break;
			}
			dirty = true;
		}
		notifyChangedListener();
	}
	
	@Override
	public List<Domain> getDomains() {
    	List<Domain> domainList = new ArrayList<Domain>();
    	//TODO remove
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

	@SuppressWarnings("unchecked")
	@Override
	public List<ServiceProviderDO> getServiceProviders() {
		DomainObjectContainer<? extends DomainObject> c = getContainer(ServiceProviderDO.class);
		return (List<ServiceProviderDO>) c.getList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DnsResolverListDO> getDnsResolverLists() {
		DomainObjectContainer<? extends DomainObject> c = getContainer(DnsResolverListDO.class);
		return (List<DnsResolverListDO>) c.getList();
	}

	@Override
	public DnsResolverListDO getDnsResolverList(String id) {
		if ( id == null ) {
			return null;
		}
		DomainObject dom = objects.get(id);
		if ( dom instanceof DnsResolverListDO ) {
			return (DnsResolverListDO)dom;
		}
		return null;
	}
	
	@Override
	public SystemProxyDO getSystemProxy() {
		return proxySettings;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<X509CertificateDO> getX509Certificates() {
		DomainObjectContainer<? extends DomainObject> c = getContainer(X509CertificateDO.class);
		return (List<X509CertificateDO>) c.getList();
	}

	@Override
	public X509CertificateDO getX509Certificate(String id) {
		if ( id == null ) {
			return null;
		}
		DomainObject dom = objects.get(id);
		if ( dom instanceof X509CertificateDO ) {
			return (X509CertificateDO)dom;
		}
		return null;
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
	
	private void init() {
		objects.clear();
		classMap.clear();
		classMap.put(X509CertificateDO.class.getName(), new DomainObjectContainer<X509CertificateDO>());
		classMap.put(DnsResolverListDO.class.getName(), new DomainObjectContainer<DnsResolverListDO>());
		classMap.put(ServiceProviderDO.class.getName(), new DomainObjectContainer<ServiceProviderDO>());
		//TODO new domain objects
	}
	
	private DomainObjectContainer<? extends DomainObject> getContainer(Class<?> c) {
		DomainObjectContainer<? extends DomainObject> l = classMap.get(c.getName());
		return l;
	}
	
	@SuppressWarnings("unchecked")
	private <E extends DomainObject> void add(E obj) {
		DomainObjectContainer<? extends DomainObject> l = classMap.get(obj.getClass().getName());
		DomainObjectContainer<E> cl = (DomainObjectContainer<E>)l;
		if ( cl.add(obj) ) {
			objects.put(obj.getId(), obj);
		}
		return;
	}
	
	@SuppressWarnings("unchecked")
	private <E extends DomainObject> void remove(E obj) {
		DomainObjectContainer<? extends DomainObject> l = classMap.get(obj.getClass().getName());
		DomainObjectContainer<E> cl = (DomainObjectContainer<E>)l;
		if ( cl.remove(obj) ) {
			objects.remove(obj.getId());
		}
		return;
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

	public boolean isCleanLoad() {
		return cleanLoad;
	}
}
