package org.tdmx.console.application.job;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.tdmx.console.application.domain.DomainObject;
import org.tdmx.console.application.domain.DomainObjectChangesHolder;
import org.tdmx.console.application.domain.DomainObjectField;
import org.tdmx.console.application.domain.DomainObjectFieldChanges;
import org.tdmx.console.application.domain.DomainObjectType;
import org.tdmx.console.application.domain.ProblemDO;
import org.tdmx.console.application.search.SearchService;
import org.tdmx.console.application.search.SearchServiceImpl.ObjectSearchContext;
import org.tdmx.console.application.search.SearchableObjectField;
import org.tdmx.console.application.service.ObjectRegistry;
import org.tdmx.console.application.service.ProblemRegistry;
import org.tdmx.console.domain.validation.FieldError;
import org.tdmx.console.domain.validation.FieldValidationException;

public abstract class AbstractBackgroundJob implements BackgroundJobSPI {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------
	public static final DomainObjectField F_NAME	= new DomainObjectField("name", DomainObjectType.BackgroundJob);

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------

	protected String name;
	protected ProblemRegistry problemRegistry;
	protected SearchService searchService;

	protected AtomicInteger processingId = new AtomicInteger(0);
	protected Date lastCompletedDate;
	protected Date startedRunningDate;
	protected ProblemDO lastProblem;
	protected List<SearchableObjectField> searchFields = NO_SEARCH_FIELDS;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	/**
	 * Initialize the BackgroundJob. 
	 */
	@Override
	public abstract void init();
	
	/**
	 * Shutdown the BackgroundJob.
	 */
	@Override
	public abstract void shutdown();
	
	
	@Override
	public int getExecutions() {
		return processingId.get();
	}

	@Override
	public Date getRunningDate() {
		return startedRunningDate;
	}

	@Override
	public Date getLastCompletedDate() {
		return lastCompletedDate;
	}
	
	@Override
	public ProblemDO getLastProblem() {
		return lastProblem;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractBackgroundJob other = (AbstractBackgroundJob) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String getId() {
		return getName();
	}

	@Override
	public DomainObjectType getType() {
		return DomainObjectType.BackgroundJob;
	}

	@Override
	public void updateSearchFields(ObjectRegistry registry) {
		ObjectSearchContext ctx = new ObjectSearchContext();
		ctx.sof(this, BackgroundJobSO.NAME, getName());
		searchFields = ctx.getSearchFields();
	}

	@Override
	public List<SearchableObjectField> getSearchFields() {
		return searchFields;
	}

	@Override
	public <E extends DomainObject> DomainObjectFieldChanges merge(E other) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<FieldError> validate() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void check() throws FieldValidationException {
		throw new UnsupportedOperationException();
	}

    //-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	protected void updateSearch() {
		if ( getSearchService() != null ) {
			DomainObjectChangesHolder h = new DomainObjectChangesHolder();
			DomainObjectFieldChanges dofc = new DomainObjectFieldChanges(this);
			h.registerModified(dofc);
			getSearchService().update(h);
		}
	}
	
	protected void initRun() {
		startedRunningDate = new Date();
		int runNr = processingId.getAndIncrement();
		lastProblem = null;
		logInfo(getName() + " started " + runNr);
		updateSearch();
	}
	
	protected void finishRun() {
		lastCompletedDate = new Date();
		startedRunningDate = null;
		logInfo(getName() + " completed " + processingId.get());
		updateSearch();
	}
	
	protected abstract void logInfo( String msg );
	
	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ProblemRegistry getProblemRegistry() {
		return problemRegistry;
	}

	public void setProblemRegistry(ProblemRegistry problemRegistry) {
		this.problemRegistry = problemRegistry;
	}

	public SearchService getSearchService() {
		return searchService;
	}

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}
}
