

/*
  * <copyright>
  *  Copyright 2003 (Intelligent Automation, Inc.)
  *  under sponsorship of the Defense Advanced Research Projects
  *  Agency (DARPA).
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the Cougaar Open Source License as published by
  *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
  *
  *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
  *  PROVIDED "AS IS" WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
  *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
  *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
  *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
  *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
  *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
  *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
  *  PERFORMANCE OF THE COUGAAR SOFTWARE.
  *
  * </copyright>
  *
  */

package org.cougaar.tools.alf.predictor.plugin;

import java.util.ArrayList;
import java.util.Collection;

public class PredictorArrayList implements java.io.Serializable {

	private ArrayList list;
	private String name;

   public PredictorArrayList(String name){
      this.name = name;
    }

	public PredictorArrayList(ArrayList list){
      this.list = list;
    }

   public String getName() {
    return name;
  }


	/*public final int hashCode() {
		return name.hashCode();
  }*/

  /*public final boolean equals(Object o) {
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