package com.gams.examples.interrupt;

import java.awt.ComponentOrientation;
import java.awt.Dimension;
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

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.gams.api.GAMSJob;
import com.gams.api.GAMSOptions;
import com.gams.api.GAMSWorkspace;
import com.gams.api.GAMSWorkspaceInfo;

/**
 * This small example demonstrates how to run a GAMS model in a graphical 
 * user interface. This has all rudimentary features we know from the GAMS IDE: 
 * starting a job, capture the GAMS log in a window, and providing a button to 
 * interrupt. The underlying mechanism to interrupt the job is similar to the 
 * Interrupt Example, but the trigger mechanism is very different.
 */ 
public class InterruptGUI extends JFrame implements ActionListener {
    private final GridBagConstraints constraints;
    private final JLabel headsLabel;
    private final JTextArea logTextArea;
    private final JScrollPane logScrollPanel;
    private final JButton runButton, stopButton, exitButton;
    private final GAMSWorkspace ws;
    private final GAMSJob job;
    private final GAMSOptions opt;
    private final PrintStream printStream;
    private Worker worker;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                 new InterruptGUI();
            }
        });
    }

    public InterruptGUI() {
        super("GAMS Java API - Interrupt GUI Example");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        getContentPane().setLayout(new GridBagLayout());
        getContentPane().setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        
        constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 10, 5, 10);
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.weightx = 1;

        // Create a label 
        headsLabel = new JLabel("GAMS Log -- running [circpack] model from GAMS Model Library");
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0; 
        constraints.gridy = 0; 
        getContentPane().add(headsLabel, constraints);

        // Create a text area to display running job's log
        logTextArea = new JTextArea(40, 140);
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
        logScrollPanel.setMinimumSize(new Dimension(800, 500));
        logScrollPanel.setAutoscrolls(true);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0; 
        constraints.gridy = 1; 
        constraints.gridwidth = 4;
        getContentPane().add(logScrollPanel, constraints);

        // Create buttons
        runButton = makeButton("Run");
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0; 
        constraints.gridy = 2; 
        constraints.gridwidth = 1;
        getContentPane().add(runButton, constraints);

        stopButton = makeButton("Stop");
        stopButton.setEnabled(false);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 2; 
        constraints.gridy = 2; 
        constraints.gridwidth = 1;
        getContentPane().add(stopButton, constraints);

        exitButton = makeButton("Exit");
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 3; 
        constraints.gridy = 2; 
        constraints.gridwidth = 1;
        getContentPane().add(exitButton, constraints);

        // initialize GAMS Workspace and Job
        GAMSWorkspaceInfo  wsInfo  = new GAMSWorkspaceInfo();
        File workingDirectory = new File(System.getProperty("user.dir"), "InterruptGUI");
        workingDirectory.mkdir();
        wsInfo.setWorkingDirectory(workingDirectory.getAbsolutePath());
        ws = new GAMSWorkspace(wsInfo);

        // Use a MIP that needs some time to solve
        job = ws.addJobFromGamsLib("circpack");
        opt = ws.addOptions();
        opt.setAllModelTypes("scip");

        // Display the window
        pack();
        setVisible(true);
    }

    private JButton makeButton(String caption) {
        JButton b = new JButton(caption);
        b.setActionCommand(caption);
        b.addActionListener(this);
        getContentPane().add(b, constraints);
        return b;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if ("Run" == e.getActionCommand()) {
            runButton.setEnabled(false);
            stopButton.setEnabled(true);
            if ((worker == null) || (worker.getState()==Thread.State.TERMINATED)) 
                worker  = new Worker(job, opt, printStream, this);
            worker.start();
        } else if ("Stop" == e.getActionCommand()) {
            runButton.setEnabled(true);
            stopButton.setEnabled(false);
            worker.interrupt();
        } else if ("Exit" == e.getActionCommand()) {
            System.out.println("Closing Interrupt GUI Example...");
            if (worker != null) {
               if (!worker.interrupted || (worker.getState() != Thread.State.TERMINATED))
                   System.exit(-1);
            }
            System.exit(0); 
        }
    }

    private void reportJobTerminated() {
        runButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    /** A worker thread to run a GAMSJob. */
    static class Worker extends Thread {
       GAMSJob job;
       GAMSOptions option;
       PrintStream output;
       boolean interrupted;
       InterruptGUI caller;

       /** Worker constructor
        *  @param   jb   a job to run
        *  @param   opt  GAMS options to run a job
        *  @param   out  Stream to capture GAMS log
        */
       public Worker(GAMSJob jb, GAMSOptions opt, PrintStream out, InterruptGUI c) { 
           interrupted = false; 
           job = jb; 
           option = opt;
           output = out;
           caller = c;
       }

       /** Run a job */ 
       @Override
       public void run() { 
          try {
              job.run(option, output);
          } catch(Exception e) { 
              e.printStackTrace(); 
          } finally {
              caller.reportJobTerminated();
          }
       }

       /** interrupts the running job */
       public void interrupt() {
          if (getState() != Thread.State.TERMINATED)
             interrupted = job.interrupt();
       }
    }
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