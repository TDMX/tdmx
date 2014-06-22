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

	public static final int DEFAULT_MAX_SIZE_MEMORY = 1024 * 1024;
	public static final int DEFAULT_INITIAL_SIZE_MEMORY = 8192;

	private int maxSizeMemory;
	private int initialSizeMemory;
	private final File directory;

	public TemporaryFileManagerImpl(int maxSizeMemory, int initialSizeMemory) {
		this(maxSizeMemory, initialSizeMemory, System.getProperty("java.io.tmpdir"));
	}

	public TemporaryFileManagerImpl(String directoryName) {
		this(DEFAULT_MAX_SIZE_MEMORY, DEFAULT_INITIAL_SIZE_MEMORY, directoryName);
	}

	public TemporaryFileManagerImpl() {
		this(DEFAULT_MAX_SIZE_MEMORY, DEFAULT_INITIAL_SIZE_MEMORY, System.getProperty("java.io.tmpdir"));
	}

	public TemporaryFileManagerImpl(int maxSizeMemory, int initialSizeMemory, String directoryName) {
		directory = new File(directoryName);
	}

	/**
	 * @return the maxSizeMemory
	 */
	public int getMaxSizeMemory() {
		return maxSizeMemory;
	}

	/**
	 * @param maxSizeMemory
	 *            the maxSizeMemory to set
	 */
	public void setMaxSizeMemory(int maxSizeMemory) {
		this.maxSizeMemory = maxSizeMemory;
	}

	/**
	 * @return the initialSizeMemory
	 */
	public int getInitialSizeMemory() {
		return initialSizeMemory;
	}

	/**
	 * @param initialSizeMemory
	 *            the initialSizeMemory to set
	 */
	public void setInitialSizeMemory(int initialSizeMemory) {
		this.initialSizeMemory = initialSizeMemory;
	}

	public File getTempDirectory() {
		return directory;
	}

	@Override
	public FileBackedOutputStream getOutputStream() {
		return new FileBackedOutputStream(initialSizeMemory, maxSizeMemory, directory);
	}

}
