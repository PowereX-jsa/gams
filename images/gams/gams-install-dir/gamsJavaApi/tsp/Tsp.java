package com.gams.examples.tsp;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gams.api.GAMSCheckpoint;
import com.gams.api.GAMSDatabase;
import com.gams.api.GAMSGlobals;
import com.gams.api.GAMSJob;
import com.gams.api.GAMSModelInstance;
import com.gams.api.GAMSModifier;
import com.gams.api.GAMSOptions;
import com.gams.api.GAMSParameter;
import com.gams.api.GAMSSet;
import com.gams.api.GAMSSetRecord;
import com.gams.api.GAMSWorkspace;
import com.gams.api.GAMSWorkspaceInfo;

/**
 * This example demonstrates how to use a GAMSModelInstance to implement the subtour 
 * elimination algorithm for the Traveling Salesman Problem (TSP) problem. Similar
 * to \ref GAMS_APIS_EXAMPLES_Benders2Stage "Benders2Stage example", we have a placeholder 
 * for the subtour elimination constraint that gets generated in each iteration of the 
 * algorithm. In contrast to the Benders example, here we regenerate the GAMSModelInstance 
 * if the original number of placeholders was not big enough. We continue this process until 
 * all subtours are eliminated.
 */ 
public class Tsp {

    public static void main(String[] args)  {

        GAMSWorkspaceInfo wsInfo = new GAMSWorkspaceInfo();
        // check if system directory has been passed as an argument
        if (args.length > 0)
            wsInfo.setSystemDirectory(args[0]);

        // create a directory
        File workingDirectory = new File(System.getProperty("user.dir"), "Tsp");
        workingDirectory.mkdir();

        wsInfo.setWorkingDirectory(workingDirectory.getAbsolutePath());

        // create a workspace
        GAMSWorkspace ws = new GAMSWorkspace(wsInfo);

        // number of cuts that can be added to a model instance
        int cutsPerRound = 10;
        // current cut
        int curCut = 0;
        // cut limit for current model instance (cmax = curCut + cutsPerRound)
        int cMax = 0;

        // database used to collect all generated cuts
        GAMSDatabase cutData =  ws.addDatabase();
        GAMSSet cc = cutData.addSet("cc", 1, "");
        GAMSParameter acut = cutData.addParameter("acut", 3, "");
        GAMSParameter rhscut = cutData.addParameter("rhscut", 1, "");

        // list of cities (i1, i2, i3, ...)
        List<String> n = new ArrayList<String>();

        GAMSCheckpoint cp = null;
        GAMSModelInstance mi = null;
        GAMSParameter miAcut = null;
        GAMSParameter miRhscut = null;
        List<String> subTour = null;


        do {
            // create a new model instance when the cut limit is reached
            if (curCut >= cMax) {
                System.out.print(",");
                cMax = curCut + cutsPerRound;
                cutData.export();

                // create the checkpoint
                GAMSJob tspJob = ws.addJobFromString(model);
                cp = ws.addCheckpoint();
                GAMSOptions opt = ws.addOptions();
                opt.defines("nrcities", "20");
                opt.defines("cmax", Integer.toString(cMax - 1));
                opt.defines("cutdata", cutData.getName());


                // read input data from "tsp.gdx"
                String gamsdir = ws.systemDirectory();
                if (!gamsdir.endsWith(GAMSGlobals.FILE_SEPARATOR))
                    gamsdir += GAMSGlobals.FILE_SEPARATOR;
                File datafile = new File(gamsdir + "apifiles" + GAMSGlobals.FILE_SEPARATOR
                                                 + "Data" + GAMSGlobals.FILE_SEPARATOR
                                                 + "tsp.gdx");
                opt.defines("tspdata", "\"" + datafile.getAbsolutePath() + "\"");
                tspJob.run(opt, cp);

                // fill the n list only once
                if (n.size() == 0)
                    for(GAMSSetRecord i : tspJob.OutDB().getSet("i"))
                        n.add(i.getKey(0));

                // instantiate the model instance with modifiers miAcut and miRhscut
                mi = cp.addModelInstance();
                miAcut = mi.SyncDB().addParameter("acut", 3, "");
                miRhscut = mi.SyncDB().addParameter("rhscut", 1, "");
                GAMSModifier[] modifiers = { new GAMSModifier(miAcut), new GAMSModifier(miRhscut) };
                mi.instantiate("assign use mip min z", modifiers);
            }
            else {
                System.out.print(".");
            }
            // solve model instance using update type accumulate and clear acut and rhscut afterwards
            mi.solve(GAMSModelInstance.SymbolUpdateType.ACCUMULATE);
            mi.SyncDB().getParameter("acut").clear();
            mi.SyncDB().getParameter("rhscut").clear();

            // collect graph information from the solution
            Map<String, String> graph = new HashMap<String, String>();

            List <String> notVisited = new ArrayList<String>(n);
            for(String i : n) {
                for(String j : n) {
                    String[] keys = { i, j };
                    if (mi.SyncDB().getVariable("x").findRecord( keys ).getLevel() > 0.5)
                        graph.put(i, j);
                }
            }


            // find all subtours and add the required cuts by modifying acut and rhscut
            while (notVisited.size() != 0)  {
                String ii = notVisited.get(0);
                subTour = new ArrayList<String>();
                subTour.add(ii);
                while (graph.get(ii) != notVisited.get(0))
                {
                    ii = graph.get(ii);
                    subTour.add(ii);
                }

                final List<String> source = subTour;
                IPredicate<String> doesNotContain = new IPredicate<String>() {
                    public boolean apply(String element) {
                        return !source.contains(element);
                    }
                };
                notVisited = (List<String>) filter(subTour, doesNotContain);

                // add the cuts to both databases (cutData, mi.SyncDB)
                for(String i : subTour) {
                    for(String j : subTour) {
                        String[] keys = { "c"+curCut, i, j };
                        acut.addRecord(keys).setValue( 1 );
                        miAcut.addRecord( keys ).setValue( 1 );
                    }
                }

                String key = "c"+curCut;
                rhscut.addRecord( key ).setValue( subTour.size()-0.5 );
                miRhscut.addRecord( key ).setValue( subTour.size()-0.5 );
                cc.addRecord( key );
                curCut += 1;
            }

        }
        while(subTour.size() < n.size());

        System.out.println();
        System.out.println("z=" + mi.SyncDB().getVariable("z").getFirstRecord().getLevel());
        System.out.println("sub_tour: ");
        for(String i : subTour)
            System.out.print(i + " -> ");
        System.out.println( subTour.get(0) );

    }

