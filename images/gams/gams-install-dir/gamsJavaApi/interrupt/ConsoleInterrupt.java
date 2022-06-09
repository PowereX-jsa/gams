package com.gams.examples.interrupt;

import java.io.File;
import java.io.PrintStream;

import com.gams.api.GAMSJob;
import com.gams.api.GAMSOptions;
import com.gams.api.GAMSWorkspace;
import com.gams.api.GAMSWorkspaceInfo;

/**
 * This example demonstrates how to interrupt a solve in a running job but 
 * then continues with the execution of the remaining GAMS job. The example 
 * can be run in two modes; in the interactive mode ('interactive' string 
 * must be passed as a command line argument) it waits for a user to send
 * an interrupt signal to the job e.g. by pressing Ctrl-C and in the non-
 * interactive mode (no 'interactive' string passed as a command line 
 * argument) it will send a signal to interrupt the job after 2 seconds
 */ 
public class ConsoleInterrupt {

    public static void main(String[] args) {
        GAMSWorkspaceInfo  wsInfo  = new GAMSWorkspaceInfo();
        boolean interactive = false;
        if (args.length > 0) {
            wsInfo.setSystemDirectory( args[0] );
            if (args.length > 1) {
               interactive = args[1].equals("interactive")  ? true : false;
            }
        }

        File workingDirectory = new File(System.getProperty("user.dir"), "ConsoleInterrupt");
        workingDirectory.mkdir();
        wsInfo.setWorkingDirectory(workingDirectory.getAbsolutePath());
        GAMSWorkspace ws = new GAMSWorkspace(wsInfo);
        
        //Use a MIP that needs some time to solve
        final GAMSJob job = ws.addJobFromGamsLib("circpack");
        final GAMSOptions opt = ws.addOptions();
        opt.setAllModelTypes("scip");

        // start thread asynchronously 
        final Worker w  = new Worker(job, opt, System.out);
        w.start();

        if (interactive) {
            // register actions to be performed at termination, including user interrupt signal 
            Runtime.getRuntime().addShutdownHook(new Thread() { 
                @Override 
                public void run() { 
                   // Send interrupt signal to running GAMSJob only when job is not yet terminated
                   boolean terminated = (w.getState()==Thread.State.TERMINATED);
                   boolean interrupted = false;
                   if (!terminated) 
                      interrupted = job.interrupt();
                   System.out.println("*** job: " + job.getJobName() + " finished in interactive mode "
                                                  + (terminated ? "without interruption : " : " with interruption : " )  
                                                  + interrupted 
                                     );
                }
            }); 
        } else {
            // interrupts the job after 2 seconds
            try { 
                Thread.currentThread();
                Thread.sleep(2000); }
            catch ( Exception e ) { 
                e.printStackTrace(); 
                System.exit(-1);
            }
            // Send interrupt signal to running GAMSJob only when job is not yet terminated
            boolean terminated = (w.getState()==Thread.State.TERMINATED);
            boolean interrupted = false;
            if (!terminated) 
               interrupted = job.interrupt();
            System.out.println("*** job: " + job.getJobName()+" finished in non-interactive mode "
                                           + (interrupted ? "with " : "without ")
                                           + "interrut signal sent."
                              );
        } 
    }

    /** A worker thread to run a GAMSJob. */
    static class Worker extends Thread {
       GAMSJob job;
       GAMSOptions option;
       PrintStream output;

       /** Worker constructor
        *  @param   jb   a job to run
        *  @param   opt  GAMS options to run a job
        *  @param   out  Stream to capture GAMS log
        */
       public Worker(GAMSJob jb, GAMSOptions opt, PrintStream out) { job = jb; option = opt; output = out; }

       /** Run a job. It terminates if a worker could not run the job successfully. */ 
       @Override
       public void run() { 
           try {
               job.run(option, output);
           } catch(Exception e) { 
               e.printStackTrace(); 
               System.exit(-1);
           }
       }
       
    }
}
