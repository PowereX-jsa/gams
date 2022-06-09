package com.gams.examples.benders;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.gams.api.GAMSCheckpoint;
import com.gams.api.GAMSException;
import com.gams.api.GAMSGlobals;
import com.gams.api.GAMSJob;
import com.gams.api.GAMSModelInstance;
import com.gams.api.GAMSModifier;
import com.gams.api.GAMSOptions;
import com.gams.api.GAMSParameter;
import com.gams.api.GAMSSetRecord;
import com.gams.api.GAMSVariable;
import com.gams.api.GAMSVariableRecord;
import com.gams.api.GAMSWorkspace;
import com.gams.api.GAMSWorkspaceInfo;

/**
 * This example demonstrates a parallel implementation of a simple 
 * Benders decomposition method for a stochastic linear program. 
 * The underlying model implements a simple distribution system with 
 * stochastic demand data. The parallel version extends the 
 * \ref Benders2Stage example by solving the independent subproblems 
 * in parallel. For that we need to instantiate a separate 
 * GAMSModelInstance for each  parallel worker. We use the efficient 
 * GAMSModelInstance.copyModelInstance method to accomplish this in 
 * the most effective way. The number of demand scenarios can be larger 
 * than the number of parallel workers. The distribution of work is handles 
 * through a work queue. The parallel execution of the subproblems is done 
 * in separate threads (the MT in the name of the example stands for Multi 
 * Threading), so there is very little overhead from disk activity.
 */
@SuppressWarnings("unchecked")
public class Benders2StageMT {

