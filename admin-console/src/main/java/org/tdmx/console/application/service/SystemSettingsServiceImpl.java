/*
 * TDMX - Trusted Domain Messaging eXchange
 * 
 * Enterprise B2B messaging between separate corporations via interoperable cloud service providers.
 * 
 * Copyright (C) 2014 Peter Klauser (http://tdmx.org)
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses/.
 */
package org.tdmx.console.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.console.application.domain.SystemPropertiesVO;

public class SystemSettingsServiceImpl implements SystemSettingsService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------
	public List<String> settingNames = new ArrayList<>();

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private final Logger log = LoggerFactory.getLogger(SystemSettingsServiceImpl.class);

	private ObjectRegistry objectRegistry;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------
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

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void updateSystemProperties() {
		SystemPropertiesVO existing = objectRegistry.getSystemProperties();

		// check updates
		SystemPropertiesVO newProps = new SystemPropertiesVO(getSettingNames());

		Set<String> deletes = existing.getDeletedKeys(newProps.getProperties());
		if (!deletes.isEmpty()) {
			log.info("SystemProperties removed: " + deletes);
			// TODO audit removed
		}
		Set<String> modified = existing.getModifiedKeys(newProps.getProperties());
		if (!modified.isEmpty()) {
			log.info("SystemProperties changed: " + modified);
			// TODO audit changed

		}
		Set<String> added = existing.getNewKeys(newProps.getProperties());
		if (!added.isEmpty()) {
			log.info("SystemProperties added: " + added);
			// TODO audit added
		}
		objectRegistry.setSystemProperties(newProps);
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------
	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

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
