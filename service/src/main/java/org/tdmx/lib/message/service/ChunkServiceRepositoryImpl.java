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

package org.tdmx.lib.message.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.lib.control.datasource.ThreadLocalPartitionIdProvider;
import org.tdmx.lib.message.dao.ChunkDao;
import org.tdmx.lib.message.domain.Chunk;
import org.tdmx.lib.zone.domain.ChannelMessage;

/**
 * NonTransactional CRUD Services for Chunk Entity.
 * 
 * @author Peter Klauser
 * 
 */
public class ChunkServiceRepositoryImpl implements ChunkService {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(ChunkServiceRepositoryImpl.class);

	private ThreadLocalPartitionIdProvider partitionIdProvider;
	private ChunkDao chunkDao;

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void storeChunk(ChannelMessage msg, Chunk chunk) {
		String partitionId = getPartitionId(msg);
		partitionIdProvider.setPartitionId(partitionId);
		try {
			chunkDao.store(chunk);
		} finally {
			partitionIdProvider.clearPartitionId();
		}
	}

	@Override
	public Chunk fetchChunk(ChannelMessage msg, int pos) {
		String partitionId = getPartitionId(msg);
		partitionIdProvider.setPartitionId(partitionId);
		try {
			return chunkDao.loadByMsgIdAndPos(msg.getMsgId(), pos);
		} finally {
			partitionIdProvider.clearPartitionId();
		}
	}

	@Override
	public void deleteChunks(ChannelMessage msg) {
		String partitionId = getPartitionId(msg);
		partitionIdProvider.setPartitionId(partitionId);
		try {
			chunkDao.deleteByMsgId(msg.getMsgId());
		} finally {
			partitionIdProvider.clearPartitionId();
		}

	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private String getPartitionId(ChannelMessage msg) {
		return "p1"; // TODO
	}
	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public ChunkDao getChunkDao() {
		return chunkDao;
	}

	public void setChunkDao(ChunkDao chunkDao) {
		this.chunkDao = chunkDao;
	}

	public ThreadLocalPartitionIdProvider getPartitionIdProvider() {
		return partitionIdProvider;
	}

	public void setPartitionIdProvider(ThreadLocalPartitionIdProvider partitionIdProvider) {
		this.partitionIdProvider = partitionIdProvider;
	}

}
