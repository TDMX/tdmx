package org.tdmx.console.application.domain;

import java.util.List;

import org.tdmx.console.application.domain.validation.FieldError;
import org.tdmx.console.application.domain.validation.FieldValidationException;



/**
 * A domain object.
 * 
 * @author Peter
 *
 */
public interface DomainObject {

	public String getId();

	/**
	 * Merge another DomainObject of the same type and declare in the result 
	 * which fields' values have changed.
	 * @param other
	 * @return list of fields who's values have changed in the merge.
	 */
	public <E extends DomainObject> DomainObjectFieldChanges merge( E other );
	
	public <E extends DomainObject> E copy();
	
	public List<FieldError> validate();

	public void check() throws FieldValidationException;
}
