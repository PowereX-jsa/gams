package com.gams.examples.cutstock;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.gams.api.GAMSCheckpoint;
import com.gams.api.GAMSDatabase;
import com.gams.api.GAMSEquationRecord;
import com.gams.api.GAMSJob;
import com.gams.api.GAMSModelInstance;
import com.gams.api.GAMSModifier;
import com.gams.api.GAMSOptions;
import com.gams.api.GAMSParameter;
import com.gams.api.GAMSParameterRecord;
import com.gams.api.GAMSSet;
import com.gams.api.GAMSSetRecord;
import com.gams.api.GAMSVariableRecord;
import com.gams.api.GAMSWorkspace;
import com.gams.api.GAMSWorkspaceInfo;

/**
 * This example implements a column generation approach. The column 
 * generation approach has been entirely implemented in the program 
 * using GAMSJob for the master and GAMSModelInstance for the pricing 
 * problem. GAMS is used to build the master and pricing problems. 
 * The logic of the column generation method is in the application 
 * program.
 */ 
public class Cutstock {

    public static void main(String[] args) {
        // check workspace info from command line arguments
        GAMSWorkspaceInfo  wsInfo  = new GAMSWorkspaceInfo();
        if (args.length > 0)
            wsInfo.setSystemDirectory( args[0] );
        // create a directory
        File workingDirectory = new File(System.getProperty("user.dir"), "Cutstock");
        workingDirectory.mkdir();
        wsInfo.setWorkingDirectory(workingDirectory.getAbsolutePath());
        // create a workspace
        GAMSWorkspace ws = new GAMSWorkspace(wsInfo);

        // instantiate GAMSOptions and define parameters
        GAMSOptions opt = ws.addOptions();
        GAMSDatabase cutstockData = ws.addDatabase("csdata"); 
        opt.setAllModelTypes("Cplex");
        opt.setOptCR( 0.0 );    // Solve to optimality
        int maxpattern = 35;
        opt.defines("pmax", String.valueOf(maxpattern));
        opt.defines("solveMasterAs", "RMIP");
        
        // define input data       
        Map<String, Double> d = new HashMap<String, Double>();
        {
            d.put( "i1", Double.valueOf(97) );
            d.put( "i2", Double.valueOf(610) );
            d.put( "i3", Double.valueOf(395) );
            d.put( "i4", Double.valueOf(211)  ); 
        }
        Map<String, Double> w = new HashMap<String, Double>();
        {
            w.put( "i1", Double.valueOf(45) );
            w.put( "i2", Double.valueOf(36) );
            w.put( "i3", Double.valueOf(31) );
            w.put( "i4", Double.valueOf(14) );

        } 
        int r = 100; // raw width

        GAMSSet widths = cutstockData.addSet("i", 1, "widths");
        GAMSParameter rawWidth = cutstockData.addParameter("r", 0, "raw width");
        GAMSParameter demand = cutstockData.addParameter("d", 1, "demand");
        GAMSParameter width = cutstockData.addParameter("w", 1, "width");	

        rawWidth.addRecord().setValue( 100 );
        for (String i : d.keySet())
        widths.addRecord(i);
        for (Entry<String, Double> e : d.entrySet())
            demand.addRecord( e.getKey() ).setValue( e.getValue() );
        for (Entry<String, Double> e : w.entrySet())
            width.addRecord( e.getKey() ).setValue( e.getValue() );

        // create initial checkpoint
        GAMSCheckpoint masterCP = ws.addCheckpoint();
        GAMSJob masterInitJob = ws.addJobFromString(masterModel);
        masterInitJob.run(opt, masterCP, cutstockData);

        GAMSJob masterJob = ws.addJobFromString("execute_load 'csdata', aip, pp; solve master min z using %solveMasterAs%;", masterCP);

        GAMSSet pattern = cutstockData.addSet("pp", 1, "pattern index");
        GAMSParameter patternData = cutstockData.addParameter("aip", 2, "pattern data");
        
        // Initial pattern: pattern i hold width i
        int patternCount = 0;
        for (Entry<String, Double> e : w.entrySet()) {
            String[] keys = new String[] { e.getKey(), pattern.addRecord( new Integer(++patternCount).toString() ).getKey(0) };
            patternData.addRecord(keys).setValue( (int)(r / e.getValue()) );
        }

        // create model instance for sub job
        GAMSCheckpoint subCP = ws.addCheckpoint();
        ws.addJobFromString(subModel).run(opt, subCP, cutstockData);

        GAMSModelInstance subMI = subCP.addModelInstance();

        // define modifier demdual
        GAMSParameter demandDual = subMI.SyncDB().addParameter("demdual", 1, "dual of demand from master");
        subMI.instantiate("pricing min z using mip", opt, new GAMSModifier(demandDual));

        // find new pattern
        boolean patternAdded = true;
        while (patternAdded) 
        {
            masterJob.run(opt, masterCP, cutstockData);
            // Copy duals into gmssubMI.SyncDB DB
            demandDual.clear();
            for(GAMSEquationRecord dem : masterJob.OutDB().getEquation("demand"))
                demandDual.addRecord( dem.getKey(0) ).setValue( dem.getMarginal() );

            subMI.solve();

            if (subMI.SyncDB().getVariable("z").findRecord().getLevel() < -0.00001) {
                if (patternCount == maxpattern) {
                    System.out.println("Out of pattern.  Increase maxpattern (currently ["+maxpattern+"])");
                    patternAdded = false;
                }
                else {
                    System.out.println("New pattern Value: " + subMI.SyncDB().getVariable("z").findRecord().getLevel());
                    GAMSSetRecord s = pattern.addRecord( new Integer(++patternCount).toString() );
                    for (GAMSVariableRecord y : subMI.SyncDB().getVariable("y")) {
                        if (y.getLevel() > 0.5) {
                            String[] keys = new String[] { y.getKey(0), s.getKey(0) };	
                            patternData.addRecord( keys ).setValue( Math.round(y.getLevel()) );
                        }
                    }
                }
            } else {
                patternAdded = false;
            }
        }

        // solve final MIP
        opt.defines("solveMasterAs", "MIP");
        masterJob.run(opt, cutstockData);

        System.out.println("Optimal Solution: "+masterJob.OutDB().getVariable("z").findRecord().getLevel());

        for (GAMSVariableRecord xp : masterJob.OutDB().getVariable("xp"))  {
            if (xp.getLevel() > 0.5) {
                System.out.println("  pattern ["+xp.getKey(0)+"] ["+xp.getLevel()+"] times: ");
                GAMSParameter param =  masterJob.OutDB().getParameter("aip"); 
                @SuppressWarnings("unused")
                GAMSParameterRecord aip = param.getFirstRecord(new String[] { " ", xp.getKey(0) });
                Iterator<GAMSParameterRecord> it = param.iterator();
                while (it.hasNext()) {
                    GAMSParameterRecord rec = it.next();
                    System.out.println(" ["+rec.getKey(0)+"] : ["+ rec.getValue()+"]");	
                } 
                System.out.println();
            }
        }

        // clean up of unmanaged resources
        cutstockData.dispose();
        subMI.dispose();
        opt.dispose();

    }

