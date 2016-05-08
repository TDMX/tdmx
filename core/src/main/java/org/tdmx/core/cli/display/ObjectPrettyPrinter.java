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
package org.tdmx.core.cli.display;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.cli.display.annotation.CliAttribute;
import org.tdmx.core.cli.display.annotation.CliRepresentation;
import org.tdmx.core.system.lang.StringUtils;

/**
 * Pretty-prints an instance of an object to the output.
 * 
 * @author Peter
 *
 */
public class ObjectPrettyPrinter implements CliPrinter {

	// -------------------------------------------------------------------------
	// PUBLIC CONSTANTS
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	// PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	// -------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(ObjectPrettyPrinter.class);

	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public ObjectPrettyPrinter() {
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void output(PrintStream out, Object object) {
		// TODO #105: verbose as thread local setting
		outputObject(out, object, 0, true);
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	protected void outputObject(PrintStream out, Object object, int indentation, boolean verbose) {
		// TODO #105
		CliRepresentation rep = getRepresentation(object);
		if (rep != null) {
			// specialized printing for annotated classes
			out.println(getName(rep, object) + " {");

			List<StringBuffer> lines = lineRepresentation(rep, object, indentation, verbose);
			for (StringBuffer l : lines) {
				out.println(l.toString());
			}

			out.print(getIndentation(indentation));
			out.println("}");
		} else {
			out.println(object);
		}
	}

	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private List<StringBuffer> lineRepresentation(CliRepresentation rep, Object object, int indentation,
			boolean verbose) {
		List<StringBuffer> lines = new ArrayList<>();

		int childIndentation = indentation + 4;
		for (CliAttributeHolder h : getAttributes(object, verbose)) {
			CliRepresentation attrRep = getRepresentation(h.instanceValue);
			if (attrRep != null) {
				StringBuffer attributeHeaderLine = getIndentation(childIndentation);
				attributeHeaderLine.append(h.attrName);
				for (int i = 0; i < h.padding; i++) {
					attributeHeaderLine.append(" ");
				}
				attributeHeaderLine.append(" : ").append(getName(attrRep, h.instanceValue)).append(" {");
				lines.add(attributeHeaderLine);

				List<StringBuffer> childLines = lineRepresentation(attrRep, h.instanceValue, childIndentation, verbose);
				for (StringBuffer childLine : childLines) {
					lines.add(childLine);
				}

				StringBuffer attributeFooterLine = getIndentation(childIndentation);
				attributeFooterLine.append("}");
				lines.add(attributeFooterLine);

			} else {
				StringBuffer attributeLine = getIndentation(childIndentation);
				attributeLine.append(h.attrName);
				for (int i = 0; i < h.padding; i++) {
					attributeLine.append(" ");
				}
				attributeLine.append(" : ").append(h.instanceValue.toString());
				lines.add(attributeLine);
			}
		}
		return lines;
	}

	private StringBuffer getIndentation(int indentation) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < indentation; i++) {
			buf.append(" ");
		}
		return buf;
	}

	private String getName(CliRepresentation rep, Object object) {
		if (!StringUtils.hasText(rep.name())) {
			return object.getClass().getSimpleName();
		} else {
			return rep.name();
		}
	}

	private static class CliAttributeHolder {
		String attrName;
		CliAttribute annotation;
		Object instanceValue;
		int padding = 0;
	}

	private CliRepresentation getRepresentation(Object object) {

		Class<?> clazz = object.getClass();
		CliRepresentation[] cliRepresentations = clazz.getAnnotationsByType(CliRepresentation.class);
		if (cliRepresentations == null) {
			return null;
		}
		if (cliRepresentations.length != 1) {
			return null;
		}
		return cliRepresentations[0];
	}

	private CliAttributeHolder[] getAttributes(Object object, boolean verbose) {
		List<CliAttributeHolder> gatheredAttributes = new ArrayList<>();

		int longestName = 0;

		Class<?> clazz = object.getClass();
		Field[] fields = clazz.getDeclaredFields();
		for (Field f : fields) {
			CliAttribute[] cliAttributes = f.getAnnotationsByType(CliAttribute.class);
			if (cliAttributes.length == 0) {
				continue;
			}
			if (cliAttributes.length > 1) {
				throw new IllegalStateException(
						"Too many CliAttribute annotations on " + clazz.getName() + "#" + f.getName());
			}

			CliAttributeHolder h = new CliAttributeHolder();
			h.annotation = cliAttributes[0];
			if (StringUtils.hasText(h.annotation.name())) {
				h.attrName = h.annotation.name();
			} else {
				h.attrName = f.getName();
			}
			// get instance value via reflection
			try {
				f.setAccessible(true);
				h.instanceValue = f.get(object);
			} catch (IllegalArgumentException e) {
				throw new IllegalStateException(e);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
			// we only want to print something if it's there.
			if (h.instanceValue != null) {
				// we print everything when verbose, otherwise only if the annotation is not verbose
				if (verbose || !h.annotation.verbose()) {
					// we keep track of longest name so that we can adjust the padding
					if (h.attrName.length() > longestName) {
						longestName = h.attrName.length();
					}
					gatheredAttributes.add(h);
				}
			}
		}
		// adjust the padding to the longest displayed attribute
		for (CliAttributeHolder h : gatheredAttributes) {
			h.padding = longestName - h.attrName.length();
		}

		// sort according to the CliAttribute order annotation.
		Collections.sort(gatheredAttributes, new Comparator<CliAttributeHolder>() {

			@Override
			public int compare(CliAttributeHolder o1, CliAttributeHolder o2) {
				return o1.annotation.order() - o2.annotation.order();
			}
		});
		return gatheredAttributes.toArray(new CliAttributeHolder[0]);
	}

	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

}
