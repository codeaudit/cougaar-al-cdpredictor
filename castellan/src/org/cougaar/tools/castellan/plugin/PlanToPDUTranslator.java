/*
 * PlanToPDUTranslator.java
 *
 * Created on August 16, 2001, 5:36 PM
 */

package org.cougaar.tools.castellan.plugin;

import org.cougaar.tools.castellan.pdu.*;

import java.util.*;

import org.cougaar.util.UnaryPredicate;
import org.cougaar.core.util.*;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.planning.ldm.asset.*;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.core.mts.*;
import org.cougaar.core.agent.*;
import org.cougaar.glm.ldm.asset.OrganizationPG;
import org.cougaar.planning.ldm.plan.*;

import org.cougaar.glm.ldm.asset.*;

import java.io.*;

/**
 *
 * @author  wpeng
 * @version
 */
public abstract class PlanToPDUTranslator
{

    protected static SymStringPDU toSym( String s )
    {
        if ( s == null )
        {
            return null;
        }
        return new SymStringPDU( s );
    }

    protected static UIDStringPDU toSym( org.cougaar.core.util.UID uid )
    {
        if ( uid == null )
        {
            return null;
        }
        return new UIDStringPDU( uid.getOwner(), uid.getId() );
    }

    public static MPTaskPDU makeMPTaskPDU( long executionTime, long time, MPTask t, int action )
    {
        MPTaskPDU mpt = new MPTaskPDU( toSym( t.getVerb().toString() ), toSym( t.getUID() ),
                action, executionTime, time );
        return mpt;
    }

    public static final TaskPDU makeTaskMessage( long executionTime, long time, Task t, int action )
    {
/*  Original code
        // Do not infer the parent.  This is a significant change from the previous version
        UIDPDU directObject = null ;
        Asset doAsset = t.getDirectObject() ;
        if ( doAsset != null ) {
            directObject = toSym( doAsset.getUID() ) ;
        }
        TaskPDU tm =
                new TaskPDU( toSym( t.getVerb().toString() ), toSym( t.getParentTaskUID() ), toSym( t.getUID() ), directObject,
                        action, executionTime, time );
        return tm;
*/
      // Do not infer the parent.  This is a significant change from the previous version
//        UIDPDU directObject = null ;

		String directObject = "10";
//		int directObject = 0;
        Asset doAsset = t.getDirectObject() ;

        if ( doAsset != null ) {
//            directObject = toSym( doAsset.getUID() ) ;
//////
			if (doAsset instanceof ClassISubsistence)
			{
				directObject = "1";
			} else if (doAsset instanceof ClassIIClothingAndEquipment)
			{
				directObject = "2";
			} else if (doAsset instanceof ClassIIIPOL)
			{
				directObject = "3";
			} else if (doAsset instanceof ClassIVConstructionMaterial)
			{
				directObject = "4";
			} else if (doAsset instanceof ClassVAmmunition )
			{
				directObject = "5";
			} else if (doAsset instanceof ClassIXRepairPart)
			{
				directObject = "9";  
			} //else {
//				directObject = "null;
//				directObject = doAsset.toString();
//			}
///////
        }

//		System.out.println("directObject = "+ directObject);
        
        TaskPDU tm =
                new TaskPDU( toSym( t.getVerb().toString() ), toSym( t.getParentTaskUID() ), toSym( t.getUID() ), directObject,
                        action, executionTime, time );
        return tm;
    }

    static class MyByteArrayOutputStream extends ByteArrayOutputStream
    {
        public MyByteArrayOutputStream( int size )
        {
            super( size );
        }

        public byte[] getByteArray()
        {
            return buf;
        }
    }

    static final MyByteArrayOutputStream bos = new MyByteArrayOutputStream( 4096 );
    static final DataOutputStream dos = new DataOutputStream( bos );
    static final java.util.zip.CRC32 crc = new java.util.zip.CRC32();
    public static final byte ASPECT_VALUE_TYPE = ( byte ) 0;
    public static final byte TIME_ASPECT_VALUE_TYPE = ( byte ) 1;
    public static final byte ASSET_ASPECT_VALUE_TYPE = ( byte ) 2;
    public static final byte TYPED_QUANTITY_ASPECT_VALUE_TYPE = ( byte ) 3;

