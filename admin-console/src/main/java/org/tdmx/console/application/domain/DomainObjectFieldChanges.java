package org.tdmx.console.application.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * 
 * @author Peter
 *
 */
public final class DomainObjectFieldChanges {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------

	private DomainObject object;
	
	public List<DomainObjectField> fields = new ArrayList<>();

	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	public DomainObjectFieldChanges( DomainObject object ) {
		this.object = object;
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	public void field( DomainObjectField field ) {
		fields.add(field);
	}
	
	public boolean isEmpty() {
		return !fields.isEmpty();
	}
	
	public void combine( DomainObjectFieldChanges other ) {
		fields.addAll(other.getFields());
	}
	
    //-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------
	
	public DomainObject getObject() {
		return object;
	}

	public List<DomainObjectField> getFields() {
		return fields;
	}

}
