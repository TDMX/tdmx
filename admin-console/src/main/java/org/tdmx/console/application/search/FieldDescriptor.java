package org.tdmx.console.application.search;

import org.tdmx.console.application.domain.DomainObjectType;


/**
 * A field descriptor value object.
 * 
 * @author Peter
 *
 */
public class FieldDescriptor {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	public static enum FieldType {
		Text, // Free text with length of up to 2k, multiple strings, " " separated.
		String, // single free string, no spaces
		Token, // single token, limited range of values
		Number,
		Date,
		DateTime,
		Time,
	}
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------

	private DomainObjectType objectType;
	private String name; 
	private FieldType fieldType;

	private String description;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	public FieldDescriptor( DomainObjectType objectType, String name, FieldType fieldType ) {
		this.objectType = objectType;
		this.name = name.intern();
		this.fieldType = fieldType;
		
		StringBuilder sb = new StringBuilder();
		sb.append("FD{");
		sb.append(objectType);
		sb.append(".");
		sb.append(name);
		sb.append("/");
		sb.append(fieldType);
		sb.append("}");
		description = sb.toString();
	}

	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@Override
	public String toString() {
		return description;
	}
    //-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

	public DomainObjectType getObjectType() {
		return objectType;
	}

	public String getName() {
		return name;
	}

	public FieldType getFieldType() {
		return fieldType;
	}

}
