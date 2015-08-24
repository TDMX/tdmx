/*
 * TDMX - Trusted Domain Messaging eXchange
 * 
 * Enterprise B2B messaging between separate corporations via interoperable cloud service providers.
 * 
 * Copyright (C) 2014 Peter Klauser (http://tdmx.org)
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses/.
 */
package org.tdmx.server.session;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.certificate.PKIXCertificate;
import org.tdmx.lib.control.job.NamedThreadFactory;
import org.tdmx.server.runtime.Manageable;
import org.tdmx.server.ws.session.WebServiceApiName;
import org.tdmx.server.ws.session.WebServiceSessionFactory.SeedAttribute;
import org.tdmx.server.ws.session.WebServiceSessionManager;

/**
 * The ServerSessionManager manages all the WebServiceSessionManagers and is controlled by the PartitionControlService
 * to create sessions. Notifies the PartitionControlService when sessions are idle and are removed.
 * 
 * @author Peter
 * 
 */
public class ServerSessionManagerImpl implements Manageable, Runnable, ServerSessionManager {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(ServerSessionManagerImpl.class);

	/**
	 * Delay in seconds between session timeout checks.
	 */
	private int timeoutCheckIntervalSec = 60;

	// - internal
	private List<WebServiceApiName> apiList;
	private String segment;
	private ScheduledExecutorService scheduledThreadPool = null;
	private ExecutorService sessionTimeoutExecutor = null;
	/**
	 * The WebServiceSessionManagers for MOS, MDS, MRS, ZAS arranged in a Map.
	 */
	private Map<WebServiceApiName, WebServiceSessionManager> apiManagerMap = null;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public int createSession(WebServiceApiName apiName, String sessionId, PKIXCertificate cert,
			Map<SeedAttribute, Long> seedAttributes) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int addCertificate(WebServiceApiName apiName, String sessionId, PKIXCertificate cert) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int removeCertificate(WebServiceApiName apiName, String sessionId, PKIXCertificate cert) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void init() {
		scheduledThreadPool = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(
				"SessionTimeoutExecutionService"));

		sessionTimeoutExecutor = Executors.newFixedThreadPool(1, new NamedThreadFactory("SessionTimeoutExecutor"));
	}

	@Override
	public void start(String segment, List<WebServiceApiName> apis) {
		this.apiList = Collections.unmodifiableList(apis);
		this.segment = segment;
		scheduledThreadPool.scheduleWithFixedDelay(this, getTimeoutCheckIntervalSec(), getTimeoutCheckIntervalSec(),
				TimeUnit.SECONDS);

	}

	@Override
	public void stop() {
		if (scheduledThreadPool == null) {
			return; // never initialized
		}
		scheduledThreadPool.shutdown();
		try {
			scheduledThreadPool.awaitTermination(60, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.warn("Interrupted whilst waiting for termination of scheduledThreadPool.", e);
		}
		sessionTimeoutExecutor.shutdown();
		try {
			sessionTimeoutExecutor.awaitTermination(60, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.warn("Interrupted whilst waiting for termination of jobRunners.", e);
		}
	}

	@Override
	public void run() {
		for (WebServiceApiName api : apiList) {
			processIdleSessions(api);
		}
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private void processIdleSessions(WebServiceApiName api) {
		log.info("Processing idle sessions for " + api + " in segment " + segment);
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public void setApiManagerMap(Map<WebServiceApiName, WebServiceSessionManager> apiManagerMap) {
		this.apiManagerMap = apiManagerMap;
	}

	public Map<WebServiceApiName, WebServiceSessionManager> getApiManagerMap() {
		return apiManagerMap;
	}

	public int getTimeoutCheckIntervalSec() {
		return timeoutCheckIntervalSec;
	}

	public void setTimeoutCheckIntervalSec(int timeoutCheckIntervalSec) {
		this.timeoutCheckIntervalSec = timeoutCheckIntervalSec;
	}

}
