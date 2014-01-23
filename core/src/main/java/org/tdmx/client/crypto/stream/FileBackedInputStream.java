package org.tdmx.client.crypto.stream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * 
 * The temporary File backing the input stream is deleted on {@link #close()} or at latest
 * on {@link #finalize()}
 * 
 * 
 * @author Peter
 *
 */
public class FileBackedInputStream extends FileInputStream {

	private File tmpFile;
	
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
		} catch ( IOException e ) {
			delegate = e;
		}
		if ( tmpFile != null ) {
			if ( !tmpFile.delete() ){
				// TODO warn but ignore
			}
		}
		if ( delegate != null ) {
			throw delegate;
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws IOException {
		close();
		super.finalize();
	}
}
