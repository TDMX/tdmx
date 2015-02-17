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

import java.util.Date;
import java.util.List;

import org.tdmx.lib.control.domain.Lock;

/**
 * The LockService.
 * 
 * Used to control concurrent access from multiple JVMs running the same code concurrently - like polling for ready jobs
 * every X seconds.
 * 
 * 
 * @author Peter
 * 
 */
public interface LockService {

	/**
	 * Acquire a lock and reserve it in the name of the holderIdentifier.
	 * 
	 * @param lockName
	 * @param holderIdentitifier
	 * @return true if the lock is held by the caller, false if the lock is not aquireable.
	 */
	public boolean acquireLock(String lockName, String holderIdentitifier);

	/**
	 * Release a lock held by the holderIdentifier until the resrveUntil date.
	 * 
	 * @param lockId
	 * @param holderIdentitifier
	 *            the holder of the lock @see {@link #acquireLock(String, String)}
	 * @param reserveUntil
	 *            null if release immediately, or some future date.
	 */
	public void releaseLock(String lockName, String holderIdentitifier, Date reserveUntil);

	public void createOrUpdate(Lock value);

	// a partition can only be deleted if it is not yet activated.
	public void delete(Lock value);

	public Lock findByName(String lockName);

	public List<Lock> findAll();

}
