/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.ATF150x;

import de.neemann.digital.builder.tt2.OSExecute;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.gui.Settings;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ErrorMessage;
import de.neemann.gui.LineBreaker;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static de.neemann.gui.Screen.isLinux;

/**
 * DIalog used to show the result of the external fitter.
 */
public class ATFDialog extends JDialog {
    private final JDialog parent;
    private final JLabel label;
    private final ToolTipAction startATMISP;
    private final JButton okButton;
    private File chnFile;
    private String fitterResult;

    ATFDialog(JDialog parent) {
        super(parent, Lang.get("msg_fitterResult"), true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.parent = parent;

        label = new JLabel();
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        getContentPane().add(label);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        startATMISP = new ToolTipAction(Lang.get("btn_startATMISP")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                startATMISP.setEnabled(false);
                try {
                    File atmisp = Settings.getInstance().get(Keys.SETTINGS_ATMISP);

                    ArrayList<String> args = new ArrayList<>();
                    if (isLinux())
                        args.add("wine");
                    args.add(atmisp.getPath());
                    args.add(chnFile.getName());

                    OSExecute os = new OSExecute(args)
                            .setWorkingDir(chnFile.getParentFile())
                            .setTimeOutSec(600);
                    try {
                        os.startProcess();
                        addWindowListener(new WindowAdapter() {
                            @Override
                            public void windowClosed(WindowEvent e) {
                                if (os.isAlive()) {
                                    JOptionPane.showMessageDialog(ATFDialog.this,
                                            Lang.get("msg_ATMISPIsStillRunning"));
                                }

                                os.terminate();

                                try {
                                    os.waitForProcess();
                                } catch (IOException e1) {
                                    new ErrorMessage(Lang.get("msg_errorStartingATMISP")).addCause(e1).show(ATFDialog.this);
                                }
                            }
                        });
                    } catch (IOException e) {
                        new ErrorMessage(Lang.get("msg_errorStartingATMISP")).addCause(e).show(ATFDialog.this);
                    }
                } finally {
                    startATMISP.setEnabled(true);
                }
            }
        }.setToolTip(Lang.get("btn_startATMISP_tt")).setEnabledChain(false);
        buttons.add(startATMISP.createJButton());
        okButton = new ToolTipAction(Lang.get("ok")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                dispose();
            }
        }.createJButton();
        buttons.add(okButton);
        getContentPane().add(buttons, BorderLayout.SOUTH);
        SwingUtilities.getRootPane(okButton).setDefaultButton(okButton);
    }

    void setChnFile(File chnFile) {
        this.chnFile = chnFile;
        checkStartATMISP();
    }

    private void checkStartATMISP() {
        if (fitterResult != null
                && chnFile != null
                && fitterResult.contains("Design fits successfully")) {
            startATMISP.setEnabled(true);
        } else
            startATMISP.setEnabled(false);
        okButton.requestFocusInWindow();
    }

    /**
     * Sets the result of the fitter.
     *
     * @param fitterResult the result message
     */
    public void setFitterResult(String fitterResult) {
        this.fitterResult = fitterResult;
        label.setText(new LineBreaker().preserveContainedLineBreaks().toHTML().breakLines(fitterResult));
        checkStartATMISP();
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }
}
