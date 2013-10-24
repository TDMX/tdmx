package org.tdmx.console.application.job;

import java.io.IOException;
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
import org.tdmx.console.application.dao.ServiceProviderStorage;
import org.tdmx.console.application.dao.ServiceProviderStore;
import org.tdmx.console.application.domain.ProblemDO;
import org.tdmx.console.application.domain.ProblemDO.ProblemCode;
import org.tdmx.console.application.service.ObjectRegistrySPI;

public class StateStorageJob extends AbstractBackgroundJob {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private Logger log = LoggerFactory.getLogger(StateStorageJob.class);
	
	private ScheduledExecutorService scheduler = null;
	
	private ObjectRegistrySPI registry = null;
	private ServiceProviderStore store = null;
	private List<ScheduledFuture<?>> futureList = new LinkedList<>();
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@Override
	public void init() {
		scheduler = Executors.newSingleThreadScheduledExecutor();
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
		futureList.clear();
	}

	@Override
	public Date getPendingDate() {
		clearCompletedTasks();
		if ( futureList.isEmpty() ) {
			return null;
		}
		long seconds = futureList.get(0).getDelay(TimeUnit.SECONDS);
		if ( seconds > 0 ) {
			Calendar c = Calendar.getInstance();
			c.add(Calendar.SECOND, (int)seconds);
			return c.getTime();
		}
		return null;
	}

	public void flushStorage()  {
		clearCompletedTasks();
		
		// start a thread off which will periodically save the app state.
		Runnable r = new Runnable() {

			@Override
			public void run() {
				startedRunningDate = new Date();
				int runNr = processingId.getAndIncrement();
				
				log.info(getName() + " started " + runNr);
				try {
					ServiceProviderStorage s = registry.getContentIfDirty();
					if ( s != null ) {
						store.save(s);
					}
				} catch (IOException e) {
					ProblemDO p = new ProblemDO(ProblemCode.CONFIGURATION_FILE_WRITE_IO, e);
					problemRegistry.addProblem(p);
				} catch (JAXBException e) {
					ProblemDO p = new ProblemDO(ProblemCode.CONFIGURATION_FILE_MARSHAL, e);
					problemRegistry.addProblem(p);
				} finally {
					lastCompletedDate = new Date();
					startedRunningDate = null;
					log.info(getName() + " completed " + runNr);
				}
			}
			
		};
		if ( scheduler != null && !scheduler.isShutdown()) {
			ScheduledFuture<?> future = scheduler.schedule(r, 10, TimeUnit.SECONDS);
			futureList.add(future);
			log.info(getName() + " scheduled.");
		}
	}


    //-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------
	private void clearCompletedTasks() {
		Iterator<ScheduledFuture<?>> iterator = futureList.iterator();
		while( iterator.hasNext()) {
			ScheduledFuture<?> future = iterator.next();
			if ( future.isDone() ) {
				iterator.remove();
			}
		}
	}
	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

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


}
