package org.cougaar.tools.castellan.ldm;

import org.cougaar.core.domain.*;
import org.cougaar.core.blackboard.*;
import org.cougaar.core.agent.*;
import org.cougaar.planning.ldm.*;
import org.cougaar.core.mts.*;
import org.cougaar.core.service.*;
import org.cougaar.core.component.*;
import org.cougaar.planning.service.LDMService;

import java.util.*;


public class CastellanDomain extends DomainAdapter {
    public static final String CASTELLAN_DOMAIN = "castellan".intern();

    public CastellanDomain() {
        super() ;
    }

    public Factory getFactory(LDMServesPlugin ldm) {
        return new CastellanFactory(ldm);
    }

    public String getDomainName() {
        return CASTELLAN_DOMAIN;
    }

    public void initialize() {
        super.initialize();
    }

    public void load() {
        super.load();
    }

    protected void loadLPs() {

    }

    protected void loadFactory() {

    }
     protected void loadXPlan() {

     }
  /*  protected void loadFactory() {

        DomainBindingSite bindingSite = (DomainBindingSite) getBindingSite();

        if (bindingSite == null) {
            throw new RuntimeException("Binding site for the Castellan domain has not be set.\n" +
                    "Unable to initialize CSMART domain Factory without a binding site.");
        }

        ldms = (LDMService) bindingSite.getServiceBroker().getService(this, LDMService.class,
                new ServiceRevokedListener() {
                    public void serviceRevoked(ServiceRevokedEvent re) {
                        if(LDMService.class.equals(re.getService()))
                            ldms = null;
                    }
                    });

        setFactory(new CastellanFactory(ldms.getLDM()));

        bindingSite.getServiceBroker().releaseService(this, LDMService.class, ldms);
    }

    protected void loadXPlan() {

        DomainBindingSite bindingSite = (DomainBindingSite) getBindingSite();
        if (bindingSite == null) {
            throw new RuntimeException("Binding site for the CSMART domain has not be set.\n" +
                    "Unable to initialize Castellan domain XPlan without a binding site.");
        }
        Collection xPlans = bindingSite.getXPlans();
        //XPlanServesBlackboard logPlan = null;
       //LogPlan logPlan = null;
        RootPlan rootPlan = null;

        for (Iterator iterator = xPlans.iterator() ; iterator.hasNext() ;) {
            //XPlanServesBlackboard xPlan = (XPlanServesBlackboard) iterator.next();
            XPlan xPlan = (XPlan) iterator.next();
            if (xPlan instanceof RootPlan) {
                // Note that this means there are 2 paths to the plan.
                // Is this okay?
                rootPlan = (RootPlan) xPlan;
                break;
            }
        }

        if (rootPlan == null) {
            return;
        }


        setXPlan(rootPlan);
    }

    protected void loadLPs() {
        DomainBindingSite bindingSite = (DomainBindingSite) getBindingSite();

        if (bindingSite == null) {
            throw new RuntimeException("Binding site for the Castellan domain has not be set.\n" +
                    "Unable to initialize domain LPs without a binding site.");
        }

       /*  ClusterServesLogicProvider cluster =
               bindingSite.getClusterServesLogicProvider(); */

   /*     ais = (AgentIdentificationService) bindingSite.getServiceBroker().getService(this, AgentIdentificationService.class,
                new ServiceRevokedListener() {
                    public void serviceRevoked(ServiceRevokedEvent re) {
                        if(AgentIdentificationService.class.equals(re.getService()))
                            ais = null;
                    }
                    });

        MessageAddress self = ais.getMessageAddress();

        bindingSite.getServiceBroker().releaseService(this, AgentIdentificationService.class, ais);

        //LogPlan logPlan = (LogPlan) getXPlan();

        RootPlan rootPlan = (RootPlan) getXPlan();


        //PlanningFactory ldmf = (PlanningFactory) bindingSite.getFactoryForDomain("castellan");


        addLogicProvider(new PlanEventLogLP(rootPlan, self));
    }   */

    AgentIdentificationService ais;
    LDMService ldms;
}
