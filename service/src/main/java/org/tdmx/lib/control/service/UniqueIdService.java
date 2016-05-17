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
package org.tdmx.lib.control.service;

/**
 * The UniqueIdService.
 * 
 * UniqueId's are generated of the MaxValue[name]+UPC-check-digit. The service uses caching and bulk incrementing of the
 * MaxValue for scalability.
 * 
 * @author Peter
 * 
 */
public interface UniqueIdService {

	/**
	 * Gets a uniqueId.
	 * 
	 * @return
	 */
	public String getNextId();

	/**
	 * Use UPC algorithm to determine if the uniqueId is valid.
	 * 
	 * @param uniqueId
	 * @return
	 */
	public boolean isValid(String uniqueId);
}
