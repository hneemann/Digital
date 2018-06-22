/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
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
import de.neemann.digital.analyse.expression.modify.*;
import de.neemann.digital.analyse.format.TruthTableFormatterLaTeX;
import de.neemann.digital.analyse.quinemc.BoolTableByteArray;
import de.neemann.digital.builder.ATF150x.ATFDevice;
import de.neemann.digital.builder.ExpressionToFileExporter;
import de.neemann.digital.builder.Gal16v8.CuplExporter;
import de.neemann.digital.builder.Gal16v8.Gal16v8JEDECExporter;
import de.neemann.digital.builder.Gal22v10.Gal22v10CuplExporter;
import de.neemann.digital.builder.Gal22v10.Gal22v10JEDECExporter;
import de.neemann.digital.builder.circuit.CircuitBuilder;
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
import de.neemann.digital.gui.components.karnaugh.KarnaughMapDialog;
import de.neemann.digital.gui.components.table.hardware.GenerateCUPL;
import de.neemann.digital.gui.components.table.hardware.GenerateFile;
import de.neemann.digital.gui.components.table.hardware.HardwareDescriptionGenerator;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ErrorMessage;
import de.neemann.gui.MyFileChooser;
import de.neemann.gui.Screen;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;

/**
 */
public class TableDialog extends JDialog {
    private static final Preferences PREFS = Preferences.userRoot().node("dig").node("generator");
    private static final Color MYGRAY = new Color(230, 230, 230);
    private static final List<Key> LIST = new ArrayList<>();

    static {
        LIST.add(Keys.LABEL);
    }

    private final JTextPane statusBar;
    private final JTable table;
    private final Font font;
    private final ElementLibrary library;
    private final ShapeFactory shapeFactory;
    private final ToolTipAction karnaughMenuAction;
    private final HashMap<String, HardwareDescriptionGenerator> availGenerators = new HashMap<>();
    private final JMenu hardwareMenu;
    private JCheckBoxMenuItem createJK;
    private File filename;
    private TruthTableTableModel model;
    private int columnIndex;
    private AllSolutionsDialog allSolutionsDialog;
    private ExpressionListenerStore lastGeneratedExpressions;
    private KarnaughMapDialog kvMap;
    private JMenuItem lastUsedGenratorMenuItem;

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
        kvMap = new KarnaughMapDialog(this, (boolTable, row) -> model.incValue(boolTable, row));

