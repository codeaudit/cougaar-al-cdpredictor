

package org.cougaar.tools.alf.predictor.plugin;

import java.util.ArrayList;
import java.util.Collection;

public class PredictorArrayList implements java.io.Serializable {

	private ArrayList list;
	private String name;

   public PredictorArrayList(String name){
     // super();
      this.name = name;
    }

    public PredictorArrayList(String name, int capacity){
    //  super(capacity);
      this.name = name;
    }

	public PredictorArrayList(ArrayList list){
     // super();
      this.list = list;
    }

   public String getName() {       //change
    return name;
  }

   //public final int hashCode() {      //change
   // return name.hashCode();
 // }

 /* public final boolean equals(Object o) {       //change
    return (o == this) ||
      (o instanceof PredictorArrayList &&
    ((PredictorArrayList)o).getName().equals(this.name));
  }*/

	public ArrayList getList(){
		return list;
	}

	public void add(Object o){
		this.list.add(o);
	}

	public void addAll(Collection c){
		this.list.addAll(c);
	}
}