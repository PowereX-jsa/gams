package com.gams.examples.transport;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.gams.api.GAMSDatabase;
import com.gams.api.GAMSException;
import com.gams.api.GAMSJob;
import com.gams.api.GAMSOptions;
import com.gams.api.GAMSParameter;
import com.gams.api.GAMSParameterRecord;
import com.gams.api.GAMSVariable;
import com.gams.api.GAMSVariableRecord;
import com.gams.api.GAMSWorkspace;
import com.gams.api.GAMSWorkspaceInfo;

/**
 * In this example a transportation is solved. Data can be entered 
 * into tables or can be loaded from a database. Results are shown 
 * in tables.
 */
public class TransportGUI extends JFrame implements ActionListener {
    private final GridBagConstraints constraints;
    private final TransportTableModel capacityTableModel, demandTableModel, distanceTableModel, shipmentTableModel;
    private final JTable capacityTable, demandTable, distanceTable, shipmentTable;
    private final JTextArea logTextArea;
    private final JScrollPane logScrollPanel;
    private final JTextField freightCost, transportCost;
    private final JButton loadButton, saveButton, runButton, exitButton;
    private final JFileChooser fc  = new JFileChooser();
    private GAMSWorkspace ws;
    private GAMSJob job;
    private GAMSDatabase inDB; 
    private PrintStream printStream;

