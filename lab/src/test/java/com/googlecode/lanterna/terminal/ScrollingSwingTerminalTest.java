/*
 * This file is part of lanterna (http://code.google.com/p/lanterna/).
 *
 * lanterna is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2010-2016 Martin
 */
package com.googlecode.lanterna.terminal;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.terminal.swing.ScrollingSwingTerminal;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFontConfiguration;
import com.googlecode.lanterna.terminal.swing.TerminalEmulatorColorConfiguration;
import com.googlecode.lanterna.terminal.swing.TerminalEmulatorDeviceConfiguration;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

/**
 *
 * @author Martin
 */
@SuppressWarnings("FieldCanBeLocal")
public class ScrollingSwingTerminalTest extends javax.swing.JFrame {

    private static final long serialVersionUID = 1L;
    private final ScrollingSwingTerminal scrollingSwingTerminal;

    /**
     * Creates new form ScrollingSwingTerminalTest
     */
    public ScrollingSwingTerminalTest() {
        initComponents();
        scrollingSwingTerminal = new ScrollingSwingTerminal(
                TerminalEmulatorDeviceConfiguration.getDefault().withLineBufferScrollbackSize(150),
                SwingTerminalFontConfiguration.getDefault(),
                TerminalEmulatorColorConfiguration.getDefault());
        panelTerminalContainer.add(scrollingSwingTerminal, BorderLayout.CENTER);
        pack();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelTerminalContainer = new javax.swing.JPanel();
        buttonPrint100Lines = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        buttonPrint10Lines = new javax.swing.JButton();
        buttonPrint1Line = new javax.swing.JButton();
        buttonMoveCursor = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        panelTerminalContainer.setBorder(javax.swing.BorderFactory.createTitledBorder("Terminal"));
        panelTerminalContainer.setLayout(new BorderLayout());

        buttonPrint100Lines.setText("Print 100 lines");
        buttonPrint100Lines.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPrint100LinesActionPerformed(evt);
            }
        });

        buttonPrint10Lines.setText("Print 10 lines");
        buttonPrint10Lines.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPrint10LinesActionPerformed(evt);
            }
        });

        buttonPrint1Line.setText("Print 1 line");
        buttonPrint1Line.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPrint1LineActionPerformed(evt);
            }
        });

        buttonMoveCursor.setText("Move cursor");
        buttonMoveCursor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonMoveCursorActionPerformed(evt);
            }
        });

        jButton1.setText("Clear");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelTerminalContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(buttonMoveCursor)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 180, Short.MAX_VALUE)
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonPrint1Line)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonPrint10Lines)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonPrint100Lines))
                    .addComponent(jSeparator1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelTerminalContainer, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonPrint100Lines)
                    .addComponent(buttonPrint10Lines)
                    .addComponent(buttonPrint1Line)
                    .addComponent(buttonMoveCursor)
                    .addComponent(jButton1))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonPrint100LinesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPrint100LinesActionPerformed
        printLines(100);
    }//GEN-LAST:event_buttonPrint100LinesActionPerformed

    private void buttonPrint10LinesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPrint10LinesActionPerformed
        printLines(10);
    }//GEN-LAST:event_buttonPrint10LinesActionPerformed

    private void buttonPrint1LineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPrint1LineActionPerformed
        printLines(1);
    }//GEN-LAST:event_buttonPrint1LineActionPerformed

    private void buttonMoveCursorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonMoveCursorActionPerformed
        TerminalSize terminalSize = scrollingSwingTerminal.getTerminalSize();
        Random random = new Random();
        scrollingSwingTerminal.setCursorPosition(random.nextInt(terminalSize.getColumns()), random.nextInt(terminalSize.getRows()));
        scrollingSwingTerminal.flush();
    }//GEN-LAST:event_buttonMoveCursorActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        scrollingSwingTerminal.clearScreen();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void printLines(int howMany) {
        Random random = new Random();
        String selection = "abcdefghijklmnopqrstuvxyzåäöABCDEFGHIJKLMNOPQRSTUVXYZÅÄÖ";
        for(int i = 0; i < howMany; i++) {
            int words = random.nextInt(10) + 1;
            for(int j = 0; j < words; j++) {
                int length = random.nextInt(10) + 2;
                for(int k = 0; k < length; k++) {
                    scrollingSwingTerminal.putCharacter(selection.charAt(random.nextInt(selection.length())));
                }
                scrollingSwingTerminal.putCharacter(' ');
            }
            scrollingSwingTerminal.putCharacter('\n');
        }
        scrollingSwingTerminal.flush();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch(ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ScrollingSwingTerminalTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch(InstantiationException ex) {
            java.util.logging.Logger.getLogger(ScrollingSwingTerminalTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch(IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ScrollingSwingTerminalTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch(javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ScrollingSwingTerminalTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ScrollingSwingTerminalTest().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonMoveCursor;
    private javax.swing.JButton buttonPrint100Lines;
    private javax.swing.JButton buttonPrint10Lines;
    private javax.swing.JButton buttonPrint1Line;
    private javax.swing.JButton jButton1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPanel panelTerminalContainer;
    // End of variables declaration//GEN-END:variables
}
