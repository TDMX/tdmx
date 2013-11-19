package org.tdmx.client.crypto.buffer;

import org.tdmx.client.crypto.stream.FileBackedOutputStream;

public interface TemporaryBufferFactory {

	public FileBackedOutputStream getOutputStream();
	
	//TODO getEncryptedOutputStream() - AES protected with one-time-key
	
}
