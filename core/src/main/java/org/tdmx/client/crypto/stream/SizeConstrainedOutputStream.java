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

import java.io.IOException;
import java.io.OutputStream;

/**
 * SizeConstrainedOutputStream can be used to write to but never consume more than capacity bytes of space. If more
 * bytes are written than capacity, then there will be an IOException thrown on write.
 * 
 * @author Peter
 * 
 */
public class SizeConstrainedOutputStream extends OutputStream {

	private final OutputStream output;

	private final long capacity;
	private long size = 0;

	public SizeConstrainedOutputStream(long capacity, OutputStream os) {
		this.capacity = capacity;
		this.output = os;
	}

	@Override
	public void write(int b) throws IOException {
		if (size < capacity) {
			output.write(b);
			size++;
		} else {
			throw new IOException("Capacity exceeded.");
		}
	}

	@Override
	public void write(byte b[]) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	public void write(byte b[], int off, int len) throws IOException {
		long capacityLeft = capacity - size;
		if (len <= capacityLeft) {
			// fits directly
			output.write(b, off, len);
		} else { // capacity exceeded - fail even not writing what would fit.
			throw new IOException("Capacity exceeded.");
		}
		size += len;
	}

	@Override
	public void flush() throws IOException {
		output.flush();
	}

	@Override
	public void close() throws IOException {
		output.close();
	}

}
