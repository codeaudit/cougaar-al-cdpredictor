

package org.cougaar.tools.alf.sensor.plugin;

import java.util.Hashtable;

public class PredictorHashtable extends Hashtable implements java.io.Serializable {

    public PredictorHashtable(){
      super();
    }

    public PredictorHashtable(int capacity){
      super(capacity);
    }

}