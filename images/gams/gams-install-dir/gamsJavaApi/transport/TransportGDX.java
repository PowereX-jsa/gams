package com.gams.examples.transport;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import com.gams.api.GAMSDatabase;
import com.gams.api.GAMSParameter;
import com.gams.api.GAMSParameterRecord;
import com.gams.api.GAMSSet;
import com.gams.api.GAMSSetRecord;
import com.gams.api.GAMSWorkspace;
import com.gams.api.GAMSWorkspaceInfo;

/**
 * This example shows the use of the GAMSDatabase class for reading and writing GDX files
 * 
 * In particular this example demonstrates: 
 *   - How to fill a GAMSDatabase from Java data structures and export it to a GDX file
 *   - How to import a GDX file as a GAMSDatabase
 */
public class TransportGDX {

    public static void main(String[] args) {
        // check workspace info from command line arguments
        GAMSWorkspaceInfo  wsInfo  = new GAMSWorkspaceInfo();
        if (args.length > 0)
            wsInfo.setSystemDirectory( args[0] );
        // create a directory
        File workingDirectory = new File(System.getProperty("user.dir"), "TransportGDX");
        workingDirectory.mkdir();
        wsInfo.setWorkingDirectory(workingDirectory.getAbsolutePath());
        // create a workspace
        GAMSWorkspace ws = new GAMSWorkspace(wsInfo);

        // prepare input data
        List<String> plants = Arrays.asList("Seattle", "San-Diego");
        List<String> markets = Arrays.asList("New-York", "Chicago", "Topeka");
        Map<String, Double> capacity = new HashMap<String, Double>();
        {
             capacity.put("Seattle", Double.valueOf(350.0));
             capacity.put("San-Diego", Double.valueOf(600.0));
        }
        Map<String, Double> demand = new HashMap<String, Double>();
        {
             demand.put("New-York", Double.valueOf(325.0));
             demand.put("Chicago", Double.valueOf(300.0));
             demand.put("Topeka", Double.valueOf(275.0));
        }

        Map<Vector<String>, Double> distance = new HashMap<Vector<String>, Double>();
        {
            distance.put( new Vector<String>( Arrays.asList(new String[]{"Seattle", "New-York"}) ), Double.valueOf(2.5));        
            distance.put( new Vector<String>( Arrays.asList(new String[]{"Seattle", "Chicago"}) ),  Double.valueOf(1.7));
            distance.put( new Vector<String>( Arrays.asList(new String[]{"Seattle", "Topeka"}) ),  Double.valueOf(1.8));
            distance.put( new Vector<String>( Arrays.asList(new String[]{"San-Diego", "New-York"}) ), Double.valueOf(2.5));
            distance.put( new Vector<String>( Arrays.asList(new String[]{"San-Diego", "Chicago"}) ),  Double.valueOf(1.8));
            distance.put( new Vector<String>( Arrays.asList(new String[]{"San-Diego", "Topeka"}) ),   Double.valueOf(1.4));
        }

        // add database and input data into the database
        GAMSDatabase db = ws.addDatabase();

        GAMSSet i = db.addSet("i", 1, "canning plants");
        for(String p : plants)
            i.addRecord(p);

        GAMSSet j = db.addSet("j", 1, "markets");
        for(String m : markets)
            j.addRecord(m);

        GAMSParameter a = db.addParameter("a", "capacity of plant i in cases", i);
        for (String p : plants) {
           a.addRecord(p).setValue( capacity.get(p) );
        }

        GAMSParameter b = db.addParameter("b", "demand at market j in cases", j);
        for(String m : markets)
            b.addRecord(m).setValue( demand.get(m) );

        GAMSParameter d = db.addParameter("d", "distance in thousands of miles", i, j);
        for(Vector<String> vd : distance.keySet())
            d.addRecord(vd).setValue( distance.get(vd).doubleValue() );

        GAMSParameter f = db.addParameter("f", "freight in dollars per case per thousand miles");
        f.addRecord().setValue( 90 );

        // export the GAMSDatabase to a GDX file with name 'data.gdx' located in the 'workingDirectory' of the GAMSWorkspace
        db.export("data.gdx");
      
        // add a new GAMSDatabase and initialize it from the GDX file just created
        GAMSDatabase gdxdb = ws.addDatabaseFromGDX("data.gdx");

        // read symbol data from the database and fill them into Java data structures
        List<String> gdxPlants = new ArrayList<String>();
        for(GAMSSetRecord rec : gdxdb.getSet("i"))
            gdxPlants.add(rec.getKey(0));

        List<String> gdxMarkets = new ArrayList<String>();
        for(GAMSSetRecord rec :  gdxdb.getSet("j"))
            gdxMarkets.add(rec.getKey(0));

        Map<String, Double> gdxCapacity = new HashMap<String, Double>();
        for(GAMSParameterRecord rec : gdxdb.getParameter("a"))
        	gdxCapacity.put(rec.getKey(0), rec.getValue());

        Map<String, Double> gdxDemand = new HashMap<String, Double>();;
        for(GAMSParameterRecord rec : gdxdb.getParameter("b"))
        	gdxDemand.put(rec.getKey(0), rec.getValue());

        Map<Vector<String>, Double> gdxDistance = new HashMap<Vector<String>, Double>();
        for(GAMSParameterRecord rec : gdxdb.getParameter("d"))
            gdxDistance.put( new Vector<String>( Arrays.asList(new String[]{rec.getKey(0), rec.getKey(1)}) ), rec.getValue());

        double gdxFreight = gdxdb.getParameter("f").getFirstRecord().getValue();

        // print out data read from GDX file
        System.out.println("Data read from data.gdx");
        System.out.println ("Set i: " + gdxdb.getSet("i").getText() );
        for(String p : gdxPlants)
            System.out.println ("  " + p);
        
        System.out.println ("Set j: "+ gdxdb.getSet("j").getText());
        for(String m : gdxMarkets)
            System.out.println ("  " + m);
        
        System.out.println ("Parameter a: "+ gdxdb.getParameter("a").getText());
   		for (Entry<String, Double> aEntry : gdxCapacity.entrySet())
            System.out.println ("  " + aEntry.getKey() + ": " + aEntry.getValue());

   		System.out.println ("Parameter b: "+ gdxdb.getParameter("b").getText());
   		for (Entry<String, Double> bEntry : gdxDemand.entrySet())
            System.out.println ("  " + bEntry.getKey() + ": " + bEntry.getValue());

        System.out.println ("Parameter d: " + gdxdb.getParameter("d").getText());
   		for (Entry<Vector<String>, Double> dEntry : gdxDistance.entrySet())
            System.out.println ("  " + dEntry.getKey() + ": " + dEntry.getValue());

        System.out.println ("Scalar f: " + gdxdb.getParameter("f").getText());
        System.out.println ("  " + gdxFreight);
    }
}
