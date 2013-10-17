package org.tdmx.console.application.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {

	public static byte[] getFileContents( String filePath ) throws IOException {
		Path path = Paths.get(filePath);
		byte[] data = Files.readAllBytes(path);	
		return data;
	}
	
	public static void storeFileContents( String filePath, byte[] bytes ) {
		//TODO
	}
}