    static String masterModel = 
        "$Title Cutting Stock - Master problem               \n" +
        "                                                    \n" +
        "Set  i    widths                                    \n" +
        "Parameter                                           \n" +
        " w(i) width                                         \n" +
        " d(i) demand                                        \n" +
        "Scalar                                              \n" +
        " r    raw width;                                    \n" +
        "$gdxin csdata                                       \n" +
        "$load i w d r                                       \n" +
        "                                                    \n" +
        "$if not set pmax $set pmax 1000                     \n" +
        "Set  p        possible patterns  /1*%pmax%/         \n" +
        "     pp(p)    dynamic subset of p                   \n" +
        "Parameter                                           \n" +
        " aip(i,p) number of width i in pattern growing in p;\n" +
        "                                                    \n" +
        "* Master model                                      \n" + 
        "Variable xp(p)     patterns used                    \n" +
        "         z         objective variable               \n" +
        "Integer variable xp; xp.up(p) = sum(i, d(i));       \n" +
        "                                                    \n" +
        "Equation numpat    number of patterns used          \n" +
        "     demand(i) meet demand;                         \n" +
        "                                                    \n" +
        "numpat..     z =e= sum(pp, xp(pp));                 \n" +
        "demand(i)..  sum(pp, aip(i,pp)*xp(pp)) =g= d(i);    \n" + 
        "                                                    \n" +
        "model master /numpat, demand/;                      \n" +
        "                                                    \n";
        
    static String subModel = 
          "$Title Cutting Stock - Pricing problem is a knapsack model \n" +
          "                                                           \n" +
          "Set  i    widths                                           \n" +
          "Parameter                                                  \n" +
          " w(i) width;                                               \n" +
          "Scalar                                                     \n" +
          " r    raw width;                                           \n" +
          "                                                           \n" +
          "$gdxin csdata                                              \n" +
          "$load i w r                                                \n" +
          "                                                           \n" +
          "Parameter                                                  \n" +
          " demdual(i) duals of master demand constraint /#i eps/;    \n" +
          "                                                           \n" +
          "Variable  z, y(i) new pattern;                             \n" +
          "Integer variable y; y.up(i) = ceil(r/w(i));                \n" +
          "                                                           \n" +
          "Equation defobj                                            \n" +
          "         knapsack;                                         \n" +
          "                                                           \n" +
          "defobj..     z =e= 1 - sum(i, demdual(i)*y(i));            \n" +
          "knapsack..   sum(i, w(i)*y(i)) =l= r;                      \n" +
          "model pricing /defobj, knapsack/;                          \n" +
          "                                                           \n";
}

