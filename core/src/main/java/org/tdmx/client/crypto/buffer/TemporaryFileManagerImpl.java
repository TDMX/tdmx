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
package org.tdmx.client.crypto.buffer;

import java.io.File;

import org.tdmx.client.crypto.stream.FileBackedOutputStream;

public class TemporaryFileManagerImpl implements TemporaryBufferFactory {

	public static final int DEFAULT_CHUNK_SIZE = 1024 * 1024;

	private int chunkSize;
	private final File directory;

	public TemporaryFileManagerImpl(int chunkSize) {
		this(chunkSize, System.getProperty("java.io.tmpdir"));
	}

	public TemporaryFileManagerImpl() {
		this(DEFAULT_CHUNK_SIZE, System.getProperty("java.io.tmpdir"));
	}

	public TemporaryFileManagerImpl(int chunkSize, String directoryName) {
		this.chunkSize = chunkSize;
		this.directory = new File(directoryName);
	}

	public File getTempDirectory() {
		return directory;
	}

	@Override
	public FileBackedOutputStream getOutputStream() {
		return new FileBackedOutputStream(chunkSize, directory);
	}

	@Override
	public int getChunkSize() {
		return chunkSize;
	}

}
