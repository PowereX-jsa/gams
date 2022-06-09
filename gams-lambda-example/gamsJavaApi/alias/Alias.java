package com.gams.examples.alias;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.gams.api.GAMSDatabase;
import com.gams.api.GAMSGlobals;
import com.gams.api.GAMSJob;
import com.gams.api.GAMSParameter;
import com.gams.api.GAMSSet;
import com.gams.api.GAMSSymbol;
import com.gams.api.GAMSWorkspace;
import com.gams.api.GAMSWorkspaceInfo;

/**
 * The Object-oriented API does not have the concept of a GAMS alias. An alias 
 * cannot be entered into a GAMSDatabase by API methods. This examples demonstrates 
 * how to retrieve an element of the aliased set as a GAMSSet from a GAMSDatabase 
 * loaded from a GDX container (e.g. GAMSJob.OutDB()).
 */ 
public class Alias {

    public static void main(String[] args) throws Exception {
        // check workspace info from command line arguments
        GAMSWorkspaceInfo  wsInfo  = new GAMSWorkspaceInfo();
        if (args.length > 0)
            wsInfo.setSystemDirectory( args[0] );
        // create a directory
        File workingDirectory = new File(System.getProperty("user.dir"), "alias");
        workingDirectory.mkdir();
        wsInfo.setWorkingDirectory(workingDirectory.getAbsolutePath());
        // create a workspace
        GAMSWorkspace ws = new GAMSWorkspace(wsInfo);

        // Create initial data containing a GAMS Alias
        // The OO API does not know about Aliases and will retrieve it as a set
        GAMSJob j1 = ws.addJobFromString(data);
        j1.run();

        checkAliasLogic("j1.OutDB", j1.OutDB());

        j1.OutDB().export("outdb.gdx");
        MyAssert(SameGdxDump(ws, "outdb.gdx", gdxdump1), " Unexpected result of gdxdump outdb.gdx");

        // Copy constructor should preserve aliases and other 
        GAMSDatabase db = ws.addDatabase(j1.OutDB());
        checkAliasLogic("db ", db);
        db.export("db.gdx");
        MyAssert(SameGdxDump(ws, "db.gdx", gdxdump1), "Unexpected result of gdxdump db.gdx");

        GAMSDatabase db2 = ws.addDatabase();
        GAMSSet ii = db2.addSet(db.getSet("ii").getName(), db.getSet("ii").getText(), "*");
        db.getSet("ii").copySymbol(ii);

        GAMSParameter aaOriginal = db.getParameter("aa");
        GAMSParameter aa = db2.addParameter(db.getParameter("aa").getName(), db.getParameter("aa").getText(), ii);
        aaOriginal.copySymbol(aa);
        db2.export("db2.gdx");

        MyAssert(SameGdxDump(ws, "db2.gdx", gdxdump2), " Unexpected result of gdxdump db2.gdx");

        // If the domain is an alias, domains should return the aliased set,  
        // but getDomainsAsStrings should return the name of the alias 
        MyAssert(aaOriginal.getDomains().get(0) instanceof GAMSSet, "The domain set should be a GAMSSet"); 
        MyAssert(((GAMSSet)aaOriginal.getDomains().get(0)).getName().equals("i"),  "The domain set should be the original set");
        MyAssert(aaOriginal.getDomainsAsStrings().get(0).equals("ii"), "The domain as string should be the alias name"); 
    }

    static void MyAssert(boolean test, String msg) throws Exception {
        if (!test)
            throw new Exception(msg);
    }

    static void checkAliasLogic(String prefix, GAMSDatabase aliasDB) throws Exception {
        // Check number of symbols
        MyAssert(aliasDB.getNumberOfSymbols() == 5, prefix + " aliasDB should have NrSymbols=5: i,j,ij,a,aa.");

        int cntSymbols = 0;
        for (@SuppressWarnings("unused") GAMSSymbol<?> sym : aliasDB) 
        	cntSymbols++;
        MyAssert(cntSymbols == 5, prefix + " there sould be 5 GAMSSymbols in aliasDB: i,j,ij,a,aa.");

        // See if we can retrieve alias sets
        MyAssert(aliasDB.getSet("ii").getName().equals("i"), prefix + " We should get set i when asking for alias ii.");
        MyAssert(aliasDB.getSet("jj").getName().equals("j"), prefix + " We should get set j when asking for alias jj.");
        MyAssert(aliasDB.getSet("iijj").getName().equals("ij"), prefix + " We should get set ij when asking for alias iijj.");

        // Check domain logic
        MyAssert(aliasDB.checkDomains() == true, prefix + " Check domains should be true");
        MyAssert(aliasDB.getParameter("aa").getDomains().get(0) instanceof GAMSSet, prefix + " domain[0] of aa should be set");
        MyAssert(((GAMSSet)aliasDB.getParameter("aa").getDomains().get(0)).getName().equals("i"), prefix + " domain[0] of aa should point to i");

        aliasDB.getSet("ii").deleteRecord("i1");
        MyAssert(aliasDB.checkDomains() == false, prefix + " Check domains should be false after removal of i1");
        aliasDB.getSet("ii").addRecord("i1");
        MyAssert(aliasDB.checkDomains() == true, prefix + " Check domains should be true after adding i1 again");

    }

