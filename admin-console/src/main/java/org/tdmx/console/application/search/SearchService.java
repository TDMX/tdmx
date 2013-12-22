package org.tdmx.console.application.search;

import java.util.List;
import java.util.Set;

import org.tdmx.console.application.domain.DomainObject;
import org.tdmx.console.application.domain.DomainObjectChangesHolder;
import org.tdmx.console.application.domain.DomainObjectType;


public interface SearchService {

	public void update( DomainObjectChangesHolder holder );

	public <E extends DomainObject> Set<E> search( DomainObjectType type, String text );
	
	public List<SearchableObjectField> getSearchableFields( DomainObject object );
}
