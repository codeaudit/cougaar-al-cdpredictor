

package org.cougaar.tools.alf.sensor.plugin;

import java.util.ArrayList;

public class PredictorArrayList extends ArrayList implements java.io.Serializable {

    public PredictorArrayList(String name){
      super();
      this.name = name;
    }

    public PredictorArrayList(String name, int capacity){
      super(capacity);
      this.name = name;
    }

   public String getName() {       //change
    return name;
  }

   public final int hashCode() {      //change
    return name.hashCode();
  }

  public final boolean equals(Object o) {       //change
    return (o == this) ||
      (o instanceof PredictorArrayList &&
    ((PredictorArrayList)o).getName().equals(this.name));
  }

    public String name;
}