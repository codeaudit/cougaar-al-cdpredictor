package org.cougaar.cpe.model;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * These are some constants to be used corresponding primarily to simulation
 * rules.  Also, it includes auxiliary methods to read in settings from an XML document.
 *
 */
public class VGWorldConstants {
    public static final String TAG_CPE_WORLD = "CPEWorld";

    private static String getNodeValueForTag(Document doc, String tagName, String namedItem ) {
        NodeList nodes = doc.getElementsByTagName( tagName );

        String value = null ;
        for (int i=0;i<nodes.getLength();i++) {
            Node n = nodes.item(i) ;
            value = n.getAttributes().getNamedItem( namedItem ).getNodeValue() ;
        }
        return value;
    }

    public static void printParameters( PrintWriter w ) {
        Class c = VGWorldConstants.class ;

        Field[] f = c.getDeclaredFields() ;

        w.println( "\n----------------------------------------------------------");
        w.println( "CPE Parameter Values");
        for (int i = 0; i < f.length; i++)
        {
            Field field = f[i];
            if ( !Modifier.isFinal( field.getModifiers() ) && Modifier.isStatic( field.getModifiers() ) &&
                 ( field.getType().isPrimitive() || field.getType() == String.class ) )
            {
                String name = field.getName() ;
                try
                {
                    w.println( name + "\t\t\t" + field.get(null) );
                }
                catch (IllegalArgumentException e)
                {
                    e.printStackTrace();
                }
                catch (IllegalAccessException e)
                {
                    e.printStackTrace();
                }
            }
        }
        w.println( "----------------------------------------------------------");
        w.flush();
    }

    public static void saveParameterValues( OutputStream os  ) {
        PrintWriter pw = new PrintWriter( os ) ;

        Class c = VGWorldConstants.class ;
//        ReflectPermission perm = new ReflectPermission( "suppressAccessChecks" ) ;

        Field[] f = c.getDeclaredFields() ;
        pw.println( "<" + TAG_CPE_WORLD + ">");
        pw.flush();

        for (int i = 0; i < f.length; i++) {
            Field field = f[i];
            if ( !Modifier.isFinal( field.getModifiers() ) && Modifier.isStatic( field.getModifiers() ) &&
                 ( field.getType().isPrimitive() || field.getType() == String.class ) )
            {
                String name = field.getName() ;
                if ( Character.isUpperCase( name.charAt(0) ) ) {
                    // Remove underscores.
                    name = convertFromAllCaps( name ) ;
                }
                else {
                    StringBuffer newName = new StringBuffer() ;
                    newName.append( Character.toUpperCase( name.charAt(0) ) ) ;
                    newName.append( name.substring(1) ) ;
                    name = newName.toString() ;
                }
                try
                {
                    pw.println( "    <" + name + " value=\"" + field.get(null) + "\" />" );
                    pw.flush();
                }
                catch (IllegalArgumentException e)
                {
                    e.printStackTrace();
                }
                catch (IllegalAccessException e)
                {
                    e.printStackTrace();
                }
            }

        }
        pw.println("</" + TAG_CPE_WORLD + ">");
        pw.close();

    }

