package org.tdmx.console.domain;

import java.io.Serializable;

public class Domain implements Serializable {
	
	private String title;
	
	public Domain(String title) {
		this.title = title;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String name) {
		this.title = name;
	}


}
