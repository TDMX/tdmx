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
package org.tdmx.server.runtime;

import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.util.StringUtils;

public class ServerLauncher {

	private static final Logger log = LoggerFactory.getLogger(ServerContainer.class);

	public static void main(String[] args) {
		String javaVersion = System.getProperty("java.version");

		StringTokenizer tokens = new StringTokenizer(javaVersion, ".-_");

		int majorVersion = Integer.parseInt(tokens.nextToken());
		int minorVersion = Integer.parseInt(tokens.nextToken());

		if (majorVersion < 2 && minorVersion < 7) {
			log.error("TDMX-Server requires Java 7 or later.");
			log.error("Your java version is " + javaVersion);
			log.error("Java Home:  " + System.getProperty("java.home"));
			System.exit(-1);
		}

		// Construct the SpringApplication
		BeanFactoryLocator beanFactoryLocator = ContextSingletonBeanFactoryLocator.getInstance();
		BeanFactoryReference beanFactoryReference = beanFactoryLocator.useBeanFactory("applicationContext");
		ApplicationContext context = (ApplicationContext) beanFactoryReference.getFactory();

		SslServerSocketInfo si = (SslServerSocketInfo) context.getBean("sslInfo");
		log.info("JVM supportedCipherSuites: " + StringUtils.arrayToCommaDelimitedString(si.getSupportedCipherSuites()));
		log.info("JVM supportedProtocols: " + StringUtils.arrayToCommaDelimitedString(si.getSupportedProtocols()));
		log.info("default TrustManagerFactoryAlgorithm: " + si.getDefaultTrustManagerFactoryAlgorithm());

		// Start the Jetty
		ServerContainer sc = (ServerContainer) context.getBean("serverContainer");
		sc.runUntilStopped();
	}

	private ServerLauncher() {
	}
}
