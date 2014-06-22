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
package org.tdmx.client.crypto.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils {

	private static Logger log = LoggerFactory.getLogger(FileUtils.class);

	/**
	 * read the contents of the file at filePath.
	 * 
	 * @param filePath
	 * @return null if the file does not exist, else the contents.
	 * @throws IOException
	 */
	public static byte[] getFileContents(String filePath) throws IOException {
		log.debug("getFileContents " + filePath);
		Path path = Paths.get(filePath);
		if (!Files.exists(path)) {
			log.debug("getFileContents " + filePath + " doesn't exist.");
			return null;
		}
		byte[] data = Files.readAllBytes(path);
		log.debug("getFileContents " + filePath + " read " + data.length + " bytes.");
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
	public static void storeFileContents(String filePath, byte[] bytes, String tempSuffix) throws IOException {
		log.debug("storeFileContents " + filePath + " writing " + bytes.length + " bytes using " + tempSuffix
				+ " suffix.");
		Path path = Paths.get(filePath);
		Path tmpPath = Paths.get(filePath + tempSuffix);

		Files.write(tmpPath, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		Files.move(tmpPath, path, StandardCopyOption.REPLACE_EXISTING);
	}
}
