package org.cougaar.cpe.util;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

public abstract class ConfigParserUtils
{
    public static Integer parseIntegerValue(Document doc, String tagName, String namedItem ) {
        NodeList nodes = doc.getElementsByTagName( tagName );

        String value = null ;
        // Get target plan log
        for (int i=0;i<nodes.getLength();i++) {
            Node n = nodes.item(i) ;
            value = n.getAttributes().getNamedItem( namedItem ).getNodeValue() ;
        }

        if ( value == null ) {
            return null ;
        }

        return new Integer( Integer.parseInt( value ) ) ;
    }
}
