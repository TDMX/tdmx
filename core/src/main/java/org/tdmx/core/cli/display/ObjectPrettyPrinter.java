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
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.cli.CliPrinterFactory;
import org.tdmx.core.cli.display.annotation.CliAttribute;
import org.tdmx.core.cli.display.annotation.CliRepresentation;
import org.tdmx.core.system.lang.StringUtils;

/**
 * Pretty-prints an instance of an object to the output.
 * 
 * The PrintStream used and verbosity are set by the creating {@link CliPrinterFactory}
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

	private final boolean verbose;
	private final PrintStream out;
	private final PrintableObjectMapper mapper;
	// -------------------------------------------------------------------------
	// CONSTRUCTORS
	// -------------------------------------------------------------------------

	public ObjectPrettyPrinter(PrintStream out, boolean verbose, PrintableObjectMapper mapper) {
		this.verbose = verbose;
		this.out = out;
		this.mapper = mapper;
	}

	// -------------------------------------------------------------------------
	// PUBLIC METHODS
	// -------------------------------------------------------------------------

	@Override
	public void print(Object... object) {
		for (Object o : object) {
			outputObject(o, 0);
		}
	}

	@Override
	public void println(Object... object) {
		for (Object o : object) {
			print(o);
		}
		out.println();
	}

	// -------------------------------------------------------------------------
	// PROTECTED METHODS
	// -------------------------------------------------------------------------

	protected void outputObject(Object object, int indentation) {

		Object rep = getRepresentation(object);
		if (rep instanceof PrintableObject) {
			outputRepresentation(object, (PrintableObject) rep, indentation);
		} else {
			out.print(rep);
		}
	}

	protected void outputRepresentation(Object original, PrintableObject rep, int indentation) {
		// specialized printing for annotated classes
		out.println(rep.getName() + " {");

		List<StringBuffer> lines = lineRepresentation(rep, original, indentation);
		for (StringBuffer l : lines) {
			out.println(l.toString());
		}

		out.print(getIndentation(indentation));
		out.print("}");

	}
	// -------------------------------------------------------------------------
	// PRIVATE METHODS
	// -------------------------------------------------------------------------

	private List<StringBuffer> lineRepresentation(PrintableObject rep, Object object, int indentation) {
		List<StringBuffer> lines = new ArrayList<>();

		int childIndentation = indentation + 4;
		for (PrintableAttributeValue h : getAttributes(rep, object, verbose)) {
			Object attrRep = getRepresentation(h.getInstanceValue());
			if (attrRep instanceof PrintableObject) {
				PrintableObject atrRep = (PrintableObject) attrRep;
				StringBuffer attributeHeaderLine = getIndentation(childIndentation);
				attributeHeaderLine.append(h.getAttributeName());
				for (int i = 0; i < h.getPadding(); i++) {
					attributeHeaderLine.append(" ");
				}
				attributeHeaderLine.append(" : ").append(atrRep.getName()).append(" {");
				lines.add(attributeHeaderLine);

				List<StringBuffer> childLines = lineRepresentation(atrRep, h.getInstanceValue(), childIndentation);
				for (StringBuffer childLine : childLines) {
					lines.add(childLine);
				}

				StringBuffer attributeFooterLine = getIndentation(childIndentation);
				attributeFooterLine.append("}");
				lines.add(attributeFooterLine);

			} else {
				StringBuffer attributeLine = getIndentation(childIndentation);
				attributeLine.append(h.getAttributeName());
				for (int i = 0; i < h.getPadding(); i++) {
					attributeLine.append(" ");
				}
				attributeLine.append(" : ").append(attrRep.toString());
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

	/**
	 * Return the object or a mapped representation of the object for printing.
	 * 
	 * @param object
	 * @return
	 */
	private Object getRepresentation(Object object) {
		if (object == null) {
			return null;
		}
		if (mapper != null) {
			Object mappedObject = mapper.map(object, verbose);
			if (mappedObject != null) {
				// print the mapped object instead
				object = mappedObject;
			}
		}

		if (object instanceof PrintableObject) {
			return object;
		}
		// if might be a CliRepresentation, in which case we transform to a PrintableObject
		Class<?> clazz = object.getClass();
		CliRepresentation[] cliRepresentations = clazz.getAnnotationsByType(CliRepresentation.class);
		if (cliRepresentations != null && cliRepresentations.length == 1) {
			if (!StringUtils.hasText(cliRepresentations[0].name())) {
				return new PrintableObject(object.getClass().getSimpleName());
			}
			return new PrintableObject(cliRepresentations[0].name());
		}

		return object;
	}

	private PrintableAttributeValue[] getAttributes(PrintableObject po, Object object, boolean verbose) {

		List<PrintableAttributeValue> gatheredAttributes = null;

		gatheredAttributes = getAttributesViaReflection(object, verbose);
		// add all attributes which the PO provide
		for (PrintableAttributeValue pav : po.getAttributeValues()) {
			if (pav.getInstanceValue() != null) {
				gatheredAttributes.add(pav);
			}
		}

		// strip verbose attributes if not verbose output
		Iterator<PrintableAttributeValue> it = gatheredAttributes.iterator();
		while (it.hasNext()) {
			PrintableAttributeValue av = it.next();
			if (!verbose && av.isVerbose()) {
				it.remove();
			}
		}

		// sort according to the CliAttribute order annotation.
		Collections.sort(gatheredAttributes, new Comparator<PrintableAttributeValue>() {

			@Override
			public int compare(PrintableAttributeValue o1, PrintableAttributeValue o2) {
				return o1.getOrder() - o2.getOrder();
			}
		});

		// adjust the padding according to the longest printed attributename
		adjustPadding(gatheredAttributes);

		return gatheredAttributes.toArray(new PrintableAttributeValue[0]);
	}

	private void adjustPadding(List<PrintableAttributeValue> attributes) {
		int longestName = 0;
		for (PrintableAttributeValue h : attributes) {
			if (h.getAttributeName().length() > longestName) {
				longestName = h.getAttributeName().length();
			}
		}
		// adjust the padding to the longest displayed attribute
		for (PrintableAttributeValue h : attributes) {
			h.setPadding(longestName - h.getAttributeName().length());
		}

	}

	private List<PrintableAttributeValue> getAttributesViaReflection(Object object, boolean verbose) {
		List<PrintableAttributeValue> gatheredAttributes = new ArrayList<>();

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
			CliAttribute fieldAttribute = cliAttributes[0];
			String attrName = StringUtils.hasText(fieldAttribute.name()) ? fieldAttribute.name() : f.getName();
			Object attrValue = null;
			// get instance value via reflection
			try {
				f.setAccessible(true);
				attrValue = f.get(object);
			} catch (IllegalArgumentException e) {
				throw new IllegalStateException(e);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
			// we only want to print something if it's there.
			if (attrValue != null) {
				PrintableAttributeValue h = new PrintableAttributeValue(attrName, attrValue);
				// we print everything when verbose, otherwise only if the annotation is not verbose
				if (verbose || !fieldAttribute.verbose()) {
					// we keep track of longest name so that we can adjust the padding
					gatheredAttributes.add(h);
				}

			}
		}
		return gatheredAttributes;
	}
	// -------------------------------------------------------------------------
	// PUBLIC ACCESSORS (GETTERS / SETTERS)
	// -------------------------------------------------------------------------

	public boolean isVerbose() {
		return verbose;
	}

	public PrintStream getOut() {
		return out;
	}

}
