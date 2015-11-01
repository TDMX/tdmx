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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(FileUtils.class);

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	private FileUtils() {
	};

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	/**
	 * Return the files in the directory matching the regular expression.
	 * 
	 * @param directory
	 * @param pattern
	 * @return
	 */
	public static List<File> getFilesMatchingPattern(String directory, String pattern) {
		File dir = new File(directory);
		final Pattern p = Pattern.compile(pattern);

		File[] files = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				Matcher m = p.matcher(name);
				return m.matches();
			}
		});

		return Arrays.asList(files);
	}

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
	 * deletes the file at filePath.
	 * 
	 * @param filePath
	 * @throws IOException
	 */
	public static void deleteFile(String filePath) throws IOException {
		log.debug("deleteFile " + filePath);
		Path path = Paths.get(filePath);
		if (Files.exists(path)) {
			Files.delete(path);
		}
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

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

}
