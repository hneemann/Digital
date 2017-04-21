package de.neemann.digital.gui.components.table;

import de.neemann.digital.analyse.AnalyseException;
import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.TruthTableTableModel;
import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.expression.format.FormatToExpression;
import de.neemann.digital.analyse.expression.format.FormatToTableLatex;
import de.neemann.digital.analyse.expression.format.FormatterException;
import de.neemann.digital.analyse.expression.modify.ExpressionModifier;
import de.neemann.digital.analyse.expression.modify.NAnd;
import de.neemann.digital.analyse.expression.modify.NOr;
import de.neemann.digital.analyse.expression.modify.TwoInputs;
import de.neemann.digital.analyse.format.TruthTableFormatterLaTeX;
import de.neemann.digital.analyse.quinemc.BoolTableByteArray;
import de.neemann.digital.builder.ATF1502.*;
import de.neemann.digital.builder.ExpressionToFileExporter;
import de.neemann.digital.builder.Gal16v8.CuplExporter;
import de.neemann.digital.builder.Gal16v8.Gal16v8JEDECExporter;
import de.neemann.digital.builder.Gal22v10.Gal22v10CuplExporter;
import de.neemann.digital.builder.Gal22v10.Gal22v10JEDECExporter;
import de.neemann.digital.builder.PinMapException;
import de.neemann.digital.builder.circuit.CircuitBuilder;
import de.neemann.digital.builder.jedec.FuseMapFillerException;
import de.neemann.digital.builder.tt2.StartATF1502Fitter;
import de.neemann.digital.builder.tt2.StartATF1504Fitter;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.Main;
import de.neemann.digital.gui.SaveAsHelper;
import de.neemann.digital.gui.components.AttributeDialog;
import de.neemann.digital.gui.components.ElementOrderer;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ErrorMessage;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * @author hneemann
 */
public class TableDialog extends JDialog {
    private static final Color MYGRAY = new Color(230, 230, 230);
    private static final List<Key> LIST = new ArrayList<>();

    static {
        LIST.add(Keys.LABEL);
        LIST.add(Keys.PIN);
    }

    private final JLabel label;
    private final JTable table;
    private final Font font;
    private final ElementLibrary library;
    private final ShapeFactory shapeFactory;
    private JCheckBoxMenuItem createJK;
    private File filename;
    private TruthTableTableModel model;
    private int columnIndex;
    private AllSolutionsDialog allSolutionsDialog;
    private ExpressionListenerStore lastGeneratedExpressions;

    /**
     * Creates a new instance
     *
     * @param parent       the parent frame
     * @param truthTable   the table to show
     * @param library      the library to use
     * @param shapeFactory the shape factory
     * @param filename     the file name used to create the names of the created files
     */
    public TableDialog(JFrame parent, TruthTable truthTable, ElementLibrary library, ShapeFactory shapeFactory, File filename) {
        super(parent, Lang.get("win_table"));
        this.library = library;
        this.shapeFactory = shapeFactory;
        this.filename = filename;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);


        label = new JLabel();
        font = label.getFont().deriveFont(20.0f);
        label.setFont(font);
        table = new JTable(model);
        JComboBox<String> comboBox = new JComboBox<>(TruthTableTableModel.STATENAMES);
        table.setDefaultEditor(Integer.class, new DefaultCellEditor(comboBox));
        table.setDefaultRenderer(Integer.class, new CenterDefaultTableCellRenderer());
        table.setRowHeight(25);

        table.getInputMap().put(KeyStroke.getKeyStroke("0"), "0_ACTION");
        table.getActionMap().put("0_ACTION", new SetAction(0));
        table.getInputMap().put(KeyStroke.getKeyStroke("1"), "1_ACTION");
        table.getActionMap().put("1_ACTION", new SetAction(1));
        table.getInputMap().put(KeyStroke.getKeyStroke("X"), "X_ACTION");
        table.getActionMap().put("X_ACTION", new SetAction(2));

        allSolutionsDialog = new AllSolutionsDialog(parent, font);

