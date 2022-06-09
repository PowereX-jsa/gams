package com.gams.examples.domain;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.gams.api.GAMSDatabase;
import com.gams.api.GAMSDatabaseDomainViolation;
import com.gams.api.GAMSException;
import com.gams.api.GAMSJob;
import com.gams.api.GAMSParameter;
import com.gams.api.GAMSSet;
import com.gams.api.GAMSSetRecord;
import com.gams.api.GAMSSymbolDomainViolation;
import com.gams.api.GAMSWorkspace;
import com.gams.api.GAMSWorkspaceInfo;

/**
 * The example enforces referential integrity also known in the GAMS lingo as domain 
 * checking. The Java API does a delayed domain checking of symbols. So you can add 
 * records to a domain controlled parameter (e.g. p(i)) even though the GAMSSet i 
 * does not contain the label (yet). The user can trigger an explicit check of the 
 * referential integrity by calling the GAMSDatabase.checkDomains (or 
 * GAMSSymbol.checkDomains) method. The Java API provides methods to access the records that 
 * violate the referential integrity (see GAMSDatabaseDomainViolation for details). 
 * Domain checking is implicitly done when the GAMSDatabase is exported to a GDX file 
 * via the GAMSDatabase.export method or for databases provided in the GAMSJob.run 
 * The implicit domain check can be suppressed (and left to GAMS when importing data) 
 * via the GAMSDatabase.suppressAutoDomainChecking property. This example demonstrates 
 * how to trigger domain checking and how to access the records that violate the referential 
 * integrity.
  */
public class DomainCheck {

