package org.tdmx.client.crypto.buffer;

import java.io.File;

import org.tdmx.client.crypto.stream.FileBackedOutputStream;

public class TemporaryFileManagerImpl implements TemporaryBufferFactory {

	public static final int DEFAULT_MAX_SIZE_MEMORY = 1024*1024;
	public static final int DEFAULT_INITIAL_SIZE_MEMORY = 8192;
	
	private int maxSizeMemory;
	private int initialSizeMemory ;
	private File directory;
	
	public TemporaryFileManagerImpl( int maxSizeMemory,  int initialSizeMemory ) {
		this( maxSizeMemory, initialSizeMemory, System.getProperty("java.io.tmpdir"));
	}
	
	public TemporaryFileManagerImpl( String directoryName ) {
		this( DEFAULT_MAX_SIZE_MEMORY, DEFAULT_INITIAL_SIZE_MEMORY, directoryName);
	}

	public TemporaryFileManagerImpl() {
		this( DEFAULT_MAX_SIZE_MEMORY, DEFAULT_INITIAL_SIZE_MEMORY, System.getProperty("java.io.tmpdir"));
	}

	public TemporaryFileManagerImpl( int maxSizeMemory,  int initialSizeMemory, String directoryName ) {
		directory = new File(directoryName);
	}

	/**
	 * @return the maxSizeMemory
	 */
	public int getMaxSizeMemory() {
		return maxSizeMemory;
	}

	/**
	 * @param maxSizeMemory the maxSizeMemory to set
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
	 * @param initialSizeMemory the initialSizeMemory to set
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