    public static final void writeAspect( AspectValue av, DataOutputStream dos )
    {
        try
        {
            dos.writeInt( av.getAspectType() );
            if ( av instanceof TimeAspectValue )
            {
                dos.writeByte( TIME_ASPECT_VALUE_TYPE );
                TimeAspectValue tav = ( TimeAspectValue ) av;
                dos.writeLong( tav.timeValue() );
            } else if ( av instanceof TypedQuantityAspectValue )
            {
                dos.writeByte( TYPED_QUANTITY_ASPECT_VALUE_TYPE );
                TypedQuantityAspectValue tqav = ( TypedQuantityAspectValue ) av;
                dos.writeLong( tqav.longValue() );
                if ( tqav.getAsset() != null && tqav.getAsset().getUID() != null )
                {
                    dos.writeUTF( tqav.getAsset().getUID().toString() );
                } else
                    dos.writeUTF( "" );
            } else if ( av instanceof AssetAspectValue )
            {
                dos.writeByte( ASSET_ASPECT_VALUE_TYPE );
                AssetAspectValue aav = ( AssetAspectValue ) av;
                dos.writeByte( ASSET_ASPECT_VALUE_TYPE );
                if ( aav.getAsset() != null && aav.getAsset().getUID() != null )
                {
                    dos.writeUTF( aav.getAsset().getUID().toString() );
                } else
                    dos.writeUTF( "" );
            } else
            {
                dos.writeByte( ASPECT_VALUE_TYPE );
                dos.writeDouble( av.getValue() );
            }
        } catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    /**
     * Compares only success and confidence rating.
     */
    public static final long computeFastCRC32( AllocationResult ar )
    {
        if ( ar == null )
        {
            return 0;
        }
        bos.reset();
        try
        {
            dos.writeBoolean( ar.isSuccess() );
            dos.writeDouble( ar.getConfidenceRating() );
            /*
            AspectValue[] avs = ar.getAspectValueResults() ;
            if ( avs != null ) {
                dos.writeByte( 1 ) ;
                dos.writeInt( avs.length ) ;
                for (int i=0;i<avs.length;i++) {
                    writeAspect( avs[i], dos ) ;
                }
            }
            else {
                dos.writeByte( 0 ) ;
            }
            if ( ar.isPhased() ) {
                dos.writeBoolean( true ) ;
                java.util.List list = ar.getPhasedAspectValueResults();
                if ( list == null ) {
                    dos.writeInt( 0 ) ;
                }
                else {
                    dos.writeInt( list.size() ) ;
                    // Write some phased aspect values out here.
                    for ( Iterator iter = list.listIterator();iter.hasNext();) {
                        AspectValue[] pavs  = ( AspectValue[] ) iter.next() ;
                        dos.writeInt( pavs.length ) ;
                        for (int i=0;i<pavs.length;i++) {
                            writeAspect( pavs[i], dos ) ;
                        }
                    }
                }
            }
            else {
                dos.writeBoolean( false ) ;
            }
            */
            dos.flush();
        } catch ( Exception e )
        {
            e.printStackTrace();  // Should never happen!
        }

        crc.reset();
        crc.update( bos.getByteArray(), 0, bos.size() ); // Override bos not to generate garbage
        long value = crc.getValue();
        return value;
    }

    public static final AllocationResultPDU makeAllocationResultMessage( long executionTime, long time,
                                                                         UID planElement, AllocationResult allocationResult,
                                                                         short arType, int action )
    {
        if ( allocationResult == null && action == AllocationResultPDU.ACTION_REMOVE ) {
            UIDPDU uidpdu = toSym( planElement );
            AllocationResultPDU pdu =
                    new AllocationResultPDU( uidpdu, arType, action, executionTime, time );
            return pdu ;
        }
        else {
            UIDPDU uidpdu = toSym( planElement );
            AllocationResultPDU pdu =
                    new AllocationResultPDU( uidpdu, arType, action, executionTime, time );
            pdu.setSuccess( allocationResult.isSuccess() );
            pdu.setConfidence( ( float ) allocationResult.getConfidenceRating() );
            // Do not touch the aspect values!
            // pdu.setAspectValues( allocationResult.getAspectValueResults() ) ;
            return pdu;
        }
    }

    public static final AssetPDU makeAssetMessage( long executionTime, long time, Asset asset, int action )
    {
        String typeId = null, typeNomenclature = null;
        TypeIdentificationPG typeIdPG = asset.getTypeIdentificationPG();
        typeId = typeIdPG.getTypeIdentification();
        typeNomenclature = typeIdPG.getNomenclature();
        AssetPDU am = new AssetPDU( toSym( asset.getClass().toString() ),
                typeId,
                typeNomenclature,
                asset.getItemIdentificationPG().getItemIdentification(),
                toSym( asset.getUID() ),
                action, executionTime, time );
        return am;
    }

    public static final AllocationPDU makeAllocationMessage(
            long executionTime, long time, Allocation alloc, int action )
    {
        Asset asset = alloc.getAsset();
        UID allocTaskUID = null;

        if ( asset != null )
        {
            // link to allocated asset
            ClusterPG clusterPG = asset.getClusterPG();
            MessageAddress clusterID = null;   //Himanshu

            // Check to see if this asset is a cluster.
            String remoteClusterID =
                    ( clusterPG != null && ( clusterID = clusterPG.getMessageAddress() ) != null ) //Himanshu
                    ? clusterID.toString() : null;

            if ( remoteClusterID != null )
            {
                // Find the "down" task e.g. (task1 -> cluster ->  task2), where task2 "shadows" task1.
                Task task = ( ( AllocationforCollections ) alloc ).getAllocationTask();

                if ( task != null )
                {
                    UID taskU;
                    String taskUID;
                    //UID atU;
                    //String atUID;
                    //String atCluster;

                    // Attempt to get the UID of the remote task
                    if ( ( ( taskU = task.getUID() ) != null ) &&
                            ( ( taskUID = taskU.toString() ) != null ) )
                    {
                        allocTaskUID = taskU;
                        // if (((atCluster =
                        //    ((task == asset) ?
                        //     getClusterNameFromUID(taskUID) :
                        //     (((asset != null) &&
                        //       ((atU = asset.getUID()) != null) &&
                        //       ((atUID = atU.getUID()) != null)) ?
                        //      getClusterNameFromUID(atUID) :
                        //      null))) == null))
                        //{
                        //}

                    }
                }
            }
        }
        AllocationPDU am = new AllocationPDU( toSym( alloc.getTask().getUID() ),
                toSym( asset.getUID() ),
                toSym( allocTaskUID ), toSym( alloc.getUID() ),
                action, executionTime, time );
        return am;
    }

    public static final AggregationPDU makeAggregationPDU( long executionTime, long time,
                                                           AggregationImpl aggregation, int action )
    {
        Composition c = aggregation.getComposition();

        AggregationPDU am = new AggregationPDU(
                toSym( c.getCombinedTask().getUID() ),
                toSym( aggregation.getTask().getUID() ),
                toSym( aggregation.getUID() ),
                action, executionTime, time );
        return am;
    }

    public static final ExpansionPDU makeExpansionPDU( long executionTime, long time,
                                                       Expansion expansion, int action )
    {
        Workflow wf = expansion.getWorkflow();

        ArrayList alist = new ArrayList();
        for ( Enumeration e = wf.getTasks(); e.hasMoreElements(); )
        {
            Task t = ( Task ) e.nextElement();
            alist.add( t );
        }
        UIDPDU[] tasks = new UIDPDU[alist.size()];
        for ( int i = 0; i < alist.size(); i++ )
        {
            tasks[i] = toSym( ( ( Task ) alist.get( i ) ).getUID() );
            //System.out.println( "Expanding " + tasks[i]  );
        }

        ExpansionPDU am = new ExpansionPDU(
                toSym( expansion.getTask().getUID() ),
                tasks,
                toSym( expansion.getUID() ),
                action, executionTime, time );
        return am;
    }

    public static final ClusterPGPDU makeClusterPDU( ClusterPG cluster ) {
        if ( cluster == null ) {
            throw new IllegalArgumentException( "PropertyGroup must be non-null." ) ;
        }
        String clusterId = null ;
        if ( cluster.getMessageAddress() != null ) {
            clusterId = cluster.getMessageAddress().toString() ;
        }
        ClusterPGPDU pgpdu = new ClusterPGPDU( clusterId ) ;
        return pgpdu ;
    }

    public static final RelationshipPGPDU makeRelationshipPDU( long executionTime, long time,
                                                                          RelationshipPG pg )
    {
        if ( pg == null ) {
            throw new IllegalArgumentException( "PropertyGroup must be non-null." ) ;
        }

        RelationshipSchedule schedule = pg.getRelationshipSchedule() ;
        ArrayList list = new ArrayList( schedule ) ;
        ArrayList relationships = new ArrayList() ;
        for ( Iterator iter = list.iterator(); iter.hasNext(); ) {
            Relationship r = ( Relationship ) iter.next() ;
            Role selfRole = Role.getRole("Self" ) ;
            if ( r.getRoleA().equals( selfRole ) || r.getRoleB().equals( selfRole ) ) {
                continue ;
            }

            HasRelationships a = r.getA(), b = r.getB() ;
            if ( a instanceof Asset && b instanceof Asset ) {
                Asset orga = ( Asset ) a ;
                Asset orgb = ( Asset ) b ;
                relationships.add( new RelationshipPDU( orga.getKey().toString(), orgb.getKey().toString(),
                   r.getRoleA().getName(), r.getRoleB().getName() ) ) ;
            }
            else {
                continue ;
            }
            //
        }

        RelationshipPDU[] rpdus =
                new RelationshipPDU[ relationships.size() ];
        for (int i=0;i<relationships.size();i++) {
            rpdus[i] = ( RelationshipPDU ) relationships.get(i) ;
        }

        return new RelationshipPGPDU( rpdus ) ;
    }
}
