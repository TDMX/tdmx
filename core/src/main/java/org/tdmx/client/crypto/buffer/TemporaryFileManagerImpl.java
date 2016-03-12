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

import org.tdmx.client.crypto.algorithm.DigestAlgorithm;
import org.tdmx.client.crypto.stream.FileBackedOutputStream;

public class TemporaryFileManagerImpl implements TemporaryBufferFactory {

	public static final int DEFAULT_CHUNK_SIZE = 1024 * 1024;
	public static final DigestAlgorithm DEFAULT_CHUNK_MAC = DigestAlgorithm.SHA_256;

	private int chunkSize = DEFAULT_CHUNK_SIZE;
	private DigestAlgorithm chunkDigestAlgorithm = DEFAULT_CHUNK_MAC;
	private String tempDirectory = System.getProperty("java.io.tmpdir");

	public TemporaryFileManagerImpl() {
	}

	@Override
	public FileBackedOutputStream getOutputStream() {
		return new FileBackedOutputStream(chunkSize, new File(tempDirectory));
	}

	@Override
	public int getChunkSize() {
		return chunkSize;
	}

	@Override
	public DigestAlgorithm getChunkDigestAlgorithm() {
		return chunkDigestAlgorithm;
	}

	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}

	public void setChunkDigestAlgorithm(DigestAlgorithm chunkDigestAlgorithm) {
		this.chunkDigestAlgorithm = chunkDigestAlgorithm;
	}

	public void setTempDirectory(String tempDirectory) {
		this.tempDirectory = tempDirectory;
	}

	public String getTempDirectory() {
		return tempDirectory;
	}

}
