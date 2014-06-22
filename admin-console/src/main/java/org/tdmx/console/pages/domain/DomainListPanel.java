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

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.console.AdminApplication;
import org.tdmx.console.domain.Domain;
import org.tdmx.console.service.UIService;

import de.agilecoders.wicket.core.markup.html.bootstrap.button.BootstrapBookmarkablePageLink;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons.Type;
import de.agilecoders.wicket.core.markup.html.bootstrap.image.IconType;

public class DomainListPanel extends Panel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger log = LoggerFactory.getLogger(DomainListPanel.class);

	private transient UIService searchService = AdminApplication.getUIService();

	public DomainListPanel(String id) {
		super(id);
		setOutputMarkupId(true);
		createComponents();
	}

	private void createComponents() {
		IModel<List<Domain>> domainListModel = new LoadableDetachableModel<List<Domain>>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			protected List<Domain> load() {
				return searchService.listDomains();
			}
		};

		ListView<Domain> listview = new ListView<Domain>("listview", domainListModel) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final ListItem<Domain> item) {
				item.add(new Label("title", item.getModelObject().getTitle()));

				PageParameters params = new PageParameters();
				params.set("domain", item.getModelObject().getTitle());
				BootstrapBookmarkablePageLink<Domain> domainDetailsLink = new BootstrapBookmarkablePageLink<Domain>(
						"domainDetailsLink", DomainDetailsPage.class, params, Type.Default);
				domainDetailsLink.setIconType(IconType.wrench);
				domainDetailsLink.setLabel(Model.of("Modify"));

				item.add(domainDetailsLink);
			}
		};

		add(listview);

	}

}
