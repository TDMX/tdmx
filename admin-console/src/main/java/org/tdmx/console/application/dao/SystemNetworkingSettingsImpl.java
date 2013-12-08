package org.tdmx.console.application.dao;

import org.xbill.DNS.ResolverConfig;

public class SystemNetworkingSettingsImpl implements SystemNetworkingSettings {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	public SystemNetworkingSettingsImpl() {
	}

	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------

	@Override
	public String getHttpsProxy() {
		// TODO CXF get system proxy.
		return null;
	}

	@Override
	public String getHttpsProxyExclusionList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSocksProxy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getSystemDnsResolverList() {
		String[] hostnames = ResolverConfig.getCurrentConfig().servers();
		return hostnames;
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

}
