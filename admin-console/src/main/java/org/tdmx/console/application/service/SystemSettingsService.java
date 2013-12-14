package org.tdmx.console.application.service;


public interface SystemSettingsService {

	/**
	 * Update the SystemProperties.
	 */
	public void updateSystemProperties();
	
	/**
	 * Update the System's DnsResolverList
	 */
	public void updateSystemDnsResolverList();
	
}
