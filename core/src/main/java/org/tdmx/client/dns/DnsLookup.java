package org.tdmx.client.dns;

public interface DnsLookup {

	public String getAuthoratitativeNameServer();
	
	public String getIpAddress();
	
	public String[] getTxtRecords();
}