	public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                 new TransportGUI();
            }
        });
    }

    public TransportGUI() {
        super("Transport - GAMS Java API Example");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // initialize GAMS Workspace and Job
        GAMSWorkspaceInfo  wsInfo  = new GAMSWorkspaceInfo();
        File workingDirectory = new File(System.getProperty("user.dir"), "TransportGUI");
        workingDirectory.mkdir();
        wsInfo.setWorkingDirectory(workingDirectory.getAbsolutePath());
        ws = new GAMSWorkspace(wsInfo);

        // Create and run a job from static data file
        job = ws.addJobFromString(data);
        job.run();
        inDB = job.OutDB();

        // Create components and set their layouts
        JPanel dataPane = new JPanel();
        dataPane.setLayout(new GridBagLayout());
        dataPane.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        dataPane.setBorder(BorderFactory.createTitledBorder("Data"));

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(Box.createHorizontalGlue());
        getContentPane().add(dataPane);
        getContentPane().add(Box.createHorizontalGlue());

        constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 10, 5, 10);

        JLabel capacityLabel = new JLabel("Capacity:");
        capacityLabel.setToolTipText("capacity of plant");
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridx = 0; 
        constraints.gridy = 0;
        dataPane.add(capacityLabel, constraints);

        capacityTableModel = new TransportTableModel(inDB.getParameter("a"), capacityLabel.getText());
        capacityTable = new JTable( capacityTableModel );
        capacityTable.setPreferredScrollableViewportSize(capacityTable.getPreferredSize());
        capacityTable.setFillsViewportHeight(true);
        capacityTable.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JScrollPane capacityScrollPane = new JScrollPane(capacityTable);
        capacityScrollPane.setMinimumSize(capacityTable.getPreferredSize());
        capacityScrollPane.setAutoscrolls(true);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridx = 0; 
        constraints.gridy = 1;
        dataPane.add(capacityScrollPane, constraints);

        NumberCellRenderer renderer = new NumberCellRenderer() ;
        renderer.setHorizontalAlignment(JLabel.RIGHT);
        TableColumnModel columnModel = capacityTable.getColumnModel();
        for(int i=0; i<2; i++)
            columnModel.getColumn(i).setCellRenderer(renderer);
 
        JLabel demandLabel = new JLabel("Demand:");
        demandLabel.setToolTipText("demand at market");
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridx = 0; 
        constraints.gridy = 2;
        dataPane.add(demandLabel, constraints);

        demandTableModel = new TransportTableModel(inDB.getParameter("b"), demandLabel.getText());
        demandTable = new JTable(demandTableModel);
        demandTable.setPreferredScrollableViewportSize(demandTable.getPreferredSize());
        demandTable.setFillsViewportHeight(true);
        JScrollPane demandScrollPane = new JScrollPane(demandTable);
        demandScrollPane.setMinimumSize(capacityTable.getPreferredSize());
        demandScrollPane.setAutoscrolls(true);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridx = 0; 
        constraints.gridy = 3;
        dataPane.add(demandScrollPane, constraints);

        columnModel = demandTable.getColumnModel();
        for(int i=0; i<3; i++)
          columnModel.getColumn(i).setCellRenderer(renderer);

        JLabel distanceLabel = new JLabel("Distance:");
        distanceLabel.setToolTipText("distance in thousands of miles ");
        constraints.anchor = GridBagConstraints.WEST; 
        constraints.gridx = 0; 
        constraints.gridy = 4; 
        dataPane.add(distanceLabel, constraints);

        distanceTableModel = new TransportTableModel(inDB.getParameter("d"), distanceLabel.getText());
        distanceTable = new JTable(distanceTableModel);
        distanceTable.setPreferredScrollableViewportSize(distanceTable.getPreferredSize());
        distanceTable.setFillsViewportHeight(true);
        JScrollPane distanceScrollPane = new JScrollPane(distanceTable);
        distanceScrollPane.setMinimumSize(distanceTable.getPreferredSize());
        distanceScrollPane.setAutoscrolls(true);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridx = 0; 
        constraints.gridy = 5;
        dataPane.add(distanceScrollPane, constraints);

        columnModel = distanceTable.getColumnModel();
        columnModel.getColumn(2).setCellRenderer(renderer);

        JLabel costLabel = new JLabel("Freight Cost:");
        costLabel.setToolTipText("freight in dollars per case per thousand miles");
        constraints.anchor = GridBagConstraints.WEST; 
        constraints.gridx = 0; 
        constraints.gridy = 6; 
        dataPane.add(costLabel, constraints);

        freightCost = new JTextField(8);
        freightCost.setHorizontalAlignment(SwingConstants.RIGHT);
        freightCost.setText(Double.toString(inDB.getParameter("f").getFirstRecord().getValue() ));
        freightCost.setPreferredSize(freightCost.getPreferredSize());
        constraints.anchor = GridBagConstraints.WEST; 
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0; 
        constraints.gridy = 7; 
        dataPane.add(freightCost, constraints);

        // Create a text area to display running job's log
        logTextArea = new JTextArea(15, 50);
        logTextArea.setLineWrap(true);
        logTextArea.setWrapStyleWord(true);
        logTextArea.setEditable(false);
        Font font = new Font( "Monospaced", Font.PLAIN, 12 );
        logTextArea.setFont( font );

        // Create a scrollable Text Panel to output the running job's log
        printStream = new PrintStream(new LogOutputStream(logTextArea));
        System.setOut(printStream);
        System.setErr(printStream);

        logScrollPanel = new JScrollPane(logTextArea); 
        logScrollPanel.setMinimumSize(logTextArea.getPreferredSize());
        logScrollPanel.setAutoscrolls(true);

        JPanel runPane = new JPanel();
        runPane.setLayout(new GridBagLayout());
        runPane.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        runPane.setBorder(new EmptyBorder(5,5,5,5));

        loadButton = makeButton("Load Data");
        loadButton.setToolTipText("Load Data from gdx file");
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.gridy = 0;
        runPane.add(loadButton, constraints);

        saveButton = makeButton("Save Data");
        saveButton.setToolTipText("Save Data to gdx file");
        constraints.gridx = 1;
        runPane.add(saveButton, constraints);

        runButton = makeButton("Run");
        runButton.setToolTipText("Run Transport Model");
        constraints.gridx = 2;
        runPane.add(runButton, constraints);

        exitButton = makeButton("Exit");
        runButton.setToolTipText("Exit Transport Example");
        constraints.gridx = 3;
        runPane.add(exitButton, constraints);
        getContentPane().add(runPane);

        // Create Output panel
        JPanel outputPane = new JPanel();
        outputPane.setBorder(BorderFactory.createTitledBorder("Output"));
        getContentPane().add(outputPane);

        JTabbedPane resultPane = new JTabbedPane();

        JPanel resultInnerPane = new JPanel();
        resultInnerPane.setLayout(new GridBagLayout());
        resultInnerPane.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

        JLabel transportCostLabel = new JLabel("Transportation Cost:");
        transportCostLabel.setToolTipText("total transportation costs in thousands of dollars");
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridx = 0; 
        constraints.gridy = 1;
        resultInnerPane.add(transportCostLabel, constraints);

        transportCost = new JTextField(12);
        transportCost.setHorizontalAlignment(SwingConstants.RIGHT);
        transportCost.setText("");
        constraints.anchor = GridBagConstraints.WEST; 
        constraints.gridx = 0; 
        constraints.gridy = 2; 
        resultInnerPane.add(transportCost, constraints);
        
        JLabel shipmentLabel = new JLabel("Shipment:");
        shipmentLabel.setToolTipText("shipment quantities");
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridx = 0; 
        constraints.gridy = 3;
        resultInnerPane.add(shipmentLabel, constraints);

        shipmentTableModel = new TransportTableModel((GAMSVariable)null, shipmentLabel.getText());
        shipmentTable = new JTable(shipmentTableModel);
        shipmentTable.setPreferredScrollableViewportSize(shipmentTable.getPreferredSize());
        shipmentTable.setFillsViewportHeight(true);
        JScrollPane shipmentScrollPane = new JScrollPane(shipmentTable);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridx = 0; 
        constraints.gridy = 4;
        resultInnerPane.add(shipmentScrollPane, constraints);

        columnModel = shipmentTable.getColumnModel();
        for(int i=2; i<4; i++)
          columnModel.getColumn(i).setCellRenderer(renderer);

        resultPane.setAutoscrolls(true);
        resultPane.addTab("Results", resultInnerPane);
        resultPane.addTab("log",logScrollPanel);

        outputPane.add(resultPane);
        outputPane.add(Box.createHorizontalGlue());

        // Display the window
        pack();
        setVisible(true);
    }

    /** create Buttons */
    private JButton makeButton(String caption) {
        JButton b = new JButton(caption);
        b.setActionCommand(caption);
        b.addActionListener(this);
        getContentPane().add(b, constraints);
        return b;
    }

    /** update component's value into GAMSDatabase */
    private void updateDB(GAMSDatabase db) {
        GAMSParameter capacity = db.getParameter("a");
        capacity.findRecord("seattle").setValue( 
            (capacityTable.getValueAt(0,0) instanceof Double) ? ((Double)capacityTable.getValueAt(0,0)).doubleValue() :  Double.parseDouble((String)capacityTable.getValueAt(0,0))
        );
        capacity.findRecord("san-diego").setValue( 
                (capacityTable.getValueAt(0,1) instanceof Double) ? ((Double)capacityTable.getValueAt(0,1)).doubleValue() :  Double.parseDouble((String)capacityTable.getValueAt(0,1))
        );
        GAMSParameter demand = db.getParameter("b");
        demand.findRecord("new-york").setValue( 
            (demandTable.getValueAt(0,0) instanceof Double) ? ((Double)demandTable.getValueAt(0,0)).doubleValue() :  Double.parseDouble((String)demandTable.getValueAt(0,0))
        );
        demand.findRecord("chicago").setValue( 
            (demandTable.getValueAt(0,1) instanceof Double) ? ((Double)demandTable.getValueAt(0,1)).doubleValue() :  Double.parseDouble((String)demandTable.getValueAt(0,1))
        );
        demand.findRecord("topeka").setValue( 
            (demandTable.getValueAt(0,2) instanceof Double) ? ((Double)demandTable.getValueAt(0,2)).doubleValue() :  Double.parseDouble((String)demandTable.getValueAt(0,2))
        );
        GAMSParameter distance = db.getParameter("d");
        distance.findRecord("seattle", "new-york").setValue( 
            (distanceTable.getValueAt(0,2) instanceof Double) ? ((Double)distanceTable.getValueAt(0,2)).doubleValue() :  Double.parseDouble((String)distanceTable.getValueAt(0,2))
        );
        distance.findRecord("seattle", "chicago").setValue( 
            (distanceTable.getValueAt(1,2) instanceof Double) ? ((Double)distanceTable.getValueAt(1,2)).doubleValue() :  Double.parseDouble((String)distanceTable.getValueAt(1,2))
        );
        distance.findRecord("seattle", "topeka").setValue( 
            (distanceTable.getValueAt(2,2) instanceof Double) ? ((Double)distanceTable.getValueAt(2,2)).doubleValue() :  Double.parseDouble((String)distanceTable.getValueAt(2,2))
        );
        distance.findRecord("san-diego", "new-york").setValue( 
            (distanceTable.getValueAt(3,2) instanceof Double) ? ((Double)distanceTable.getValueAt(3,2)).doubleValue() :  Double.parseDouble((String)distanceTable.getValueAt(3,2))
        );
        distance.findRecord("san-diego", "chicago").setValue( 
            (distanceTable.getValueAt(4,2) instanceof Double) ? ((Double)distanceTable.getValueAt(4,2)).doubleValue() :  Double.parseDouble((String)distanceTable.getValueAt(4,2))
        );
        distance.findRecord("san-diego", "topeka").setValue( 
            (distanceTable.getValueAt(5,2) instanceof Double) ? ((Double)distanceTable.getValueAt(5,2)).doubleValue() :  Double.parseDouble((String)distanceTable.getValueAt(5,2))
        );
        GAMSParameter f = db.getParameter("f");
        f.getFirstRecord().setValue( Double.parseDouble(freightCost.getText()) );
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == runButton) {
            runButton.setEnabled(false);
            try {
                updateDB(inDB);
                job = ws.addJobFromString(model);
                GAMSOptions opt = ws.addOptions();
                opt.defines("gdxincname", inDB.getName());
                job.run(opt, printStream, inDB);
            } catch(Exception e) { 
                e.printStackTrace(); 
            } finally {
                runButton.setEnabled(true);
                if (job.OutDB() != null) {
                   double roundedValue = Math.round(job.OutDB().getVariable("z").getFirstRecord().getLevel() * 100D) / 100D;
                   transportCost.setText( Double.toString( roundedValue ));
                   int i = 0;
                   for(GAMSVariableRecord rec: job.OutDB().getVariable("x")) {
                       shipmentTableModel.setValueAt(rec.getKey(0), i, 0);
                       shipmentTableModel.setValueAt(rec.getKey(1), i, 1);
                       shipmentTableModel.setValueAt(new Double(rec.getLevel()), i, 2);
                       shipmentTableModel.setValueAt(new Double(rec.getMarginal()), i, 3);
                       i++;
                   }
                }
            }
        } else if (event.getSource() == loadButton) {
            runButton.setEnabled(true);
            FileNameExtensionFilter gdxfilter = new FileNameExtensionFilter( "GAMS Data eXchange files (*.gdx)", "gdx");
            fc.setDialogTitle("Load data from .gdx file");
            fc.setFileFilter(gdxfilter);
            int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                try {
                   inDB = ws.addDatabaseFromGDX(file.getAbsolutePath());
                   int j = 0;
                   for(GAMSParameterRecord rec : inDB.getParameter("a")) {
                       capacityTableModel.setValueAt(new Double(rec.getValue()), 0, j++);
                    }
                   j = 0;
                   for(GAMSParameterRecord rec : inDB.getParameter("b")) {
                       demandTableModel.setValueAt(new Double(rec.getValue()), 0, j++);
                    }
                   int i = 0;
                   for(GAMSParameterRecord rec : inDB.getParameter("d")) {
                       distanceTableModel.setValueAt(rec.getKey(0), i, 0);
                       distanceTableModel.setValueAt(rec.getKey(1), i, 1);
                       distanceTableModel.setValueAt(new Double(rec.getValue()), i, 2);
                       i++;
                    }

                    transportCost.setText("");
                    for(i=0; i<6; i++) {
                       shipmentTableModel.setValueAt("", i, 0);
                       shipmentTableModel.setValueAt("", i, 1);
                       shipmentTableModel.setValueAt("", i, 2);
                       shipmentTableModel.setValueAt("", i, 3);
                    }
                    System.out.println("--- Data loaded from [" + file.getAbsolutePath() + "].\n");
                } catch(GAMSException e) {
                    System.out.println("--- Data could not be loaded from [" + file.getAbsolutePath() + "]");
                    System.out.println(e.getMessage());
                }
           } else {
               System.out.println("--- Load Data command cancelled by user.\n");
           }
        } else if (event.getSource() == saveButton) {
            runButton.setEnabled(true);
            FileNameExtensionFilter gdxfilter = new FileNameExtensionFilter( "GAMS Data eXchange files (*.gdx)", "gdx");
            fc.setFileFilter(gdxfilter);
            fc.setDialogTitle("Save Data to .gdx file");
            int returnVal = fc.showSaveDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                try {
                    updateDB(inDB);
                    inDB.export(file.getAbsolutePath());
                    System.out.println("--- Data saved to ["+file.getAbsolutePath()+"].\n");
                 } catch(GAMSException e) {
                     System.out.println("--- Data could not be saved to [" + file.getAbsolutePath() + "]\n");
                     System.out.println(e.getMessage());
                 }
            } else {
                System.out.println("--- Save Data command cancelled by user.\n");
            }
        } else if (event.getSource() == exitButton) {
            System.out.println("--- Closing Transport GUI Example.\n");
            System.exit(0); 
        }
     }

    static String data =
            "Sets                                                             \n" +
            "  i   canning plants   / seattle, san-diego /                    \n" +
            "  j   markets          / new-york, chicago, topeka / ;           \n" +
            "Parameters                                                       \n" +
            "                                                                 \n" +
            "  a(i)  capacity of plant i in cases                             \n" +
            "                     /    seattle     350                        \n" +
            "                          san-diego   600  /                     \n" +
            "                                                                 \n" +
            "  b(j)  demand at market j in cases                              \n" +
            "                     /    new-york    325                        \n" +
            "                          chicago     300                        \n" +
            "                          topeka      275  / ;                   \n" +
            "                                                                 \n" +
            "Table d(i,j)  distance in thousands of miles                     \n" +
            "               new-york       chicago      topeka                \n" +
            "  seattle          2.5           1.7          1.8                \n" +
            "  san-diego        2.5           1.8          1.4  ;             \n" +
            "                                                                 \n" +
            "Scalar f  freight in dollars per case per thousand miles  /90/ ; \n ";

    static String model = "Sets                                                         \n" +
            "      i   canning plants                                                   \n" +
            "      j   markets                                                          \n" +
            "                                                                           \n" +
            " Parameters                                                                \n" +
            "      a(i)   capacity of plant i in cases                                  \n" +
            "      b(j)   demand at market j in cases                                   \n" +
            "      d(i,j) distance in thousands of miles                                \n" +
            " Scalar f  freight in dollars per case per thousand miles;                 \n" +
            "                                                                           \n" +
            "$if not set gdxincname $abort 'no include file name for data file provided'\n" +
            "$gdxin %gdxincname%                                                        \n" +
            "$load i j a b d f                                                          \n" +
            "$gdxin                                                                     \n" +
            "                                                                           \n" +
            " Parameter c(i,j)  transport cost in thousands of dollars per case ;       \n" +
            "                                                                           \n" +
            "            c(i,j) = f * d(i,j) / 1000 ;                                   \n" +
            "                                                                           \n" +
            " Variables                                                                 \n" +
            "       x(i,j)  shipment quantities in cases                                \n" +
            "       z       total transportation costs in thousands of dollars ;        \n" +
            "                                                                           \n" +
            " Positive Variable x ;                                                     \n" +
            "                                                                           \n" +
            " Equations                                                                 \n" +
            "                                                                           \n" +
            "      cost        define objective function                                \n" +
            "      supply(i)   observe supply limit at plant i                          \n" +
            "      demand(j)   satisfy demand at market j ;                             \n" +
            "                                                                           \n" +
            "  cost ..        z  =e=  sum((i,j), c(i,j)*x(i,j)) ;                       \n" +
            "                                                                           \n" +
            "  supply(i) ..   sum(j, x(i,j))  =l=  a(i) ;                               \n" +
            "                                                                           \n" +
            "  demand(j) ..   sum(i, x(i,j))  =g=  b(j) ;                               \n" +
            "                                                                           \n" +
            " Model transport /all/ ;                                                   \n" +
            "                                                                           \n" +
            " Solve transport using lp minimizing z ;                                   \n" +
            "                                                                           \n" +
            " Display x.l, x.m ;                                                        \n" +
            "                                                                           \n";
}

