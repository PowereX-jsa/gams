package com.gams.examples.benders;

import java.io.File;

import com.gams.api.GAMSCheckpoint;
import com.gams.api.GAMSException;
import com.gams.api.GAMSGlobals;
import com.gams.api.GAMSJob;
import com.gams.api.GAMSModelInstance;
import com.gams.api.GAMSModifier;
import com.gams.api.GAMSOptions;
import com.gams.api.GAMSParameter;
import com.gams.api.GAMSParameterRecord;
import com.gams.api.GAMSSetRecord;
import com.gams.api.GAMSVariable;
import com.gams.api.GAMSVariableRecord;
import com.gams.api.GAMSWorkspace;
import com.gams.api.GAMSWorkspaceInfo;

/**
 * This example demonstrates a sequential implementation of a simple 
 * Benders decomposition method for a stochastic linear program. 
 * The underlying model implements a simple distribution system with 
 * stochastic demand data. The master and the subproblems are implemented 
 * with the GAMSModelInstance object which allows resolving the model with 
 * modified input without regenerating the model. 
 * A GAMSModelInstance has a fixed model rim, so this provides a challenge 
 * for Benders master problem because every iteration adds new constraints 
 * (the Benders cuts) to the  master. We get around this limitation of 
 * GAMSModelInstance by initializing the GAMSModelInstance of the master 
 * with a fixed number of empty (i.e. non-binding) placeholders constraints 
 * and during the run of the algorithm turn these placeholder constraints 
 * into valid Benders cuts. 
 */
public class Benders2Stage {

    public static void main(String[] args) {
         GAMSWorkspaceInfo  wsInfo  = new GAMSWorkspaceInfo();
         if (args.length > 0)
            wsInfo.setSystemDirectory( args[0] );

         File workingDirectory = new File(System.getProperty("user.dir"), "Benders2Stage");
         workingDirectory.mkdir();
         wsInfo.setWorkingDirectory(workingDirectory.getAbsolutePath());

         GAMSWorkspace ws = new GAMSWorkspace( wsInfo );

         GAMSJob dataJob = ws.addJobFromString(data);

         GAMSOptions optData = ws.addOptions();
         optData.defines("useBig", "1");
         optData.defines("nrScen", "100");

         dataJob.run(optData);

         optData.dispose();

         GAMSParameter scenarioData = dataJob.OutDB().getParameter("ScenarioData");

         GAMSOptions opt = ws.addOptions();
         opt.defines("datain", dataJob.OutDB().getName());
         int maxiter = 40;
         opt.defines("maxiter", Integer.toString(maxiter));
         opt.setAllModelTypes( "cplex" );

         GAMSCheckpoint cpMaster = ws.addCheckpoint();
         GAMSCheckpoint cpSub = ws.addCheckpoint();

         ws.addJobFromString(masterModel).run(opt, cpMaster, dataJob.OutDB());

         GAMSModelInstance masteri = cpMaster.addModelInstance();
         GAMSParameter cutconst = masteri.SyncDB().addParameter("cutconst", 1, "Benders optimality cut constant");
         GAMSParameter cutcoeff = masteri.SyncDB().addParameter("cutcoeff", 2, "Benders optimality coefficients");
         GAMSVariable theta = masteri.SyncDB().addVariable("theta", 0, GAMSGlobals.VarType.FREE, "Future profit function variable");
         GAMSParameter thetaFix = masteri.SyncDB().addParameter("thetaFix", 0, "");
         masteri.instantiate("masterproblem max zmaster using lp", opt, 
            new GAMSModifier[] { new GAMSModifier(cutconst), new GAMSModifier(cutcoeff), new GAMSModifier(theta,GAMSGlobals.UpdateAction.FIXED,thetaFix) }
         );

         ws.addJobFromString(subModel).run(opt, cpSub, dataJob.OutDB());
         
         GAMSModelInstance subi = cpSub.addModelInstance();
         GAMSParameter received = subi.SyncDB().addParameter("received", 1, "units received from master");
         GAMSParameter demand = subi.SyncDB().addParameter("demand", 1, "stochastic demand");
         subi.instantiate("subproblem max zsub using lp", opt, 
            new GAMSModifier[] { new GAMSModifier(received), new GAMSModifier(demand) }
         );

         opt.dispose();

         double lowerbound = Double.NEGATIVE_INFINITY, upperbound = Double.POSITIVE_INFINITY, objmaster = Double.POSITIVE_INFINITY;
         int iter = 1;
         do {
             System.out.println("Iteration: " + iter);
             // Solve master
             if (1 == iter)  // fix theta for first iteration
                 thetaFix.addRecord().setValue( 0 );
             else
                 thetaFix.clear();

             masteri.solve(GAMSModelInstance.SymbolUpdateType.BASECASE);

             System.out.println(" Master " + masteri.getModelStatus() + " : obj=" + masteri.SyncDB().getVariable("zmaster").getFirstRecord().getLevel());
             if (1 < iter)
                 upperbound = masteri.SyncDB().getVariable("zmaster").getFirstRecord().getLevel();
             objmaster = masteri.SyncDB().getVariable("zmaster").getFirstRecord().getLevel() - theta.getFirstRecord().getLevel();

             // Set received from master
             received.clear();
             for (GAMSVariableRecord r : masteri.SyncDB().getVariable("received")) {
                 received.addRecord( r.getKeys()).setValue( r.getLevel() );
                 cutcoeff.addRecord(new String[] { Integer.toString(iter), r.getKey(0) });
             }

             cutconst.addRecord(Integer.toString(iter));
             double objsub = 0.0;
             for(GAMSSetRecord s : dataJob.OutDB().getSet("s")) {
                 demand.clear();
                 for(GAMSSetRecord j : dataJob.OutDB().getSet("j")) {
                     demand.addRecord(j.getKeys()).setValue( scenarioData.findRecord(new String[] {s.getKey(0), j.getKey(0)}).getValue() );
                 }
                 
                 subi.solve(GAMSModelInstance.SymbolUpdateType.BASECASE);
                 System.out.println(" Sub " + subi.getModelStatus() + " : obj=" + subi.SyncDB().getVariable("zsub").getFirstRecord().getLevel());

                 double probability = scenarioData.findRecord(new String[] {s.getKey(0), "prob"}).getValue();
                 objsub += probability * subi.SyncDB().getVariable("zsub").getFirstRecord().getLevel();
                 for (GAMSSetRecord j : dataJob.OutDB().getSet("j")) {
                     GAMSParameterRecord record = cutconst.findRecord(Integer.toString(iter));
                     double newValue = record.getValue() + probability * subi.SyncDB().getEquation("market").findRecord(j.getKeys()).getMarginal() * demand.findRecord(j.getKeys()).getValue(); 
                     record.setValue( newValue );
                     record = cutcoeff.findRecord(new String[] {Integer.toString(iter), j.getKey(0)});
                     newValue = record.getValue() + probability * subi.SyncDB().getEquation("selling").findRecord(j.getKeys()).getMarginal();
                     record.setValue( newValue );
                 }
             }

             lowerbound = Math.max(lowerbound, objmaster + objsub);
             
             iter++;
             if (iter == maxiter + 1)
                 throw new GAMSException("Benders out of iterations");

             System.out.println(" lowerbound: " + lowerbound + " upperbound: " + upperbound + " objmaster: " + objmaster);
         } while ((upperbound - lowerbound) >= 0.001 * (1 + Math.abs(upperbound)));

         masteri.dispose();
         subi.dispose();
    }
    
