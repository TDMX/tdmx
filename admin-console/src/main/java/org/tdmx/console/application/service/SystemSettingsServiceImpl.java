package org.tdmx.console.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.console.application.domain.SystemPropertiesVO;


public class SystemSettingsServiceImpl implements SystemSettingsService {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------
	public List<String> settingNames = new ArrayList<>();
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private Logger log = LoggerFactory.getLogger(SystemSettingsServiceImpl.class);

	private ObjectRegistry objectRegistry;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------
	public SystemSettingsServiceImpl() {
		settingNames.add("http.proxyHost");
		settingNames.add("http.proxyPort");
		settingNames.add("http.nonProxyHosts");
		settingNames.add("https.proxyHost");
		settingNames.add("https.proxyPort");
		settingNames.add("socksProxyHost ");
		settingNames.add("socksProxyPort");
		settingNames.add("socksProxyVersion");
		settingNames.add("java.net.socks.username");
		settingNames.add("java.net.useSystemProxies");
		settingNames.add("http.auth.ntlm.domain");
		settingNames.add("java.net.preferIPv4Stack ");
		settingNames.add("java.net.preferIPv6Addresses ");
		settingNames.add("networkaddress.cache.ttl");
		settingNames.add("networkaddress.cache.negative.ttl");
		settingNames.add("http.agent");
		settingNames.add("http.keepalive");
		settingNames.add("http.maxConnections");
		settingNames.add("http.maxRedirects");
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	

	@Override
	public void updateSystemProperties() {
		SystemPropertiesVO existing = objectRegistry.getSystemProperties();

		//check updates
		SystemPropertiesVO newProps = new SystemPropertiesVO(getSettingNames());
		
		Set<String> deletes = existing.getDeletedKeys(newProps.getProperties());
		if ( !deletes.isEmpty() ) {
			log.info("SystemProperties removed: " + deletes);
			//TODO audit removed
		}
		Set<String> modified = existing.getModifiedKeys(newProps.getProperties());
		if ( !modified.isEmpty() ) {
			log.info("SystemProperties changed: " + modified);
			//TODO audit changed
			
		}
		Set<String> added = existing.getNewKeys(newProps.getProperties());
		if ( !added.isEmpty() ) {
			log.info("SystemProperties added: " + added);
			//TODO audit added
		}
		objectRegistry.setSystemProperties(newProps);
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

	public List<String> getSettingNames() {
		return settingNames;
	}

	public void setSettingNames(List<String> settingNames) {
		this.settingNames = settingNames;
	}

}
