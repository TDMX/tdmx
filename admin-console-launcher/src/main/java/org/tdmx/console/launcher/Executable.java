package org.tdmx.console.launcher;

import java.io.*;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Simple boot class to make the war executable
 *
 * Code adapted from Hudson - org.eclipse.hudson.war.Executable
 * @author Winston Prakash
 */
public class Executable {

    private final String[] jettyJars = {
        "lib/jetty.jar",
        "lib/jetty-web-app.jar",
        "lib/jetty-continuation.jar",
        "lib/jetty-util.jar",
        "lib/jetty-http.jar",
        "lib/jetty-io.jar",
        "lib/jetty-security.jar",
        "lib/jetty-servlet.jar",
        "lib/jetty-servlet-api.jar",
        "lib/jetty-xml.jar",
        "lib/admin-console-launcher.jar"
    };
    private List<String> arguments;
    private String warPath;
    
    public static void main(String[] args) throws Exception {

        String javaVersion = System.getProperty("java.version");

        StringTokenizer tokens = new StringTokenizer(javaVersion, ".-_");

        int majorVersion = Integer.parseInt(tokens.nextToken());
        int minorVersion = Integer.parseInt(tokens.nextToken());

        // Make sure Java version is 1.7 or later
        if (majorVersion < 2) {
            if (minorVersion < 7) {
                System.err.println("Hudson requires Java 7 or later.");
                System.err.println("Your java version is " + javaVersion);
                System.err.println("Java Home:  " + System.getProperty("java.home"));
                System.exit(0);
            }
        }

        Executable executable = new Executable();
        if ( executable.parseArguments(args) ) {
        	executable.startJetty();
        }
    }

    private boolean parseArguments(String[] args) throws Exception {
        arguments = Arrays.asList(args);
        boolean matched = false;
        String cmd = null;
        for (String arg : arguments) {
            if (arg.startsWith("--version")) {
                System.out.println("TDMX Administration Console Server " + getConsoleVersion());
                matched = true;
            } else if (arg.startsWith("--war=")) {
                warPath = arg.substring("--war=".length());
            } else if (arg.startsWith("--cmd=")) {
                cmd = arg.substring("--cmd=".length());
                if ( cmd.equals("stop" ) ){
                	stopJetty();
                    matched = true;
                }
          } else if (arg.startsWith("--logfile=")) {
                String logFile = arg.substring("--logfile=".length());
                System.out.println("Logging information is send to file " + logFile);
                FileOutputStream fos = new FileOutputStream(new File(logFile));
                PrintStream ps = new PrintStream(fos);
                System.setOut(ps);
                System.setErr(ps);
                matched = true;
            }
        }
        if ( "start".equals(cmd) ) {
        	return true;
        } else if ( !matched ) {
        	printUsage();
        }
        return false;
    }

    private void printUsage() throws IOException {
    	//TODO change httpPort to shutdown port
        String usageStr = "TDMX Administration Console Server " + getConsoleVersion() + "\n"
                + "Usage: java -jar tdmx-console.war [--option=value] [--option=value] ... \n"
                + "\n"
                + "Options:\n"
                + "   --version                        Show version and quit\n"
                + "   --usage                          Prints this usage info.\n"
                + "   --cmd=start|stop                 Command to execute.\n"
                + "   --logfile=<filename>             Send the output log to this file\n"
                + "   --prefix=<prefix-string>         Add this prefix to all URLs (eg http://localhost:8080/prefix/resource). Default is none\n\n"
                + "   --httpPort=<value>               HTTP listening port. Default value is 8080\n\n"
                + "   --httpsPort=<value>              HTTPS listening port. Disabled by default\n"
                + "   --httpsKeyStore=<filepath>       Location of the SSL KeyStore file.\n"
                + "   --httpsKeyStorePassword=<value>  Password for the SSL KeyStore file\n\n"
                ;
        
        System.out.println(usageStr);
    }

    private void startJetty() throws Exception {
    	URL warUrl = null;
    	// we either explicitly say which war to start, or the war is provided.
    	if ( warPath != null ) {
            File file = new File(warPath);
            warUrl = file.toURI().toURL();
    	} else {
            ProtectionDomain protectionDomain = Executable.class.getProtectionDomain();
            warUrl = protectionDomain.getCodeSource().getLocation();
    	}
        
        //TODO remove
        System.out.println(warUrl.getPath());

        List<URL> jarUrls = extractJettyJarsFromWar(warUrl.getPath());

        ClassLoader urlClassLoader = new URLClassLoader(jarUrls.toArray(new URL[jarUrls.size()]));
        Thread.currentThread().setContextClassLoader(urlClassLoader);

        Class jettyUtil = urlClassLoader.loadClass("org.tdmx.console.launcher.JettyLauncher");
        Method mainMethod = jettyUtil.getMethod("start", new Class[]{String[].class, URL.class});
        mainMethod.invoke(null, new Object[]{arguments.toArray(new String[arguments.size()]), warUrl});
    }

	   public void stopJetty() throws Exception {
	        Socket s = new Socket(InetAddress.getByName("127.0.0.1"), 8079);
	        OutputStream out = s.getOutputStream();
	        System.out.println("*** sending jetty stop request");
	        out.write(("\r\n").getBytes());
	        out.flush();
	        s.close();
	    }
	   
    /**
     * Find the TDMX-Console version from war manifest
     *
     * @return
     * @throws IOException
     */
    private static String getConsoleVersion() throws IOException {
        Enumeration manifests = Executable.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
        while (manifests.hasMoreElements()) {
            URL manifestUrl = (URL) manifests.nextElement();
            Manifest manifest = new Manifest(manifestUrl.openStream());
            String tdmxVersion = manifest.getMainAttributes().getValue("TDMX-Console-Version");
            if (tdmxVersion != null) {
                return tdmxVersion;
            }
        }
        return "Unknown Version";
    }

    /**
     * Extract the Jetty Jars from the war
     *
     * @throws IOException
     */
    private List<URL> extractJettyJarsFromWar(String warPath) throws IOException {

        JarFile jarFile = new JarFile(warPath);

        List<URL> jarUrls = new ArrayList<URL>();

        InputStream inStream = null;

        try {

            for (String entryPath : jettyJars) {

                File tmpFile;
                try {
                    tmpFile = File.createTempFile(entryPath.replaceAll("/", "_"), "tdmx");
                } catch (IOException e) {
                    String tmpdir = System.getProperty("java.io.tmpdir");
                    throw new IOException("Failed to extract " + entryPath + " to " + tmpdir, e);
                }
                JarEntry jarEntry = jarFile.getJarEntry(entryPath);
                inStream = jarFile.getInputStream(jarEntry);

                OutputStream outStream = new FileOutputStream(tmpFile);
                try {
                    byte[] buffer = new byte[8192];
                    int readLength;
                    while ((readLength = inStream.read(buffer)) > 0) {
                        outStream.write(buffer, 0, readLength);
                    }
                } catch (Exception exc) {
                    exc.printStackTrace();
                } finally {
                    outStream.close();
                }

                tmpFile.deleteOnExit();
                //System.out.println("Extracted " + entryPath + " to " + tmpFile);
                jarUrls.add(tmpFile.toURI().toURL());
            }

        } catch (Exception exc) {
            exc.printStackTrace();
        } finally {
            if (inStream != null) {
                inStream.close();
            }
        }

        return jarUrls;
    }
}