    static String data  = "Sets                                                        \n"+
          "i factories                                   /f1*f3/                       \n"+
          "j distribution centers                        /d1*d5/                       \n"+
          "                                                                            \n"+
          "Parameter                                                                   \n"+
          "capacity(i) unit capacity at factories                                      \n"+
          "             /f1 500, f2 450, f3 650/                                       \n"+
          "demand(j)   unit demand at distribution centers                             \n"+
          "             /d1 160, d2 120, d3 270, d4 325, d5 700 /                      \n"+
          "prodcost    unit production cost                   /14/                     \n"+
          "price       sales price                            /24/                     \n"+
          "wastecost   cost of removal of overstocked products /4/                     \n"+
          "                                                                            \n"+
          "Table transcost(i,j) unit transportation cost                               \n"+
          "      d1    d2    d3    d4    d5                                            \n"+
          " f1   2.49  5.21  3.76  4.85  2.07                                          \n"+
          " f2   1.46  2.54  1.83  1.86  4.76                                          \n"+
          " f3   3.26  3.08  2.60  3.76  4.45;                                         \n"+
          "                                                                            \n"+
          "$ifthen not set useBig                                                      \n"+
          "Set                                                                         \n"+
          " s scenarios /lo,mid,hi/                                                    \n"+
          "                                                                            \n"+
          "Table ScenarioData(s,*) possible outcomes for demand plus probabilities     \n"+
          "     d1  d2  d3  d4  d5  prob                                               \n"+
          " lo  150 100 250 300 600 0.25                                               \n"+
          " mid 160 120 270 325 700 0.50                                               \n"+
          " hi  170 135 300 350 800 0.25;                                              \n"+
          "$else                                                                       \n"+
          "$if not set nrScen $set nrScen 10                                           \n"+
          "Set       s                 scenarios                        /s1*s%nrScen%/;\n"+
          "parameter ScenarioData(s,*) possible outcomes for demand plus probabilities;\n"+
          "option seed=1234;                                                           \n"+
          "ScenarioData(s,'prob') = 1/card(s);                                         \n"+
          "ScenarioData(s,j)      = demand(j)*uniform(0.6,1.4);                        \n"+
          "$endif                                                                      \n"+
          "                                                                            \n";

