

package org.cougaar.tools.alf.predictor.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class PredictorSupplyArrayList implements java.io.Serializable {

		private ArrayList list;

    public PredictorSupplyArrayList(ArrayList list){
      this.list = list;
    }
  //  public PredictorSupplyArrayList(int capacity){
  //    super(capacity);
  //  }

  //  public PredictorSupplyArrayList(Collection capacity1){
  //      super(capacity1);
  //  }

	//	public Object clone() {
	//		return super.clone();
	//	}

    //ArrayList capacity1;
	public ArrayList getList(){
			return list;
		}

	public void add(HashMap map){
		this.list.add(map);
	}

	public void addAll(Collection c){
		this.list.addAll(c);
	}
}