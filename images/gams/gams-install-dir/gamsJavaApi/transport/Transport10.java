package com.gams.examples.transport;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.gams.api.GAMSGlobals;
import com.gams.api.GAMSParameter;
import com.gams.api.GAMSSet;
import com.gams.api.GAMSVariableRecord;
import com.gams.api.GAMSWorkspace;
import com.gams.api.GAMSWorkspaceInfo;

/**
 * This example demonstrates how to retrieve an input for GAMS Transport Model
 * from an Excel file (transport.xls) using JExcelApi, a open source java API
 * for reading/writing Excel (See http://jexcelapi.sourceforge.net/) and 
 * Apache POI, Apache Java API for the Microsoft Documents (https://poi.apache.org/).
 *  
 * The example runs a job via the wrapper transport model of [trnsport] model in GAMS 
 * Model Library.  
 */
public class Transport10 {

    public static void main(String[] args) throws IOException {
        // create a workspace info
        GAMSWorkspaceInfo  wsInfo  = new GAMSWorkspaceInfo();
        // set default a spreadsheet reader
        String reader = "JXL";

        if (args.length > 0) {
            // check system directory info from the first command line argument
            wsInfo.setSystemDirectory( args[0] );
            // check spreadsheet reader info from the second command line argument
           if (args.length > 1) {
              if (args[0].toUpperCase().equals("POI")) {
                 reader = args[0].toUpperCase();
              }
           }
        }

        // create a working directory
        File workingDirectory = new File(System.getProperty("user.dir"), "Transport10");
        workingDirectory.mkdir();
        wsInfo.setWorkingDirectory(workingDirectory.getAbsolutePath());

        // create a workspace
        GAMSWorkspace ws = new GAMSWorkspace(wsInfo);

        // create a transport model wrapper
        TransportModel t = new TransportModel(ws);

        if (reader.equals("POI")) {
           // create a POI spreadsheet reader 
           readPOIFromFile poiReader = new readPOIFromFile(getInputString(ws));
           poiReader.read(t.geti(), "capacity");
           poiReader.read(t.geta(), "capacity", false);
           poiReader.read(t.getj(), "demand");
           poiReader.read(t.getb(), "demand", false);
           poiReader.read(t.getd(), "distance", true);
           poiReader.close();
        } else {
            // create a JXL spreadsheet reader 
            try {
               readJXLFromFile jxlReader = new readJXLFromFile(getInputString(ws));
               jxlReader.read(t.geti(), "capacity");
               jxlReader.read(t.geta(), "capacity", false);
               jxlReader.read(t.getj(), "demand");
               jxlReader.read(t.getb(), "demand", false);
               jxlReader.read(t.getd(), "distance", true);
               jxlReader.close();
            } catch (jxl.read.biff.BiffException e) {
               e.printStackTrace();
            }
        }

        // set option of all model types for xpress
        t.getopt().setAllModelTypes("xpress");

        // run the model
        t.run(System.out);

        // retrieve GAMSVariable "x" from the model wrapper
        for (GAMSVariableRecord rec : t.getx())
            System.out.println("x(" + rec.getKey(0) + "," + rec.getKey(1) + "): level=" + rec.getLevel() + " marginal=" + rec.getMarginal());
    }

    private static String getInputString(GAMSWorkspace ws) {
        // the directory [path/to/gams] where GAMS has been installed
        String gamsdir = ws.systemDirectory();

        // read input data from workbook "[path/to/gams]/apifiles/Data/transport.xls"
        if (!gamsdir.endsWith(GAMSGlobals.FILE_SEPARATOR))
            gamsdir += GAMSGlobals.FILE_SEPARATOR;
        return gamsdir + "apifiles" + GAMSGlobals.FILE_SEPARATOR + "Data" 
                       + GAMSGlobals.FILE_SEPARATOR + "transport.xls";
    }
}

/**
 * This class reads data from a Microsoft spreadsheet file (.xls) 
 * using Apache POI (https://poi.apache.org/) and 
 * adds them to GAMSSet and GAMSParameter.  
 */
class readJXLFromFile {
    private jxl.Workbook w;

    /** Represents a JXL spreadsheet reader. 
     * @param input absolute path of input file
     * @throws IOException if the input file cannot be read 
     * @throws jxl.read.biff.BiffException When unable to read a workbook from file
     */
    public readJXLFromFile(String input) throws IOException, jxl.read.biff.BiffException {
        File inputFile = new File(input);
        w = jxl.Workbook.getWorkbook(inputFile);
    }

    /** Read GAMSSet object from work sheet. 
     * @param set a GAMSSet object
     * @param fromWorksheet work sheet name
     */
    public void read(GAMSSet set, String fromWorksheet) {
    	jxl.Sheet sheet = w.getSheet(fromWorksheet);
        for(jxl.Cell cell : sheet.getRow(0))
           set.addRecord( cell.getContents() );
    }

    /** Read GAMSSet object from work sheet.
     * @param param a GAMSParameter object
     * @param fromWorksheet work sheet name
     * @param twoDimensioned true if two-dimensioned domain otherwise false (1-dimensioned)
     */
    public void read(GAMSParameter param, String fromWorksheet, boolean twoDimensioned) {
    	jxl.Sheet sheet = w.getSheet(fromWorksheet);
        if (twoDimensioned) {
           for (int j = 1; j < sheet.getColumns(); j++)
               for (int i = 1; i < sheet.getRows(); i++)
                   param.addRecord( sheet.getCell(0,i).getContents(), 
                                    sheet.getCell(j,0).getContents()  
                                  ).setValue( Double.valueOf(sheet.getCell(j,i).getContents()) );
        } else {
           for (int j = 0; j < sheet.getColumns(); j++)
               param.addRecord( sheet.getCell(j, 0).getContents() ).setValue( Double.valueOf(sheet.getCell(j,1).getContents()) );
        }
    }

