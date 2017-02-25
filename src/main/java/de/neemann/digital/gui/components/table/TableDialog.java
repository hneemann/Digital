package de.neemann.digital.gui.components.table;

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
import de.neemann.digital.analyse.quinemc.BoolTableIntArray;
import de.neemann.digital.builder.ATF1502.ATF1502CuplExporter;
import de.neemann.digital.builder.*;
import de.neemann.digital.builder.Gal16v8.Gal16v8CuplExporter;
import de.neemann.digital.builder.Gal16v8.Gal16v8JEDECExporter;
import de.neemann.digital.builder.Gal22v10.Gal22v10CuplExporter;
import de.neemann.digital.builder.Gal22v10.Gal22v10JEDECExporter;
import de.neemann.digital.builder.circuit.CircuitBuilder;
import de.neemann.digital.builder.jedec.FuseMapFillerException;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.Main;
import de.neemann.digital.gui.components.ElementOrderer;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ErrorMessage;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author hneemann
 */
public class TableDialog extends JDialog {
    private static final Color MYGRAY = new Color(230, 230, 230);
    private final JLabel label;
    private final JTable table;
    private final JTableHeader header;
    private final JTextField text;
    private final JPopupMenu renamePopup;
    private final Font font;
    private final ShapeFactory shapeFactory;
    private JCheckBoxMenuItem createJK;
    private File filename;
    private TruthTableTableModel model;
    private TableColumn column;
    private int columnIndex;
    private AllSolutionsDialog allSolutionsDialog;
    private PinMap pinMap;

