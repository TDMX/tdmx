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
package org.tdmx.console.application.job;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.console.application.dao.PrivateKeyStore;
import org.tdmx.console.application.dao.ServiceProviderStorage;
import org.tdmx.console.application.dao.ServiceProviderStore;
import org.tdmx.console.application.domain.ProblemDO;
import org.tdmx.console.application.domain.ProblemDO.ProblemCode;
import org.tdmx.console.application.service.ObjectRegistryChangeListener;
import org.tdmx.console.application.service.ObjectRegistrySPI;

public class StateStorageJob extends AbstractBackgroundJob implements ObjectRegistryChangeListener {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private final Logger log = LoggerFactory.getLogger(StateStorageJob.class);

	private ScheduledExecutorService scheduler = null;

	private ObjectRegistrySPI registry = null;
	private PrivateKeyStore keyStore = null;

	private ServiceProviderStore store = null;
	private final List<ScheduledFuture<?>> futureList = new LinkedList<>();

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void init() {
		scheduler = Executors.newSingleThreadScheduledExecutor();
	}

	@Override
	public void shutdown() {
		if (scheduler != null) {
			scheduler.shutdown();
			try {
				scheduler.awaitTermination(60, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// ignore.
			}
		}
		scheduler = null;
		futureList.clear();
	}

	@Override
	public Date getPendingDate() {
		clearCompletedTasks();
		if (futureList.isEmpty()) {
			return null;
		}
		long seconds = futureList.get(0).getDelay(TimeUnit.SECONDS);
		if (seconds > 0) {
			Calendar c = Calendar.getInstance();
			c.add(Calendar.SECOND, (int) seconds);
			return c.getTime();
		}
		return null;
	}

	@Override
	public void notifyObjectRegistryChanged() {
		flushStorage();
	}

	public void flushStorage() {
		clearCompletedTasks();

		// start a thread off which will periodically save the app state.
		Runnable r = new Runnable() {

			@Override
			public void run() {
				initRun();
				try {

					try {
						if (keyStore == null) {
							log.warn("keyStore missing.");
						} else {
							keyStore.save();
						}
					} catch (NoSuchAlgorithmException e) {
						ProblemDO p = new ProblemDO(ProblemCode.KEY_STORE_ALGORITHM, e);
						problemRegistry.addProblem(p);
					} catch (CertificateException e) {
						ProblemDO p = new ProblemDO(ProblemCode.KEY_STORE_EXCEPTION, e);
						problemRegistry.addProblem(p);
					} catch (KeyStoreException e) {
						ProblemDO p = new ProblemDO(ProblemCode.KEY_STORE_KEYSTORE_EXCEPTION, e);
						problemRegistry.addProblem(p);
					} catch (IOException e) {
						ProblemDO p = new ProblemDO(ProblemCode.KEY_STORE_IO_EXCEPTION, e);
						problemRegistry.addProblem(p);
					}

					ServiceProviderStorage s = null;
					try {
						if (registry == null) {
							log.warn("registry missing.");
						} else {
							s = registry.getContentIfDirty();
						}
					} catch (Exception c) {
						ProblemDO p = new ProblemDO(ProblemCode.OBJECT_REGISTRY_WRITE, c);
						problemRegistry.addProblem(p);
					}

					try {
						if (store == null) {
							log.warn("store missing.");
						} else {
							if (s != null) {
								store.save(s);
							}
						}
					} catch (IOException e) {
						lastProblem = new ProblemDO(ProblemCode.CONFIGURATION_FILE_WRITE_IO, e);
						problemRegistry.addProblem(lastProblem);
					} catch (JAXBException e) {
						lastProblem = new ProblemDO(ProblemCode.CONFIGURATION_FILE_MARSHAL, e);
						problemRegistry.addProblem(lastProblem);
					}

				} catch (Throwable t) {
					log.warn("Unexpected RuntimeException.", t);
					ProblemDO p = new ProblemDO(ProblemCode.RUNTIME_EXCEPTION, t);
					problemRegistry.addProblem(p);
					throw t;
				} finally {
					finishRun();
				}
			}

		};
		if (scheduler != null && !scheduler.isShutdown()) {
			ScheduledFuture<?> future = scheduler.schedule(r, 10, TimeUnit.SECONDS);
			futureList.add(future);
			log.info(getName() + " scheduled.");
			updateSearch();
		}
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	@Override
	protected void logInfo(String msg) {
		log.info(msg);
	}

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------
	private void clearCompletedTasks() {
		Iterator<ScheduledFuture<?>> iterator = futureList.iterator();
		while (iterator.hasNext()) {
			ScheduledFuture<?> future = iterator.next();
			if (future.isDone()) {
				iterator.remove();
			}
		}
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public ObjectRegistrySPI getRegistry() {
		return registry;
	}

	public void setRegistry(ObjectRegistrySPI registry) {
		this.registry = registry;
	}

	public ServiceProviderStore getStore() {
		return store;
	}

	public void setStore(ServiceProviderStore store) {
		this.store = store;
	}

	public PrivateKeyStore getPrivateKeyStore() {
		return keyStore;
	}

	public void setPrivateKeyStore(PrivateKeyStore keyStore) {
		this.keyStore = keyStore;
	}

}
