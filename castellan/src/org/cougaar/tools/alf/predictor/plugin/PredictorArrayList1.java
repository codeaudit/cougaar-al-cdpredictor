

package org.cougaar.tools.alf.predictor.plugin;

import java.util.ArrayList;
import org.cougaar.core.util.UID;
import org.cougaar.core.service.UIDService;
import org.cougaar.core.service.DomainService;

public class PredictorArrayList1 extends ArrayList implements java.io.Serializable {

		private UID uid;

    public PredictorArrayList1(){
      super();
    }

    public PredictorArrayList1(int capacity){
      super(capacity);
    }

		public int hashCode() {
			//System.out.println("PredictorArrayList1 UID HashCode "+uid.hashCode());
		  return uid.hashCode();
		}

		public boolean equals(Object o) {
			if (o == this) return true;
			if  (!(o instanceof PredictorArrayList1)) return false;
			return this.uid.equals(((PredictorArrayList1) o).uid);
		}

		public void setUID(UID uid)	{
			this.uid = uid;
		}

	  public UID getUID(){
			return uid;
		}

}