    interface IPredicate<T> { boolean apply(T type); }
    static <T> Collection<T> filter(Collection<T> target, IPredicate<T> predicate) {
        Collection<T> result = new ArrayList<T>();
        for (T element : target) {
            if (predicate.apply(element)) {
                result.add(element);
            }
        }
        return result;
    }

    static String model =
        "$Title Traveling Salesman Problem                               \n" +
        "$Ontext                                                         \n" +
        "                                                                \n" +
        "The sub_tour elimination constraints are generated by a Python  \n" +
        "script. The MIP is solved over and over, but GAMS have to       \n" +
        "generate the model only after n cuts have been added.           \n" +
        "                                                                \n" +
        "$Offtext                                                        \n" +
        "                                                                \n" +
    	"$if not set tspdata $abort 'tspdata not set'                    \n" +
        "                                                                \n" +
    	"set ii    cities                                                \n" +
    	"    i(ii) subset of cities                                      \n" +
    	"alias (ii,jj),(i,j,k);                                          \n" +
        "                                                                \n" +
    	"parameter c(ii,jj) distance matrix;                             \n" +
        "                                                                \n" +
    	"$gdxin %tspdata%                                                \n" +
    	"$load ii c                                                      \n" +
        "                                                                \n" +
    	"$if not set nrCities $set nrCities 20                           \n" +
    	"i(ii)$(ord(ii) < %nrCities%) = yes;                             \n" +
        "                                                                \n" +
    	"variables x(ii,jj)  decision variables - leg of trip            \n" +
    	"          z         objective variable;                         \n" +
    	"binary variable x; x.fx(ii,ii) = 0;                             \n" +
        "                                                                \n" +
    	"equations objective   total cost                                \n" +
    	"          rowsum(ii)  leave each city only once                 \n" +
    	"          colsum(jj)  arrive at each city only once;            \n" +
        "                                                                \n" +
    	"* the assignment problem is a relaxation of the TSP             \n" +
    	"objective.. z =e= sum((i,j), c(i,j)*x(i,j));                    \n" +
    	"rowsum(i).. sum(j, x(i,j)) =e= 1;                               \n" +
    	"colsum(j).. sum(i, x(i,j)) =e= 1;                               \n" +
        "                                                                \n" +
    	"$if not set cmax $set cmax 2                                    \n" +
    	"set cut /c0*c%cmax%/;                                           \n" +
    	"parameter                                                       \n" +
    	"    acut(cut,ii,jj) cut constraint matrix                       \n" +
    	"    rhscut(cut)  cut constraint rhs;                            \n" +
        "                                                                \n" +
    	"equation sscut(cut) sub_tour elimination cut;                   \n" +
    	"sscut(cut).. sum((i,j), Acut(cut,i,j)*x(i,j)) =l= RHScut(cut);  \n" +
        "                                                                \n" +
    	"set cc(cut) previous cuts; cc(cut) = no;                        \n" +
    	"$if set cutdata execute_load '%cutdata%', cc, Acut, RHScut;     \n" +
        "                                                                \n" +
    	"Acut(cut,i,j)$(not cc(cut)) = eps;                              \n" +
    	"RHScut(cut)$(not cc(cut)) = card(ii);                           \n" +
        "                                                                \n" +
    	"model assign /all/;                                             \n" +
        "                                                                \n" +
    	"option optcr=0;                                                 \n";

}
