package org.tdmx.console.layout;

import org.apache.wicket.request.resource.CssResourceReference;

public class DocsCssResourceReference extends CssResourceReference {

    public static DocsCssResourceReference GOOGLE = new DocsCssResourceReference("google-docss.css");

    private DocsCssResourceReference(String name) {
        super(DocsCssResourceReference.class, name);
    }
}
