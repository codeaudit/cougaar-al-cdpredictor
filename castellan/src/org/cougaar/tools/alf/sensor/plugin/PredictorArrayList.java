

package org.cougaar.tools.alf.sensor.plugin;

import java.util.ArrayList;

public class PredictorArrayList extends ArrayList implements java.io.Serializable {

    public PredictorArrayList(){
      super();
    }

    public PredictorArrayList(int capacity){
      super(capacity);
    }

}