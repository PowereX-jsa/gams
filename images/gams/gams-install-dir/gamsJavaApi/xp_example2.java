package com.gams.xp_examples;
import com.gams.api.*;

public class xp_example2 {
///////////////////////////////////////////////////////////////
// This program performs the following steps:                //
//    1. Generate a gdx file with demand data                //
//    2. Calls GAMS to solve a simple transportation model   //
//       (The GAMS model writes the solution to a gdx file)  //
//    3. The solution is read from the gdx file              //
// Paul van der Eijk Apr-14, 2008                            //
///////////////////////////////////////////////////////////////
    static final int maxdim = 20;

    static final int val_level    = 0;
    static final int val_marginal = 1;
    static final int val_lower    = 2;
    static final int val_upper    = 3;
    static final int val_scale    = 4;
    static final int val_max      = 5;

    static final int dt_set   = 0;
    static final int dt_par   = 1;
    static final int dt_var   = 2;
    static final int dt_equ   = 3;
    static final int dt_alias = 4;
    static final int dt_max   = 5;
    
    static gdx gdx = new gdx();    
    static gamsx gamsx = new gamsx();
    static opt opt = new opt();

    static void ReportGDXError(String s) {
        String Msg[] = new String[1];
        gdx.ErrorStr(gdx.GetLastError(), Msg);
        if(Msg[0].compareTo("") != 0) System.out.println("*** Error " + s + ": " + Msg[0]);
    }
  
    static void WriteData(String s, double V){
        String[] Indx = new String[maxdim];
        double[] Values = new double[val_max];
        Indx[0] = s;
        Values[val_level] = V;
        gdx.DataWriteStr(Indx, Values);   
    }
    
    static boolean WriteModelData(String fngdxfile){        
        String[] Msg = new String[1];
        int[] ErrNr = new int[1];
        
        gdx.OpenWrite(fngdxfile, "Example1", ErrNr);
        if(ErrNr[0] != 0){
            gdx.ErrorStr(ErrNr[0], Msg); 
            if(Msg[0].compareTo("") != 0) System.out.println("*** Error gdxOpenWrite: " + Msg[0]);
            return false;    
        }
        if(gdx.DataWriteStrStart("Demand", "Demand data", 1, dt_par, 0) == 0){
            ReportGDXError("DataWriteStrStart");
            return false;    
        }

        WriteData("New-York", 324.0);
        WriteData("Chicago" , 299.0);
        WriteData("Topeka"  , 274.0);

        if(gdx.DataWriteDone() == 0)
            ReportGDXError("WriteData");
        ErrNr[0] = gdx.GetLastError();
        if(ErrNr[0] != 0){
            gdx.ErrorStr(ErrNr[0], Msg);
            if(Msg[0].compareTo("") != 0) System.out.println("*** Error while writing GDX File: " + Msg[0]);
            return false;    
        }
        
        ErrNr[0] = gdx.Close();
        if(ErrNr[0] != 0){
            gdx.ErrorStr(ErrNr[0], Msg);
            if(Msg[0].compareTo("") != 0) System.out.println("*** Error gdxClose: " + Msg[0]);
            return false;    
        }
        return true;    
    }
    
    static boolean CallGams(String SysDir, String model){
        String[] Msg = new String[1];
        int[] ErrNr =new int[1];
        int n;

        if(opt.ReadDefinition(SysDir + "/optgams.def") != 0){
            System.out.println("*** Error ReadDefinition, cannot read def file:" + SysDir + "/optgams.def");
            return false;
        }

        opt.SetStrStr("SysDir", SysDir);
        opt.SetStrStr("Input", model);
        opt.SetIntStr("LogOption", 2);        // write .log and .lst files
		
        ErrNr[0] = gamsx.RunExecDLL(opt.GetoptPtr(), SysDir, 1, Msg);
        if(ErrNr[0] != 0){
            System.out.println("*** Error RunExecDLL: Error in GAMS call = " + ErrNr[0]);
            return false;
        }
        return true;
    }

    static void ReadData(int Dim){
        String[] Indx = new String[maxdim];
        double[] Values = new double[val_max];
        int[] N = new int[1];
        int D;
        
        while(gdx.DataReadStr(Indx, Values, N) != 0){
            if(Values[val_level] == 0) //skip level = 0.0 is default
                continue;
            for(D = 0; D < Dim; D++){
                System.out.print(Indx[D]);
                if(D < Dim-1)
                    System.out.print(".");
            }
            System.out.println(" = " + Values[val_level]);
        }
        System.out.println("All solution values shown");
    }
   
    static boolean ReadSolutionData(String fngdxfile){
        int[] Dim = new int[1];
        String[] Msg = new String[1];
        int[] ErrNr = new int[1];
        int[] VarNr = new int[1];
        String[] VarName = new String[1];
        int[] VarTyp = new int[1];
        int[] NrRecs = new int[1];
        
        gdx.OpenRead(fngdxfile, ErrNr);
        if(ErrNr[0] != 0){
            gdx.ErrorStr(ErrNr[0], Msg); 
            if(Msg[0].compareTo("") != 0) System.out.println("*** Error OpenWrite: " + Msg[0]);
            return false;    
        }

        VarName[0] = "result";
        if(gdx.FindSymbol(VarName[0], VarNr) == 0){
            System.out.println("*** Error FindSymbol: Could not find variable " + VarName[0]);
            return false;    
        }

        gdx.SymbolInfo(VarNr[0], VarName, Dim, VarTyp);
        if((Dim[0] != 2) || (VarTyp[0] != dt_var)){
            System.out.println("*** Error SymbolInfo: " + VarName[0] + " is not a two dimensional variable");
            return false;    
        }

        if(gdx.DataReadStrStart(VarNr[0], NrRecs) == 0){
            ReportGDXError("DataReadStrStart");
            return false;    
        }

        ReadData(Dim[0]);
        gdx.DataReadDone();
        gdx.Close();
        return true;
    }
    
    public static void main(String[] args) {
        String fngdxinp = "demanddata.gdx";
        String SysDir = "";
        String model = "";
        String[] Msg = new String[1];
        boolean ok = true;
        
        if(args.length < 1) {
            System.out.println("GAMS system directory needs to be specified on comand line!");
            return;
        }
        SysDir = args[0];

        if(args.length > 1)
            model = args[1];
        else
            model = "../GAMS/model2.gms";
        SysDir = args[0];

        System.out.println("Example2 using GAMS system directory: " + SysDir);
        
        if(gdx.CreateD(SysDir, Msg) != 1) {
            System.out.println("Cannot create GDX object: " + Msg[0]);
            ok = false;
        }

        if(ok && gamsx.CreateD(SysDir, Msg) != 1) {
            System.out.println("Cannot create GAMSX object: " + Msg[0]);
            ok = false;
        }
        
        if(ok && opt.CreateD(SysDir, Msg) != 1) {
            System.out.println("Cannot create Option object: " + Msg[0]);
            ok = false;
        }

        if(ok && !WriteModelData(fngdxinp)){
            System.out.println("Model data not written");
            ok = false;
        }

        if(ok && !CallGams(SysDir, model)){
            System.out.println("Call to GAMS failed");
            ok = false;
        }    

        if(ok && !ReadSolutionData("results.gdx"))
            System.out.println("Could not read solution back");           
        
        gdx.Free();
        gamsx.Free();
        opt.Free();
    }
}