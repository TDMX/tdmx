package org.tdmx.core.system.dns;

public interface DnsLookup {

	public String getAuthoratitativeNameServer();
	
	public String getIpAddress();
	
	public String[] getTxtRecords();
}
