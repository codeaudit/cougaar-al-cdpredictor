

package org.cougaar.tools.alf.sensor.plugin;

import java.util.ArrayList;
import java.util.Collection;

public class PredictorSupplyArrayList extends ArrayList implements java.io.Serializable {

    public PredictorSupplyArrayList(){
      super();
    }

    public PredictorSupplyArrayList(int capacity){
      super(capacity);
    }
    public PredictorSupplyArrayList(Collection capacity1){
        super(capacity1);
    }

    ArrayList capacity1;
}