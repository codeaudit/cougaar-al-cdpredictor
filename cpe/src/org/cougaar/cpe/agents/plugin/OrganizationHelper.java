/*
 * <copyright>
 *  Copyright 2003-2004 Intelligent Automation, Inc.
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 *
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.cpe.agents.plugin;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.RelationshipSchedule;
import org.cougaar.planning.ldm.plan.Relationship;
import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.planning.ldm.plan.HasRelationships;
import org.cougaar.glm.ldm.asset.Organization;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * User: wpeng
 * Date: Apr 11, 2003
 * Time: 3:29:42 PM
 */
public class OrganizationHelper {

    public static void processCustomerAndProviderOrganizations( MessageAddress agentAddress, Collection collection,
                                                                ArrayList customers, ArrayList providers )
    {
        Iterator iter = collection.iterator();
        Role selfRole = Role.getRole("Self" ) ;
        Role supplyProvider = Role.getRole( "SupplyProvider" ) ;
        Role supplyCustomer = Role.getRole( "SupplyCustomer" );

        // Find self entitiy.
        while (iter.hasNext()) {
            Organization organization = (Organization) iter.next();
            RelationshipSchedule schedule = organization.getRelationshipSchedule() ;
            ArrayList list = new ArrayList( schedule ) ;
            for ( Iterator iter2 = list.iterator(); iter2.hasNext(); ) {
                Relationship r = ( Relationship ) iter2.next() ;
                if ( r.getRoleA().equals( selfRole ) || r.getRoleB().equals( selfRole ) ) {
                    continue ;
                }
                else {
                    HasRelationships a = r.getA(), b = r.getB() ;
                    Asset orga = ( Asset ) a ;
                    Asset orgb = ( Asset ) b ;
                    if ( orga.getClusterPG().getMessageAddress().equals( agentAddress ) &&
                            orgb.getClusterPG().getMessageAddress().equals(organization.getMessageAddress())) {
                        if ( a instanceof Asset && b instanceof Asset ) {
                            if ( r.getRoleB().equals( supplyProvider ) ) {
                                System.out.println("ORG:: Agent " + agentAddress +
                                        " found supply provider " + orgb + " with address " + orgb.getClusterPG().getMessageAddress() );
                                providers.add( organization ) ;
                            }
                            else if ( r.getRoleB().equals( supplyCustomer ) ) {
                                System.out.println("ORG:: Agent " + agentAddress +
                                        " found supply customer " + orgb + " with address " + orgb.getClusterPG().getMessageAddress() );
                                customers.add( organization ) ;
                            }
                        }
                    }
                    else if ( orgb.getClusterPG().getMessageAddress().equals( agentAddress ) &&
                              orga.getClusterPG().getMessageAddress().equals( organization.getMessageAddress() )) {
                        if ( r.getRoleA().equals( supplyProvider ) ) {
                            System.out.println("ORG:: Agent " + agentAddress + " found supply provider " + orga + " with address " + orga.getClusterPG().getMessageAddress() );
                            providers.add( organization ) ;
                        }
                        else if ( r.getRoleA().equals( supplyCustomer ) ) {
                            System.out.println("ORG:: Agent " + agentAddress + " found supply customer " + orga + " with address " + orga.getClusterPG().getMessageAddress() );
                            customers.add( organization ) ;
                        }
                    }
                }
            }
        }

    }

    public static void processSuperiorAndSubordinateOrganizations( MessageAddress agentAddress,
                                             Collection addedCollection,
                                             ArrayList superiorOrgs,
                                             ArrayList subordinateOrgs,
                                             ArrayList selfOrgs )
    {
        Iterator iter = addedCollection.iterator();
        Asset self = null ;

        // Find self entitiy.
        while (iter.hasNext()) {
            Organization organization = (Organization) iter.next();
            RelationshipSchedule schedule = organization.getRelationshipSchedule() ;
            ArrayList list = new ArrayList( schedule ) ;
            for ( Iterator iter2 = list.iterator(); iter2.hasNext(); ) {
                Relationship r = ( Relationship ) iter2.next() ;
                Role selfRole = Role.getRole("Self" ) ;
                if ( r.getRoleA().equals( selfRole ) || r.getRoleB().equals( selfRole ) ) {
                    self = ( Asset ) r.getA() ;
                    System.out.println( "Agent " + agentAddress + " found self Asset " + self );
                    selfOrgs.add( selfOrgs ) ;
                }
            }
        }

        iter = addedCollection.iterator() ;
        Role subordinateRole = Role.getRole( "AdministrativeSubordinate" ) ;
        Role superiorRole = Role.getRole( "AdministrativeSuperior" );
        Role selfRole = Role.getRole("Self" ) ;

        while (iter.hasNext()) {
            Organization organization = (Organization) iter.next();
            // OrganizationPG orgPG = organization.getOrganizationPG();

            RelationshipSchedule schedule = organization.getRelationshipSchedule() ;
            ArrayList list = new ArrayList( schedule ) ;
            for ( Iterator iter2 = list.iterator(); iter2.hasNext(); ) {
                Relationship r = ( Relationship ) iter2.next() ;
                if ( r.getRoleA().equals( selfRole ) || r.getRoleB().equals( selfRole ) ) {
                    continue ;
                }

                HasRelationships a = r.getA(), b = r.getB() ;
                if ( a instanceof Asset && b instanceof Asset ) {
                    Asset orga = ( Asset ) a ;
                    Asset orgb = ( Asset ) b ;
                    if ( orga.getClusterPG().getMessageAddress().equals( agentAddress ) &&
                            orgb.getClusterPG().getMessageAddress().equals(organization.getMessageAddress())) {
                        if ( r.getRoleB().equals( subordinateRole ) ) {
                            System.out.println("ORG:: Agent " + agentAddress + " found subordinate " + orgb + " with address " + orgb.getClusterPG().getMessageAddress() );
                            subordinateOrgs.add( organization ) ;
                        }
                        else if ( r.getRoleB().equals( superiorRole ) ) {
                            System.out.println("ORG:: Agent " + agentAddress + " found superior " + orgb + " with address " + orgb.getClusterPG().getMessageAddress() );
                            superiorOrgs.add( organization ) ;
                        }
                    }
                    else if ( orgb.getClusterPG().getMessageAddress().equals( agentAddress ) &&
                              orga.getClusterPG().getMessageAddress().equals( organization.getMessageAddress() )) {
                        if ( r.getRoleA().equals( subordinateRole ) ) {
                            System.out.println("ORG:: Agent " + agentAddress + " found subordinate " + orga + " with address " + orga.getClusterPG().getMessageAddress() );
                            subordinateOrgs.add( organization ) ;
                        }
                        else if ( r.getRoleA().equals( superiorRole ) ) {
                            System.out.println("ORG:: Agent " + agentAddress + " found superior " + orga + " with address " + orga.getClusterPG().getMessageAddress() );
                            superiorOrgs.add( organization ) ;
                        }
                    }
                }
            }
        }
    }
}
