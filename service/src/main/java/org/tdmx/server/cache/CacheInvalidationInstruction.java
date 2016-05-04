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
package org.tdmx.server.cache;

import java.util.UUID;

import org.tdmx.server.pcs.protobuf.Cache.CacheName;

/**
 * A cache invalidation instruction as a value type. Identity based soley on ID field.
 * 
 * @author Peter Klauser
 * 
 */
public class CacheInvalidationInstruction {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	private final String id;

	private final CacheName name;

	private final String key;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	private CacheInvalidationInstruction(String id, CacheName cacheName, String key) {
		this.id = id;
		this.name = cacheName;
		this.key = key;
	}

	public static CacheInvalidationInstruction clearCache(CacheName cache) {
		return new CacheInvalidationInstruction(UUID.randomUUID().toString(), cache, null);
	}

	public static CacheInvalidationInstruction clearCacheKey(CacheName cache, String key) {
		return new CacheInvalidationInstruction(UUID.randomUUID().toString(), cache, key);
	}

	public static CacheInvalidationInstruction newInstruction(String id, CacheName cache, String key) {
		return new CacheInvalidationInstruction(id, cache, key);
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CacheInvalidation [");
		builder.append(" id=").append(id);
		builder.append(" ").append(name);
		if (key != null) {
			builder.append(" key=").append(key);
		}
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CacheInvalidationInstruction)) {
			return false;
		}
		CacheInvalidationInstruction other = (CacheInvalidationInstruction) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
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

	public String getId() {
		return id;
	}

	public CacheName getName() {
		return name;
	}

	public String getKey() {
		return key;
	}

}
