package org.tdmx.console.application.dao;



public interface SystemNetworkingSettings {

	public String getHttpsProxy();
	public String getHttpsProxyExclusionList();
	public String getSocksProxy();

	public String[] getSystemDnsResolverList();
}