    static boolean SameGdxDump(GAMSWorkspace ws, String gdxfile, String expectedResult)  {
  
        List<String> arguments = new ArrayList<String>();
        arguments.add( ws.systemDirectory() + GAMSGlobals.FILE_SEPARATOR + "gdxdump" );
        arguments.add( new File(ws.workingDirectory(),gdxfile).getAbsolutePath() );

        ProcessBuilder pb = new ProcessBuilder(arguments);
        pb.directory( new File(ws.systemDirectory()) );

        try {
            Process p = pb.start();

            BufferedReader stdOutput = new BufferedReader(new InputStreamReader(p.getInputStream())); 
            StringBuilder sb = new StringBuilder();
            String s = null;
            while ((s = stdOutput.readLine()) != null) {
                sb.append(s);
                sb.append(GAMSGlobals.LINE_SEPARATOR);
            }
            stdOutput.close();

            if (p.waitFor() != 0) 
               return false;

            return (expectedResult.replaceAll("\\s+","").equalsIgnoreCase( sb.toString().replaceAll("\\s+","")) );

        } catch (InterruptedException e) {
            return false;
        } catch (IOException e) {
           return false;
        }
    }

    static String data = 
       "set i   / i1*i3 /" + GAMSGlobals.LINE_SEPARATOR +
       "    j   / j1*j3 /" + GAMSGlobals.LINE_SEPARATOR +
       "   ij   / #i:#j /" + GAMSGlobals.LINE_SEPARATOR +
       "alias (i,ii), (j,jj), (ij,iijj);"+ GAMSGlobals.LINE_SEPARATOR +
       "parameter"         + GAMSGlobals.LINE_SEPARATOR +
       "    a(i) / #i 1 /, aa(ii) / #ii 2 /;"+ GAMSGlobals.LINE_SEPARATOR +
       GAMSGlobals.LINE_SEPARATOR;

    static String gdxdump1 = 
       "$onempty"+ GAMSGlobals.LINE_SEPARATOR +
       GAMSGlobals.LINE_SEPARATOR +
       "Set i(*) /"+ GAMSGlobals.LINE_SEPARATOR +
       "'i1',"     + GAMSGlobals.LINE_SEPARATOR +
       "'i2',"     + GAMSGlobals.LINE_SEPARATOR + 
       "'i3' /;"   + GAMSGlobals.LINE_SEPARATOR + 
       GAMSGlobals.LINE_SEPARATOR +
       "Set j(*) /"+ GAMSGlobals.LINE_SEPARATOR +
       "'j1',"     + GAMSGlobals.LINE_SEPARATOR + 
       "'j2',"     + GAMSGlobals.LINE_SEPARATOR + 
       "'j3' /;"   + GAMSGlobals.LINE_SEPARATOR +
       GAMSGlobals.LINE_SEPARATOR +
       "Set ij(*,*) /"     + GAMSGlobals.LINE_SEPARATOR +
       GAMSGlobals.LINE_SEPARATOR +
       "'i1'.'j1',"        + GAMSGlobals.LINE_SEPARATOR + 
       "'i2'.'j2',"        + GAMSGlobals.LINE_SEPARATOR + 
       "'i3'.'j3' /;"      + GAMSGlobals.LINE_SEPARATOR +
       GAMSGlobals.LINE_SEPARATOR +
       "Alias (ii, i);"    + GAMSGlobals.LINE_SEPARATOR +
       GAMSGlobals.LINE_SEPARATOR +
       "Alias (jj, j);"    + GAMSGlobals.LINE_SEPARATOR +
       GAMSGlobals.LINE_SEPARATOR +
       "Alias (iijj, ij);" + GAMSGlobals.LINE_SEPARATOR +
       GAMSGlobals.LINE_SEPARATOR +
       "Parameter a(i) /"  + GAMSGlobals.LINE_SEPARATOR +
       "'i1' 1,"   + GAMSGlobals.LINE_SEPARATOR + 
       "'i2' 1,"   + GAMSGlobals.LINE_SEPARATOR + 
       "'i3' 1 /;" + GAMSGlobals.LINE_SEPARATOR +
       GAMSGlobals.LINE_SEPARATOR +
       "Parameter aa(ii) /" + GAMSGlobals.LINE_SEPARATOR +
       "'i1' 2," + GAMSGlobals.LINE_SEPARATOR + 
       "'i2' 2," + GAMSGlobals.LINE_SEPARATOR + 
       "'i3' 2 /;" + GAMSGlobals.LINE_SEPARATOR +
       GAMSGlobals.LINE_SEPARATOR +
       "$offempty"+ GAMSGlobals.LINE_SEPARATOR;

    static String gdxdump2 =
       "$onempty" + GAMSGlobals.LINE_SEPARATOR +
       GAMSGlobals.LINE_SEPARATOR +
       "Set i(*) /" + GAMSGlobals.LINE_SEPARATOR +
       "'i1',"     + GAMSGlobals.LINE_SEPARATOR +
       "'i2',"     + GAMSGlobals.LINE_SEPARATOR +
       "'i3' /;"   + GAMSGlobals.LINE_SEPARATOR +
       GAMSGlobals.LINE_SEPARATOR +
       "Parameter aa(i) /  \n" +
       "'i1' 2,"   + GAMSGlobals.LINE_SEPARATOR +
       "'i2' 2,"   + GAMSGlobals.LINE_SEPARATOR +
       "'i3' 2 /;" + GAMSGlobals.LINE_SEPARATOR +
       GAMSGlobals.LINE_SEPARATOR +
       "$offempty"+ GAMSGlobals.LINE_SEPARATOR;

}

