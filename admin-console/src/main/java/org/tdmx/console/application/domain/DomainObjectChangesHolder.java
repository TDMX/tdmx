package org.tdmx.console.application.domain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 
 * 
 * @author Peter
 *
 */
public final class DomainObjectChangesHolder {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------

	
	public Set<DomainObject> newObjects = new HashSet<>();
	public Set<DomainObject> modifiedObjects = new HashSet<>();
	public Set<DomainObject> deletedObjects = new HashSet<>();
	
	public Map<DomainObject, DomainObjectFieldChanges> changedMap = new HashMap<>();

	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	public void registerNew( DomainObject object ) {
		newObjects.add(object);
	}
	
	public void registerModified( DomainObjectFieldChanges changes ) {
		DomainObject object = changes.getObject();
		if ( !deletedObjects.contains(object)) {
			modifiedObjects.add(object);
			
			DomainObjectFieldChanges existingFieldChanges = changedMap.get(object);
			if ( existingFieldChanges != null ) {
				existingFieldChanges.combine(changes);
			} else {
				changedMap.put(object, changes);
			}
		}
	}
	
	public void registerDeleted( DomainObject object ) {
		deletedObjects.add(object);
		modifiedObjects.remove(object);
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
	
}
