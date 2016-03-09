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
package org.tdmx.client.crypto.stream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A FileBackedOutputStream writes through to a File after chunkSize bytes have been written to the underlying output
 * stream.
 * 
 * The number of bytes written to the underlying stream is returned with {@link #getSize()}
 * 
 * The File backing the OutputStream can be retrieved after closing with {@link #getStorage()} if there is one, else the
 * {@link #getMemory()} will return the underlying bytes from memory.
 * 
 * The caller should take care to call {@link #close()}.
 * 
 * The caller should always call {@link #discard()}, especially if the {@link #getInputStream()} has not been called.
 * {@link #getInputStream()} converts the backing File into an InputStream and removes it from this OutputStream.
 * 
 * @author Peter
 * 
 */
public class FileBackedOutputStream extends OutputStream {

	private static final Logger log = LoggerFactory.getLogger(FileBackedOutputStream.class);

	private OutputStream output;
	private long size = 0;
	private final int chunkSize;
	private boolean closed = false;
	private ByteArrayOutputStream baos;
	private File tmpFile;
	private final File tmpDirectory;

	public FileBackedOutputStream(int chunkSize, File directory) {
		this.baos = new ByteArrayOutputStream(chunkSize);
		this.output = baos;
		this.chunkSize = chunkSize;
		this.tmpDirectory = directory;
	}

	public InputStream getInputStream() {
		if (!isClosed()) {
			throw new IllegalStateException("getInputStream called when output stream open.");
		}
		if (baos != null) {
			InputStream is = new ByteArrayInputStream(baos.toByteArray());
			baos = null;
			return is;
		} else if (tmpFile != null) {
			InputStream fis;
			try {
				fis = new FileBackedInputStream(tmpFile);
			} catch (FileNotFoundException e) {
				throw new IllegalStateException("Converting existing File into InputStream failed unexpectedly.", e);
			}
			tmpFile = null;
			return fis;
		}
		throw new IllegalStateException("getInputStream called when output stream open.");
	}

	@Override
	public void write(int b) throws IOException {
		output.write(b);
		size++;
	}

	@Override
	public void write(byte b[]) throws IOException {
		size += b.length;
		if (tmpFile == null && size > chunkSize) {
			replaceStorage();
		}
		output.write(b, 0, b.length);
	}

	@Override
	public void write(byte b[], int off, int len) throws IOException {
		size += len;
		if (tmpFile == null && size > chunkSize) {
			replaceStorage();
		}
		output.write(b, off, len);
	}

	@Override
	public void flush() throws IOException {
		output.flush();
	}

	@Override
	public void close() throws IOException {
		output.close();
		this.closed = true;
	}

	public void discard() {
		if (!isClosed()) {
			try {
				close();
			} catch (IOException e) {
				log.warn("Unable to close.", e);
			}
		}
		baos = null;
		if (tmpFile != null) {
			if (!tmpFile.delete()) {
				log.warn("Unable to delete.");
			}
		}
	}

	private void replaceStorage() throws IOException {
		tmpFile = File.createTempFile("fbos", ".buf", tmpDirectory);
		FileOutputStream fos = new FileOutputStream(tmpFile);
		fos.write(baos.toByteArray());

		output = fos;
		baos = null;
	}

	/**
	 * @return the size
	 */
	public long getSize() {
		return size;
	}

	/**
	 * @return the closed
	 */
	public boolean isClosed() {
		return closed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		discard();
		super.finalize();
	}
}
