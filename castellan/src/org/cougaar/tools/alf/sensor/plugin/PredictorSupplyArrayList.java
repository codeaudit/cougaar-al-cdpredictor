

package org.cougaar.tools.predictor.plugin;

import java.util.ArrayList;
import java.util.Collection;

public class PredictorSupplyArrayList extends ArrayList implements java.io.Serializable,java.lang.Cloneable {

    public PredictorSupplyArrayList(){
      super();
    }

    public PredictorSupplyArrayList(int capacity){
      super(capacity);
    }
    public PredictorSupplyArrayList(Collection capacity1){
        super(capacity1);
    }

	public Object clone() {
		return super.clone();
	}

    ArrayList capacity1;
}