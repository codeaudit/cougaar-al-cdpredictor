/*
  * <copyright>
  *  Copyright 2001 (Intelligent Automation, Inc.)
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
  * CHANGE RECORD
  */

package org.cougaar.tools.castellan.planlog;

public class PlanLogConstants
{
    /**
     * Boolean property, enables logging of plug-in trigger events.
     */
    public static final String LOG_PLUGIN_TRIGGERS = "LOG_PLUGIN_TRIGGERS" ;

    /**
     * Boolean property, enabls logging of epochs.
     */
    public static final String LOG_EPOCHS = "LOG_EPOCHS" ;


    /**
     * Int property
     */
    public static final String AR_LOG_LEVEL = "AR_LOG_LEVEL" ;

    /**
     * Disable logging for allocation results. No AllocationResult PDUs will be
     * sent.  Duplicating
     */
    public static final int AR_LOG_LEVEL_NONE = 0 ;

    /**
     * The success or failure of the allocation will be observed.
     * AllocationResultPDUs will be sent when changes from success/failure/null
     */
    public static final int AR_LOG_LEVEL_SUCCESS = 1 ;

    /**
     * This compares and logs the allocation results with full detail (e.g. all aspects
     * are compared and logged.)
     */
    public static final int AR_LOG_LEVEL_FULL = 2 ;

    /**
     * The state of the AR (Success/failure/null) and the flagged aspects (see AR_ASPECT_DETAIL)
     * are compared for logging.
     */
    public static final int AR_LOG_LEVEL_SELECTIVE = 3;

    /**
     * Property name indicating how much detail to log ARs at. Controls how many
     * aspects are extracted and sent. Ranges from all aspects ( FULL ), time aspects only, or
     *  none (AR_LOG_DETAIL_NONE).
     */
    public static final String AR_ASPECT_DETAIL = "AR_ASPECT_DETAIL" ;

    /**
     * Comparse no aspects.
     */
    public static final int AR_ASPECT_NONE = 1 ;

    /**
     * This compares only the time aspects and success values of the allocation results.
     */
    public static final int AR_ASPECT_TIME_ASPECT = 2 ;

    public static final int AR_ASPECT_ALL = 2;

}