        JTableHeader header = table.getTableHeader();
        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 1 && event.getButton() == 3) {
                    columnIndex = header.columnAtPoint(event.getPoint());
                    if (columnIndex != -1)
                        editColumnName(columnIndex, new Point(event.getXOnScreen(), event.getYOnScreen()));
                }
            }
        });

        JMenuBar bar = new JMenuBar();
        bar.add(createFileMenu());

        JMenu sizeMenu = new JMenu(Lang.get("menu_table_new"));

        JMenu combinatorial = new JMenu(Lang.get("menu_table_new_combinatorial"));
        sizeMenu.add(combinatorial);
        for (int i = 2; i <= 8; i++)
            combinatorial.add(new JMenuItem(new SizeAction(i)));
        JMenu sequential = new JMenu(Lang.get("menu_table_new_sequential"));
        sizeMenu.add(sequential);
        for (int i = 2; i <= 8; i++)
            sequential.add(new JMenuItem(new SizeSequentialAction(i)));
        if (Main.enableExperimental()) {
            JMenu sequentialBiDir = new JMenu(Lang.get("menu_table_new_sequential_bidir"));
            sizeMenu.add(sequentialBiDir);
            for (int i = 2; i <= 8; i++)
                sequentialBiDir.add(new JMenuItem(new SizeSequentialBidirectionalAction(i)));
        }
        bar.add(sizeMenu);

        JMenu reorderMenu = new JMenu(Lang.get("menu_table_reorder"));
        bar.add(reorderMenu);
        reorderMenu.add(new ToolTipAction(Lang.get("menu_table_inputs")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                ReorderInputs ri = new ReorderInputs(model.getTable());
                if (new ElementOrderer<>(parent, Lang.get("menu_table_inputs"), ri.getItems())
                        .addDeleteButton()
                        .addOkButton()
                        .showDialog()) {
                    try {
                        setModel(new TruthTableTableModel(ri.reorder()));
                    } catch (ExpressionException e1) {
                        new ErrorMessage().addCause(e1).show(TableDialog.this);
                    }
                }
            }
        }.createJMenuItem());
        reorderMenu.add(new ToolTipAction(Lang.get("menu_table_outputs")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                ReorderOutputs ro = new ReorderOutputs(model.getTable());
                if (new ElementOrderer<>(parent, Lang.get("menu_table_outputs"), ro.getItems())
                        .addDeleteButton()
                        .addOkButton()
                        .showDialog()) {
                    try {
                        setModel(new TruthTableTableModel(ro.reorder()));
                    } catch (ExpressionException e1) {
                        new ErrorMessage().addCause(e1).show(TableDialog.this);
                    }
                }
            }
        }.createJMenuItem());

        JMenu colsMenu = new JMenu(Lang.get("menu_table_newColumns"));
        colsMenu.add(new ToolTipAction(Lang.get("menu_table_columnsAdd")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                TruthTable t = model.getTable();
                t.addResult();
                setModel(new TruthTableTableModel(t));
            }
        }.setToolTip(Lang.get("menu_table_columnsAdd_tt")).createJMenuItem());
        colsMenu.add(new ToolTipAction(Lang.get("menu_table_columnsAddVariable")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                TruthTable t = model.getTable();
                t.addVariable();
                setModel(new TruthTableTableModel(t));
            }
        }.setToolTip(Lang.get("menu_table_columnsAddVariable_tt")).createJMenuItem());

        bar.add(colsMenu);

        bar.add(createSetMenu());

        bar.add(createCreateMenu(parent));

        setJMenuBar(bar);

        setModel(new TruthTableTableModel(truthTable));

        getContentPane().add(new JScrollPane(table));
        getContentPane().add(label, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(parent);
    }

    private void editColumnName(int columnIndex, Point pos) {
        ElementAttributes attr = new ElementAttributes();
        final String name = model.getColumnName(columnIndex);
        attr.set(Keys.LABEL, name);
        final TreeMap<String, Integer> pins = model.getTable().getPins();
        if (pins.containsKey(name))
            attr.set(Keys.PIN, pins.get(name).toString());
        if (new AttributeDialog(this, pos, LIST, attr).showDialog()) {
            pins.remove(name);
            final String newName = attr.get(Keys.LABEL).trim().replace(' ', '_');
            final String pinStr = attr.get(Keys.PIN).trim();
            if (pinStr.length() > 0) {
                try {
                    int p = Integer.parseInt(pinStr);
                    pins.put(newName, p);
                } catch (NumberFormatException e) {
                    // Do nothing!
                }
            }
            if (!newName.equals(name))
                model.setColumnName(columnIndex, newName);
        }
    }

    private JMenu createFileMenu() {
        JMenu fileMenu = new JMenu(Lang.get("menu_file"));

        fileMenu.add(new ToolTipAction(Lang.get("menu_open")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                if (TableDialog.this.filename != null)
                    fc.setSelectedFile(SaveAsHelper.checkSuffix(TableDialog.this.filename, "tru"));
                if (fc.showOpenDialog(TableDialog.this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        File file = fc.getSelectedFile();
                        TruthTable truthTable = TruthTable.readFromFile(file);
                        setModel(new TruthTableTableModel(truthTable));
                        TableDialog.this.filename = file;
                    } catch (IOException e1) {
                        new ErrorMessage().addCause(e1).show(TableDialog.this);
                    }
                }
            }
        });

        fileMenu.add(new ToolTipAction(Lang.get("menu_save")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                if (TableDialog.this.filename != null)
                    fc.setSelectedFile(SaveAsHelper.checkSuffix(TableDialog.this.filename, "tru"));

                new SaveAsHelper(TableDialog.this, fc, "tru").checkOverwrite(
                        file -> {
                            model.getTable().save(file);
                            TableDialog.this.filename = file;
                        }
                );
            }
        });


        fileMenu.add(new ToolTipAction(Lang.get("menu_table_exportTableLaTeX")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    ExpressionListener expressionListener = new LaTeXExpressionListener(model);
                    if (createJK.isSelected())
                        expressionListener = new ExpressionListenerJK(expressionListener);
                    lastGeneratedExpressions.replayTo(expressionListener);
                } catch (ExpressionException | FormatterException e1) {
                    new ErrorMessage(Lang.get("msg_errorDuringCalculation")).addCause(e1).show(TableDialog.this);
                }
            }
        });

        fileMenu.add(new ToolTipAction(Lang.get("menu_table_exportHex")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                if (TableDialog.this.filename != null)
                    fc.setSelectedFile(SaveAsHelper.checkSuffix(TableDialog.this.filename, "hex"));
                new SaveAsHelper(TableDialog.this, fc, "hex")
                        .checkOverwrite(file -> model.getTable().saveHex(file));
            }
        }.setToolTip(Lang.get("menu_table_exportHex_tt")).createJMenuItem());


        createJK = new JCheckBoxMenuItem(Lang.get("menu_table_JK"));
        createJK.addActionListener(e -> calculateExpressions());
        fileMenu.add(createJK);

        return fileMenu;
    }

    private JMenu createSetMenu() {
        JMenu setMenu = new JMenu(Lang.get("menu_table_set"));
        setMenu.add(new ToolTipAction(Lang.get("menu_table_setXTo0")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                TruthTable t = model.getTable();
                t.setXto(false);
                setModel(new TruthTableTableModel(t));
            }
        }.setToolTip(Lang.get("menu_table_setXTo0_tt")).createJMenuItem());
        setMenu.add(new ToolTipAction(Lang.get("menu_table_setXTo1")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                TruthTable t = model.getTable();
                t.setXto(true);
                setModel(new TruthTableTableModel(t));
            }
        }.setToolTip(Lang.get("menu_table_setXTo1_tt")).createJMenuItem());
        setMenu.add(new ToolTipAction(Lang.get("menu_table_setAllToX")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                setAllValuesTo(2);
            }
        }.setToolTip(Lang.get("menu_table_setAllToX_tt")).createJMenuItem());
        setMenu.add(new ToolTipAction(Lang.get("menu_table_setAllTo0")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                setAllValuesTo(0);
            }
        }.setToolTip(Lang.get("menu_table_setAllTo0_tt")).createJMenuItem());
        setMenu.add(new ToolTipAction(Lang.get("menu_table_setAllTo1")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                setAllValuesTo(1);
            }
        }.setToolTip(Lang.get("menu_table_setAllTo1_tt")).createJMenuItem());
        return setMenu;
    }

    private void setAllValuesTo(int value) {
        TruthTable t = model.getTable();
        t.setAllTo(value);
        setModel(new TruthTableTableModel(t));
    }

    private JMenu createCreateMenu(JFrame parent) {
        JMenu createMenu = new JMenu(Lang.get("menu_table_create"));
        createMenu.add(new ToolTipAction(Lang.get("menu_table_createCircuit")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                createCircuit(ExpressionModifier.IDENTITY);
            }
        }.setToolTip(Lang.get("menu_table_createCircuit_tt")).createJMenuItem());
        createMenu.add(new ToolTipAction(Lang.get("menu_table_createCircuitJK")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                createCircuit(true, ExpressionModifier.IDENTITY);
            }
        }.setToolTip(Lang.get("menu_table_createCircuitJK_tt")).createJMenuItem());

        createMenu.add(new ToolTipAction(Lang.get("menu_table_createTwo")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                createCircuit(new TwoInputs());
            }
        }.setToolTip(Lang.get("menu_table_createTwo_tt")).createJMenuItem());

        createMenu.add(new ToolTipAction(Lang.get("menu_table_createNAnd")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                createCircuit(new NAnd());
            }
        }.setToolTip(Lang.get("menu_table_createNAnd_tt")).createJMenuItem());

        if (Main.enableExperimental()) {
            createMenu.add(new ToolTipAction(Lang.get("menu_table_createNAndTwo")) {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    createCircuit(new TwoInputs(), new NAnd());
                }
            }.setToolTip(Lang.get("menu_table_createNAndTwo_tt")).createJMenuItem());

            createMenu.add(new ToolTipAction(Lang.get("menu_table_createNOr")) {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    createCircuit(new NOr());
                }
            }.setToolTip(Lang.get("menu_table_createNOr_tt")).createJMenuItem());

            createMenu.add(new ToolTipAction(Lang.get("menu_table_createNOrTwo")) {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    createCircuit(new TwoInputs(), new NOr());
                }
            }.setToolTip(Lang.get("menu_table_createNOrTwo_tt")).createJMenuItem());

        }

        JMenu hardware = new JMenu(Lang.get("menu_table_create_hardware"));
        JMenu gal16v8 = new JMenu("GAL16v8");
        gal16v8.add(new ToolTipAction(Lang.get("menu_table_createCUPL")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                createCUPL(new CuplExporter());
            }
        }.setToolTip(Lang.get("menu_table_createCUPL_tt")).createJMenuItem());
        gal16v8.add(new ToolTipAction(Lang.get("menu_table_create_jedec")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Gal16v8JEDECExporter jedecExporter = new Gal16v8JEDECExporter();
                createHardware(new ExpressionToFileExporter(jedecExporter), filename, "jed");
                new ShowStringDialog(parent, Lang.get("win_pinMapDialog"), jedecExporter.getPinMapping().toString()).setVisible(true);
            }
        }.setToolTip(Lang.get("menu_table_create_jedec_tt")).createJMenuItem());
        hardware.add(gal16v8);

        JMenu gal22v10 = new JMenu("GAL22v10");
        gal22v10.add(new ToolTipAction(Lang.get("menu_table_createCUPL")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                createCUPL(new Gal22v10CuplExporter());
            }
        }.setToolTip(Lang.get("menu_table_createCUPL_tt")).createJMenuItem());
        gal22v10.add(new ToolTipAction(Lang.get("menu_table_create_jedec")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Gal22v10JEDECExporter jedecExporter = new Gal22v10JEDECExporter();
                createHardware(new ExpressionToFileExporter(jedecExporter), filename, "jed");
                new ShowStringDialog(parent, Lang.get("win_pinMapDialog"), jedecExporter.getPinMapping().toString()).setVisible(true);
            }
        }.setToolTip(Lang.get("menu_table_create_jedec_tt")).createJMenuItem());
        hardware.add(gal22v10);


        JMenu atf1502 = new JMenu("ATF1502");
        atf1502.add(new ToolTipAction(Lang.get("menu_table_createCUPL")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                createCUPL(new ATF1502CuplExporter());
            }
        }.setToolTip(Lang.get("menu_table_createCUPL_tt")).createJMenuItem());
        atf1502.add(new ToolTipAction(Lang.get("menu_table_createTT2")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                createHardware(
                        new ExpressionToFileExporter(new ATF1502TT2Exporter(filename.getName()))
                                .addProcessingStep(new StartATF1502Fitter(TableDialog.this))
                                .addProcessingStep(new CreateCHN("ATF1502AS")), filename, "tt2");
            }
        }.setToolTip(Lang.get("menu_table_createTT2_tt")).createJMenuItem());
        hardware.add(atf1502);

        JMenu atf1504 = new JMenu("ATF1504");
        atf1504.add(new ToolTipAction(Lang.get("menu_table_createCUPL")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                createCUPL(new ATF1504CuplExporter());
            }
        }.setToolTip(Lang.get("menu_table_createCUPL_tt")).createJMenuItem());
        atf1504.add(new ToolTipAction(Lang.get("menu_table_createTT2")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                createHardware(
                        new ExpressionToFileExporter(new ATF1504TT2Exporter(filename.getName()))
                                .addProcessingStep(new StartATF1504Fitter(TableDialog.this))
                                .addProcessingStep(new CreateCHN("ATF1504AS")), filename, "tt2");
            }
        }.setToolTip(Lang.get("menu_table_createTT2_tt")).createJMenuItem());
        hardware.add(atf1504);


        createMenu.add(hardware);

        return createMenu;
    }

    private void createHardware(ExpressionToFileExporter expressionExporter, File filename, String suffix) {
        if (filename == null)
            filename = new File("circuit." + suffix);
        else
            filename = SaveAsHelper.checkSuffix(filename, suffix);

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("JEDEC", suffix));
        fileChooser.setSelectedFile(filename);
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                expressionExporter.getPinMapping().addAll(model.getTable().getPins());
                new BuilderExpressionCreator(expressionExporter.getBuilder(), ExpressionModifier.IDENTITY).create(lastGeneratedExpressions);
                expressionExporter.export(SaveAsHelper.checkSuffix(fileChooser.getSelectedFile(), suffix));
            } catch (ExpressionException | FormatterException | IOException | FuseMapFillerException | PinMapException e) {
                new ErrorMessage(Lang.get("msg_errorDuringCalculation")).addCause(e).show(this);
            }
        }
    }

    private void createCircuit(ExpressionModifier... modifier) {
        createCircuit(false, modifier);
    }

    private void createCircuit(boolean useJKff, ExpressionModifier... modifier) {
        try {
            CircuitBuilder circuitBuilder = new CircuitBuilder(shapeFactory, useJKff, model.getTable().getVars());
            new BuilderExpressionCreator(circuitBuilder, modifier).setUseJKOptimizer(useJKff).create(lastGeneratedExpressions);
            Circuit circuit = circuitBuilder.createCircuit();
            SwingUtilities.invokeLater(new Main.MainBuilder()
                    .setParent(TableDialog.this)
                    .setLibrary(library)
                    .setCircuit(circuit));
        } catch (ExpressionException | FormatterException | RuntimeException e) {
            new ErrorMessage(Lang.get("msg_errorDuringCalculation")).addCause(e).show();
        }
    }

    private void createCUPL(CuplExporter cupl) {
        try {
            File cuplPath;
            if (filename == null) {
                JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fc.setDialogTitle(Lang.get("msg_selectAnEmptyFolder"));
                if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    cuplPath = fc.getSelectedFile();
                    filename = cuplPath;
                } else {
                    return;
                }
            } else {
                if (filename.isDirectory()) {
                    cuplPath = filename;
                } else {
                    String name = filename.getName();
                    if (name.length() > 3 && name.charAt(name.length() - 4) == '.')
                        name = name.substring(0, name.length() - 4);
                    cuplPath = new File(filename.getParentFile(), "CUPL_" + name);
                }
            }

            if (!cuplPath.mkdirs())
                if (!cuplPath.exists())
                    throw new IOException(Lang.get("err_couldNotCreateFolder_N0", cuplPath.getPath()));

            File f = new File(cuplPath, "CUPL.PLD");
            cupl.setProjectName(filename.getName());
            cupl.getPinMapping().addAll(model.getTable().getPins());
            new BuilderExpressionCreator(cupl.getBuilder(), ExpressionModifier.IDENTITY).create(lastGeneratedExpressions);
            try (FileOutputStream out = new FileOutputStream(f)) {
                cupl.writeTo(out);
            }
        } catch (ExpressionException | FormatterException | RuntimeException | IOException | FuseMapFillerException | PinMapException e) {
            new ErrorMessage(Lang.get("msg_errorDuringCalculation")).addCause(e).show();
        }
    }

    private void setModel(TruthTableTableModel model) {
        this.model = model;
        model.addTableModelListener(new CalculationTableModelListener());
        table.setModel(model);
        calculateExpressions();
    }

    private class CalculationTableModelListener implements TableModelListener {
        @Override
        public void tableChanged(TableModelEvent tableModelEvent) {
            calculateExpressions();
        }
    }

    private void calculateExpressions() {
        try {
            ExpressionListener expressionListener = new HTMLExpressionListener();

            if (createJK.isSelected())
                expressionListener = new ExpressionListenerJK(expressionListener);

            lastGeneratedExpressions = new ExpressionListenerStore(expressionListener);
            new ExpressionCreator(model.getTable()).create(lastGeneratedExpressions);

        } catch (ExpressionException | FormatterException | AnalyseException e1) {
            lastGeneratedExpressions = null;
            new ErrorMessage(Lang.get("msg_errorDuringCalculation")).addCause(e1).show();
        }
    }

    private final class SizeAction extends AbstractAction {
        private int n;

        private SizeAction(int n) {
            super(Lang.get("menu_table_N_variables", n));
            this.n = n;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            setModel(new TruthTableTableModel(new TruthTable(n).addResult()));
        }
    }

    private final class SizeSequentialAction extends AbstractAction {
        private int n;

        private SizeSequentialAction(int n) {
            super(Lang.get("menu_table_N_variables", n));
            this.n = n;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            ArrayList<Variable> vars = new ArrayList<>();
            for (int i = n - 1; i >= 0; i--)
                vars.add(new Variable("Q_" + i + "n"));
            TruthTable truthTable = new TruthTable(vars);
            int i = n - 1;
            int rows = 1 << n;
            for (Variable v : vars) {
                BoolTableByteArray val = new BoolTableByteArray(rows);
                for (int n = 0; n < rows; n++)
                    val.set(n, ((n + 1) >> i) & 1);
                truthTable.addResult(v.getIdentifier() + "+1", val);
                i--;
            }

            setModel(new TruthTableTableModel(truthTable));
        }
    }

    private final class SizeSequentialBidirectionalAction extends AbstractAction {
        private int n;

        private SizeSequentialBidirectionalAction(int n) {
            super(Lang.get("menu_table_N_variables", n));
            this.n = n;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            ArrayList<Variable> vars = new ArrayList<>();
            vars.add(new Variable("dir"));
            for (int i = n - 1; i >= 0; i--)
                vars.add(new Variable("Q_" + i + "n"));
            TruthTable truthTable = new TruthTable(vars);
            int i = n - 1;
            int rows = 1 << (n + 1);
            for (int vi = 1; vi < vars.size(); vi++) {
                BoolTableByteArray val = new BoolTableByteArray(rows);
                for (int n = 0; n < rows; n++) {
                    if (n >= rows / 2)
                        val.set(n, ((n - 1) >> i) & 1);
                    else
                        val.set(n, ((n + 1) >> i) & 1);
                }
                truthTable.addResult(vars.get(vi).getIdentifier() + "+1", val);
                i--;
            }

            setModel(new TruthTableTableModel(truthTable));
        }
    }

    private final class CenterDefaultTableCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(font);
            if (column < model.getTable().getVars().size())
                label.setBackground(MYGRAY);
            else
                label.setBackground(Color.WHITE);

            if (value instanceof Integer) {
                int v = (int) value;
                if (v > 1)
                    label.setText("x");
            }

            return label;
        }
    }

    private final class HTMLExpressionListener implements ExpressionListener {
        private FormatToExpression htmlFormatter = new HTMLFormatter(FormatToExpression.getDefaultFormat());
        private final StringBuilder html;
        private int count;
        private String firstExp;

        private HTMLExpressionListener() {
            html = new StringBuilder("<html><table style=\"white-space: nowrap\">\n");
            count = 0;
        }

        @Override
        public void resultFound(String name, Expression expression) throws FormatterException, ExpressionException {
            if (count == 0)
                firstExp = "<html>" + htmlFormatter.identifier(name) + "=" + htmlFormatter.format(expression) + "</html>";
            html.append("<tr>");
            html.append("<td>").append(htmlFormatter.identifier(name)).append("</td>");
            html.append("<td>=</td>");
            html.append("<td>").append(htmlFormatter.format(expression)).append("</td>");
            html.append("</tr>\n");
            count++;
        }

        @Override
        public void close() {
            html.append("</table></html>");

            switch (count) {
                case 0:
                    label.setText("");
                    allSolutionsDialog.setVisible(false);
                    break;
                case 1:
                    label.setText(firstExp);
                    allSolutionsDialog.setVisible(false);
                    break;
                default:
                    label.setText("");
                    allSolutionsDialog.setText(html.toString());
                    if (!allSolutionsDialog.isVisible())
                        allSolutionsDialog.setVisible(true);
            }
        }
    }

    private final static class HTMLFormatter extends FormatToExpression {
        private HTMLFormatter(FormatToExpression format) {
            super(format);
        }

        @Override
        public String identifier(String ident) {
            int p = ident.indexOf("_");
            if (p < 0)
                return ident;
            else
                return ident.substring(0, p) + "<sub>" + ident.substring(p + 1) + "</sub>";
        }
    }

    private final class SetAction extends AbstractAction {
        private final int value;

        private SetAction(int value) {
            this.value = value;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            int r = table.getSelectedRow();
            int c = table.getSelectedColumn();
            if (r < 0 || c < 0) {
                r = 0;
                c = model.getTable().getVars().size();
            }
            model.setValueAt(value, r, c);

            c++;
            if (c >= table.getColumnCount()) {
                c = model.getTable().getVars().size();
                r++;
                if (r >= model.getRowCount())
                    r = 0;
            }
            table.changeSelection(r, c, false, false);
        }
    }

    private final class LaTeXExpressionListener implements ExpressionListener {
        private final StringBuilder sb;

        private LaTeXExpressionListener(TruthTableTableModel model) throws ExpressionException {
            String text = new TruthTableFormatterLaTeX().format(model.getTable());
            sb = new StringBuilder(text);
            sb.append("\\begin{eqnarray*}\n");
        }

        @Override
        public void resultFound(String name, Expression expression) throws FormatterException, ExpressionException {
            sb.append(FormatToTableLatex.formatIdentifier(name))
                    .append("&=&")
                    .append(FormatToExpression.FORMATTER_LATEX.format(expression))
                    .append("\\\\\n");
        }

        @Override
        public void close() {
            sb.append("\\end{eqnarray*}\n");
            new ShowStringDialog(TableDialog.this, Lang.get("win_table_exportDialog"), sb.toString()).setVisible(true);
        }
    }
}
