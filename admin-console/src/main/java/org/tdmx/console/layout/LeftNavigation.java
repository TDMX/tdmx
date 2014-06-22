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

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.tdmx.console.pages.domain.DomainPage;
import org.tdmx.console.pages.profile.ProfilePage;

public class LeftNavigation extends Panel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LeftNavigation(String id) {
		super(id);

		add(new BookmarkablePageLink("profilePageLink", ProfilePage.class));
		add(new BookmarkablePageLink("domainPageLink", DomainPage.class));
	}

	@Override
	public boolean isVisible() {
		return false;
	}

}
