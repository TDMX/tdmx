/**
 * 
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

/**
 * A FileBackedOutputStream writes through to a File after maxSizeMemory bytes have
 * been written to the underlying output stream.
 * 
 * The number of bytes written to the underlying stream is returned with {@link #getSize()}
 * 
 * The File backing the OutputStream can be retrieved after closing with {@link #getStorage()} 
 * if there is one, else the {@link #getMemory()} will return the underlying bytes from memory.
 * 
 * The caller should take care to call {@link #close()}.
 * 
 * The caller should always call {@link #discard()}, especially if the {@link #getInputStream()}
 * has not been called. {@link #getInputStream()} converts the backing File into an InputStream
 * and removes it from this OutputStream.
 * 
 * @author Peter
 *
 */
public class FileBackedOutputStream extends OutputStream {

	private OutputStream output;
	private long size = 0;
	private int maxSizeMemory;
	private boolean closed = false;
	private ByteArrayOutputStream baos;
	private File tmpFile;
	private File tmpDirectory;
	
	public FileBackedOutputStream( int initialSize, int maxSizeMemory, File directory ) {
		this.baos = new ByteArrayOutputStream( Math.min(initialSize, maxSizeMemory));
		this.output = baos;
		this.maxSizeMemory = maxSizeMemory;
		this.tmpDirectory = directory;
	}

	public InputStream getInputStream() {
		if ( !isClosed() ) {
			throw new IllegalStateException("getInputStream called when output stream open.");
		}
		if ( baos != null ) {
			InputStream is = new ByteArrayInputStream(baos.toByteArray());
			baos = null;
			return is;
		} else if ( tmpFile != null ) {
			InputStream fis;
			try {
				fis = new FileBackedInputStream(tmpFile);
			} catch (FileNotFoundException e) {
				throw new IllegalStateException("Converting existing File into InputStream failed unexpectedly.",e);
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
		if ( tmpFile == null && size > maxSizeMemory ) {
			replaceStorage();
		}
        output.write(b, 0, b.length);
    }
	
	@Override
    public void write(byte b[], int off, int len) throws IOException {
    	size += len;
		if ( tmpFile == null && size > maxSizeMemory ) {
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
		if ( !isClosed() ) {
			try {
				close();
			} catch ( IOException e) {
				// TODO warn but ignore
			}
		}
		baos = null;
		if ( tmpFile != null ) {
			if ( !tmpFile.delete() ){
				// TODO warn but ignore
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

	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		discard();
		super.finalize();
	}
}
