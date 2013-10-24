package org.tdmx.console.domain;

import java.io.Serializable;

public class Domain implements Serializable {
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------

	private String title;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	public Domain(String title) {
		this.title = title;
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

	public String getTitle() {
		return title;
	}

	public void setTitle(String name) {
		this.title = name;
	}
}
