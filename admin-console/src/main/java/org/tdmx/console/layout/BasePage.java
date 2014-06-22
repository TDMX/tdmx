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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.filter.FilteredHeaderItem;
import org.apache.wicket.markup.head.filter.HeaderResponseContainer;
import org.apache.wicket.markup.html.GenericWebPage;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.tdmx.console.base.CustomSession;
import org.tdmx.console.pages.profile.ProfilePage;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.dropdown.DropDownButton;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.dropdown.MenuBookmarkablePageLink;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.dropdown.MenuHeader;
import de.agilecoders.wicket.core.markup.html.bootstrap.html.ChromeFrameMetaTag;
import de.agilecoders.wicket.core.markup.html.bootstrap.html.HtmlTag;
import de.agilecoders.wicket.core.markup.html.bootstrap.html.MetaTag;
import de.agilecoders.wicket.core.markup.html.bootstrap.html.OptimizedMobileViewportMetaTag;
import de.agilecoders.wicket.core.markup.html.bootstrap.image.IconType;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.ImmutableNavbarComponent;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.Navbar;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.NavbarAjaxLink;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.NavbarButton;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.NavbarComponents;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.NavbarDropDownButton;
import de.agilecoders.wicket.core.markup.html.references.BootstrapJavaScriptReference;
import de.agilecoders.wicket.core.settings.IBootstrapSettings;
import de.agilecoders.wicket.core.settings.ITheme;

/**
 * Base class for application pages
 * 
 */
public abstract class BasePage extends GenericWebPage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CustomSession getCustomSession() {
		return (CustomSession) getSession();
	}

	public BasePage() {
		this(null);
	}

	public BasePage(IModel model) {
		super(model);
		createComponents();
	}

	private void createComponents() {
		add(new HtmlTag("html"));
		add(new OptimizedMobileViewportMetaTag("viewport"));
		add(new ChromeFrameMetaTag("chrome-frame"));
		add(new MetaTag("description", Model.of("description"), Model.of("TBD description")));
		add(new MetaTag("author", Model.of("author"), Model.of("TBD authors")));

		add(createNavigationBar("navigationBar"));

		add(new HeaderResponseContainer("footer-container", "footer-container"));

		final String packageName = getClass().getPackage().getName();
		// add(new TopHeader("topNavigation", Strings.afterLast(packageName, '.'), this));
		add(new LeftNavigation("leftNavigation"));
		// add(new Footer("footer"));
	}

	protected Navbar createNavigationBar(String markupId) {
		Navbar navbar = new Navbar(markupId);

		navbar.setPosition(Navbar.Position.TOP);

		// show brand name
		navbar.brandName(Model.of("TDMX Console"));

		navbar.addComponents(NavbarComponents.transform(Navbar.ComponentPosition.LEFT));

		DropDownButton themesDropdown = new NavbarDropDownButton(Model.of("Themes")) {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isActive(Component item) {
				return false;
			}

			@Override
			protected List<AbstractLink> newSubMenuButtons(final String buttonMarkupId) {
				final List<AbstractLink> subMenu = new ArrayList<AbstractLink>();
				subMenu.add(new MenuHeader(Model.of("all available themes:")));
				// subMenu.add(new MenuDivider());

				final IBootstrapSettings settings = Bootstrap.getSettings(getApplication());
				final List<ITheme> themes = settings.getThemeProvider().available();

				for (final ITheme theme : themes) {
					PageParameters params = new PageParameters();
					params.set("theme", theme.name());
					subMenu.add(new MenuBookmarkablePageLink<Page>(getPageClass(), params, Model.of(theme.name())));
				}

				return subMenu;
			}
		}.setIconType(IconType.book);
		// dropdown.add(new DropDownAutoOpen());

		NavbarButton userNameButon = new NavbarButton(ProfilePage.class, Model.of(getCustomSession().getUser()
				.getFirstName() + " " + getCustomSession().getUser().getLastName())).setIconType(IconType.user);

		// NavbarButton inspectorButon = new NavbarButton(InspectorPage.class,
		// Model.of(" ")).setIconType(IconType.eyeopen);

		NavbarAjaxLink loggoutButton = new NavbarAjaxLink(Model.of("Loggout")) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				((CustomSession) getSession()).logOut();
				setResponsePage(getApplication().getHomePage());

			};
		}.setIconType(IconType.off);

		navbar.addComponents(
				// new ImmutableNavbarComponent(themesDropdown, Navbar.ComponentPosition.LEFT),
				// new ImmutableNavbarComponent(inspectorButon, Navbar.ComponentPosition.LEFT),
				new ImmutableNavbarComponent(userNameButon, Navbar.ComponentPosition.RIGHT),
				new ImmutableNavbarComponent(loggoutButton, Navbar.ComponentPosition.RIGHT));

		return navbar;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		response.render(CssHeaderItem.forReference(FixBootstrapStylesCssResourceReference.INSTANCE));
		response.render(JavaScriptHeaderItem.forReference(BootstrapJavaScriptReference.instance()));
		response.render(new FilteredHeaderItem(JavaScriptHeaderItem.forReference(ApplicationJavaScript.INSTANCE),
				"footer-container"));

		// if ("google".equalsIgnoreCase(getActiveTheme().name())) {
		// / response.render(CssHeaderItem.forReference(DocsCssResourceReference.GOOGLE));
		// }
	}

	protected ITheme getActiveTheme() {
		IBootstrapSettings settings = Bootstrap.getSettings(getApplication());
		return settings.getActiveThemeProvider().getActiveTheme();
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		configureTheme(getPageParameters());
	}

	/**
	 * sets the theme for the current user.
	 * 
	 * @param pageParameters
	 *            current page parameters
	 */
	private void configureTheme(PageParameters pageParameters) {
		StringValue theme = pageParameters.get("theme");

		if (!theme.isEmpty()) {
			IBootstrapSettings settings = Bootstrap.getSettings(getApplication());
			settings.getActiveThemeProvider().setActiveTheme(theme.toString(""));
		}
	}

}
