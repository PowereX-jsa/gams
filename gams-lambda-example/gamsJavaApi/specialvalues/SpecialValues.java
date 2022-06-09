package com.gams.examples.specialvalues;

import java.io.File;

import com.gams.api.GAMSDatabase;
import com.gams.api.GAMSJob;
import com.gams.api.GAMSOptions;
import com.gams.api.GAMSWorkspace;
import com.gams.api.GAMSWorkspaceInfo;

/**
 * This example shows how special values of the programming language (e.g. infinity) 
 * percolate down to GAMS. Infinity and NaN (not a number) are well defined. The GAMS 
 * Undefined and EPS need special considerations.
 */
public class SpecialValues {

    public static void main(String[] args) throws Exception  {
          GAMSWorkspaceInfo  wsInfo  = new GAMSWorkspaceInfo();
          if (args.length > 0)
              wsInfo.setSystemDirectory( args[0] );
          File workingDirectory = new File(System.getProperty("user.dir"), "sp");              
          workingDirectory.mkdir();
          wsInfo.setWorkingDirectory(workingDirectory.getAbsolutePath());
          GAMSWorkspace ws = new GAMSWorkspace( wsInfo );
 
          GAMSDatabase dbIn = ws.addDatabase("myDB");
          dbIn.addParameter("javaUndef", 0).addRecord().setValue( 1.0E300 );
          dbIn.addParameter("javaNA", 0).addRecord().setValue( Double.NaN );
          dbIn.addParameter("javaPInf", 0).addRecord().setValue( Double.POSITIVE_INFINITY );
          dbIn.addParameter("javaMInf", 0).addRecord().setValue( Double.NEGATIVE_INFINITY );
          dbIn.addParameter("javaEps", 0).addRecord().setValue( 4.94066E-324 );

          GAMSJob gj = ws.addJobFromString( model );
          
          GAMSOptions opt = ws.addOptions();
          opt.defines("gdxincname", dbIn.getName());

          gj.run(opt, dbIn);

          GAMSDatabase dbOut = gj.OutDB();
          double GUndef = dbOut.getParameter("GUndef").getFirstRecord().getValue();
          if (GUndef != 1.0E300 )
              throw new Exception("GUndef not as expected: " + GUndef);
          double GNA = dbOut.getParameter("GNA").getFirstRecord().getValue();
          if (!Double.isNaN(GNA))
               throw new Exception("GNA not as expected: " + GNA);
          double GPInf = dbOut.getParameter("GPInf").getFirstRecord().getValue();
          if (GPInf != Double.POSITIVE_INFINITY)
              throw new Exception("GPInf not as expected: " + GPInf);
          double GMInf = dbOut.getParameter("GMInf").getFirstRecord().getValue();
          if (GMInf != Double.NEGATIVE_INFINITY)
             throw new Exception("GMInf not as expected: " + GMInf);
          double GEps = dbOut.getParameter("GEps").getFirstRecord().getValue();
          if (GEps != 4.94066E-324)
              throw new Exception("GEps not as expected: " + GEps);
              
          System.exit(0);   
   }

   static String model =
            "Scalar GUndef                                      \n" +
            "       GNA    / NA    /                            \n" +
            "       GPInf  / +Inf  /                            \n" +
            "       GMInf  / -Inf  /                            \n" +
            "       GEps   / eps   /                            \n" +
            "       javaUndef                                   \n" +
            "       javaNA                                      \n" +
            "       javaPInf                                    \n" +
            "       javaMInf                                    \n" +
            "       javaEps ;                                   \n" +
            "                                                   \n" +
            "$onUndf                                            \n" +
            "$gdxIn %gdxincname%                                \n" +
            "$load javaUndef javaNA javaPInf javaMInf javaEps   \n" + 
            "$gdxIn                                             \n" +
            "                                                   \n" +
            "GUndef = 1/0;                                      \n" +
            "ExecError = 0;                                     \n" +
            "                                                   \n" +
            "abort$(GUndef <> javaUndef) 'javaUndef not as expected', GUndef, javaUndef;\n" +
            "abort$(GNA    <> javaNA   ) 'javaNA    not as expected', GNA,    javaNA;   \n" +
            "abort$(GPInf  <> javaPInf ) 'javaPInf  not as expected', GPInf,  javaPInf; \n" +
            "abort$(GMInf  <> javaMInf ) 'javaMInf  not as expected', GMInf,  javaMInf; \n" +
            "abort$(GEps   <> javaEps  ) 'javaEps   not as expected', GEps,   javaEps   \n"; 
}
 
