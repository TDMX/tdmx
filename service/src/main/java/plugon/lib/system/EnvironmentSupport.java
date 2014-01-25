package plugon.lib.system;


/**
 * Utility class for environment variable support
 */
public class EnvironmentSupport {
    
    /**
     * returns the value of the property for propertyName or null
     * @param propertyName the property to search for
     * @return the value of the property for propertyName or null
     */
    public static String getProperty(String propertyName) {
        // first check for system property, 
        // this supports values such as user.name
        String expandValue = System.getProperty(propertyName);
        
        // if property not found use loaded env vars
        if(expandValue == null) {
            expandValue = (String)System.getenv(propertyName);
        }

        if("user.name".equals(propertyName)) {
        	expandValue = expandValue.toLowerCase();
        }
        
        return expandValue;
    }

    /**
     * expands environment variables allowing for the escaping of variables.
     * 
     * Note only supports environment variables in the format ${VARNAME}
     * To escape an environment variable use $${VARNAME} or
     * To escape an environment variable use $\{VARNAME\}
     * 
     * Assume variable VARNAME=Hello
     * 
     * "${VARNAME} world" expanded would be "Hello world";
     * "$${VARNAME} world" expanded would be "${VARNAME} world";
     * "$\{VARNAME\} world" expanded would be "${VARNAME} world";
     * 
     */
    public static String expandVars(String string) {
        int i = 0;
        // start with the first index of ${
        i = string.indexOf("${");
        while(i != -1 && i < string.length()) {
            
            // check to see if the preceeding character was the escape character
            if(i == 0 || string.charAt(i-1) != '$') {
                
                // the ${ was not escaped so replace
                int end = string.indexOf("}", i);
                
                // if } found
                if(end != -1) {
                    
                    // expand part will be something like ${VITRIA}
                    String expandPart = string.substring(i, end+1);
                    
                    // property name will be something like VITRIA
                    String propertyName = expandPart.substring(2, expandPart.length()-1);
                    
                    // first check for system property, 
                    // this supports values such as user.name
                    String expandValue = getProperty(propertyName);
                    
                    if(expandValue == null) {
                        expandValue = expandPart;
                    }
                    
                    // whack it all back together with the new value
                    String prefix = string.substring(0,i);
                    String suffix = string.substring(end + 1);
                    
                    string = prefix + expandValue + suffix;
                    i = prefix.length() + expandValue.length();
                }
                else {
                    i = string.length() + 1;
                }
            }
            else {
                // skip the found ${ as it was escaped
                // but replace $${ with ${
                
                String prefix = string.substring(0,i);
                String suffix = string.substring(i+1);
                
                string = prefix + suffix;
            }
            
            // find next "${
            i = string.indexOf("${", i);
        }

        
        // now get rid of the escaped characters in the format $\{VARNAME\}
        // and replace them with ${VARNAME}
        i = string.indexOf("$\\{");
        while(i != -1 && i < string.length()) {
            // find next $\{
            
            int end = string.indexOf("\\}", i);

            // expand part will be something like $\{VITRIA\}
            String expandPart = string.substring(i, end+1);
            // property name will be something like VITRIA
            String propertyName = expandPart.substring(3, expandPart.length()-1);

            // whack it all back together with the new corrected value
            String prefix = string.substring(0,i);
            String suffix = string.substring(end + 2);
            
            string = prefix + "${" + propertyName + "}" + suffix;
            // two charactes have been removed so reset end.
            i = end - 2;

            i = string.indexOf("$\\{", i);
        }
        
        
        return string;
    }

}
