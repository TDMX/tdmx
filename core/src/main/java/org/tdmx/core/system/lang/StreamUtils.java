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
package org.tdmx.core.system.lang;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamUtils {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(StreamUtils.class);

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	private StreamUtils() {
	};

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	/**
	 * Transfer the contents of the input stream to the output stream.
	 * 
	 * @param is
	 * @param os
	 */
	public static void transfer(InputStream is, OutputStream os) throws IOException {
		byte[] buffer = new byte[2048];
		int read = -1;
		while ((read = is.read(buffer)) != -1) {
			os.write(buffer, 0, read);
		}
	}

	/**
	 * Read len bytes from the input stream into the buf at the offset position.
	 * 
	 * @param is
	 * @param buf
	 * @param offset
	 * @param len
	 * @throws IOException
	 *             if the EOF is reached
	 */
	public static void fill(InputStream is, byte[] buf, int offset, int len) throws IOException {
		while (len > 0) {
			int lenRead = is.read(buf, offset, len);
			if (lenRead == -1) {
				throw new IOException("read past EOF");
			}
			len -= lenRead;
			offset += lenRead;
		}
	}
	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

}
