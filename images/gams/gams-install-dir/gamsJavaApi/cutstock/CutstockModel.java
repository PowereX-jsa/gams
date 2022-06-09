package com.gams.examples.cutstock;

import java.io.PrintStream;

import com.gams.api.GAMSDatabase;
import com.gams.api.GAMSException;
import com.gams.api.GAMSJob;
import com.gams.api.GAMSOptions;
import com.gams.api.GAMSParameter;
import com.gams.api.GAMSSet;
import com.gams.api.GAMSWorkspace;

/** 
 * This example shows the wrapper model of a cutstock problem based 
 * on the simple GAMS [cutstock] model from the GAMS Model Library. 
 */ 
public class CutstockModel {
    private GAMSSet fWidths;
    private GAMSParameter fRawWidth;
    private GAMSParameter fDemand;
    private GAMSParameter fWidth;
    private GAMSParameter fPatRep;

    private GAMSWorkspace fws;
    private GAMSDatabase fCutstockData, fDbOut;
    private GAMSOptions fopt;
    private GAMSJob job;

    /** CutstockModel constructor 
     * @param ws a GAMSWorkspace where the files are located 
     */
    public CutstockModel(GAMSWorkspace ws) {
        fws = ws;
        fopt = ws.addOptions();

        fCutstockData = ws.addDatabase("gdxincname"); 

        fopt.defines("gdxincname", "gdxincname");
        fopt.setSolveLink( GAMSOptions.ESolveLink.LoadLibrary );
        fopt.defines("dbOut1", "dbOut1");

        fWidths = fCutstockData.addSet("i", "widths");
        fRawWidth = fCutstockData.addParameter("r", "raw width");
        fDemand = fCutstockData.addParameter("d", "demand", fWidths);
        fWidth = fCutstockData.addParameter("w", "width", fWidths);

        job = ws.addJobFromString( this.getModelSource() );
    }

    /** Executes the cutstock model */
    public void run() {
        this.run(null);
    }

    /** Executes the cutstock model 
     *  @param   output  Stream to capture GAMS log
     */
    public void run(PrintStream output) {
        if (!fCutstockData.checkDomains())
            throw new GAMSException("Domain Errors in Cutstock Database");
        
        job.run(fopt, null, output, false, fCutstockData);
        fDbOut = fws.addDatabaseFromGDX(fopt.getDefinitionOf("dbOut1") + ".gdx");
        fPatRep = fDbOut.getParameter("patrep");
    }

    /** get an input symbol, i : widths */
    public GAMSSet getWidths() { return fWidths; } 

    /** get an input symbol, r : raw width */
    public GAMSParameter getRawWidth() { return fRawWidth; }

    /** get an input symbol, d : demand */
    public GAMSParameter getDemand() { return fDemand; } 

    /** get an input symbol, w : width */
    public GAMSParameter getWidth() { return fWidth; } 

    /** get an output symbol, patrep : Solution pattern report */
    public GAMSParameter getPatRep() { return fPatRep; }

    /** get Options for the execution of the cutstock model */
    public GAMSOptions getOpt() { return fopt; }

    /** get the source of cutstock model */ 
    public String getModelSource() { return model; }

