package org.tdmx.console.application.domain;


/**
 * A value object which can be used as a component part of a DomainObject.
 * 
 * @author Peter
 *
 */
public interface ValueObject {

	public <E extends ValueObject> E copy();
	
}
