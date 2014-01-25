package org.tdmx.core.system.env;

import org.tdmx.core.system.env.EnvironmentSupport;

import junit.framework.TestCase;

public class EnvironmentSupportUnitTest extends TestCase {

	public void testExpandVars() {
        for(int i = 0; i < testStrings.length; i ++ ) {
            String testValue = testStrings[i][0];
            String expectedValue = testStrings[i][1];
            String testResult = EnvironmentSupport.expandVars(testValue);
            
            assertEquals(expectedValue, testResult);
        }
	}
	
    private static final String jb = System.getenv("JAVA_HOME");
    
    private static final String[][] testStrings = {
            new String[] { "hello $${JAVA_HOME} world ${JAVA_HOME} hello again", "hello ${JAVA_HOME} world " + jb + " hello again" },
            new String[] { "$${JAVA_HOME}world", "${JAVA_HOME}world" },
            new String[] { "${JAVA_HOME}world", "" + jb + "world" },
            new String[] { "hello${JAVA_HOME}", "hello" + jb + "" },
            new String[] { "hello$${JAVA_HOME}", "hello${JAVA_HOME}" },
            new String[] { "${JAVA_HOME}${JAVA_HOME}", "" + jb + "" + jb + "" },
            new String[] { "hello ${JAVA_HOME world", "hello ${JAVA_HOME world" },
            new String[] { "hello ${", "hello ${" },
            new String[] { "${ hello", "${ hello" },
            new String[] { "hello $${", "hello ${" },
            new String[] { "hello $$${JAVA_HOME} world ${JAVA_HOME} hello again", "hello $${JAVA_HOME} world " + jb + " hello again" },
            new String[] { "hello ${DOESNOTEXIST} world", "hello ${DOESNOTEXIST} world" },
            new String[] { "hello $${${JAVA_HOME}} world", "hello ${" + jb +"} world" },
            new String[] { "hello ${user.name} world", "hello " + System.getProperty("user.name").toLowerCase() +" world" },
            
            // tests for new escaped values
            new String[] {"$\\{JAVA_HOME\\}", "${JAVA_HOME}"},
            new String[] {"$\\{\\}", "${}"},
            new String[] {"$\\{HELLO\\}$\\{WORLD\\}", "${HELLO}${WORLD}"},
            new String[] {"$\\{\\}$\\{\\}", "${}${}"},
            new String[] {"$\\{HELLO\\}\\$\\{WORLD\\}", "${HELLO}\\${WORLD}"},
    };

}
