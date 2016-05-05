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
package org.tdmx.server.rs.sas.resource;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.tdmx.core.system.lang.EnumUtils;
import org.tdmx.server.cache.CacheInvalidationInstruction;
import org.tdmx.server.pcs.protobuf.Cache.CacheName;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "cacheInvalidationInstruction")
@XmlType(name = "CacheInvalidationInstruction")
public class CacheInvalidationInstructionValue {

	public enum FIELD {
		ID("id"),
		CACHE("cache"),
		KEY("key");

		private final String n;

		private FIELD(String n) {
			this.n = n;
		}

		@Override
		public String toString() {
			return this.n;
		}
	}

	private String id;
	private String cache;
	private String key;

	public String getCliRepresentation() {
		StringBuilder buf = new StringBuilder();
		buf.append("CacheInvalidationInstruction");
		buf.append("; ").append(id);
		buf.append("; ").append(cache);
		if (key != null) {
			buf.append("; ").append(key);
		}
		return buf.toString();
	}

	public static CacheInvalidationInstructionValue mapFrom(CacheInvalidationInstruction other) {
		if (other == null) {
			return null;
		}
		CacheInvalidationInstructionValue r = new CacheInvalidationInstructionValue();
		r.setId(other.getId());
		r.setCache(EnumUtils.mapToString(other.getName()));
		r.setKey(other.getKey());
		return r;
	}

	public static CacheInvalidationInstruction mapTo(CacheInvalidationInstructionValue other) {
		if (other == null) {
			return null;
		}
		CacheInvalidationInstruction r = CacheInvalidationInstruction.newInstruction(other.getId(),
				EnumUtils.mapTo(CacheName.class, other.getCache()), other.getKey());

		return r;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCache() {
		return cache;
	}

	public void setCache(String cache) {
		this.cache = cache;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