    public static void main(String[] args) {
        GAMSWorkspaceInfo  wsInfo  = new GAMSWorkspaceInfo();
        if (args.length > 0)
           wsInfo.setSystemDirectory( args[0] );

        File workingDirectory = new File(System.getProperty("user.dir"), "Benders2StageMT");
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
        
        
        int numThreads = 4;
        GAMSModelInstance[] subi = new GAMSModelInstance[numThreads];
        LinkedList<Tuple> demQueue = new LinkedList<Tuple>();
        
        subi[0] = cpSub.addModelInstance();
        GAMSParameter received = subi[0].SyncDB().addParameter("received", 1, "units received from first stage solution");
        GAMSParameter demand = subi[0].SyncDB().addParameter("demand", 1, "stochastic demand");
        subi[0].instantiate("subproblem max zsub using lp", opt, new GAMSModifier[] { new GAMSModifier(received), new GAMSModifier(demand) } );

        for (int i = 1; i < numThreads; i++) 
        {
            subi[i] = subi[0].copyModelInstance();
        }
            
        opt.dispose();
        
        double lowerbound = Double.NEGATIVE_INFINITY, upperbound = Double.POSITIVE_INFINITY, objmaster = Double.POSITIVE_INFINITY;
        int iter = 1;
        do {
            System.out.println("Iteration: " + iter);
            // Solve master
            if (iter == 1)  // fix theta for first iteration
                thetaFix.addRecord().setValue( 0.0 );
            else
                thetaFix.clear();

            masteri.solve(GAMSModelInstance.SymbolUpdateType.BASECASE);
            System.out.println(" Master " + masteri.getModelStatus() + " : obj=" + masteri.SyncDB().getVariable("zmaster").getFirstRecord().getLevel());
            
            if (iter > 1)
                upperbound = masteri.SyncDB().getVariable("zmaster").getFirstRecord().getLevel();
            
            objmaster = masteri.SyncDB().getVariable("zmaster").getFirstRecord().getLevel() - theta.getFirstRecord().getLevel();
            
            for (GAMSSetRecord s : dataJob.OutDB().getSet("s"))  
            {
                Map<String,Double> demDict = new HashMap<String, Double>();
                for (GAMSSetRecord j : dataJob.OutDB().getSet("j"))  {
                    String[] keys = new String[] { s.getKey(0), j.getKey(0) };
                    demDict.put( j.getKey(0), new Double( scenarioData.findRecord( keys ).getValue()) );
                }
               String item1 = s.getKey(0);
               Double item2 = new Double( scenarioData.findRecord(new String[] { s.getKey(0), "prob" } ).getValue() );
               Tuple items = new Tuple( item1, item2, demDict );
               demQueue.add(items);
            }
            
            for (int i = 0; i < numThreads; i++) 
                subi[i].SyncDB().getParameter("received").clear();

            for (GAMSVariableRecord r : masteri.SyncDB().getVariable("received")) 
            {
                cutcoeff.addRecord(new String[] {Integer.toString(iter), r.getKey(0)});
                for (int i = 0; i < numThreads; i++) 
                {
                   subi[i].SyncDB().getParameter("received").addRecord(r.getKeys()).setValue( r.getLevel() );
                }
            }

            cutconst.addRecord(Integer.toString(iter));
            double objsubsum = 0.0;
            
            // solve multiple model instances in parallel
            Object queueMutex = new Object();
            Object ioMutex = new Object();
            Wrapper<Double>[] objsub = new Wrapper[numThreads];
            Object[] coef = new Object[numThreads];
            Wrapper<Double>[] cons = new Wrapper[numThreads] ;

            for (int i = 0; i < numThreads; i++) 
            {
                objsub[i] = new Wrapper<Double>(new Double(0.0));
                cons[i]   = new Wrapper<Double>(new Double(0.0));
                coef[i]   =  new HashMap<String, Double>();
                for (GAMSSetRecord j : dataJob.OutDB().getSet("j")) {
                    Map<String, Double> cmap = (Map<String, Double>) coef[i];
                    cmap.put( j.getKey(0), new Double(0.0) );
                }
            }

            // Solve of sub problems in parallel
            Scenario[] sc = new Scenario[numThreads];
            for (int i = 0; i < numThreads; i++)
            {
                sc[i] = new Scenario( i, subi[i], cons[i], (Map<String, Double>) coef[i], demQueue, objsub[i], queueMutex, 	ioMutex ); 
                sc[i].start();
            }
            

            // Synchronize all sub problems
            for (int i = 0; i < numThreads; i++)
            {
               try {
                    sc[i].join();
                } catch (InterruptedException e) {
                        e.printStackTrace();
                }
            }
            
            for (int i = 0; i < numThreads; i++) 
            {
                objsubsum += objsub[i].get().doubleValue(); 
                double new_consValue = cutconst.findRecord( Integer.toString(iter) ).getValue() + cons[i].get().doubleValue();
                cutconst.findRecord( Integer.toString(iter) ).setValue( new_consValue ); 
               
                for (GAMSSetRecord j : dataJob.OutDB().getSet("j")) 
                {
                    Map<String, Double> map = (Map<String, Double>) coef[i];
                    String[] keys = new String[] { Integer.toString(iter), j.getKey(0) };
                    double newvalue = cutcoeff.findRecord( keys ).getValue( ) + map.get( j.getKey(0) ).doubleValue();
                    cutcoeff.findRecord( keys ).setValue( newvalue );  
                }
            }

            lowerbound = Math.max(lowerbound, objmaster + objsubsum);
 
            iter++;
            if (iter == maxiter + 1)
                throw new GAMSException("Benders out of iterations");

            System.out.println(" lowerbound: " + lowerbound + " upperbound: " + upperbound + " objmaster: " + objmaster);
            
        } while ((upperbound - lowerbound) >= 0.001 * (1 + Math.abs(upperbound)));

        masteri.dispose();

        for(GAMSModelInstance inst : subi) 
        {
            inst.dispose();
        }
    }

    /** A scenario thread to instantiate a separate GAMSModelInstance for each parallel worker. */
    static class Scenario extends Thread {
        int _i;
        GAMSModelInstance _submi;
        Wrapper<Double> _cutconst;
        List<Tuple> _demQueue;
        Map<String, Double> _cutcoeff;
        Wrapper<Double> _objsub;
        Object _queueMutex;
        Object _ioMutex;

