package org.tdmx.console.application;

import org.tdmx.console.application.dao.SystemTrustStore;
import org.tdmx.console.application.job.BackgroundJobRegistry;
import org.tdmx.console.application.search.SearchService;
import org.tdmx.console.application.service.CertificateAuthorityService;
import org.tdmx.console.application.service.CertificateService;
import org.tdmx.console.application.service.DnsResolverService;
import org.tdmx.console.application.service.ObjectRegistry;
import org.tdmx.console.application.service.ProblemRegistry;
import org.tdmx.console.application.service.SystemSettingsService;

public interface Administration {

	public ProblemRegistry getProblemRegistry();
	public ObjectRegistry getObjectRegistry();
	public SearchService getSearchService();
	public SystemTrustStore getTrustStore();
	public BackgroundJobRegistry getBackgroundJobRegistry();
	public SystemSettingsService getSystemSettingService();
	public DnsResolverService getDnsResolverService();
	public CertificateService getCertificateService();
	public CertificateAuthorityService getCertificateAuthorityService();

}
