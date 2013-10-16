package org.tdmx.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.wicket.Application;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.authorization.strategies.page.SimplePageAuthorizationStrategy;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.response.filter.ServerAndClientTimeFilter;
import org.apache.wicket.settings.IApplicationSettings;
import org.tdmx.console.application.Administration;
import org.tdmx.console.application.AdministrationImpl;
import org.tdmx.console.base.CustomSession;
import org.tdmx.console.base.IProtectedPage;
import org.tdmx.console.base.MountedMapperWithoutPageComponentInfo;
import org.tdmx.console.pages.domain.DomainDetailsPage;
import org.tdmx.console.pages.domain.DomainPage;
import org.tdmx.console.pages.login.LoginPage;
import org.tdmx.console.pages.profile.ProfilePage;
import org.tdmx.console.service.SearchService;
import org.tdmx.console.service.SearchServiceImpl;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.markup.html.RenderJavaScriptToFooterHeaderResponseDecorator;
import de.agilecoders.wicket.core.settings.BootstrapSettings;
import de.agilecoders.wicket.core.settings.ThemeProvider;
import de.agilecoders.wicket.themes.markup.html.bootstrap3.Bootstrap3Theme;
import de.agilecoders.wicket.themes.markup.html.google.GoogleTheme;
import de.agilecoders.wicket.themes.markup.html.metro.MetroTheme;
import de.agilecoders.wicket.themes.markup.html.wicket.WicketTheme;
import de.agilecoders.wicket.themes.settings.BootswatchThemeProvider;


/**
 * TDMX Admin Console
 * 
 * 
 */
public class AdminApplication extends WebApplication {

	public static final String CONFIG_PROPERTY = "org.tdmx.console.config";
	public static final String PASSPHRASE_PROPERTY = "org.tdmx.console.passphrase";
	private Administration admin;
	
	public static AdminApplication get() {
		Application application = Application.get();
		if (application instanceof AdminApplication == false) {
			throw new WicketRuntimeException("The application attached to the current thread is not a "
							+ AdminApplication.class.getSimpleName());
		}

		return (AdminApplication) application;
	}
	
	public static SearchService getSearchService() {
		SearchServiceImpl impl = new SearchServiceImpl();
		impl.setObjectRegistry(get().getAdministration().getObjectRegistry());
		return impl;
	}
	
	public AdminApplication() {
		
	}

	@Override
	public Class getHomePage() {
		return DomainPage.class;
	}

	@Override
	public Session newSession(Request request, Response response) {
	    return new CustomSession(AdminApplication.this, request);
	}
	
	@Override
	protected void init() {
		super.init();
		
		String configFilePath = System.getProperty(CONFIG_PROPERTY);
		if ( configFilePath == null ) {
	    	throw new RuntimeException("Missing System property " + CONFIG_PROPERTY);
		}
		String passphrase = System.getProperty(PASSPHRASE_PROPERTY);
		if ( passphrase == null ) {
			System.out.println("Enter passphrase:");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			try {
				passphrase = br.readLine();
		    } catch (IOException ioe) {
		    	throw new RuntimeException("Unable to read passphrase from stdin.");
		    }
		}
		AdministrationImpl admin = new AdministrationImpl();
		admin.initialize(configFilePath, passphrase);
		this.admin = admin;
		
		// dev utilities
		getDebugSettings().setDevelopmentUtilitiesEnabled(false);
		getDebugSettings().setAjaxDebugModeEnabled(false);
		getRequestCycleSettings().addResponseFilter(new ServerAndClientTimeFilter());
		
		// wicket bootstrap
		configureBootstrap();
		
		// javascripts to the bottom
		setHeaderResponseDecorator(new RenderJavaScriptToFooterHeaderResponseDecorator());
		
		// Setting authorization strategy
 		SimplePageAuthorizationStrategy authorizationStrategy = new SimplePageAuthorizationStrategy(IProtectedPage.class, LoginPage.class) {
			@Override
			protected boolean isAuthorized() {
				return (((CustomSession)Session.get()).isLoggedIn());
			}
		};
		getSecuritySettings().setAuthorizationStrategy(authorizationStrategy);
		
		// setting session expired error page
		IApplicationSettings applicationSettings = getApplicationSettings();
		applicationSettings.setPageExpiredErrorPage(LoginPage.class);

		// mount pages
		mountPage("login", LoginPage.class);
		mountPage("profile", ProfilePage.class);
		mount(new MountedMapperWithoutPageComponentInfo("domain", DomainPage.class));
		mountPage("domain/details", DomainDetailsPage.class);
		
		getMarkupSettings().setStripWicketTags(true);

		
	}
	
	/**
	 * configures wicket-bootstrap and installs the settings.
	 */
	private void configureBootstrap() {
		final ThemeProvider themeProvider = new BootswatchThemeProvider() {{
				add(new MetroTheme());
				add(new GoogleTheme());
				add(new WicketTheme());
				add(new Bootstrap3Theme());
				
				defaultTheme(new GoogleTheme().name());
		}};

		final BootstrapSettings settings = new BootstrapSettings();
		settings.setJsResourceFilterName("footer-container");
		settings.setThemeProvider(themeProvider);
		Bootstrap.install(this, settings);
	}

	public Administration getAdministration() {
		return this.admin;
	}
}