    static String model =
      "$Title Cutting Stock - A Column Generation Approach (CUTSTOCK,SEQ=294)     \n" +
      "                                                                           \n" +
      "$ontext                                                                    \n" +
      "The task is to cut out some paper products of different sizes from a       \n" +
      "large raw paper roll, in order to meet a customer's order. The objective   \n" +
      "is to minimize the required number of paper rolls.                         \n" +
      "                                                                           \n" +
      "P. C. Gilmore and R. E. Gomory, A linear programming approach to the       \n" +
      "cutting stock problem, Part I, Operations Research 9 (1961), 849-859.      \n" +
      "                                                                           \n" +
      "P. C. Gilmore and R. E. Gomory, A linear programming approach to the       \n" +
      "cutting stock problem, Part II, Operations Research 11 (1963), 863-888.    \n" +
      "$offtext                                                                   \n" +
      "                                                                           \n" +
      "Set  i    widths                                                           \n" +
      "Parameter                                                                  \n" +
      "    r    raw width                                                         \n" +
      "    w(i) width                                                             \n" +
      "    d(i) demand ;                                                          \n" +
      "                                                                           \n" +
      "$if not set gdxincname $abort 'no include file name for data file provided'\n" +
      "$gdxin %gdxincname%                                                        \n" +
      "$load r i w d                                                              \n" +
      "$gdxin                                                                     \n" +
      "                                                                           \n" +
      "* Gilmore-Gomory column generation algorithm                              \n" +
      "                                                                           \n" +
      "Set  p        possible patterns  /p1*p1000/                                \n" +
      "     pp(p)    dynamic subset of p                                          \n" +
      "Parameter                                                                  \n" +
      "    aip(i,p) number of width i in pattern growing in p;                    \n" +
      "                                                                           \n" +
      "* Master model                                                             \n" +
      "Variable xp(p)     patterns used                                           \n" +
      "         z         objective variable                                      \n" +
      "Integer variable xp; xp.up(p) = sum(i, d(i));                              \n" +
      "                                                                           \n" +
      "Equation numpat    number of patterns used                                 \n" +
      "         demand(i) meet demand;                                            \n" +
      "                                                                           \n" +
      "numpat..     z =e= sum(pp, xp(pp));                                        \n" +
      "demand(i)..  sum(pp, aip(i,pp)*xp(pp)) =g= d(i);                           \n" +
      "                                                                           \n" +
      "model master /numpat, demand/;                                             \n" +
      "                                                                           \n" +
      "* Pricing problem - Knapsack model                                         \n" +
      "Variable  y(i) new pattern;                                                \n" +
      "Integer variable y; y.up(i) = ceil(r/w(i));                                \n" +
      "                                                                           \n" +
      "Equation defobj                                                            \n" +
      "         knapsack knapsack constraint;                                     \n" +
      "                                                                           \n" +
      "defobj..     z =e= 1 - sum(i, demand.m(i)*y(i));                           \n" +
      "knapsack..   sum(i, w(i)*y(i)) =l= r;                                      \n" +
      "                                                                           \n" +
      "model pricing /defobj, knapsack/;                                          \n" +
      "                                                                           \n" +
      "* Initialization - the initial patterns have a single width                \n" +
      "pp(p) = ord(p)<=card(i);                                                   \n" +
      "aip(i,pp(p))$(ord(i)=ord(p)) = floor(r/w(i));                              \n" +
      "*display aip;                                                              \n" +
      "                                                                           \n" +
      "Scalar done  loop indicator /0/                                            \n" +
      "Set    pi(p) set of the last pattern; pi(p) = ord(p)=card(pp)+1;           \n" +
      "                                                                           \n" +
      "option optcr=0,limrow=0,limcol=0,solprint=off;                             \n" +
      "                                                                           \n" +
      "While(not done and card(pp)<card(p),                                       \n" +
      "    solve master using rmip minimizing z;                                  \n" +
      "    solve pricing using mip minimizing z;                                  \n" +
      "                                                                           \n" +
      "* pattern that might improve the master model found?                       \n" +
      "  if(z.l < -0.001,                                                         \n" +
      "     aip(i,pi) = round(y.l(i));                                            \n" +
      "     pp(pi) = yes; pi(p) = pi(p-1);                                        \n" +
      "  else                                                                     \n" +
      "     done = 1;                                                             \n" +
      "  );                                                                       \n" +
      ");                                                                         \n" +
      "display 'lower bound for number of rolls', master.objval;                  \n" +
      "                                                                           \n" +
      "option solprint=on;                                                        \n" +
      "solve master using mip minimizing z;                                       \n" +
      "                                                                           \n" +
      "Parameter patrep Solution pattern report                                   \n" +
      "        demrep Solution demand supply report;                              \n" +
      "                                                                           \n" +
      "patrep('# produced',p) = round(xp.l(p));                                   \n" +
      "patrep(i,p)$patrep('# produced',p) = aip(i,p);                             \n" +
      "patrep(i,'total') = sum(p, patrep(i,p));                                   \n" +
      "patrep('# produced','total') = sum(p, patrep('# produced',p));             \n" +
      "                                                                           \n" +
      "demrep(i,'produced') = sum(p,patrep(i,p)*patrep('# produced',p));          \n" +
      "demrep(i,'demand') = d(i);                                                 \n" +
      "demrep(i,'over') = demrep(i,'produced') - demrep(i,'demand');              \n" +
      "                                                                           \n" +
      "display patrep, demrep;                                                    \n" +
      "$if not set dbOut1 $abort 'no file name for out-database 1 file provided'  \n" +
      "execute_unload '%dbOut1%', patrep;                                         \n" +
      "                                                                            "; 
}
