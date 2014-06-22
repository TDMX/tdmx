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
package org.tdmx.console.pages.login;

import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.markup.head.filter.HeaderResponseContainer;
import org.apache.wicket.markup.html.GenericWebPage;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.console.AdminApplication;
import org.tdmx.console.base.CustomSession;
import org.tdmx.console.domain.User;
import org.tdmx.console.service.UIService;

import de.agilecoders.wicket.core.markup.html.bootstrap.behavior.BootstrapBaseBehavior;
import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import de.agilecoders.wicket.core.markup.html.bootstrap.form.ControlGroup;
import de.agilecoders.wicket.core.markup.html.bootstrap.html.ChromeFrameMetaTag;
import de.agilecoders.wicket.core.markup.html.bootstrap.html.HtmlTag;
import de.agilecoders.wicket.core.markup.html.bootstrap.html.MetaTag;
import de.agilecoders.wicket.core.markup.html.bootstrap.html.OptimizedMobileViewportMetaTag;

/**
 * Login page
 */
public final class LoginPage extends GenericWebPage<LoginPage> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger log = LoggerFactory.getLogger(LoginPage.class);

	private transient UIService userService = AdminApplication.getUIService();

	public CustomSession getCustomSession() {
		return (CustomSession) getSession();
	}

	public LoginPage() {
		CustomSession session = getCustomSession();
		if (session.isLoggedIn()) {
			throw new RestartResponseAtInterceptPageException(getApplication().getHomePage());
		}
		createComponents();
	}

	private void createComponents() {
		add(new BootstrapBaseBehavior());
		add(new HtmlTag("html"));
		add(new OptimizedMobileViewportMetaTag("viewport"));
		add(new ChromeFrameMetaTag("chrome-frame"));
		add(new MetaTag("description", Model.of("description"), Model.of("TBD description")));
		add(new MetaTag("author", Model.of("author"), Model.of("TBD authors")));

		add(new HeaderResponseContainer("footer-container", "footer-container"));

		add(new NotificationPanel("loginFeedback"));

		LoginForm loginForm = new LoginForm("loginForm");
		add(loginForm);
	}

	private class LoginForm extends StatelessForm<LoginForm> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final LoginModel loginModel = new LoginModel();

		public LoginForm(String id) {
			super(id);

			ControlGroup loginNameLabel = new ControlGroup("userNameLabel", Model.of("Username"));
			TextField loginName = new TextField("userName", new PropertyModel(loginModel, "userName"));
			loginName.setRequired(true);
			loginNameLabel.add(loginName);
			add(loginNameLabel);

			ControlGroup passwordLabel = new ControlGroup("passwordLabel", Model.of("Password"));
			PasswordTextField password = new PasswordTextField("password", new PropertyModel(loginModel, "password"));
			passwordLabel.add(password);
			add(passwordLabel);

			ControlGroup loginButtonGroup = new ControlGroup("loginButtonGroup");
			add(loginButtonGroup);
		}

		@Override
		protected void onSubmit() {
			CustomSession session = getCustomSession();
			if (session.isLoggedIn()) {
				setResponsePage(getApplication().getHomePage());
			} else {
				User user = null; // TODO: load user

				if (user == null) {
					user = userService.authenticate(loginModel.getUserName(), loginModel.getPassword());
				}

				if (user != null) {
					session.setUser(user);
					continueToOriginalDestination();
					setResponsePage(getApplication().getHomePage());
				} else {
					String loginError = getLocalizer().getString("loginError", this);
					error(loginError);
				}
			}
		}

	}

}