    static String masterModel = "Sets                                      \n"+
          "i factories                                                     \n"+
          "j distribution centers                                          \n"+
          "                                                                \n"+
          "Parameter                                                       \n"+
          "  capacity(i)    unit capacity at factories                     \n"+
          "  prodcost       unit production cost                           \n"+
          "  transcost(i,j) unit transportation cost                       \n"+
          "                                                                \n"+
          "$if not set datain $abort 'datain not set'                      \n"+
          "$gdxin %datain%                                                 \n"+
          "$load i j capacity prodcost transcost                           \n"+
          "                                                                \n"+
          "* Benders master problem                                        \n"+
          "$if not set maxiter $set maxiter 25                             \n"+
          "Set                                                             \n"+
          "  iter             max Benders iterations /1*%maxiter%/         \n"+
          "                                                                \n"+
          "Parameter                                                       \n"+
          "  cutconst(iter)   constants in optimality cuts                 \n"+
          "  cutcoeff(iter,j) coefficients in optimality cuts              \n"+
          "                                                                \n"+
          "Variables                                                       \n"+
          "  ship(i,j)        shipments                                    \n"+
          "  product(i)       production                                   \n"+
          "  received(j)      quantity sent to market                      \n"+
          "  zmaster          objective variable of master problem         \n"+
          "  theta            future profit                                \n"+
          "Positive Variables ship;                                        \n"+
          "                                                                \n"+
          "Equations                                                       \n"+
          "  masterobj        master objective function                    \n"+
          "  production(i)    calculate production in each factory         \n"+
          "  receive(j)       calculate quantity to be send to markets     \n"+
          "  optcut(iter)     Benders optimality cuts;                     \n"+
          "                                                                \n"+
          "masterobj..                                                     \n"+
          "  zmaster =e=  theta -sum((i,j), transcost(i,j)*ship(i,j))      \n"+
          "                     - sum(i,prodcost*product(i));              \n"+
          "                                                                \n"+
          "receive(j)..       received(j) =e= sum(i, ship(i,j));           \n"+
          "                                                                \n"+
          "production(i)..    product(i) =e= sum(j, ship(i,j));            \n"+
          "product.up(i) = capacity(i);                                    \n"+
          "                                                                \n"+
          "optcut(iter)..  theta =l= cutconst(iter) +                      \n"+
          "                       sum(j, cutcoeff(iter,j)*received(j));    \n"+
          "                                                                \n"+
          "model masterproblem /all/;                                      \n"+
          "                                                                \n"+
          "* Initialize cut to be non-binding                              \n"+
          "cutconst(iter) = 1e15;                                          \n"+
          "cutcoeff(iter,j) = eps;                                         \n"+
          "                                                                \n";

    static String subModel = "Sets                                         \n"+
            "  i factories                                                   \n"+
            "  j distribution centers                                        \n"+
            "                                                                \n"+
            "Parameter                                                       \n"+
            "  demand(j)   unit demand at distribution centers               \n"+
            "  price       sales price                                       \n"+
            "  wastecost   cost of removal of overstocked products           \n"+
            "  received(j) first stage decision units received               \n"+
            "                                                                \n"+
            "$if not set datain $abort 'datain not set'                      \n"+
            "$gdxin %datain%                                                 \n"+
            "$load i j demand price wastecost                                \n"+
            "                                                                \n"+
            "* Benders' subproblem                                           \n"+
            "                                                                \n"+
            "Variables                                                       \n"+
            "  sales(j)         sales (actually sold)                        \n"+
            "  waste(j)         overstocked products                         \n"+
            "  zsub             objective variable of sub problem            \n"+
            "Positive variables sales, waste                                 \n"+
            "                                                                \n"+
            "Equations                                                       \n"+ 
            "  subobj           subproblem objective function                \n"+
            "  selling(j)       part of received is sold                     \n"+
            "  market(j)        upperbound on sales                          \n"+
            ";                                                               \n"+
            "                                                                \n"+
            "subobj..                                                        \n"+
            "  zsub =e= sum(j, price*sales(j)) - sum(j, wastecost*waste(j)); \n"+
            "                                                                \n"+
            "selling(j)..  sales(j) + waste(j) =e= received(j);              \n"+
            "                                                                \n"+
            "market(j)..   sales(j) =l= demand(j);                           \n"+
            "                                                                \n"+
            "model subproblem /subobj,selling,market/;                       \n"+
            "                                                                \n"+
            "* Initialize received                                           \n"+
            "received(j) = demand(j);                                        \n"+
            "                                                                \n";
}
