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

import org.tdmx.lib.message.domain.Chunk;
import org.tdmx.lib.zone.domain.ChannelMessage;

/**
 * Management Services for a Chunk.
 * 
 * @author Peter
 * 
 */
public interface ChunkService {

	/**
	 * Store a Chunk, potentially overwriting previously stored Chunk with the same msgId and pos.
	 * 
	 * @param msg
	 * @param chunk
	 * @return true if created successfully
	 */
	public boolean storeChunk(ChannelMessage msg, Chunk chunk);

	/**
	 * Fetch the Chunk for the message and position.
	 * 
	 * @param msg
	 * @param pos
	 * @return chunk if fetched, else null if not found.
	 */
	public Chunk fetchChunk(ChannelMessage msg, int pos);

	/**
	 * Delete all chunks belonging to the message.
	 * 
	 * @param msg
	 * @return true if all chunks were deleted.
	 */
	public boolean deleteChunks(ChannelMessage msg);

}