    /**
     * Load world constants configuration from the document.
     * Search by the name of the local static variable (with the initial caps.)
     *
     * @param doc
     */
    public static void setParameterValues( Document doc ) {

        Node root = doc.getDocumentElement() ;
        if( root.getNodeName().equals( "CPEWorld" ) ) {

            Class c = VGWorldConstants.class ;
            Field[] f = c.getDeclaredFields() ;
            for (int i = 0; i < f.length; i++) {
                Field field = f[i];
                if ( !Modifier.isFinal( field.getModifiers() ) && Modifier.isStatic( field.getModifiers() )
                     && ( field.getType().isPrimitive() || field.getType() == String.class ) ) {
                    String name = field.getName() ;
                    if ( Character.isUpperCase( name.charAt(0) ) ) {
                        // Remove underscores.
                        name = convertFromAllCaps( name ) ;
                    }
                    else {
                        StringBuffer newName = new StringBuffer() ;
                        newName.append( Character.toUpperCase( name.charAt(0) ) ) ;
                        newName.append( name.substring(1) ) ;
                        name = newName.toString() ;
                    }
                    String svalue = getNodeValueForTag( doc, name, "value" ) ;
                    if (svalue != null)
                    {
                        // Now, look at the type of field and parse accordingly.
                        if (field.getType().equals( Float.TYPE ) )
                        {
                            try
                            {
                                float fvalue = Float.parseFloat(svalue);
                                field.setFloat(null, fvalue);
                            }
                            catch (NumberFormatException e)
                            {
                                System.err.println("Exception " + e + " parsing " + svalue);
                            }
                            catch (IllegalAccessException e)
                            {
                                e.printStackTrace();
                            }
                        }
                        else if (field.getType() == Double.TYPE )
                        {
                            try
                            {
                                double fvalue = Double.parseDouble(svalue);
                                field.setDouble(null, fvalue);
                            }
                            catch (NumberFormatException e)
                            {
                                System.err.println("Exception " + e + " parsing " + svalue);
                            }
                            catch (IllegalAccessException e)
                            {
                                e.printStackTrace();
                            }

                        }
                        else if (field.getType() == Integer.TYPE )
                        {
                            try
                            {
                                int value = Integer.parseInt(svalue);
                                field.setInt(null, value);
                            }
                            catch (NumberFormatException e)
                            {
                                System.err.println("Exception " + e + " parsing " + svalue);
                            }
                            catch (IllegalAccessException e)
                            {
                                e.printStackTrace();
                            }
                        }
                        else if (field.getType() == Long.TYPE)
                        {
                            try
                            {
                                long value = Long.parseLong(svalue);
                                field.setLong(null, value);
                            }
                            catch (NumberFormatException e)
                            {
                                System.err.println("Exception " + e + " parsing " + svalue);
                            }
                            catch (IllegalAccessException e)
                            {
                                e.printStackTrace();
                            }
                        }
                        else if ( field.getType() == String.class ) {
                            try
                            {
                                field.set( null, svalue ) ;
                            }
                            catch (IllegalArgumentException e)
                            {
                                e.printStackTrace();
                            }
                            catch (IllegalAccessException e)
                            {
                                e.printStackTrace();
                            }
                        }
                        else {
                            System.err.println("Unexpected condition: Could not process field " + field );
                        }
                    }
                }
            }
        }
    }

    private static String convertFromAllCaps(String name) {
        StringBuffer result = new StringBuffer() ;
        result.append( name.charAt(0) ) ;
        for (int i=1;i<name.length();i++) {
            if ( name.charAt(i) == '_' ) {
                continue ;
            }
            if ( name.charAt(i-1) == '_' ) {
                result.append( name.charAt(i) ) ;
            }
            else {
                result.append( Character.toLowerCase(name.charAt(i)) ) ;
            }
        }
        return result.toString() ;
    }

    /**
     * Basic time constants.
     */
    public static final double MILLISECONDS_PER_SECOND = 1000 ;

    /**
     * Number of seconds/ ms.
     */
    public static final double SECONDS_PER_MILLISECOND = 1.0/1000.0;

    /**
     * Width in coordinate units.
     */
    private static double UNIT_RANGE_WIDTH = 2 ;

    private static float UNIT_RANGE_HEIGHT = 8 ;

    private static float LONG_SENSOR_MAX_ERROR = 2 ;

    /**
     * The sensor range at the BN level.
     */
    private static float MEDIUM_SENSOR_RANGE = 14 ;

    /**
     * The sensor error at the BN level.
     */
    private static float MEDIUM_SENSOR_MAX_ERROR = 0.5f ;

    /**
     * Sensor range for the CPY units.
     */
    private static float CPY_SENSOR_WIDTH = 4 ;

    private static float CPY_SENSOR_HEIGHT = 8 ;

    // Fuel capacity
    //
    private static float MAX_UNIT_FUEL_LOAD = 50 ;

    private static int MAX_UNIT_AMMO_LOAD = 40 ;


    /**
     * The target move rate along the y axis. in coordinate units per second.
     */
    private static double TARGET_MOVE_RATE = 0.04 ;

    private static double targetXMoveRate = 0.00 ;

    /**
     * Suppressed movement is multipled by this factor.
     */
    private static double targetSuppressedMovementFactor = 0.25 ;

    /**
     * Units with <= strengh will be routed, i.e. reverse direction.
     */
    private static double targetRoutStrength = 20 ;

    /**
     * Amount of time units are suppressed in ms.
     */
    private static long targetSuppressedTime = 50000 ;

    public static final double TARGET_RANGE_WIDTH = 2.4 ;

    public static final double TARGET_RANGE_HEIGHT = 5 ;

    /**
     * Initial target strength.
     */
    private static double targetFullStrength = 30 ;

    /**
     * Standard damage inflicted by a target.
     */
    private static double TARGET_STANDARD_ATTRITION = 1 ;

