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
package org.cougaar.tools.castellan.server.ui;

import att.grappa.Graph;
import att.grappa.Parser;
import att.grappa.Grappa;

import java.io.*;

import org.cougaar.tools.castellan.server.ServerApp;

public class GraphLayout {

    /**
     * Borrowed from CSMART. A pecialized Reader to read the output from the Grappa dot.exe process
     * Add a <code>Grappa.NEW_LINE</code> at the end of every line,
     * except the last line.
     *
     * @see BufferedReader
     */
    private static class GrappaDotReader extends BufferedReader {
        boolean firstChar = true;

        public GrappaDotReader(Reader in) {
            super(in);
        }
        public GrappaDotReader(Reader in, int sz) throws IllegalArgumentException {
            super(in, sz);
        }

        public int read() throws IOException {
            int c = super.read();
            if (firstChar) {
                firstChar = false;
                //	System.out.println("GrappaDotReader read first char at: " +
                //			   System.currentTimeMillis()/1000);
            }
            //      if (c == -1)
            //	System.out.println("GrappaDotReader read last char at: " +
            //			   System.currentTimeMillis()/1000);
            return c;
        }

        public String readLine() throws IOException {
            //      System.out.println("CSMARTGraph: GrappDotReader: readLine()");
            String ans = super.readLine();
            if (ans == "}" || ans == "}\r") {
                return ans;
            } else {
                return ans + Grappa.NEW_LINE;
            }
        } // end of readLine()
    } // end of GrappaDotReader


    /** This is borrowed from CSmart's graphing routine.
     */
    public static Graph doLayout( ServerApp app, File dotFile ) {

        // This is the size of the dot file, use this
        // for initializing the Buffer for reading the dot.exe process output
        long dotFileSize = dotFile.length();

        // done with dotFile File handle

        //    System.out.println("CSMARTGraph: Got dot file of size " + dotFileSize +
        //		       " at: " + System.currentTimeMillis()/1000);

        File dotExecutable = null ;
        String command = app.getDotPath() + ' ' + dotFile;

        //    System.out.println("Starting: " + command + " at: " +
        //		       System.currentTimeMillis()/1000);
        Process proc = null;
        try {
            proc = Runtime.getRuntime().exec(command);
        } catch(Exception ex) {
            System.out.println("Exception while executing: " +
            command + " " + ex.getMessage());
            ex.printStackTrace(System.err);
            proc = null;
        }

        // get the input stream of the dot process from which
        // we read the dot-processed file
        InputStream fromFilterRaw = null;
        try {
            if (proc == null) {
                throw new Exception("proc is null");
            } else {
                fromFilterRaw = proc.getInputStream();
            }
        } catch(Exception e) {
            System.out.println("doLayout: Couldn't get from filter: " + e);
        }

        // Use a special BufferedReader which does the newlines
        // thing for us.  By using this Reader, we can avoid
        // creating the large StringBuffer altogether
        // We'll just pass this reader directly into the Parser
        BufferedReader fromFilter =
        new GrappaDotReader(new InputStreamReader(fromFilterRaw));

        // now invoke the parser on the dot-processed file
        // AMH - pass in the original BufferedReader that reads
        // directly from the dot.exe process
        // create a new graph to get the results of the layout
        Parser program = null;
        Graph newGraph = null;

        newGraph = new Graph("Layout", true, true);
        program = new Parser(fromFilter, new PrintWriter( System.err ), newGraph);

        // garbage collect before parsing
        Runtime runtime = Runtime.getRuntime();
        //    System.out.println("Free memory: " + runtime.freeMemory());
        runtime.gc();
        //    System.out.println("After gc; Free memory: " + runtime.freeMemory());
        //    System.out.println("Invoking parser at: " + System.currentTimeMillis()/1000 + "...");
        try {
            program.parse();
        } catch(Exception ex) {
            System.out.println("Parser exception: " + ex.getMessage());
            ex.printStackTrace();
        }

        Graph graph = null;

        graph = program.getGraph();

        //System.err.println("The graph contains " + graph.countOfElements(Grappa.NODE|Grappa.EDGE|Grappa.SUBGRAPH) + " elements.");

        //graph.setEditable(true);
        //graph.setMenuable(true);
        //graph.setErrorWriter(new PrintWriter(System.err,true));
        //graph.printGraph(new PrintWriter(System.out));

        //System.err.println("bbox=" + graph.getBoundingBox().getBounds().toString());
        return graph ;
    }

}
