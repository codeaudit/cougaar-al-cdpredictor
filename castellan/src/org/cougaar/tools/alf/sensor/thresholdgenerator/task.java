package org.cougaar.tools.alf.sensor.thresholdgenerator;

import org.cougaar.tools.alf.sensor.thresholdgenerator.*;

import java.util.*;
import java.sql.*;
import java.io.*;
import java.lang.*;
import java.util.ArrayList;

public class task
{
	public task (long e, long f) {
		eventtime = e;
		finishtime = f;
	}

	public long eventtime;
	public long finishtime;
};