package org.tdmx.console.layout;

import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * A simple stylesheet to fix some styles for the demo page.
 */
public class FixBootstrapStylesCssResourceReference extends CssResourceReference {

    public static final FixBootstrapStylesCssResourceReference INSTANCE = new FixBootstrapStylesCssResourceReference();

    public static final ResourceReference INDICATOR = new PackageResourceReference(
    		FixBootstrapStylesCssResourceReference.class, "ajax-loader.gif");
    /**
     * Construct.
     */
    public FixBootstrapStylesCssResourceReference() {
        super(FixBootstrapStylesCssResourceReference.class, "fix.css");
    }
}
