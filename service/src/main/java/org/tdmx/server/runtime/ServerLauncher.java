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

import java.io.IOException;
import java.util.StringTokenizer;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;

public class ServerLauncher {

	private static Logger log = LoggerFactory.getLogger(ServerContainer.class);

	public static void main(String[] args) throws Exception {
		String javaVersion = System.getProperty("java.version");

		StringTokenizer tokens = new StringTokenizer(javaVersion, ".-_");

		int majorVersion = Integer.parseInt(tokens.nextToken());
		int minorVersion = Integer.parseInt(tokens.nextToken());

		if (majorVersion < 2) {
			if (minorVersion < 7) {
				System.err.println("TDMX-Server requires Java 7 or later.");
				System.err.println("Your java version is " + javaVersion);
				System.err.println("Java Home:  " + System.getProperty("java.home"));
				System.exit(0);
			}
		}

		/*
		 * SSlServerCheck check = new SSlServerCheck(); check.checkSSLCapabilities();
		 * 
		 * ApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"server-context.xml"});
		 * System.out.println(context);
		 */

		BeanFactoryLocator beanFactoryLocator = ContextSingletonBeanFactoryLocator.getInstance();
		BeanFactoryReference beanFactoryReference = beanFactoryLocator.useBeanFactory("applicationContext");
		ApplicationContext context = (ApplicationContext) beanFactoryReference.getFactory();

		ServerContainer sc = (ServerContainer) context.getBean("serverContainer");
		sc.runUntilStopped();
	}

	private static class SSlServerCheck {
		public void checkSSLCapabilities() {
			int port = 8888;

			try {
				System.out.println("Locating server socket factory for SSL...");
				SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

				System.out.println("Creating a server socket on port " + port);
				SSLServerSocket serverSocket = (SSLServerSocket) factory.createServerSocket(port);

				String[] suites = serverSocket.getSupportedCipherSuites();
				System.out.println("Support cipher suites are:");
				for (int i = 0; i < suites.length; i++) {
					System.out.println(suites[i]);
				}
				serverSocket.setEnabledCipherSuites(suites);

				System.out.println("Support protocols are:");
				String[] protocols = serverSocket.getSupportedProtocols();
				for (int i = 0; i < protocols.length; i++) {
					System.out.println(protocols[i]);
				}
				/*
				 * System.out.println("Waiting for client..."); SSLSocket socket = (SSLSocket) serverSocket.accept();
				 * 
				 * System.out.println("Starting handshake..."); socket.startHandshake();
				 * 
				 * System.out.println("Just connected to " + socket.getRemoteSocketAddress());
				 */
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
