package org.tdmx.console.application;

import org.tdmx.console.application.dao.SystemTrustStore;
import org.tdmx.console.application.job.BackgroundJobRegistry;
import org.tdmx.console.application.service.DnsResolverService;
import org.tdmx.console.application.service.ObjectRegistry;
import org.tdmx.console.application.service.ProblemRegistry;
import org.tdmx.console.application.service.SystemProxyService;

public interface Administration {

	public ProblemRegistry getProblemRegistry();
	public ObjectRegistry getObjectRegistry();
	public SystemTrustStore getTrustStore();
	public BackgroundJobRegistry getBackgroundJobRegistry();
	public SystemProxyService getProxyService();
	public DnsResolverService getDnsResolverService();
	
}
