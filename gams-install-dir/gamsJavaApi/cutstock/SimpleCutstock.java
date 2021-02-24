package com.gams.examples.cutstock;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.gams.api.GAMSException;
import com.gams.api.GAMSParameterRecord;
import com.gams.api.GAMSWorkspace;
import com.gams.api.GAMSWorkspaceInfo;

/**
 * This example implements a column generation approach. The column 
 * generation scheme has been implemented in GAMS and the GAMS model 
 * with the input and output data has been wrapped in a class that 
 * separates all interaction with GAMS from the driving application. 
 */ 
public class SimpleCutstock {

     public static void main(String[] args) {
         // check workspace info from command line arguments
         GAMSWorkspaceInfo  wsInfo  = new GAMSWorkspaceInfo();
         if (args.length > 0)
             wsInfo.setSystemDirectory( args[0] );
         // create a directory
         File workingDirectory = new File(System.getProperty("user.dir"), "SimpleCutstock");
         workingDirectory.mkdir();
         wsInfo.setWorkingDirectory(workingDirectory.getAbsolutePath());
         // create a workspace
         GAMSWorkspace ws = new GAMSWorkspace( wsInfo );

         CutstockModel cs = new CutstockModel(ws);

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

         cs.getRawWidth().addRecord().setValue( r );
         for(String i : d.keySet())
             cs.getWidths().addRecord(i);
         
         for(Entry<String, Double> t : d.entrySet())
             cs.getDemand().addRecord( t.getKey() ).setValue( t.getValue().doubleValue() );
         
         for(Entry<String, Double> t : w.entrySet())
             cs.getWidth().addRecord( t.getKey() ).setValue( t.getValue().doubleValue() );

         cs.getOpt().setAllModelTypes( "cplex" );

         try {
             cs.run( System.out );
             for(GAMSParameterRecord rep : cs.getPatRep())
                 System.out.println(rep.getKey(0) + ", pattern " + rep.getKey(1) + ": " + rep.getValue());
         }
         catch (GAMSException e) {
             System.out.println("Problem in GAMS: " + e.getMessage());
             return;
         }
         catch (Exception e) {
             System.out.println("System Exception: " + e.getMessage());
             return;
         }
     }
}
