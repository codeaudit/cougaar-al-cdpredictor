package org.cougaar.tools.castellan.ldm;

import org.cougaar.core.domain.*;
import org.cougaar.core.blackboard.*;
import org.cougaar.core.agent.*;

import java.util.*;

public class CastellanDomain2 extends DomainAdapter {
    public static final String CASTELLAN_DOMAIN = "castellan".intern();

    public CastellanDomain2() {
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

    protected void loadFactory() {
        DomainBindingSite bindingSite = (DomainBindingSite) getBindingSite();

        if (bindingSite == null) {
            throw new RuntimeException("Binding site for the Castellan domain has not be set.\n" +
                    "Unable to initialize CSMART domain Factory without a binding site.");
        }

        setFactory(new CastellanFactory(bindingSite.getClusterServesLogicProvider().getLDM()));
    }

    protected void loadXPlan() {
        DomainBindingSite bindingSite = (DomainBindingSite) getBindingSite();

        if (bindingSite == null) {
            throw new RuntimeException("Binding site for the CSMART domain has not be set.\n" +
                    "Unable to initialize Castellan domain XPlan without a binding site.");
        }

        Collection xPlans = bindingSite.getXPlans();
        XPlanServesBlackboard logPlan = null;

        for (Iterator iterator = xPlans.iterator() ; iterator.hasNext() ;) {
            XPlanServesBlackboard xPlan = (XPlanServesBlackboard) iterator.next();
            if (xPlan instanceof LogPlan) {
                // Note that this means there are 2 paths to the plan.
                // Is this okay?
                logPlan = xPlan;
                break;
            }
        }

        if (logPlan == null) {
            logPlan = new LogPlan();
        }

        setXPlan(logPlan);
    }

    protected void loadLPs() {
        DomainBindingSite bindingSite = (DomainBindingSite) getBindingSite();

        if (bindingSite == null) {
            throw new RuntimeException("Binding site for the Castellan domain has not be set.\n" +
                    "Unable to initialize domain LPs without a binding site.");
        }

        ClusterServesLogicProvider cluster =
                bindingSite.getClusterServesLogicProvider();

        LogPlan logPlan = (LogPlan) getXPlan();

        addLogicProvider( new PlanLogLP( ( LogPlanServesLogicProvider ) logPlan, cluster ) ) ;
    }

}
