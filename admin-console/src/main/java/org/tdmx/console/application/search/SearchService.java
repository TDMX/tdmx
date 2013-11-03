package org.tdmx.console.application.search;

import java.util.List;
import java.util.Set;

import org.tdmx.console.application.domain.DomainObject;
import org.tdmx.console.application.domain.DomainObjectChangesHolder;


public interface SearchService {

	public void update( DomainObjectChangesHolder holder );

	public List<String> suggestion( String text );
	
	public SearchCriteria parse( String text );
	
	public Set<DomainObject> search( SearchCriteria criteria );
}
