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
package org.tdmx.lib.message.dao;

import static org.tdmx.lib.message.domain.QChunk.chunk;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.tdmx.lib.message.domain.Chunk;

import com.mysema.query.jpa.impl.JPAQuery;

public class ChunkDaoImpl implements ChunkDao {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------

	@PersistenceContext(unitName = "MessageDB")
	private EntityManager em;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void persist(Chunk value) {
		em.persist(value);
	}

	@Override
	public void delete(Chunk value) {
		em.remove(value);
	}

	@Override
	public Chunk merge(Chunk value) {
		return em.merge(value);
	}

	@Override
	public Chunk loadById(Long id) {
		return new JPAQuery(em).from(chunk).where(chunk.id.eq(id)).uniqueResult(chunk);
	}

	@Override
	public Chunk loadByMsgIdAndPos(String msgId, int pos) {
		return new JPAQuery(em).from(chunk).where(chunk.msgId.eq(msgId).and(chunk.pos.eq(pos))).uniqueResult(chunk);
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
