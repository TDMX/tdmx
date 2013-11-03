package org.tdmx.console.application.search;


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

	public static enum DomainObjectType {
		
		HttpProxy("Proxy"),
		ServiceProvider("Provider"),
		;
		private String alias;
		private DomainObjectType(String alias) {
			this.alias = alias;
		}
		public String getAlias() {
			return alias;
		}
	}
	
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
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	public FieldDescriptor( DomainObjectType objectType, String name, FieldType fieldType ) {
		this.objectType = objectType;
		this.name = name.intern();
		this.fieldType = fieldType;
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
