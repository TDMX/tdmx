package org.tdmx.console.application.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.tdmx.console.application.search.SearchableObjectField;
import org.tdmx.console.application.service.ObjectRegistry;
import org.tdmx.console.domain.validation.FieldError;
import org.tdmx.console.domain.validation.FieldValidationException;



/**
 * A domain object.
 * 
 * @author Peter
 *
 */
public interface DomainObject {

	public static final List<SearchableObjectField> NO_SEARCH_FIELDS = Collections.unmodifiableList(new ArrayList<SearchableObjectField>());
	
	public String getId();

	public DomainObjectType getType();
	
	/**
	 * Merge another DomainObject of the same type and declare in the result 
	 * which fields' values have changed.
	 * @param other
	 * @return list of fields who's values have changed in the merge.
	 */
	public <E extends DomainObject> DomainObjectFieldChanges merge( E other );
	
	public List<FieldError> validate();

	public void check() throws FieldValidationException;
	
	public void updateSearchFields( ObjectRegistry registry );
	
	public List<SearchableObjectField> getSearchFields();
}
