package org.cougaar.tools.techspecs;

import java.io.Serializable;
import java.util.LinkedList;
public class TechSpec implements Serializable
{
    public static final int TYPE_PLUGIN = 0 ;

    public static final int TYPE_AGENT = 1 ;

    public static final int TYPE_NODE = 2 ;

    public static final int TYPE_HOST = 3 ;

    public final static int TYPE_ROLE = 4 ;

    public final static int TYPE_ACTION = 5 ;

    public static final int TYPE_ROLESTATE = 6 ;

    public static final int TYPE_MEASUREMENT = 7 ;

    public static final int TYPE_OPERATING_MODE = 8 ;

    public static final int TYPE_ACTION_INPUT = 9;

    /**
     * This is layer neutral.
     */
    public static final int LAYER_NONE = -1;

    public static final int LAYER_NETWORk = 0 ;

    public static final int LAYER_APPLICATION = 1 ;

    public static final int LAYER_EXECUTION = 2 ;

    public TechSpec(String name, TechSpec parent, int type, int layer )
    {
        this.name = name;
        this.parent = parent;
        this.type = type;
        this.layer = layer ;
    }

    public String getName()
    {
        return name;
    }

    public String[] getFullyQualifiedName() {
        LinkedList names = new LinkedList() ;
        TechSpec current = this ;
        while ( current != null ) {
            names.addFirst( current.getName() );
            current = current.getParent() ;
        }
        String[] result = new String[ names.size() ] ;
        result = (String[]) names.toArray( result ) ;
        return result ;
    }

    public TechSpec getParent()
    {
        return parent;
    }

    public int getType()
    {
        return type;
    }

    public int getLayer()
    {
        return layer;
    }



    private int type ;

    private int layer ;

    private String name ;

    private String[] fullName ;
    protected TechSpec parent ;
}
