/*
* This software contains OpenCybele(TM) software and Cybele(TM) software.
* OpenCybele(TM) software and Cybele(TM) software,
* copyright © 2001-2003 Intelligent Automation, Inc.
*
* This software has been delivered with unlimited rights in accordance with
* FAR 52.227-14 (Rights in Data-General) to Raytheon Company and to NASA
* under Contract Number NAS2-00015, and Raytheon and the US Government
* are free to use, reproduce, create derivative works, display and disclose the
* software without restriction provided that this copyright notice appears in all copies.
*/

package org.cougaar.cpe.model;

import java.io.Serializable;

public abstract class CPEEvent implements Serializable
{
    protected CPEEvent(long time)
    {
        this.time = time;
    }

    long time ;
}
