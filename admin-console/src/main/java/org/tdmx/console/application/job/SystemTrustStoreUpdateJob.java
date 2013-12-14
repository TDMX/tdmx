package org.tdmx.console.application.job;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.CryptoCertificateException;
import org.tdmx.console.application.dao.SystemTrustStore;
import org.tdmx.console.application.domain.ProblemDO;
import org.tdmx.console.application.domain.ProblemDO.ProblemCode;

public class SystemTrustStoreUpdateJob extends AbstractBackgroundJob {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private Logger log = LoggerFactory.getLogger(SystemTrustStoreUpdateJob.class);
	
	private ScheduledExecutorService scheduler = null;
	
	private SystemTrustStore trustStore = null;

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

					try {
						trustStore.getAllTrustedCAs();
						
						//TODO feed to replaceList
						
						//TODO getSystem RootCA-List from RootCAService replaceList(System)
						
					} catch (CryptoCertificateException e) {
						ProblemDO p = new ProblemDO(ProblemCode.SYSTEM_TRUST_STORE_EXCEPTION, e);
						problemRegistry.addProblem(p);
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

	public SystemTrustStore getTrustStore() {
		return trustStore;
	}

	public void setTrustStore(SystemTrustStore trustStore) {
		this.trustStore = trustStore;
	}

}
