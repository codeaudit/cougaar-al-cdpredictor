
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


package org.cougaar.tools.predictor.plugin;

public class CustomerRoleKey implements java.io.Serializable {

	private String role = null;
	private String customer = null;

	public CustomerRoleKey(String customer, String role){
		this.role = role;
		this.customer = customer;
	}

	public boolean equals(Object o) {
		if(o == this) return true;
		if(!(o instanceof CustomerRoleKey)) return false;
		else{
			CustomerRoleKey otherKey = (CustomerRoleKey) o;
			if(otherKey.role.equals(this.role) && otherKey.customer.equals(this.customer)) return true;
		}
		return false;
	}

	public int hashCode() {
		int hash = 17;
    hash = 37 * hash + role.hashCode();
		hash = 37 * hash + customer.hashCode();
    return hash;
	}

	public String getCustomerName(){
		return customer;
	}

	public String getRoleName(){
		return role;
	}
	
}
