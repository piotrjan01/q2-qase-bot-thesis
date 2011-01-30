/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * OptimizationFrame.java
 *
 * Created on 2010-11-01, 13:24:20
 */
package piotrrr.thesis.gui;

import java.util.Enumeration;
import java.util.LinkedList;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import org.apache.log4j.PropertyConfigurator;
import piotrrr.thesis.bots.tuning.DuelEvalResults;
import piotrrr.thesis.bots.tuning.OptResults;
import piotrrr.thesis.bots.tuning.OptResultsTools;
import piotrrr.thesis.bots.tuning.OptimizationRunner;
import piotrrr.thesis.common.CommFun;
import piotrrr.thesis.common.stats.BotStatistic;
import piotrrr.thesis.common.stats.StatsChartsFactory;
import piotrrr.thesis.tools.Dbg;

/**
 *
 * @author piotrrr
 */
public class OptimizationFrame extends javax.swing.JFrame {

    private static OptimizationFrame optFrameInstance;
    private OptResults optResults;

    /** Creates new form OptimizationFrame */
    public OptimizationFrame() {
        initComponents();
    }

    public OptResults getOptResults() {
        return optResults;
    }

    private String getRadioButtonSelectedString(ButtonGroup group) {
        String ret=null;
        Enumeration<AbstractButton> elems = group.getElements();
        while (elems.hasMoreElements()) {
            AbstractButton but = elems.nextElement();
            if (but.isSelected()) {
                ret = but.getText();
            }
        }
        return ret;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mapsButtonGroup1 = new javax.swing.ButtonGroup();
        algButtonGroup1 = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        runjButton1 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        evalRepetitionsjTextField1 = new javax.swing.JTextField();
        maxIterScorejTextField1 = new javax.swing.JTextField();
        iterationsjTextField2 = new javax.swing.JTextField();
        timescalejTextField1 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jRadioButton4 = new javax.swing.JRadioButton();
        jRadioButton5 = new javax.swing.JRadioButton();
        jRadioButton6 = new javax.swing.JRadioButton();
        jRadioButton7 = new javax.swing.JRadioButton();
        jRadioButton8 = new javax.swing.JRadioButton();
        jRadioButton9 = new javax.swing.JRadioButton();
        jPanel5 = new javax.swing.JPanel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        jButton5 = new javax.swing.JButton();
        estTimejLabel5 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jTabbedPane3 = new javax.swing.JTabbedPane();
        infojPanel7 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        resultsInfojTextArea1 = new javax.swing.JTextArea();
        iterFitnessjPanel7 = new javax.swing.JPanel();
        fitnessjPanel5 = new javax.swing.JPanel();
        errorjPanel7 = new javax.swing.JPanel();
        avgRelError = new javax.swing.JPanel();
        refreshResultsChartsjButton4 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        resultsjList1 = new javax.swing.JList();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        textjPanel4 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        resultDetailsTextArea1 = new javax.swing.JTextArea();
        killsChartjPanel5 = new javax.swing.JPanel();
        killsPerDeathjPanel6 = new javax.swing.JPanel();
        killsByBotTypejPanel5 = new javax.swing.JPanel();
        resultsDistributionjPanel7 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Bots Optimization");
        setBackground(new java.awt.Color(204, 204, 255));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Config"));

        runjButton1.setText("Run");
        runjButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runjButton1ActionPerformed(evt);
            }
        });

        jButton4.setText("Unblock current optimization");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton1.setText("Stop");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel4.setText("Evaluation repetitions");

        evalRepetitionsjTextField1.setText("20");
        evalRepetitionsjTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                evalRepetitionsjTextField1ActionPerformed(evt);
            }
        });

        maxIterScorejTextField1.setText("800");
        maxIterScorejTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maxIterScorejTextField1ActionPerformed(evt);
            }
        });

        iterationsjTextField2.setText("500");

        timescalejTextField1.setText("100");
        timescalejTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                timescalejTextField1ActionPerformed(evt);
            }
        });

        jLabel1.setText("Timescale");

        jLabel2.setText("Iterations");

        jLabel3.setText("Iter time in seconds");

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Optimization method"));

        algButtonGroup1.add(jRadioButton4);
        jRadioButton4.setText("Hill Climbing step decreasing");

        algButtonGroup1.add(jRadioButton5);
        jRadioButton5.setText("Hill Climbing");

        algButtonGroup1.add(jRadioButton6);
        jRadioButton6.setText("Constant solution");
        jRadioButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton6ActionPerformed(evt);
            }
        });

        algButtonGroup1.add(jRadioButton7);
        jRadioButton7.setText("Simulated Annealing");

        algButtonGroup1.add(jRadioButton8);
        jRadioButton8.setSelected(true);
        jRadioButton8.setText("Gradient Ascend");

        algButtonGroup1.add(jRadioButton9);
        jRadioButton9.setText("Localized Random Search");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButton5)
                    .addComponent(jRadioButton4)
                    .addComponent(jRadioButton9)
                    .addComponent(jRadioButton8)
                    .addComponent(jRadioButton7)
                    .addComponent(jRadioButton6))
                .addContainerGap(50, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jRadioButton5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton7)
                .addGap(18, 18, 18)
                .addComponent(jRadioButton6)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Map"));

        mapsButtonGroup1.add(jRadioButton1);
        jRadioButton1.setText("map1");

        mapsButtonGroup1.add(jRadioButton2);
        jRadioButton2.setSelected(true);
        jRadioButton2.setText("map2");

        mapsButtonGroup1.add(jRadioButton3);
        jRadioButton3.setText("q2dm1");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButton1)
                    .addComponent(jRadioButton2)
                    .addComponent(jRadioButton3))
                .addContainerGap(123, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jRadioButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton3)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        jButton5.setText("Estimate time");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        estTimejLabel5.setText("100 evals = x h 1 eval = x min");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(94, 94, 94)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(maxIterScorejTextField1)
                            .addComponent(iterationsjTextField2)
                            .addComponent(timescalejTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE)
                            .addComponent(evalRepetitionsjTextField1)))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel6Layout.createSequentialGroup()
                                .addComponent(runjButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jButton4, javax.swing.GroupLayout.Alignment.LEADING)))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton5)
                            .addComponent(estTimejLabel5))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap(39, Short.MAX_VALUE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(timescalejTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(iterationsjTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(maxIterScorejTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(evalRepetitionsjTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 110, Short.MAX_VALUE)
                        .addComponent(jButton5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(estTimejLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(runjButton1)
                            .addComponent(jButton1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton4))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(86, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Run Optimization", jPanel1);

        jButton2.setText("Save results");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Load results");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        resultsInfojTextArea1.setColumns(20);
        resultsInfojTextArea1.setEditable(false);
        resultsInfojTextArea1.setRows(5);
        jScrollPane3.setViewportView(resultsInfojTextArea1);

        javax.swing.GroupLayout infojPanel7Layout = new javax.swing.GroupLayout(infojPanel7);
        infojPanel7.setLayout(infojPanel7Layout);
        infojPanel7Layout.setHorizontalGroup(
            infojPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(infojPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 510, Short.MAX_VALUE)
                .addContainerGap())
        );
        infojPanel7Layout.setVerticalGroup(
            infojPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(infojPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane3.addTab("Info", infojPanel7);

        iterFitnessjPanel7.setLayout(new java.awt.GridLayout(1, 1));
        jTabbedPane3.addTab("Iter fitness", iterFitnessjPanel7);

        fitnessjPanel5.setLayout(new java.awt.GridLayout(1, 1));
        jTabbedPane3.addTab("Evals fitness", fitnessjPanel5);

        errorjPanel7.setLayout(new java.awt.GridLayout(1, 1));
        jTabbedPane3.addTab("evaluations variance", errorjPanel7);

        avgRelError.setLayout(new java.awt.GridLayout(1, 1));
        jTabbedPane3.addTab("Avg evaluations variance", avgRelError);

        refreshResultsChartsjButton4.setText("Refresh charts");
        refreshResultsChartsjButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshResultsChartsjButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 535, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(refreshResultsChartsjButton4)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jButton3)
                    .addComponent(refreshResultsChartsjButton4))
                .addGap(18, 18, 18)
                .addComponent(jTabbedPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 385, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Results", jPanel3);

        resultsjList1.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                resultsjList1ValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(resultsjList1);

        resultDetailsTextArea1.setColumns(20);
        resultDetailsTextArea1.setRows(5);
        jScrollPane2.setViewportView(resultDetailsTextArea1);

        javax.swing.GroupLayout textjPanel4Layout = new javax.swing.GroupLayout(textjPanel4);
        textjPanel4.setLayout(textjPanel4Layout);
        textjPanel4Layout.setHorizontalGroup(
            textjPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(textjPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE)
                .addContainerGap())
        );
        textjPanel4Layout.setVerticalGroup(
            textjPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(textjPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane2.addTab("Text", textjPanel4);

        killsChartjPanel5.setLayout(new java.awt.GridLayout(1, 0));
        jTabbedPane2.addTab("Kills", killsChartjPanel5);

        killsPerDeathjPanel6.setLayout(new java.awt.GridLayout(1, 1));
        jTabbedPane2.addTab("Kills per death", killsPerDeathjPanel6);

        killsByBotTypejPanel5.setLayout(new java.awt.GridLayout(1, 1));
        jTabbedPane2.addTab("Kills by bot type", killsByBotTypejPanel5);

        resultsDistributionjPanel7.setLayout(new java.awt.GridLayout(1, 1));
        jTabbedPane2.addTab("Results distribution", resultsDistributionjPanel7);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jTabbedPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Simulation Results", jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 473, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void runjButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runjButton1ActionPerformed
        int timescale = Integer.parseInt(timescalejTextField1.getText());
        int iterations = Integer.parseInt(iterationsjTextField2.getText());
        int iterScore = Integer.parseInt(maxIterScorejTextField1.getText());
        int reps = Integer.parseInt(evalRepetitionsjTextField1.getText());
        double tau = 0;
        String mapName=getRadioButtonSelectedString(mapsButtonGroup1);        
        String opt=getRadioButtonSelectedString(algButtonGroup1);
        OptimizationRunner.getInstance().runOptimization(timescale, iterations, iterScore, mapName, opt, reps, tau);
        optResults = new OptResults();
    }//GEN-LAST:event_runjButton1ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        OptimizationRunner.getInstance().stopOptimization();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        OptimizationRunner.getInstance().stopOptimization();
    }//GEN-LAST:event_formWindowClosing

    private void resultsjList1ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_resultsjList1ValueChanged
        int si = resultsjList1.getSelectedIndex();
        if (si < 0 || si >= resultsjList1.getModel().getSize()) {
            return;
        }
        DuelEvalResults r = optResults.iterResults.get(si);
        displayResults(r);
    }//GEN-LAST:event_resultsjList1ValueChanged

    private void timescalejTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_timescalejTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_timescalejTextField1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        try {
            if (optResults == null) {
                return;
            }
            for (DuelEvalResults s : optResults.iterResults) {
                s.stats.pickups = new LinkedList<BotStatistic.Pickup>();
            }
            JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
            fc.setApproveButtonText("Save");
            fc.setDialogTitle("Save current results");
            fc.showOpenDialog(this);
            System.out.println("Saving results to file: " + fc.getSelectedFile());
            CommFun.saveToFile(fc.getSelectedFile().getPath(), optResults);
            MyPopUpDialog.showMyDialogBox("Success", "Results saved successfully", MyPopUpDialog.info);
        } catch (Exception ex) {
            MyPopUpDialog.showMyDialogBox("Failed saving", "Failed saving the results!\n" + ex.toString(), MyPopUpDialog.error);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        try {
            JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
            fc.setApproveButtonText("Open");
            fc.setDialogTitle("Open results");
            fc.showOpenDialog(this);
            System.out.println("Reading results from file: " + fc.getSelectedFile());
            OptResults res = (OptResults) CommFun.readFromFile(fc.getSelectedFile().getPath());
            optResults = res;
            refreshIterResults();
            MyPopUpDialog.showMyDialogBox("Success", "Results loaded successfully", MyPopUpDialog.info);
        } catch (Exception ex) {
            MyPopUpDialog.showMyDialogBox("Failed reading", "Failed reading the results!\n" + ex.toString(), MyPopUpDialog.error);
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void refreshResultsChartsjButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshResultsChartsjButton4ActionPerformed
        fitnessjPanel5.removeAll();       
        fitnessjPanel5.add(StatsChartsFactory.getEvalFitnessPlot(optResults));
        fitnessjPanel5.revalidate();

        if (!AppConfig.debug) {
            errorjPanel7.removeAll();
            errorjPanel7.add(StatsChartsFactory.getEvaluationsVarianceEstimatePlot(optResults, "LearnBot"));
            errorjPanel7.revalidate();
        }

        iterFitnessjPanel7.removeAll();
        iterFitnessjPanel7.add(StatsChartsFactory.getIterFitnessPlot(optResults));
        iterFitnessjPanel7.revalidate();

        avgRelError.removeAll();
        avgRelError.add(StatsChartsFactory.getEvaluationAvgVarianceInRepetitionsPlot(optResults, "LearnBot"));
        avgRelError.revalidate();

        resultsInfojTextArea1.setText(OptResultsTools.getOptTextDesctiption(optResults));

    }//GEN-LAST:event_refreshResultsChartsjButton4ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        OptimizationRunner.getInstance().unblockProcess();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void evalRepetitionsjTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_evalRepetitionsjTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_evalRepetitionsjTextField1ActionPerformed

    private void jRadioButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton6ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jRadioButton6ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        double itTime = Integer.parseInt(maxIterScorejTextField1.getText());
        itTime /= Integer.parseInt(timescalejTextField1.getText());
        itTime *= Integer.parseInt(evalRepetitionsjTextField1.getText());
        double itMin = itTime / 60;
        itTime /= 3600; //ile w godzinach
        itTime *= 100;
        estTimejLabel5.setText("100 evals = "+itTime+" h, 1 eval = "+itMin+" min");
    }//GEN-LAST:event_jButton5ActionPerformed

    private void maxIterScorejTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maxIterScorejTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_maxIterScorejTextField1ActionPerformed

    private void displayResults(DuelEvalResults r) {
        resultDetailsTextArea1.setText(r.toString());

        killsChartjPanel5.removeAll();
        killsChartjPanel5.add(StatsChartsFactory.getKillsInTimeByBot(r.stats));
        killsChartjPanel5.revalidate();
        
        killsPerDeathjPanel6.removeAll();
        killsPerDeathjPanel6.add(StatsChartsFactory.getKillsPerEachDeathByBot(r.stats));
        killsPerDeathjPanel6.revalidate();

        killsByBotTypejPanel5.removeAll();
        killsByBotTypejPanel5.add(StatsChartsFactory.getKillsInTimeByBotTypeSorting(r.stats));
        killsByBotTypejPanel5.revalidate();

        resultsDistributionjPanel7.removeAll();
        resultsDistributionjPanel7.add(StatsChartsFactory.getResultsDistributionPlot(r, "LearnBot", 10));
        resultsDistributionjPanel7.revalidate();

    }

    public void addResults(DuelEvalResults res) {
        Dbg.prn("adding results to list");
        optResults.iterResults.add(res);
        refreshIterResults();
    }

    public void refreshIterResults() {
        DefaultListModel m = new DefaultListModel();
        int i = 0;
        for (DuelEvalResults r : optResults.iterResults) {
            m.add(i, r.toShortString());
            i++;
        }
        resultsjList1.setModel(m);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        PropertyConfigurator.configure("log4j.properties");
        AppConfig.readConfig();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex1) {
                System.err.println("No look and feel available :(");
                ex1.printStackTrace();
                return;
            }
        }
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                OptimizationFrame mf = new OptimizationFrame();
                OptimizationFrame.optFrameInstance = mf;
                mf.setVisible(true);
//                Dbg.toAppend = mf.getMessagesTextArea();
            }
        });
    }

    public static OptimizationFrame getOptFrameInstance() {
        return optFrameInstance;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup algButtonGroup1;
    private javax.swing.JPanel avgRelError;
    private javax.swing.JPanel errorjPanel7;
    private javax.swing.JLabel estTimejLabel5;
    private javax.swing.JTextField evalRepetitionsjTextField1;
    private javax.swing.JPanel fitnessjPanel5;
    private javax.swing.JPanel infojPanel7;
    private javax.swing.JPanel iterFitnessjPanel7;
    private javax.swing.JTextField iterationsjTextField2;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JRadioButton jRadioButton5;
    private javax.swing.JRadioButton jRadioButton6;
    private javax.swing.JRadioButton jRadioButton7;
    private javax.swing.JRadioButton jRadioButton8;
    private javax.swing.JRadioButton jRadioButton9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTabbedPane jTabbedPane3;
    private javax.swing.JPanel killsByBotTypejPanel5;
    private javax.swing.JPanel killsChartjPanel5;
    private javax.swing.JPanel killsPerDeathjPanel6;
    private javax.swing.ButtonGroup mapsButtonGroup1;
    private javax.swing.JTextField maxIterScorejTextField1;
    private javax.swing.JButton refreshResultsChartsjButton4;
    private javax.swing.JTextArea resultDetailsTextArea1;
    private javax.swing.JPanel resultsDistributionjPanel7;
    private javax.swing.JTextArea resultsInfojTextArea1;
    private javax.swing.JList resultsjList1;
    private javax.swing.JButton runjButton1;
    private javax.swing.JPanel textjPanel4;
    private javax.swing.JTextField timescalejTextField1;
    // End of variables declaration//GEN-END:variables
}