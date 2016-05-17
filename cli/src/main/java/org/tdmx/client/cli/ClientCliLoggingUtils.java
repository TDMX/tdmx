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
package org.tdmx.client.cli;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.tdmx.client.cli.ClientCliUtils.DestinationDescriptor;
import org.tdmx.client.cli.ClientCliUtils.UnencryptedSessionKey;
import org.tdmx.client.cli.ClientCliUtils.ZoneDescriptor;
import org.tdmx.core.cli.CliPrinterFactory;
import org.tdmx.core.cli.display.CliPrinter;
import org.tdmx.core.cli.display.CorePrintableObjectMapperImpl;
import org.tdmx.core.cli.display.ObjectPrettyPrinter;
import org.tdmx.core.cli.display.PrintableObject;
import org.tdmx.core.cli.display.PrintableObjectMapper;

/**
 * Utilities for logging for Client CLI commands.
 * 
 * @author Peter
 *
 */
public class ClientCliLoggingUtils {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final String LINEFEED = System.getProperty("line.separator", "\n");
	// the verbose state of the CLI output is held statically
	private static boolean verbose = false;
	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	private ClientCliLoggingUtils() {
	}

	public static CliPrinterFactory getPrinterFactory() {
		return new CliPrinterFactory() {

			@Override
			public boolean isVerbose() {
				return ClientCliLoggingUtils.isVerbose();
			}

			@Override
			public void setVerbose(boolean verbose) {
				ClientCliLoggingUtils.setVerbose(verbose);
			}

			@Override
			public CliPrinter getPrinter(PrintStream ps) {
				return new ObjectPrettyPrinter(ps, ClientCliLoggingUtils.isVerbose(), new PrintableObjectMapper() {

					final PrintableObjectMapper coreMapper = new CorePrintableObjectMapperImpl();

					@Override
					public Object map(Object object, boolean verbose) {
						Object coreMap = coreMapper.map(object, isVerbose());
						if (coreMap != null) {
							return coreMap;
						}
						return null;
					}
				});
			}
		};
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------
	public static void logException(CliPrinter out, String prefix, Exception error) {
		out.println(prefix, ". Exception message [" + error.getMessage() + "]");
		StringWriter sw = new StringWriter();
		error.printStackTrace(new PrintWriter(sw));
		out.println(sw.toString());
	}

	public static PrintableObject toLog(org.tdmx.core.api.v01.common.Error error) {
		return new PrintableObject("Error").add("code", error.getCode()).add("description", error.getDescription());
	}

	public static void logError(CliPrinter out, org.tdmx.core.api.v01.common.Error error) {
		out.println(toLog(error));
	}

	public static void log(CliPrinter out, Object... objects) {
		out.println(objects);
	}

	public static String toString(UnencryptedSessionKey usk) {
		StringBuilder sb = new StringBuilder();
		sb.append("encryptionContextId=").append(usk.getEncryptionContextId());
		sb.append(" scheme=").append(usk.getScheme().getName());
		sb.append(" validFrom=").append(usk.getValidFrom());
		return sb.toString();
	}

	public static String toString(DestinationDescriptor dd) {
		StringBuilder sb = new StringBuilder();
		sb.append("dataDir=").append(dd.getDataDirectory()).append(LINEFEED);
		sb.append("encryptionScheme=").append(dd.getEncryptionScheme().getName()).append(LINEFEED);
		sb.append("salt=").append(dd.getSalt()).append(LINEFEED);
		sb.append("sessionDurationInHours=").append(dd.getSessionDurationInHours()).append(LINEFEED);
		sb.append("sessionRetentionInDays=").append(dd.getSessionRetentionInDays()).append(LINEFEED);
		for (UnencryptedSessionKey sk : dd.getSessionKeys()) {
			sb.append("sessionKey ").append(toString(sk)).append(LINEFEED);
		}
		if (!dd.getEncryptedSessionKeys().isEmpty()) {
			sb.append("encrypted keys ").append(dd.getEncryptedSessionKeys().size());
		}
		return sb.toString();
	}

	public static String toString(ZoneDescriptor zd) {
		StringBuilder sb = new StringBuilder();
		sb.append("zone=").append(zd.getZoneApex()).append(LINEFEED);
		sb.append("scsUrl=").append(zd.getScsUrl()).append(LINEFEED);
		sb.append("version=").append(zd.getVersion()).append(LINEFEED);
		return sb.toString();
	}

	public static String truncatedMessage() {
		return "More results may exist. Use the pageNumber and pageSize parameters to get the next page of results.";
	}

	public static String commandExecuted() {
		return "Execution completed.";
	}

	public static boolean isVerbose() {
		return verbose;
	}

	public static void setVerbose(boolean verbose) {
		ClientCliLoggingUtils.verbose = verbose;
	}

}