        statusBar = new JTextPane();
        statusBar.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE); // without this setFont not work
        statusBar.setContentType("text/html");
        statusBar.setEditable(false);
        statusBar.setBackground(null);
        statusBar.setBorder(null);
        font = Screen.getInstance().getFont(1.66f);
        statusBar.setFont(font);
        table = new JTable(model);
        JComboBox<String> comboBox = new JComboBox<>(TruthTableTableModel.STATENAMES);
        table.setDefaultEditor(Integer.class, new DefaultCellEditor(comboBox));
        table.setDefaultRenderer(Integer.class, new CenterDefaultTableCellRenderer());
        table.setRowHeight(font.getSize() * 6 / 5);

        table.getInputMap().put(KeyStroke.getKeyStroke("0"), "0_ACTION");
        table.getActionMap().put("0_ACTION", new SetAction(0));
        table.getInputMap().put(KeyStroke.getKeyStroke("1"), "1_ACTION");
        table.getActionMap().put("1_ACTION", new SetAction(1));
        table.getInputMap().put(KeyStroke.getKeyStroke("X"), "X_ACTION");
        table.getActionMap().put("X_ACTION", new SetAction(2));

        new TableReorderManager(this, table);

        allSolutionsDialog = new AllSolutionsDialog(this, font);

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
        if (Main.isExperimentalMode()) {
            JMenu sequentialBiDir = new JMenu(Lang.get("menu_table_new_sequential_bidir"));
            sizeMenu.add(sequentialBiDir);
            for (int i = 2; i <= 8; i++)
                sequentialBiDir.add(new JMenuItem(new SizeSequentialBidirectionalAction(i)));
        }
        bar.add(sizeMenu);

        JMenu columnsMenu = new JMenu(Lang.get("menu_table_columns"));
        bar.add(columnsMenu);
        columnsMenu.add(new ToolTipAction(Lang.get("menu_table_reorder_inputs")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                ReorderInputs ri = new ReorderInputs(model.getTable());
                if (new ElementOrderer<>(TableDialog.this, Lang.get("menu_table_reorder_inputs"), ri.getItems())
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

        columnsMenu.add(new ToolTipAction(Lang.get("menu_table_columnsAddVariable")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                TruthTable t = model.getTable();
                t.addVariable();
                setModel(new TruthTableTableModel(t));
            }
        }.setToolTip(Lang.get("menu_table_columnsAddVariable_tt")).createJMenuItem());

        columnsMenu.add(new ToolTipAction(Lang.get("menu_table_reorder_outputs")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                ReorderOutputs ro = new ReorderOutputs(model.getTable());
                if (new ElementOrderer<>(TableDialog.this, Lang.get("menu_table_reorder_outputs"), ro.getItems())
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

        columnsMenu.add(new ToolTipAction(Lang.get("menu_table_columnsAdd")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                TruthTable t = model.getTable();
                t.addResult();
                setModel(new TruthTableTableModel(t));
            }
        }.setToolTip(Lang.get("menu_table_columnsAdd_tt")).createJMenuItem());

        bar.add(columnsMenu);

        bar.add(createSetMenu());

        hardwareMenu = createCreateMenu();
        bar.add(hardwareMenu);
        checkLastUsedGenerator();

        JMenu karnaughMenu = new JMenu(Lang.get("menu_karnaughMap"));
        karnaughMenuAction = new ToolTipAction(Lang.get("menu_karnaughMap")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                kvMap.setVisible(true);
            }
        }.setToolTip(Lang.get("menu_karnaughMap_tt")).setAccelerator("F1");
        karnaughMenu.add(karnaughMenuAction.createJMenuItem());
        bar.add(karnaughMenu);

        setJMenuBar(bar);

        setModel(new TruthTableTableModel(truthTable));

        getContentPane().add(new JScrollPane(table));
        getContentPane().add(statusBar, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(parent);
    }

    private void editColumnName(int columnIndex, Point pos) {
        ElementAttributes attr = new ElementAttributes();
        final String name = model.getColumnName(columnIndex);
        attr.set(Keys.LABEL, name);
        ElementAttributes modified = new AttributeDialog(this, pos, LIST, attr).showDialog();
        if (modified != null) {
            final String newName = modified.get(Keys.LABEL).trim().replace(' ', '_');
            if (!newName.equals(name))
                model.setColumnName(columnIndex, newName);
        }
    }

    private JMenu createFileMenu() {
        JMenu fileMenu = new JMenu(Lang.get("menu_file"));

        fileMenu.add(new ToolTipAction(Lang.get("menu_open")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new MyFileChooser();
                if (TableDialog.this.filename != null)
                    fc.setSelectedFile(SaveAsHelper.checkSuffix(TableDialog.this.filename, "tru"));
                fc.setFileFilter(new FileNameExtensionFilter(Lang.get("msg_truthTable"), "tru"));
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
                JFileChooser fc = new MyFileChooser();
                if (TableDialog.this.filename != null)
                    fc.setSelectedFile(SaveAsHelper.checkSuffix(TableDialog.this.filename, "tru"));
                fc.setFileFilter(new FileNameExtensionFilter(Lang.get("msg_truthTable"), "tru"));
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
                int res = JOptionPane.OK_OPTION;
                if (model.getTable().getVars().size() > 20)
                    res = JOptionPane.showConfirmDialog(TableDialog.this, Lang.get("msg_tableHasManyRowsConfirm"));
                if (res == JOptionPane.OK_OPTION) {
                    JFileChooser fc = new MyFileChooser();
                    if (TableDialog.this.filename != null)
                        fc.setSelectedFile(SaveAsHelper.checkSuffix(TableDialog.this.filename, "hex"));
                    new SaveAsHelper(TableDialog.this, fc, "hex")
                            .checkOverwrite(file -> model.getTable().saveHex(file));
                }
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

    private JMenu createCreateMenu() {
        JMenu createMenu = new JMenu(Lang.get("menu_table_create"));
        createMenu.add(new ToolTipAction(Lang.get("menu_table_createCircuit")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                createCircuit(ExpressionModifier.IDENTITY);
            }
        }.setToolTip(Lang.get("menu_table_createCircuit_tt")).setAccelerator("F2").enableAcceleratorIn(table).createJMenuItem());
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

        createMenu.add(new ToolTipAction(Lang.get("menu_table_createThree")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                createCircuit(new ThreeInputs());
            }
        }.setToolTip(Lang.get("menu_table_createThree_tt")).createJMenuItem());

        createMenu.add(new ToolTipAction(Lang.get("menu_table_createNAnd")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                createCircuit(new NAnd());
            }
        }.setToolTip(Lang.get("menu_table_createNAnd_tt")).createJMenuItem());

        if (Main.isExperimentalMode()) {
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
        register(hardware, new GenerateCUPL(CuplExporter::new, "GAL16v8/CUPL"));
        register(hardware, new GenerateFile("jed", () -> new ExpressionToFileExporter(new Gal16v8JEDECExporter()),
                "GAL16v8/JEDEC", Lang.get("menu_table_create_jedec_tt")));
        register(hardware, new GenerateCUPL(Gal22v10CuplExporter::new, "GAL22v10/CUPL"));
        register(hardware, new GenerateFile("jed", () -> new ExpressionToFileExporter(new Gal22v10JEDECExporter()),
                "GAL22v10/JEDEC", Lang.get("menu_table_create_jedec_tt")));
        for (ATFDevice atfDev : ATFDevice.values()) {
            register(hardware, new GenerateCUPL(atfDev::getCuplExporter, "ATF150x/" + atfDev.getMenuName() + "/CUPL"));
            register(hardware, new GenerateFile("tt2",
                    () -> atfDev.createExpressionToFileExporter(TableDialog.this, getProjectName()),
                    "ATF150x/" + atfDev.getMenuName() + "/TT2, JEDEC",
                    Lang.get("menu_table_createTT2_tt")));
        }
        createMenu.add(hardware);

        return createMenu;
    }

    private void register(JMenu hardware, final HardwareDescriptionGenerator generator) {
        availGenerators.put(generator.getMenuPath(), generator);
        JMenu m = hardware;
        String path = generator.getMenuPath();
        StringTokenizer tok = new StringTokenizer(path, "/");
        while (tok.hasMoreTokens()) {
            String menuName = tok.nextToken();
            if (tok.hasMoreTokens()) {
                JMenu toUse = null;
                for (int i = 0; i < m.getMenuComponentCount(); i++) {
                    Component menuComponent = m.getMenuComponent(i);
                    if (menuComponent instanceof JMenu) {
                        JMenu menu = (JMenu) menuComponent;
                        if (menu.getText().equalsIgnoreCase(menuName))
                            toUse = menu;
                    }
                }
                if (toUse == null) {
                    toUse = new JMenu(menuName);
                    m.add(toUse);
                }
                m = toUse;
            } else {
                m.add(new HardwareToolTipAction(menuName, generator).createJMenuItem());
            }
        }
    }

    private void createCircuit(ExpressionModifier... modifier) {
        createCircuit(false, modifier);
    }

    private void createCircuit(boolean useJKff, ExpressionModifier... modifier) {
        try {
            CircuitBuilder circuitBuilder = new CircuitBuilder(shapeFactory, useJKff, model.getTable().getVars())
                    .setModelAnalyzerInfo(model.getTable().getModelAnalyzerInfo());
            new BuilderExpressionCreator(circuitBuilder, modifier)
                    .setUseJKOptimizer(useJKff)
                    .create(lastGeneratedExpressions);
            Circuit circuit = circuitBuilder.createCircuit();
            new Main.MainBuilder()
                    .setParent(TableDialog.this)
                    .setLibrary(library)
                    .setCircuit(circuit)
                    .setBaseFileName(filename)
                    .openLater();
        } catch (ExpressionException | FormatterException | RuntimeException e) {
            new ErrorMessage(Lang.get("msg_errorDuringCalculation")).addCause(e).show(this);
        }
    }

    void setModel(TruthTableTableModel model) {
        this.model = model;
        model.addTableModelListener(new CalculationTableModelListener());
        table.setModel(model);
        karnaughMenuAction.setEnabled(model.getTable().getVars().size() <= 4);
        calculateExpressions();
    }

    private String getProjectName() {
        if (filename == null)
            return "unknown";
        else return filename.getName();
    }

    private void setLastUsedGenerator(HardwareDescriptionGenerator generator) {
        if (lastUsedGenratorMenuItem != null)
            hardwareMenu.remove(lastUsedGenratorMenuItem);
        lastUsedGenratorMenuItem = new HardwareToolTipAction(generator.getMenuPath().replace("/", " → "), generator).createJMenuItem();
        hardwareMenu.add(lastUsedGenratorMenuItem);
        PREFS.put("gen", generator.getMenuPath());
    }

    private void checkLastUsedGenerator() {
        String lu = PREFS.get("gen", "");
        if (lu.length() > 0) {
            HardwareDescriptionGenerator gen = availGenerators.get(lu);
            if (gen != null)
                setLastUsedGenerator(gen);
        }
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

            kvMap.setResult(model.getTable(), lastGeneratedExpressions.getResults());

        } catch (ExpressionException | FormatterException | AnalyseException e1) {
            lastGeneratedExpressions = null;
            new ErrorMessage(Lang.get("msg_errorDuringCalculation")).addCause(e1).show(this);
        }
    }

    /**
     * @return the last generated expressions
     */
    public ExpressionListenerStore getLastGeneratedExpressions() {
        return lastGeneratedExpressions;
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
        public void resultFound(String name, Expression expression) throws FormatterException {
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
                    statusBar.setVisible(false);
                    allSolutionsDialog.setVisible(false);
                    break;
                case 1:
                    statusBar.setVisible(true);
                    statusBar.setText(firstExp);
                    allSolutionsDialog.setVisible(false);
                    break;
                default:
                    statusBar.setVisible(false);
                    allSolutionsDialog.setText(html.toString());
                    if (!allSolutionsDialog.isVisible())
                        SwingUtilities.invokeLater(() -> allSolutionsDialog.setVisible(true));
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
            sb = new StringBuilder();
            if (model.getTable().getRows() <= 256) {
                String text = new TruthTableFormatterLaTeX().format(model.getTable());
                sb.append(text);
            }
            sb.append("\\begin{eqnarray*}\n");
        }

        @Override
        public void resultFound(String name, Expression expression) throws FormatterException {
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

    private final class HardwareToolTipAction extends ToolTipAction {
        private final HardwareDescriptionGenerator generator;

        private HardwareToolTipAction(String menuName, HardwareDescriptionGenerator generator) {
            super(menuName);
            this.generator = generator;
            setToolTip(generator.getDescription());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                generator.generate(TableDialog.this, filename, model.getTable(), lastGeneratedExpressions);
                setLastUsedGenerator(generator);
            } catch (Exception e1) {
                new ErrorMessage(Lang.get("msg_errorDuringHardwareExport")).addCause(e1).show(TableDialog.this);
            }
        }
    }
}
