/**
 * 
 */
package org.tdmx.client.crypto.stream;

import java.io.IOException;
import java.io.OutputStream;

/**
 * SizeConstrainedOutputStream can be used to write to but never consume more than
 * capacity bytes of space. If more bytes are written than capacity, then there will
 * be an IOException thrown on write.
 * 
 * @author Peter
 *
 */
public class SizeConstrainedOutputStream extends OutputStream {

	private OutputStream output;
	
	private long capacity;
	private long size = 0;
	
	public SizeConstrainedOutputStream( long capacity, OutputStream os ) {
		this.capacity = capacity;
		this.output = os; 
	}

	@Override
	public void write(int b) throws IOException {
		if ( size < capacity ) {
			output.write(b);
			size++;
		} else {
			throw new IOException("Capacity exceeded.");
		}
	}

	@Override
    public void write(byte b[]) throws IOException {
		write( b, 0, b.length);
    }
	
	@Override
    public void write(byte b[], int off, int len) throws IOException {
		long capacityLeft = capacity - size;
		if ( len <= capacityLeft ) {
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
