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
package org.tdmx.console.pages.domain;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.tdmx.console.base.IProtectedPage;
import org.tdmx.console.layout.BasePage;
import org.tdmx.console.layout.FixBootstrapStylesCssResourceReference;

public final class DomainPage extends BasePage implements IProtectedPage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DomainPage() {
		createComponents();
	}

	private void createComponents() {
		AjaxLazyLoadPanel domainList = new AjaxLazyLoadPanel("domainList") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Component getLazyLoadComponent(String id) {
				return new DomainListPanel(id);
			}

			@Override
			public Component getLoadingComponent(String markupId) {
				IRequestHandler handler = new ResourceReferenceRequestHandler(
						FixBootstrapStylesCssResourceReference.INDICATOR);
				return new Label(markupId, "<img alt=\"Loading...\" src=\"" + RequestCycle.get().urlFor(handler)
						+ "\"/>").setEscapeModelStrings(false);
			}
		};
		domainList.setOutputMarkupId(true);

		addOrReplace(domainList);
	}

}