/** To redirect output stream to JTextArea */
class LogOutputStream extends OutputStream {
    private JTextArea textArea;
     
    public LogOutputStream(JTextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void write(int b) throws IOException {
        textArea.append(String.valueOf((char)b));
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
}

/** To implement table model */
class TransportTableModel extends AbstractTableModel {
    String tableName;
    String[] columnNames;
    Object[][] data;

    public TransportTableModel (GAMSVariable var, String name) {
        tableName = name;  
        setTableValue(var);
    }

    public TransportTableModel (GAMSParameter param, String name) {
        tableName = name;  
        setTableValue(param);
    }

    public void setTableValue(GAMSVariable var) { 
        columnNames = new String[]{ "From", "To", "Quantity", "Shadow Price"}; 
        data = new Object[6][4];
        if (var == null)  {
            for(int i=0; i<6; i++)
               for(int j=0; j<columnNames.length; j++)
                  data[i][j] = "";
        } else if (var.getDimension() == 2) {
                int i =0;
                for (GAMSVariableRecord rec : var) {
                   setValueAt(rec.getKey(0), i, 0);
                   setValueAt(rec.getKey(1), i, 1);
                   setValueAt(new Double(rec.getLevel()), i, 2);
                   setValueAt(new Double(rec.getMarginal()), i, 3);
                   i++;
                }
        } 
    }

    public void setTableValue(GAMSParameter param) { 
        int j=0;
        if (param.getDimension() == 1) {
            columnNames = new String[param.getNumberOfRecords()];
            data = new Object[1][columnNames.length];
            for(GAMSParameterRecord rec : param) {
               columnNames[j] = rec.getKey(0);
               setValueAt(new Double(rec.getValue()), 0, j); 
               j++;
            }
        } else if (param.getName().equals("d")) {
            columnNames =  new String[] { "Plants", "Markets", "Distance" };
            data = new Object[param.getNumberOfRecords()][3];
            int i = 0;
            for(GAMSParameterRecord rec : param){
                setValueAt(rec.getKey(0), i, 0); 
                setValueAt(rec.getKey(1), i, 1); 
                setValueAt(new Double(rec.getValue()), i, 2); 
                i++;
            }
        } 
    }

    public String getName() { return tableName; } 
    public int getRowCount() { return data.length; }
    public int getColumnCount() { return columnNames.length; }
    public String getColumnName(int col) { return columnNames[col]; }
    public Object getValueAt( int row, int col ) { return data[row][col]; }
    public boolean isCellEditable(int row, int col) { 
        if ((tableName.equals("Distance:") && (col<2)) )
           return false;
        else 
           return true; 
    }
    public void setValueAt(Object value, int row, int col) {
        data[row][col] = value;
        fireTableCellUpdated(row, col);
    }
}

/** To render a cell in a table */
class NumberCellRenderer extends DefaultTableCellRenderer {
    DecimalFormat formatter = new DecimalFormat( "#.00" );

     @Override
     public Component getTableCellRendererComponent(JTable jTable, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
         Component c = super.getTableCellRendererComponent(jTable, value, isSelected, hasFocus, row, column);
         if (value instanceof Number)
            value = formatter.format((Number)value);
         return c;
     }
}
