package org.tdmx.console.application.job;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.console.application.service.DnsResolverService;
import org.tdmx.console.application.service.SystemSettingsService;

public class SystemPropertySettingsUpdateJob extends AbstractBackgroundJob {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private Logger log = LoggerFactory.getLogger(SystemPropertySettingsUpdateJob.class);

	private ScheduledExecutorService scheduler = null;
	
	private SystemSettingsService systemSettingService = null;
	private DnsResolverService dnsResolverService = null;
	private ScheduledFuture<?> future = null;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@Override
	public void init() {
		scheduler = Executors.newSingleThreadScheduledExecutor();
		// start a thread off which will periodically save the app state.
		Runnable r = new Runnable() {

			@Override
			public void run() {
				initRun();
				try {

					if ( systemSettingService == null ) {
						log.warn("systemSettingService missing.");
					} else {
						systemSettingService.updateSystemProperties();
					}
						
					if ( dnsResolverService == null) {
						log.warn("dnsResolverService missing.");
					} else {
						dnsResolverService.updateSystemResolverList();
					}
					
				} finally {
					finishRun();
				}
			}
			
		};
		
		future = scheduler.scheduleWithFixedDelay(r, 0, 3600, TimeUnit.SECONDS);
		updateSearch();
	}

	@Override
	public void shutdown() {
		if ( scheduler != null ) {
			scheduler.shutdown();
			try {
				scheduler.awaitTermination(60, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// ignore.
			}
		}
		scheduler = null;
		future = null;
	}

	@Override
	public Date getPendingDate() {
		if ( future == null ) {
			return null;
		}
		long seconds = future.getDelay(TimeUnit.SECONDS);
		if ( seconds > 0 ) {
			Calendar c = Calendar.getInstance();
			c.add(Calendar.SECOND, (int)seconds);
			return c.getTime();
		}
		return null;
	}

    //-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	@Override
	protected void logInfo(String msg) {
		log.info(msg);
	}

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

	public SystemSettingsService getSystemSettingService() {
		return systemSettingService;
	}

	public void setSystemSettingService(SystemSettingsService systemSettingService) {
		this.systemSettingService = systemSettingService;
	}

	public DnsResolverService getDnsResolverService() {
		return dnsResolverService;
	}

	public void setDnsResolverService(DnsResolverService dnsResolverService) {
		this.dnsResolverService = dnsResolverService;
	}


}
