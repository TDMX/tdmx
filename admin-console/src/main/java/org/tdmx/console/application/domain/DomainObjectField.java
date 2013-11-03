package org.tdmx.console.application.domain;

public final class DomainObjectField {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	public enum FieldType {
		String, Number, Enum, Date, Protected
	}
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------

	private String name;
	private FieldType type;
	private String clazz;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	public DomainObjectField( String name, FieldType type, String clazz ) {
		this.name = name;
		this.type = type;
		this.clazz = clazz;
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
    //-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

	public String getName() {
		return name;
	}

	public FieldType getType() {
		return type;
	}

	public String getClazz() {
		return clazz;
	}

}
