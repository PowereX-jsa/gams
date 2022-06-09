package com.gams.xp_examples;
import com.gams.api.*;

public class xp_example1 {
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

static gdx gdxio = new gdx();
static String[] Indx = new String[maxdim];
static double[] Values = new double[val_max];
    
static void ReportGDXError() {
  String[] S = new String[1];

  System.out.println("**** Fatal GDX Error");
  gdxio.ErrorStr(gdxio.GetLastError(), S);
  System.out.println("**** " + S[0]);
  System.exit(1);
}

static void ReportIOError(int N, String msg ) {
  System.out.println("**** Fatal I/O Error = " + N + " when calling " + msg);
  System.exit(1);
}

static void WriteData(String s, double V) {
  Indx[0] = s;
  Values[val_level] = V;
  gdxio.DataWriteStr(Indx,Values);
}

 public static void main (String[] args) {

  String[]    Msg = new String[1];
  String[]    Producer = new String[1];
  String      Sysdir;
  int[]       ErrNr = new int[1];
  int[]       VarNr = new int[1];
  int[]       NrRecs = new int[1];
  int[]       N = new int[1];
  int[]       Dim = new int[1];
  String[]    VarName = new String[1];
  int[]       VarTyp = new int[1];
  int         D;

  if (args.length < 1 || args.length > 2) {
    System.out.println("**** Example1: incorrect number of parameters");
    System.exit(1);
  }

  Sysdir = args[0];
  System.out.println("Example1 using GAMS system directory: " + Sysdir);

  if (gdxio.CreateD(Sysdir, Msg) != 1) {
    System.out.println("**** Could not load GDX library");
    System.out.println("**** " + Msg[0]);
    System.exit(1);
  }

  gdxio.GetDLLVersion(Msg);
  System.out.println("Using GDX DLL version: " + Msg[0]);

  if (1 == args.length) {
    /* Write demand data */
    gdxio.OpenWrite("demanddata.gdx", "example1", ErrNr);
    if (ErrNr[0] != 0) ReportIOError(ErrNr[0],"gdxOpenWrite");
    if (gdxio.DataWriteStrStart("Demand","Demand data",1,dt_par,0) != 1)
      ReportGDXError();
    WriteData("New-York",324.0);
    WriteData("Chicago" ,299.0);
    WriteData("Topeka"  ,274.0);
    if (gdxio.DataWriteDone() != 1) ReportGDXError();
    System.out.println("Demand data written by example1\n");
  } else {
    gdxio.OpenRead(args[1], ErrNr);
    if (ErrNr[0] != 0) ReportIOError(ErrNr[0],"gdxOpenRead");
    gdxio.FileVersion(Msg,Producer);
    System.out.println("GDX file written using version: " + Msg[0]);
    System.out.println("GDX file written by: " + Producer[0]);

    if (gdxio.FindSymbol("x",VarNr) != 1) {
      System.out.println("**** Could not find variable X");
      System.exit(1);
    }

    gdxio.SymbolInfo(VarNr[0],VarName,Dim,VarTyp);
    if (Dim[0] != 2 || dt_var != VarTyp[0]) {
      System.out.println("**** X is not a two dimensional variable: " + Dim[0] + ":" + VarTyp[0]);
      System.exit(1);
    }

    if (gdxio.DataReadStrStart(VarNr[0],NrRecs) != 1) ReportGDXError();
    System.out.println("Variable X has " + NrRecs[0] + " records");
    while (gdxio.DataReadStr(Indx,Values,N) != 0) {
      if (0 == Values[val_level]) continue; /* skip level 0.0 is default */
      for (D=0; D<Dim[0]; D++) {
          System.out.print(Indx[D]);
          if (D<Dim[0]-1) System.out.print(".");
      }
      System.out.println(" = " + Values[val_level]);
    }
    System.out.println("All solution values shown");
    gdxio.DataReadDone();
  }

  ErrNr[0] = gdxio.Close();
  if (ErrNr[0] != 0) ReportIOError(ErrNr[0],"gdxClose");

  if (gdxio.Free() != 1) {
    System.out.println("Problems unloading the GDX DLL");
    System.exit(1);
  }

} /* main */

}


