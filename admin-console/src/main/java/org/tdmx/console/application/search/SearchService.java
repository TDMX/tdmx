package org.tdmx.console.application.search;

import java.util.Set;

import org.tdmx.console.application.domain.DomainObject;
import org.tdmx.console.application.domain.DomainObjectChangesHolder;
import org.tdmx.console.application.domain.DomainObjectType;


public interface SearchService {

	public void update( DomainObjectChangesHolder holder );

	public SearchCriteria parse( DomainObjectType type, String text );
	
	public Set<DomainObject> search( SearchCriteria criteria );
}