    /**
     * Consumption rate in fuel units/ coordinate unit traveled.
     */
    private static double UNIT_FUEL_CONSUMPTION_RATE = 10.0 ;

    private static double UNIT_STANDARD_ATTRITION_RATE = 5.0 ;

    private static double UNIT_STANDARD_HIT_PROBABILITY = 0.65 ;

    private static double UNIT_MULTI_HIT_PROBABILITY = 0.85 ;

    private static double UNIT_STANDARD_ATTRITION = 5 ;

    /**
     * Movement rate in units/second.
     */
    private static double UNIT_NORMAL_MOVEMENT_RATE = 0.04 ;

    /**
     * The grid size in coordinate units.
     */
    public static final double WORLD_GRID_SIZE = 0.02 ;

    private static double SUPPLY_VEHICLE_MOVEMENT_RATE = 0.08 ;

    /**
     * Increase speed by a factor but at a increased fuel
     * consumption rate.
     */
    private static final double UNIT_MAXIMUM_MOVEMENT_FACTOR = 1.25 ;

    /**
     * Fuel consumption multiplier at increased speed.  When moving at max speed,
     * fuel consumption is increased by a factor of 3.
     */
    private static final double MAXIMUM_FUEL_CONSUMPTION_FACTOR = 2.5 ;

    /**
     * The maximum number of supply units held by a supply vehicle.
     */
    private static int MAX_SUPPLY_UNIT_LOAD = 20 ;


    public static double getTargetMoveRate() {
        return TARGET_MOVE_RATE;
    }

    public static double getTargetXMoveRate()
    {
        return targetXMoveRate;
    }

    public static double getUnitRangeWidth() {
        return UNIT_RANGE_WIDTH;
    }

    public static double getUnitRangeHeight() {
        return UNIT_RANGE_HEIGHT;
    }

    public static double getUnitSensorWidth() {
        return CPY_SENSOR_WIDTH;
    }

    public static float getUnitSensorHeight() {
        return CPY_SENSOR_HEIGHT;
    }

    public static float getMediumSensorRange()
    {
        return MEDIUM_SENSOR_RANGE;
    }

    public static float getMediumSensorMaxError()
    {
        return MEDIUM_SENSOR_MAX_ERROR;
    }

    public static float getLongSensorMaxError()
    {
        return LONG_SENSOR_MAX_ERROR;
    }

    public static int getMaxUnitAmmoLoad() {
        return MAX_UNIT_AMMO_LOAD;
    }

    public static float getMaxUnitFuelLoad() {
        return MAX_UNIT_FUEL_LOAD;
    }

    public static double getRecoveryLine() {
        return 0;
    }

    /**
     * Speed of supply vehicles, given in units/second.
     * @return
     */
    public static double getSupplyVehicleMovementRate() {
        return SUPPLY_VEHICLE_MOVEMENT_RATE;
    }

    public static long getTargetSuppressedTime()
    {
        return targetSuppressedTime;
    }

    public static double getTargetSuppressedMovementFactor()
    {
        return targetSuppressedMovementFactor;
    }

    public static double getTargetRoutStrength()
    {
        return targetRoutStrength;
    }

    public static double getTargetFullStrength()
    {
        return targetFullStrength;
    }

    public static double getUnitFuelConsumptionRate()
    {
        return UNIT_FUEL_CONSUMPTION_RATE;
    }

    public static double getUnitStandardAttritionRate()
    {
        return UNIT_STANDARD_ATTRITION_RATE;
    }

    public static double getUnitStandardHitProbability()
    {
        return UNIT_STANDARD_HIT_PROBABILITY;
    }

    public static double getUnitMultiHitProbability()
    {
        return UNIT_MULTI_HIT_PROBABILITY;
    }

    public static double getUnitStandardAttrition()
    {
        return UNIT_STANDARD_ATTRITION;
    }

    public static double getUnitNormalMovementRate()
    {
        return UNIT_NORMAL_MOVEMENT_RATE;
    }

    public static double getTargetStandardAttrition()
    {
        return TARGET_STANDARD_ATTRITION;
    }

    public static int getMaxSupplyUnitLoad()
    {
        return MAX_SUPPLY_UNIT_LOAD;
    }

    public static double getUnitMaximumMovementFactor()
    {
        return UNIT_MAXIMUM_MOVEMENT_FACTOR;
    }

    public static double getMaximumFuelConsumptionFactor()
    {
        return MAXIMUM_FUEL_CONSUMPTION_FACTOR;
    }

}
