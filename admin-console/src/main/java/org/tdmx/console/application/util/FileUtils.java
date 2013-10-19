package org.tdmx.console.application.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

public class FileUtils {

	/**
	 * read the contents of the file at filePath.
	 * 
	 * @param filePath
	 * @return null if the file does not exist, else the contents.
	 * @throws IOException
	 */
	public static byte[] getFileContents( String filePath ) throws IOException {
		Path path = Paths.get(filePath);
		if ( !Files.exists(path) ) {
			return null;
		}
		byte[] data = Files.readAllBytes(path);	
		return data;
	}
	
	/**
	 * write the contents to a temporary file and then replace the existing file.
	 * 
	 * @param filePath
	 * @param bytes
	 * @param tempSuffix
	 * @throws IOException
	 */
	public static void storeFileContents( String filePath, byte[] bytes, String tempSuffix ) throws IOException {
		Path path = Paths.get(filePath);
		Path tmpPath = Paths.get(filePath+tempSuffix);
		
		Files.write(tmpPath, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		Files.move(tmpPath, path, StandardCopyOption.REPLACE_EXISTING);
	}
}