    /**
     * Creates a new instance
     *
     * @param parent     the parent frame
     * @param truthTable the table to show
     */
    public TableDialog(JFrame parent, TruthTable truthTable, ShapeFactory shapeFactory, File filename) {
        super(parent, Lang.get("win_table"));
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

        allSolutionsDialog = new AllSolutionsDialog(parent, font);

        header = table.getTableHeader();
        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    editColumnAt(event.getPoint());
                }
            }
        });

        text = new JTextField();
        text.setBorder(null);
        text.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                column.setHeaderValue(text.getText());
                renamePopup.setVisible(false);
                header.repaint();
                model.getTable().setColumnName(columnIndex, text.getText());
                calculateExpressions();
            }
        });

        renamePopup = new JPopupMenu();
        renamePopup.setBorder(new MatteBorder(0, 1, 1, 1, Color.DARK_GRAY));
        renamePopup.add(text);

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

    private JMenu createFileMenu() {
        JMenu fileMenu = new JMenu(Lang.get("menu_file"));

        fileMenu.add(new ToolTipAction(Lang.get("menu_open")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                if (TableDialog.this.filename != null)
                    fc.setSelectedFile(Main.checkSuffix(TableDialog.this.filename, "tru"));
                if (fc.showOpenDialog(TableDialog.this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        File file = Main.checkSuffix(fc.getSelectedFile(), "tru");
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
                    fc.setSelectedFile(Main.checkSuffix(TableDialog.this.filename, "tru"));
                if (fc.showSaveDialog(TableDialog.this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        File file = Main.checkSuffix(fc.getSelectedFile(), "tru");
                        model.getTable().save(file);
                        TableDialog.this.filename = file;
                    } catch (IOException e1) {
                        new ErrorMessage().addCause(e1).show(TableDialog.this);
                    }
                }
            }
        });


        fileMenu.add(new ToolTipAction(Lang.get("menu_table_exportTableLaTeX")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String text = new TruthTableFormatterLaTeX().format(model.getTable());
                    text += getExpressionsLaTeX();
                    new ShowStringDialog(TableDialog.this, Lang.get("win_table_exportDialog"), text).setVisible(true);
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
                    fc.setSelectedFile(Main.checkSuffix(TableDialog.this.filename, "hex"));
                if (fc.showSaveDialog(TableDialog.this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        File file = Main.checkSuffix(fc.getSelectedFile(), "hex");
                        model.getTable().saveHex(file);
                    } catch (IOException e1) {
                        new ErrorMessage().addCause(e1).show(TableDialog.this);
                    }
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
                settAllValuesTo(2);
            }
        }.setToolTip(Lang.get("menu_table_setAllToX_tt")).createJMenuItem());
        setMenu.add(new ToolTipAction(Lang.get("menu_table_setAllTo0")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                settAllValuesTo(0);
            }
        }.setToolTip(Lang.get("menu_table_setAllTo0_tt")).createJMenuItem());
        setMenu.add(new ToolTipAction(Lang.get("menu_table_setAllTo1")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                settAllValuesTo(1);
            }
        }.setToolTip(Lang.get("menu_table_setAllTo1_tt")).createJMenuItem());
        return setMenu;
    }

    private void settAllValuesTo(int value) {
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
                createCUPL(new Gal16v8CuplExporter());
            }
        }.setToolTip(Lang.get("menu_table_createCUPL_tt")).createJMenuItem());
        gal16v8.add(new ToolTipAction(Lang.get("menu_table_create_jedec")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Gal16v8JEDECExporter jedecExporter = new Gal16v8JEDECExporter();
                createHardware(jedecExporter, filename);
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
                createHardware(jedecExporter, filename);
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
        hardware.add(atf1502);


        createMenu.add(hardware);

        return createMenu;
    }

    private void createHardware(ExpressionExporter expressionExporter, File filename) {
        if (filename == null)
            filename = new File("circuit.jed");
        else
            filename = Main.checkSuffix(filename, "jed");

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("JEDEC", "jed"));
        fileChooser.setSelectedFile(filename);
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                try (OutputStream out = new FileOutputStream(Main.checkSuffix(fileChooser.getSelectedFile(), "jed"))) {
                    expressionExporter.getPinMapping().addAll(pinMap);
                    new BuilderExpressionCreator(expressionExporter.getBuilder(), ExpressionModifier.IDENTITY).create();
                    expressionExporter.writeTo(out);
                }
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
            CircuitBuilder circuitBuilder = new CircuitBuilder(shapeFactory, useJKff);
            new BuilderExpressionCreator(circuitBuilder, modifier).setUseJKOptimizer(useJKff).create();
            Circuit circuit = circuitBuilder.createCircuit();
            SwingUtilities.invokeLater(() -> new Main(null, circuit).setVisible(true));
        } catch (ExpressionException | FormatterException | RuntimeException e) {
            new ErrorMessage(Lang.get("msg_errorDuringCalculation")).addCause(e).show();
        }
    }

    private void createCUPL(Gal16v8CuplExporter cupl) {
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
            cupl.setProjectName(f.getName());
            cupl.getPinMapping().addAll(pinMap);
            new BuilderExpressionCreator(cupl.getBuilder(), ExpressionModifier.IDENTITY).create();
            try (FileOutputStream out = new FileOutputStream(f)) {
                cupl.writeTo(out);
            }
        } catch (ExpressionException | FormatterException | RuntimeException | IOException | FuseMapFillerException | PinMapException e) {
            new ErrorMessage(Lang.get("msg_errorDuringCalculation")).addCause(e).show();
        }
    }

    private void editColumnAt(Point p) {
        columnIndex = header.columnAtPoint(p);

        if (columnIndex != -1) {
            column = header.getColumnModel().getColumn(columnIndex);
            Rectangle columnRectangle = header.getHeaderRect(columnIndex);

            text.setText(column.getHeaderValue().toString());
            renamePopup.setPreferredSize(
                    new Dimension(columnRectangle.width, columnRectangle.height - 1));
            renamePopup.show(header, columnRectangle.x, 0);

            text.requestFocusInWindow();
            text.selectAll();
        }
    }

    private void setModel(TruthTableTableModel model) {
        this.model = model;
        model.addTableModelListener(new CalculationTableModelListener());
        table.setModel(model);
        calculateExpressions();
    }

    /**
     * Sets a pin map
     *
     * @param pinMap the pin description
     * @return this for chained calls
     */
    public TableDialog setPinMap(PinMap pinMap) {
        this.pinMap = pinMap;
        return this;
    }

    private class CalculationTableModelListener implements TableModelListener {
        @Override
        public void tableChanged(TableModelEvent tableModelEvent) {
            calculateExpressions();
        }
    }

    private void calculateExpressions() {
        try {
            final HTMLExpressionListener html = new HTMLExpressionListener();
            ExpressionListener expressionListener = html;

            if (createJK.isSelected())
                expressionListener = new ExpressionListenerJK(expressionListener);

            new ExpressionCreator(model.getTable()).create(expressionListener);

            switch (html.getExpressionsCount()) {
                case 0:
                    label.setText("");
                    allSolutionsDialog.setVisible(false);
                    break;
                case 1:
                    label.setText(html.getFirstExp());
                    allSolutionsDialog.setVisible(false);
                    break;
                default:
                    label.setText("");
                    allSolutionsDialog.setText(html.getHtml());
                    allSolutionsDialog.setVisible(true);
            }
        } catch (ExpressionException | FormatterException e1) {
            new ErrorMessage(Lang.get("msg_errorDuringCalculation")).addCause(e1).show();
        }
    }

    private String getExpressionsLaTeX() throws ExpressionException, FormatterException {
        StringBuilder sb = new StringBuilder();
        ExpressionListener expressionListener = new ExpressionListener() {
            @Override
            public void resultFound(String name, Expression expression) throws FormatterException, ExpressionException {
                sb.append(FormatToTableLatex.formatIdentifier(name))
                        .append("&=&")
                        .append(FormatToExpression.FORMATTER_LATEX.format(expression))
                        .append("\\\\\n");
            }

            @Override
            public void close() {
            }
        };

        if (createJK.isSelected())
            expressionListener = new ExpressionListenerJK(expressionListener);

        sb.append("\\begin{eqnarray*}\n");
        new ExpressionCreator(model.getTable()).create(expressionListener);
        sb.append("\\end{eqnarray*}\n");
        return sb.toString();
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
                BoolTableIntArray val = new BoolTableIntArray(rows);
                for (int n = 0; n < rows; n++)
                    val.set(n, ((n + 1) >> i) & 1);
                truthTable.addResult(v.getIdentifier() + "+1", val);
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

    private class BuilderExpressionCreator extends ExpressionCreator {
        private final HashSet<String> contained;
        private final BuilderInterface builder;
        private final ExpressionModifier[] modifier;
        private boolean useJKOptimizer = false;

        BuilderExpressionCreator(BuilderInterface builder, ExpressionModifier... modifier) {
            super(TableDialog.this.model.getTable());
            contained = new HashSet<>();
            this.builder = builder;
            this.modifier = modifier;
        }

        public void create() throws ExpressionException, FormatterException {
            ExpressionListener el = new ExpressionListener() {
                @Override
                public void resultFound(String name, Expression expression) throws FormatterException, ExpressionException {
                    if (!contained.contains(name)) {
                        contained.add(name);
                        try {
                            if (name.endsWith("n+1")) {
                                name = name.substring(0, name.length() - 2);
                                builder.addSequential(name, ExpressionModifier.modifyExpression(expression, modifier));
                            } else
                                builder.addCombinatorial(name, ExpressionModifier.modifyExpression(expression, modifier));
                        } catch (BuilderException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                @Override
                public void close() {
                }
            };
            if (useJKOptimizer)
                el = new ExpressionListenerOptimizeJK(el);

            create(el);
        }

        BuilderExpressionCreator setUseJKOptimizer(boolean useJKOptimizer) {
            this.useJKOptimizer = useJKOptimizer;
            return this;
        }
    }

    private static final class HTMLExpressionListener implements ExpressionListener {
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

        private int getExpressionsCount() {
            return count;
        }

        private String getFirstExp() {
            return firstExp;
        }

        private String getHtml() {
            return html.toString();
        }

        @Override
        public void close() {
            html.append("</table></html>");
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
}
