package com.gams.examples.clad;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.gams.api.GAMSException;
import com.gams.api.GAMSJob;
import com.gams.api.GAMSOptions;
import com.gams.api.GAMSWorkspace;
import com.gams.api.GAMSWorkspaceInfo;

/**
 * This example demonstrates how to implement a complex termination criterion 
 * for a difficult MIP using GAMS/Cplex. We would like to achieve a globally 
 * optimal solution (relative gap 0%) but if solution time becomes larger than 
 * n1 seconds, we can compromise for a 10% gap, and if this is not achieved 
 * after n2 seconds, we compromise for a 20% gap, and again if this is not 
 * accomplished in n3 seconds we take whatever the solver has done so far and 
 * terminate the solve. This is implemented by executing GAMSJob.run in an 
 * independent thread and  providing new tolerances for the relative gap in 
 * the main thread by supplying new GAMS/Cplex option files and triggers the 
 * processing of the new tolerance option by GAMS/Cplex through the 
 * GAMSJob.interrupt method.
 */
public class Clad {

    public static void main(String[] args) {
        GAMSWorkspaceInfo  wsInfo  = new GAMSWorkspaceInfo();
        if (args.length > 0)
            wsInfo.setSystemDirectory( args[0] );

        File workingDirectory = new File(System.getProperty("user.dir"), "Clad");
        workingDirectory.mkdir();
        wsInfo.setWorkingDirectory(workingDirectory.getAbsolutePath());

        GAMSWorkspace ws = new GAMSWorkspace(wsInfo);

        List<Map.Entry<Long,String>> steps = new ArrayList<Map.Entry<Long,String>>();
        steps.add( new AbstractMap.SimpleEntry<Long, String>( new Long(10), "epgap 0.1" ) );
        steps.add( new AbstractMap.SimpleEntry<Long, String>( new Long(20), "epgap 0.2" ));
        steps.add( new AbstractMap.SimpleEntry<Long, String>( new Long(30), "epagap 1e9" ));

        GAMSJob job = ws.addJobFromGamsLib("clad");

        try {
            File logFile = new File(ws.workingDirectory(), "cplex.opt");
            BufferedWriter outfile = new BufferedWriter(new FileWriter(logFile));
            outfile.write("interactive 1");       outfile.newLine();
            outfile.write("iafile cplex.op2");    outfile.newLine();
            outfile.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } 

        GAMSOptions opt = ws.addOptions();
        opt.setMIP( "cplex" );
        opt.setOptFile( 1 );
        opt.setSolveLink(  GAMSOptions.ESolveLink.LoadLibrary );

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);

        Worker w  = new Worker(job, opt, ps);
        w.start();

        long prevStep = 0;
        for (Map.Entry<Long, String> entry : steps) {
            long diffInSeconds = entry.getKey().longValue() - prevStep ;
            try { 
                w.join( diffInSeconds * 1000 ); 
            } catch( Exception e ) {  break;  }

            prevStep = entry.getKey().longValue();
            try {
                File logFile = new File(ws.workingDirectory(), "cplex.op2");
                BufferedWriter outfile = new BufferedWriter(new FileWriter(logFile));
                outfile.write( entry.getValue() );       outfile.newLine();
                outfile.close();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            } 
            job.interrupt();
        }
        if (w.isAlive()) {
            try {
                w.join();
            } catch (InterruptedException e) { }
        }

        String log = os.toString();
        if (!log.contains("MIP status (113): aborted")) {
            System.out.println("Expected the solver to be interrupted at least once.");
            System.exit(1);
        }
        System.exit(0);
   }
   
   /** A worker thread to run a GAMSJob. */
   static class Worker extends Thread {
        GAMSJob job;
        PrintStream output;
        GAMSOptions option;

        /** Worker constructor
        *  @param   jb   a job to run
        *  @param   opt  GAMS options to run a job
        *  @param   out  Stream to capture GAMS log
        */
        public Worker(GAMSJob jb, GAMSOptions opt, PrintStream out) { 
            job = jb; 
            option = opt; 
            output = out;  
        }

       /** Run a job.
        * @throws GAMSException when a worker could not run the job successfully. */ 
        public void run() throws GAMSException { 
            job.run(option, output);
        }
   }
}
