/*
  * <copyright>
  *  Copyright 2001 (Intelligent Automation, Inc.)
  *  under sponsorship of the Defense Advanced Research Projects
  *  Agency (DARPA).
  * 
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the Cougaar Open Source License as published by
  *  DARPA on the Cougaar Open Source Website (www.cougaar.org).  
  *  
  *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS 
  *  PROVIDED "AS IS" WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR 
  *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF 
  *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT 
  *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT 
  *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL 
  *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS, 
  *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR 
  *  PERFORMANCE OF THE COUGAAR SOFTWARE.  
  * 
  * </copyright>
  *
  * CHANGE RECORD
  *  8/16/01 Initial version  by Penn State/IAI
  */

/**
 * AssetPDU.java
 *
 * Created on August 16, 2001, 6:52 PM
 */

package org.cougaar.tools.castellan.pdu;

/**
 * Represents an asset.
 *
 * @author  wpeng
 * @version 
 */
public class AssetPDU extends UniqueObjectPDU {

    /** Creates new AssetPDU */
    public AssetPDU(SymbolPDU assetClass, String typeId, String newItemId, String assetTypeNomenclature, UIDPDU uid, int action, long executionTime, long time ) {
       super( uid, EventPDU.TYPE_ASSET, action, executionTime, time ) ;
       this.assetClass = assetClass ;
       this.assetTypeId = typeId ;
       this.itemId = newItemId ;
       this.assetTypeNomenclature = assetTypeNomenclature ;
    }

    public void outputParamString( StringBuffer buf ) {
       super.outputParamString( buf ) ;
       buf.append( ',' ).append( assetClass ).append( ' ' ) ;
       buf.append( ",assetType=" ).append( assetTypeId ) ;
       buf.append( ",itemId=" ).append( itemId ) ;

        if ( pgPDUs != null ) {
            buf.append( ",pgs=[" ) ;
            for (int i=0;i<pgPDUs.length;i++) {
                buf.append( pgPDUs[i] ) ;
            }
            buf.append( "]" ) ;
        }

    }
    
    public SymbolPDU getAssetClass() { return assetClass ; }

    public String getAssetTypeId() { return assetTypeId ; }

    public String getItemId() { return itemId ; }
    
    public String getAssetTypeNomenclature() { return assetTypeNomenclature ; }

    public void setPropertyGroups( PropertyGroupPDU[] pgs ) {
        this.pgPDUs = pgs ;
    }

    public PropertyGroupPDU[] getPropertyGroups() { return pgPDUs ; }

    // String assetClass ;
    SymbolPDU assetClass ; 
    String assetTypeId ;
    String assetTypeNomenclature ;
    String itemId ;
    PropertyGroupPDU[] pgPDUs ;
    
    static final long serialVersionUID = 1989778668705861190L;    
}