    /** close reader connection */
    public void close() {
        if (w!=null)
            w.close();
     }
}

/** 
 * This class reads data from a Microsoft spreadsheet file (.xls) 
 * using JExcelApi (See http://jexcelapi.sourceforge.net/) and 
 * adds them to GAMSSet and GAMSParameter.  
 */
class readPOIFromFile {
    private org.apache.poi.hssf.usermodel.HSSFWorkbook workbook;
    private FileInputStream file;

   /** Represents a POI spreadsheet reader.
     * @param input absolute path of input file
     * @throws IOException if the input file cannot be read  
     */
    public readPOIFromFile(String input) throws IOException {
        file = new FileInputStream(new File(input));
        workbook = new org.apache.poi.hssf.usermodel.HSSFWorkbook(file);
    }

    /** Read GAMSSet object from work sheet. 
     * @param set a GAMSSet object
     * @param fromWorksheet work sheet name
     */
    public void read(GAMSSet set, String fromWorksheet) {
        org.apache.poi.hssf.usermodel.HSSFSheet sheet = workbook.getSheet(fromWorksheet);
        fillSetFromRow( set, sheet.getRow( sheet.getFirstRowNum()) );
    }
 
    /** Read GAMSSet object from work sheet.
     * @param param a GAMSParameter object
     * @param fromWorksheet work sheet name
     * @param twoDimensioned true if two-dimensioned domain otherwise false (1-dimensioned)
     */
    public void read(GAMSParameter param, String fromWorksheet, boolean twoDimensioned) {
        org.apache.poi.hssf.usermodel.HSSFSheet sheet = workbook.getSheet(fromWorksheet);
        if (twoDimensioned)
           fillTwoDimensionedParameterFromSheet(param, sheet);
        else 
            fillParameterFromSheet( param, sheet );
    }

    /** close reader connection */
    public void close() throws IOException {
       if (file!=null)
          file.close();
    }

    private void fillSetFromRow(GAMSSet set, org.apache.poi.hssf.usermodel.HSSFRow row)  {
        for(org.apache.poi.ss.usermodel.Cell cell : row) {
           switch(cell.getCellTypeEnum()) {
              case BOOLEAN: set.addRecord( String.valueOf( cell.getBooleanCellValue() ) ); break;
              case NUMERIC: set.addRecord( String.valueOf( cell.getNumericCellValue() ) ); break;
              case STRING: set.addRecord( cell.getStringCellValue() );  break;
              default: break;
           }
        }
    }

    private void fillParameterFromSheet(GAMSParameter param, org.apache.poi.hssf.usermodel.HSSFSheet sheet)  {
       org.apache.poi.hssf.usermodel.HSSFRow firstRow = sheet.getRow( sheet.getFirstRowNum() );
       org.apache.poi.hssf.usermodel.HSSFRow row  = sheet.getRow( sheet.getLastRowNum() );
       int idx = 0;
       for (org.apache.poi.ss.usermodel.Cell cell : row) {
          switch(cell.getCellTypeEnum()) {
             case NUMERIC: param.addRecord( firstRow.getCell(idx).getStringCellValue() ).setValue( cell.getNumericCellValue() );  break;
             case STRING:
                param.addRecord( firstRow.getCell(idx).getStringCellValue() ).setValue( Double.valueOf( cell.getStringCellValue() )); break;
             case BOOLEAN:
                param.addRecord( firstRow.getCell(idx).getStringCellValue() ).setValue( cell.getBooleanCellValue() ? 1 : 0 ); break;
             default: break;
          }
          idx++;
        }
    }

    private void fillTwoDimensionedParameterFromSheet(GAMSParameter param, org.apache.poi.hssf.usermodel.HSSFSheet sheet) {
        for (org.apache.poi.ss.usermodel.Row row : sheet) {
           if (row.getRowNum() == sheet.getFirstRowNum()) {
              continue;
          } else {
        	  org.apache.poi.ss.usermodel.Cell cell = row.getCell( 0 );
               for (short j = 1; j<row.getLastCellNum() ; j++) { 
                   cell = row.getCell( j );
                   switch(cell.getCellTypeEnum()) {
                       case NUMERIC:
                         param.addRecord( row.getCell(0).getStringCellValue() , 
                                          sheet.getRow(0).getCell( j ).getStringCellValue()  
                                        ).setValue( cell.getNumericCellValue() ); 
                         break;
                      case STRING:
                         param.addRecord( row.getCell(0).getStringCellValue() , sheet.getRow( 0 ).getCell( j ).getStringCellValue() ).setValue( Double.valueOf( cell.getStringCellValue() ));
                         break;
                      case BOOLEAN:
                         param.addRecord( row.getCell(0).getStringCellValue() , sheet.getRow( 0 ).getCell( j ).getStringCellValue() ).setValue( cell.getBooleanCellValue() ? 1 : 0 );
                         break;
                      default: 
                         break;
                   }               
               }
           }
       }
    }
}