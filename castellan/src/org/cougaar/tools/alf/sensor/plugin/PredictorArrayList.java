

package org.cougaar.tools.alf.sensor.plugin;

import java.util.ArrayList;

public class PredictorArrayList extends ArrayList implements java.io.Serializable {

    public PredictorArrayList(String name){
      super();
      this.name = name;
    }

    public PredictorArrayList(int capacity){
      super(capacity);
    }

    public String name;
}