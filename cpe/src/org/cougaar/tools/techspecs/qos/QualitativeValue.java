package org.cougaar.tools.techspecs.qos;

/**
 * User: wpeng
 * Date: May 28, 2003
 * Time: 11:35:17 PM
 */
public class QualitativeValue extends Value {

    protected QualitativeValue( String symbol ) {
        this.symbol = symbol ;
    }

    public String getSymbol() {
        return symbol;
    }

    private String symbol ;
}