       /** Scenario constructor
        * @param i           scenario number
        * @param submi       a GAMS model instance
        * @param cutconst    a wrapper object of cut constant 
        * @param cutcoeff    a cut coefficient Map
        * @param demQueue    a linear collection of dictionay data elements
        * @param objsub      objective function value of subproblem 
        * @param queueMutex  a queue synchronization object
        * @param ioMutex     an io synchronization object
        */   
        public Scenario(int i , GAMSModelInstance submi, Wrapper<Double> cutconst, Map<String, Double> cutcoeff, LinkedList<Tuple> demQueue,  
           Wrapper<Double> objsub, Object queueMutex, Object ioMutex) {
           _i = i;
           _submi = submi;
           _cutconst = cutconst;
           _cutcoeff = cutcoeff;
           _demQueue = demQueue;
           _objsub = objsub;
           _queueMutex = queueMutex;
           _ioMutex = ioMutex;
        }
 
       /** Run a job */ 
        public void run() {
            while (true)
            {
               Tuple demDict;
                
                synchronized (_queueMutex)
                {
                    if (_demQueue.size() == 0)
                        break;
                    else 
                        demDict = _demQueue.remove(0);  // dequeue
                }

                _submi.SyncDB().getParameter("demand").clear();
                for (Entry<String, Double> kv : demDict.getItem3().entrySet())
                    _submi.SyncDB().getParameter("demand").addRecord(kv.getKey()).setValue( kv.getValue() );

                _submi.solve(GAMSModelInstance.SymbolUpdateType.BASECASE);
                
                
                synchronized (_ioMutex) 
                {
                    System.out.println(" Thread "+_i+" : Sub " + _submi.getModelStatus().toString() + " : obj=" + _submi.SyncDB().getVariable("zsub").getFirstRecord().getLevel());
                }
                
                double probability = demDict.getItem2().doubleValue();
                double new_objsubValue = _objsub.get().doubleValue() + probability * _submi.SyncDB().getVariable("zsub").getFirstRecord().getLevel();
                _objsub.set( new Double( new_objsubValue ) );
                
                for (Entry<String, Double> kv : demDict.getItem3().entrySet())
                {
                    double new_custconstValue = _cutconst.get().doubleValue() + probability * _submi.SyncDB().getEquation("market").findRecord( kv.getKey() ).getMarginal() * kv.getValue().doubleValue();  
                    _cutconst.set( new Double( new_custconstValue ) ); 
                    double new_cutcoeffValue = _cutcoeff.get( kv.getKey() ).doubleValue() + probability * _submi.SyncDB().getEquation("selling").findRecord(kv.getKey()).getMarginal();
                    _cutcoeff.put( kv.getKey(), new_cutcoeffValue );
                }
            }
        }
    }

    static String data  = "Sets                                                          \n"+
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
            "capacity(i)    unit capacity at factories                       \n"+
            "prodcost       unit production cost                             \n"+
            "transcost(i,j) unit transportation cost                         \n"+
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
            "                          sum(j, cutcoeff(iter,j)*received(j)); \n"+
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

/** A wrapper class for T */  
class Wrapper<T> {
     T _value; 
     public Wrapper(T value) {  _value = value;  }
     public T get() { return _value; }
     public void set(T anotherValue) { _value = anotherValue;  }
}

/** A wrapper class for a 3-Tuple dictionary data element where the first element 
 * is String data, the second element is a Double data, and the third element is 
 * a Map data that maps String to Double. */  
class Tuple {
    String _item1;
    Double _item2;
    Map<String, Double> _item3;
    public Tuple(String item1, Double item2, Map<String, Double> item3) {
        _item1 = item1;
        _item2 = item2;
        _item3 = item3;
    }
    public String getItem1() { return _item1; }
    public Double getItem2() { return _item2; }
    public Map<String, Double> getItem3() { return _item3; }
}
