package org.tdmx.console.application;

import java.util.List;

import org.tdmx.console.application.dao.CertificateStore;
import org.tdmx.console.application.job.BackgroundJob;
import org.tdmx.console.application.service.ObjectRegistry;
import org.tdmx.console.application.service.ProblemRegistry;
import org.tdmx.console.application.service.ProxyService;

public interface Administration {

	public ProblemRegistry getProblemRegistry();
	public ObjectRegistry getObjectRegistry();
	public CertificateStore getCertificateStore();
	public List<BackgroundJob> getBackgroundJobs();
	public ProxyService getProxyService();
	
}
