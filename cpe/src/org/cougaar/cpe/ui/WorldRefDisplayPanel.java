package org.cougaar.cpe.ui;

import org.cougaar.cpe.agents.plugin.WorldStateReference;

import java.awt.*;

/**
 * User: wpeng
 * Date: May 31, 2003
 * Time: 6:00:17 PM
 */
public class WorldRefDisplayPanel extends WorldDisplayPanel {

    public WorldRefDisplayPanel(WorldStateReference ref) {
        this.ref = ref;
    }

    public WorldStateReference getWorldStateRefence() {
        return ref;
    }

    public synchronized void paint(Graphics g) {
        if ( getWorldState() != ref.getState() ) {
            setWorldState( ref.getState() ) ;
        }
        super.paint(g);
    }

    WorldStateReference ref ;
}
