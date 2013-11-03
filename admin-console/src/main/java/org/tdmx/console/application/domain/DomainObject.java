package org.tdmx.console.application.domain;



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
}
