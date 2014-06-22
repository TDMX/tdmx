/*
 * TDMX - Trusted Domain Messaging eXchange
 * 
 * Enterprise B2B messaging between separate corporations via interoperable cloud service providers.
 * 
 * Copyright (C) 2014 Peter Klauser (http://tdmx.org)
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses/.
 */
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

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	public Set<DomainObject> newObjects = new HashSet<>();
	public Set<DomainObject> modifiedObjects = new HashSet<>();
	public Set<DomainObject> deletedObjects = new HashSet<>();

	public Map<DomainObject, DomainObjectFieldChanges> changedMap = new HashMap<>();

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	public void registerNew(DomainObject object) {
		newObjects.add(object);
	}

	public void registerModified(DomainObjectFieldChanges changes) {
		DomainObject object = changes.getObject();
		if (!deletedObjects.contains(object)) {
			modifiedObjects.add(object);

			DomainObjectFieldChanges existingFieldChanges = changedMap.get(object);
			if (existingFieldChanges != null) {
				existingFieldChanges.combine(changes);
			} else {
				changedMap.put(object, changes);
			}
		}
	}

	public void registerDeleted(DomainObject object) {
		deletedObjects.add(object);
		modifiedObjects.remove(object);
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

}
