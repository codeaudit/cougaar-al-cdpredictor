package org.cougaar.tools.castellan.analysis;
import org.cougaar.tools.castellan.pdu.* ;


public class SocietyMapBuilder {

    public SocietyMapBuilder() {
    }

    public void processLog( AssetPDU pdu ) {
        if ( pdu.getAssetClass().equals( "org.cougaar.domain.glm.ldm.asset.Organization" ) ) {
            // Get the RelationshipPG
        }

    }

    
} 