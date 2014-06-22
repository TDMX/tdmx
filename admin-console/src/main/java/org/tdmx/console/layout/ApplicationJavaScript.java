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
package org.tdmx.console.layout;

import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.google.common.collect.Lists;

import de.agilecoders.wicket.core.markup.html.references.BootstrapJavaScriptReference;

public class ApplicationJavaScript extends JavaScriptResourceReference {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final ApplicationJavaScript INSTANCE = new ApplicationJavaScript();

	private ApplicationJavaScript() {
		super(ApplicationJavaScript.class, "application.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		final List<HeaderItem> dependencies = Lists.newArrayList(super.getDependencies());
		dependencies.add(JavaScriptHeaderItem.forReference(Application.get().getJavaScriptLibrarySettings()
				.getJQueryReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(BootstrapJavaScriptReference.instance()));

		return dependencies;
	}
}