     public static void main(String[] args) {
         // check workspace info from command line arguments
         GAMSWorkspaceInfo  wsInfo  = new GAMSWorkspaceInfo();
         if (args.length > 0)
             wsInfo.setSystemDirectory( args[0] );
         // create a directory
         File workingDirectory = new File(System.getProperty("user.dir"), "DomainCheck");
         workingDirectory.mkdir();
         wsInfo.setWorkingDirectory(workingDirectory.getAbsolutePath());
         // create a workspace
         GAMSWorkspace ws = new GAMSWorkspace( wsInfo );
         
         // define some data by using C# data structures
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
         
         // prepare a GAMSDatabase with data from the C# data structures
         GAMSDatabase db = ws.addDatabase();

         // add two sets to the GAMSDatabase
         GAMSSet i = db.addSet("i", "");
         for(String p : plants)
             i.addRecord(p);
         GAMSSet j = db.addSet("j", "");
         for(String m : markets)
             j.addRecord(m);

         // add a parameter with domain information
         GAMSParameter a = db.addParameter("a", "capacity at plant", i);
         for(String p : plants)
             a.addRecord(p).setValue( capacity.get(p).doubleValue() );
         // if we see a domain violation something went wrong
         if (!a.checkDomains())
         {
             System.out.println("***ABORT*** Unexpected domain violation in a");
             System.exit(1);
         }
         // add a parameter with relaxed domain information
         GAMSParameter b = db.addParameter("b", "demand at market j in cases", "j");
         for(String m : markets)
             b.addRecord(m).setValue( demand.get(m).doubleValue() );

         // if we see a domain violation something went wrong
         if (!b.checkDomains()) {
             System.out.println("***ABORT*** Unexpected domain violation in b");
             System.exit(1);
         }

         // add a 2-dim parameter with domain information
         Object[] domains = new Object[] { i, j };
         GAMSParameter d = db.addParameter("d", "distance in thousands of miles", domains);
         for(Vector<String> t : distance.keySet()) {
             String[] keys = new String[t.size()];
             t.toArray(keys);
             d.addRecord( keys ).setValue( distance.get(t).doubleValue() ) ;
         }

         // if we see a domain violation something went wrong
         if (!d.checkDomains()) {
             System.out.println("***ABORT*** Unexpected domain violation in d");
             System.exit(1);
         }

         // if we see a domain violation in the database something went wrong
         if (!db.checkDomains()) {
             System.out.println("***ABORT*** Unexpected domain violation in db");
             System.exit(1);
         }

         // create some "wrong" entries
         d.addRecord( new String[] { "Seattle", "aa" } ).setValue( 1 );
         d.addRecord( new String[] { "bb", "Seattle"} ).setValue( 1 );

         a.addRecord( "aa" ).setValue( 1 );
         a.addRecord( "bb" ).setValue( 1 );
         b.addRecord( "aa" ).setValue( 1 );
         b.addRecord( "bb" ).setValue( 1 );

         // now the GAMSdatabase as well as the symbols a and d should have domain violations
         if (db.checkDomains()) {
             System.out.println("***ABORT*** Domain violation for db not recognized");
             System.exit(1);
         }
         if (a.checkDomains())  {
             System.out.println("***ABORT*** Domain violation for a not recognized");
             System.exit(1);
         }

         if (d.checkDomains())  {
             System.out.println("***ABORT*** Domain violation for d not recognized");
             System.exit(1);
         }

         // b in contrast was defined with relaxed domain info only, therefore we should never see a domain violation
         if (!b.checkDomains()) {
             System.out.println("***ABORT*** Unexpected domain violation in b");
             System.exit(1);
         }

         // for a, we should see 2 domain violations ("aa" and "bb")
         int dvCnt = 0; 
         System.out.println("Symbol Domain Violations of a:");
         for(GAMSSymbolDomainViolation item : a.getSymbolDomainViolations(0)) {
             System.out.print(" > [");
             for(boolean item1 : item.getViolationArray())
                 System.out.print(item1 + " ");
             System.out.print("] <> ");
             for(String item2 : item.getRecord().getKeys())
                 System.out.print(item2 + " ");
             System.out.println("<<");
             dvCnt++;
         }
         if (dvCnt != 2)  {
             System.out.print("***ABORT*** Expected 3 domain violation records of a, but found [" + dvCnt+"]");
             System.exit(1);
         }

         // for d, we should see 3 domain violations ("Seattle", *"aa"*; *"bb"*, *"Seattle"*)
         dvCnt = 0;
         System.out.println("Symbol Domain Violations of d:");
         for(GAMSSymbolDomainViolation item : d.getSymbolDomainViolations(0)) {
             System.out.print(" > [");
             for(boolean item1 : item.getViolationArray()) {
                 System.out.print(item1 + " ");
                 if (item1)
                     dvCnt++;
             }
             System.out.print("] <> ");
             for(String item2 : item.getRecord().getKeys())
                 System.out.print(item2 + " ");
             System.out.println("<<");
         }
         if (dvCnt != 3) {
             System.out.println("***ABORT*** Expected 3 domain violation records of d, but found [" + dvCnt+"]");
             System.exit(1);
         }

         // for db, we should see 5 domain violations (all the ones from a and d)
         dvCnt = 0;
         System.out.println("Database Domain Violations of db without maximum limit of record numbers :");
         for(GAMSDatabaseDomainViolation item : db.getDatabaseDomainViolations(0,0)) {
             for(GAMSSymbolDomainViolation info : item.getSymbolDomainViolations()) {
                 System.out.print(" > " + item.getSymbol().getName() + ": [");
                for(boolean element : info.getViolationArray()) {  
                 System.out.print(element + " ");
                 if (element)
                     dvCnt++;
                }
                System.out.print("] <> ");
                for(String key : info.getRecord().getKeys()) 
                     System.out.print(key + " ");
                System.out.println(" <<");
             }
         }
         if (dvCnt != 5) {
             System.out.println("***ABORT*** Expected 5 domain violation records of db, but found [" + dvCnt+"]");
             System.exit(1);
         }

         // now we limit the amount of violated records reported to a total of 3
         dvCnt = 0;
         System.out.println("Database Domain Violations of db with no more than 3 violation records :");
         for(GAMSDatabaseDomainViolation item : db.getDatabaseDomainViolations(3,0)) {
             for(GAMSSymbolDomainViolation info : item.getSymbolDomainViolations()) {
                System.out.print(" > " + item.getSymbol().getName() + ": [");
                for(boolean element : info.getViolationArray()) {  
                    System.out.print(element + " ");
                    if (element)
                        dvCnt++;
                }
                System.out.print("] <> ");
                for(String key : info.getRecord().getKeys()) 
                     System.out.print(key + " ");
                System.out.println(" <<");
             }
         }
         if (dvCnt != 3) {
             System.out.println("***ABORT*** Expected 3 domain violation records of db, but found [" + dvCnt+"]");
             System.exit(1);
         }

         // now we limit the amount of violated records reported to 1 per symbol
         dvCnt = 0;
         System.out.println("Database Domain Violations of db with no more than 1 violation record per 1 symbol :");
         for(GAMSDatabaseDomainViolation item : db.getDatabaseDomainViolations(0, 1)) {
             for(GAMSSymbolDomainViolation info : item.getSymbolDomainViolations()) {
                 System.out.print(" > " + item.getSymbol().getName() + ": [");
                for(boolean element : info.getViolationArray()) {  
                    System.out.print(element + " ");
                }
                System.out.print("] <> ");
                for(String key : info.getRecord().getKeys()) 
                     System.out.print(key + " ");
                System.out.println(" <<");
             }
             dvCnt++;
         }
         if (dvCnt != 2) {
             System.out.println("***ABORT*** Expected 2 domain violation records of a, but found [" + dvCnt+"]");
             System.exit(1);
         }

         // by default we should get an exception when exporting a GAMSDatabase with domain violations
         boolean sawException = false;
         try {
             db.export("test.gdx");
         } catch(Exception e) {
             sawException = true;
             db.suppressAutoDomainChecking(true);
             db.export("test.gdx");
         }
         if(!sawException) {
             System.out.println("***ABORT*** It should not be possible to export a GAMSDatabase containing domain violations by default");
             System.exit(1);
         }

         // read a parameter with domain info from gdx
         GAMSDatabase db2 = ws.addDatabaseFromGDX("test.gdx");
         GAMSParameter d2 = db2.getParameter("d");

         // the domain of the parameter should be GAMSSet i and GAMSSet j
         for(Object item : d2.getDomains()) {
             if (item instanceof GAMSSet) {
                 if (((GAMSSet)item).getName().equals("i")) {
                     for(GAMSSetRecord uel : (GAMSSet)item)
                         if (!plants.contains(uel.getKey(0))) {
                             System.out.println("***ABORT*** Unexpected uel " + uel.getKey(0) + " found in domain i");
                             System.exit(1);
                         }
                 }
                 else if (((GAMSSet)item).getName().equals("j")) {
                     for(GAMSSetRecord uel : (GAMSSet)item)
                         if (!markets.contains(uel.getKey(0))) {
                             System.out.println("***ABORT*** Unexpected uel " + uel.getKey(0) + " found in domain j");
                             System.exit(1);
                         }
                 }
                 else
                 {
                     System.out.println("***ABORT*** Expected GAMSSet i and j but found " + ((GAMSSet)item).getName());
                     System.exit(1);
                 }
             }
             else
             {
                 System.out.println("***ABORT*** Expected GAMSSet as domain but found relaxed domain " + (String)item);
                 System.exit(1);
             }
         }

        /* *************************************************************** *
         * This next section is acutally not about domain checking, but we * 
         * make sure that certain things are working as expected.          *
         * *************************************************************** */

         // Try reading an Alias as Set
         GAMSJob aliasJob = ws.addJobFromString(aliasData);
         aliasJob.run();
         GAMSSet ii = aliasJob.OutDB().getSet("ii");
         System.out.println("Elements of aliased Set:");
         for(GAMSSetRecord item : ii)
              System.out.println(" > " + item.getKey(0));

         GAMSDatabase testDB = ws.addDatabase();
         GAMSSet testSet = testDB.addSet("test", 1);

         // Try adding empty UEL
         testSet.addRecord("");
         System.out.println("Elements of test Set after adding empty UEL:");
         System.out.println(" > " + testSet.getNumberOfRecords());

         // GAMS strips pending blanks while leading blanks are relevant
         testSet.addRecord(" a ").setText("a");
         System.out.println("Record ' a ' should be the same as ' a':");
         System.out.println(" > " + testSet.findRecord(" a").getText());

         // GAMS cannot handle UELs with more than 63 characters
         // This should be OK ...
         testSet.addRecord("123456789012345678901234567890123456789012345678901234567890123 ").setText("OK");
         // ... but not this
         try {
             testSet.addRecord("1234567890123456789012345678901234567890123456789012345678901234").setText("not OK");
             System.out.println("*** It should not be possible to add a record with more than 63 characters");
             System.exit(1);
         } catch(GAMSException e) { }

         // GAMS cannot handle explanatory texts with more than 255 characters
         testDB.addSet("textOK", "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345");
         try {
             testDB.addSet("textNotOK", "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456");
             System.out.println("*** It should not be possible to add an explanatory text with more than 255 characters");
             System.exit(1);
         } catch(GAMSException e) { }

         testSet.addRecord("OK").setText("123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345");
         try {
             testSet.addRecord("notOK").setText("1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456");
             System.out.println("*** It should not be possible to add an explanatory text with more than 255 characters");
             System.exit(1);
         } catch(GAMSException e) { }

         // GAMS can handle UELs containing single and double quotes but not at the same time
         testSet.addRecord("quote'");
         testSet.addRecord("quote\"");
         try {
             testSet.addRecord("quote'\"");
             System.out.println("*** It should not be possible to add a record single AND double quote");
             System.exit(1);
         } catch(GAMSException e) { }

         testDB.export("test.gdx");

         System.out.println("successfully terminated!");
         System.exit(0);
     }

     static String aliasData = 
         "Sets                                             \n"+
         "    i   canning plants   / seattle, san-diego /; \n"+
         "                                                 \n"+ 
         "Alias (i,ii);                                    \n";

}