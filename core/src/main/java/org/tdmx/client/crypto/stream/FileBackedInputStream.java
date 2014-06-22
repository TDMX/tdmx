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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * 
 * The temporary File backing the input stream is deleted on {@link #close()} or at latest on {@link #finalize()}
 * 
 * 
 * @author Peter
 * 
 */
public class FileBackedInputStream extends FileInputStream {

	private final File tmpFile;

	public FileBackedInputStream(File file) throws FileNotFoundException {
		super(file);
		this.tmpFile = file;
	}

	public File getFile() {
		return tmpFile;
	}

	@Override
	public void close() throws IOException {
		IOException delegate = null;
		try {
			super.close();
		} catch (IOException e) {
			delegate = e;
		}
		if (tmpFile != null) {
			if (!tmpFile.delete()) {
				// TODO warn but ignore
			}
		}
		if (delegate != null) {
			throw delegate;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws IOException {
		close();
		super.finalize();
	}
}
