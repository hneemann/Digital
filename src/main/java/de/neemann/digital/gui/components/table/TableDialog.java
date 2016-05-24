package de.neemann.digital.gui.components.table;

import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.TruthTableTableModel;
import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.expression.format.FormatToExpression;
import de.neemann.digital.analyse.expression.format.FormatterException;
import de.neemann.digital.analyse.expression.modify.ExpressionModifier;
import de.neemann.digital.analyse.expression.modify.NAnd;
import de.neemann.digital.analyse.expression.modify.NOr;
import de.neemann.digital.analyse.expression.modify.TwoInputs;
import de.neemann.digital.analyse.quinemc.BoolTableIntArray;
import de.neemann.digital.draw.builder.BuilderException;
import de.neemann.digital.draw.builder.BuilderInterface;
import de.neemann.digital.draw.builder.CircuitBuilder;
import de.neemann.digital.draw.builder.CuplCreator;
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
    private final File filename;
    private TruthTableTableModel model;
    private TableColumn column;
    private int columnIndex;
    private AllSolutionsDialog allSolutionsDialog;

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
        JComboBox<String> comboBox = new JComboBox<String>(TruthTableTableModel.STATENAMES);
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

        JMenu sizeMenu = new JMenu(Lang.get("menu_table_new"));

        JMenu combinatorial = new JMenu(Lang.get("menu_table_new_combinatorial"));
        sizeMenu.add(combinatorial);
        for (int i = 2; i <= 8; i++)
            combinatorial.add(new JMenuItem(new SizeAction(i)));
        JMenu sequential = new JMenu(Lang.get("menu_table_new_sequential"));
        sizeMenu.add(sequential);
        for (int i = 2; i <= 5; i++)
            sequential.add(new JMenuItem(new SizeSequentialAction(i)));
        bar.add(sizeMenu);

        JMenu reorderMenu = new JMenu(Lang.get("menu_table_reorder"));
        bar.add(reorderMenu);
        reorderMenu.add(new ToolTipAction(Lang.get("menu_table_inputs")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                ReorderInputs ri = new ReorderInputs(model.getTable());
                new ElementOrderer<>(parent, Lang.get("menu_table_inputs"), ri.getItems(), true).setVisible(true);
                try {
                    setModel(new TruthTableTableModel(ri.reorder()));
                } catch (ExpressionException e1) {
                    new ErrorMessage().addCause(e1).show(TableDialog.this);
                }
            }
        }.createJMenuItem());
        reorderMenu.add(new ToolTipAction(Lang.get("menu_table_outputs")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                ReorderOutputs ro = new ReorderOutputs(model.getTable());
                new ElementOrderer<>(parent, Lang.get("menu_table_inputs"), ro.getItems(), true).setVisible(true);
                try {
                    setModel(new TruthTableTableModel(ro.reorder()));
                } catch (ExpressionException e1) {
                    new ErrorMessage().addCause(e1).show(TableDialog.this);
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

        JMenu createMenu = new JMenu(Lang.get("menu_table_create"));
        createMenu.add(new ToolTipAction(Lang.get("menu_table_create")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                createCircuit(ExpressionModifier.IDENTITY);
            }
        }.setToolTip(Lang.get("menu_table_create_tt")).createJMenuItem());

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

            createMenu.add(new ToolTipAction(Lang.get("menu_table_createCUPL")) {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    createCUPL();
                }
            }.setToolTip(Lang.get("menu_table_createCUPL_tt")).createJMenuItem());
        }
        bar.add(createMenu);


        setJMenuBar(bar);

        setModel(new TruthTableTableModel(truthTable));

        getContentPane().add(new JScrollPane(table));
        getContentPane().add(label, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(parent);
    }

    private void createCircuit(ExpressionModifier... modifier) {
        try {
            CircuitBuilder circuitBuilder = new CircuitBuilder(shapeFactory);
            new BuiderExpressionCreator(circuitBuilder, modifier).create();
            Circuit circuit = circuitBuilder.createCircuit();
            SwingUtilities.invokeLater(() -> new Main(null, circuit).setVisible(true));
        } catch (ExpressionException | FormatterException | RuntimeException e) {
            new ErrorMessage(Lang.get("msg_errorDuringCalculation")).addCause(e).show();
        }
    }

    private void createCUPL() {
        JFileChooser fileChooser = new JFileChooser();
        if (filename != null && filename.getName().endsWith(".dig")) {
            String name = filename.getName();
            File cuplPath = new File(filename.getParentFile(), "cupl");
            File file = new File(cuplPath, name.substring(0, name.length() - 3) + "PLD");
            if (!cuplPath.exists())
                cuplPath.mkdirs();
            fileChooser.setSelectedFile(file);
        }
        fileChooser.setFileFilter(new FileNameExtensionFilter("PLD", "PLD"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File f = fileChooser.getSelectedFile();
                String name = f.getName();
                if (!name.endsWith(".PLD"))
                    name = name + ".PLD";

                f = new File(f.getParentFile(), name);

                CuplCreator cupl = new CuplCreator(name.substring(0, name.length() - 4));
                new BuiderExpressionCreator(cupl, ExpressionModifier.IDENTITY).create();
                try (FileOutputStream out = new FileOutputStream(f)) {
                    cupl.writeTo(out);
                }
            } catch (ExpressionException | FormatterException | RuntimeException | IOException e) {
                new ErrorMessage(Lang.get("msg_errorDuringCalculation")).addCause(e).show();
            }
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

    private class CalculationTableModelListener implements TableModelListener {
        @Override
        public void tableChanged(TableModelEvent tableModelEvent) {
            calculateExpressions();
        }
    }

    private void calculateExpressions() {
        try {
            final StringBuilder sb = new StringBuilder();
            new ExpressionCreator(model.getTable()) {
                private int count = 0;

                @Override
                public void resultFound(String name, Expression expression) throws FormatterException {
                    String expr = name + "\t=" + FormatToExpression.FORMATTER_UNICODE.format(expression);
                    if (count == 0)
                        label.setText(expr);
                    if (sb.length() > 0) sb.append('\n');
                    sb.append(expr);
                    count++;
                    if (count == 2)
                        allSolutionsDialog.setVisible(true);
                }
            }.create();

            if (sb.length() == 0)
                label.setText("");

            allSolutionsDialog.setText(sb.toString());
        } catch (ExpressionException | FormatterException e1) {
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

    private class BuiderExpressionCreator extends ExpressionCreator {
        private final HashSet<String> contained;
        private final BuilderInterface builder;
        private final ExpressionModifier[] modifier;

        BuiderExpressionCreator(BuilderInterface builder, ExpressionModifier... modifier) {
            super(TableDialog.this.model.getTable());
            contained = new HashSet<>();
            this.builder = builder;
            this.modifier = modifier;
        }

        @Override
        public void resultFound(String name, Expression expression) throws FormatterException {
            if (!contained.contains(name)) {
                contained.add(name);
                try {
                    if (name.endsWith("n+1")) {
                        name = name.substring(0, name.length() - 2);
                        builder.addState(name, ExpressionModifier.modifyExpression(expression, modifier));
                    } else
                        builder.addExpression(name, ExpressionModifier.modifyExpression(expression, modifier));
                } catch (BuilderException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
