/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import de.neemann.digital.XStreamValid;
import de.neemann.digital.analyse.expression.BitSetter;
import de.neemann.digital.analyse.expression.Context;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.quinemc.BoolTable;
import de.neemann.digital.analyse.quinemc.BoolTableByteArray;
import de.neemann.digital.analyse.quinemc.ThreeStateValue;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.undo.Copyable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;

/**
 * The description of a truth table.
 */
public class TruthTable implements Copyable<TruthTable> {

    private final ArrayList<Variable> variables;
    private final ArrayList<Result> results;
    private transient BitSetter bitSetter;
    private transient ModelAnalyserInfo modelAnalyzerInfo;

    /**
     * Load the given file and returns a truth table instance
     *
     * @param filename filename
     * @return the {@link TruthTable}
     * @throws IOException IOException
     */
    public static TruthTable readFromFile(File filename) throws IOException {
        XStream xStream = getxStream();
        try (InputStream in = new FileInputStream(filename)) {
            return (TruthTable) xStream.fromXML(in);
        }
    }

    /**
     * Writes the table to the given file.
     *
     * @param filename the file
     * @throws IOException IOException
     */
    public void save(File filename) throws IOException {
        XStream xStream = getxStream();
        try (Writer out = new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_8)) {
            out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
            xStream.marshal(this, new PrettyPrintWriter(out));
        }
    }

    /**
     * Save the table as hex file to be loaded in a ROM or LUT element.
     *
     * @param filename filename
     * @throws IOException IOException
     */
    public void saveHex(File filename) throws IOException {
        if (results.size() > 63)
            throw new IOException(Lang.get("err_tableHasToManyResultColumns"));

        try (Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_8))) {
            saveHex(out);
        }
    }

    /**
     * Save the table as hex file to be loaded in a ROM or LUT element.
     *
     * @param writer the filename to use
     * @throws IOException IOException
     */
    public void saveHex(Writer writer) throws IOException {
        writer.write("v2.0 raw\n");
        int count = results.get(0).getValues().size();
        for (int i = 0; i < count; i++) {
            long val = 0;
            long mask = 1;
            for (Result r : results) {
                ThreeStateValue v = r.getValues().get(i);
                if (v == ThreeStateValue.one)
                    val |= mask;
                mask *= 2;
            }
            writer.write(Long.toHexString(val));
            writer.write('\n');
        }
    }


    private static XStream getxStream() {
        XStream xStream = new XStreamValid();
        xStream.alias("truthTable", TruthTable.class);
        xStream.alias("variable", Variable.class);
        xStream.aliasAttribute(Variable.class, "identifier", "name");
        xStream.alias("result", Result.class);
        xStream.alias("BoolTable", BoolTableByteArray.class);
        xStream.alias("BoolTableEx", BoolTableExpanded.class);
        return xStream;
    }

    /**
     * Creates a new instance
     */
    public TruthTable() {
        this(new ArrayList<>());
    }

    /**
     * Creates a new instance with <code>vars</code> variables
     *
     * @param vars number of variables
     */
    public TruthTable(int vars) {
        this(Variable.vars(vars));
    }

    /**
     * Creates a new instance with the given variables
     *
     * @param vars the variables
     */
    public TruthTable(ArrayList<Variable> vars) {
        this.variables = vars;
        results = new ArrayList<>();
    }

    /**
     * Creates a new instance
     *
     * @param newVars  the variables to use
     * @param oldTable delivers the column names for the results
     */
    public TruthTable(ArrayList<Variable> newVars, TruthTable oldTable) {
        this(newVars);
        for (int i = 0; i < oldTable.getResultCount(); i++) {
            addResult(oldTable.results.get(i).getName(), new BoolTableByteArray(getRows()));
        }
    }

    private TruthTable(TruthTable truthTable) {
        variables = new ArrayList<>(truthTable.variables.size());
        for (Variable v : truthTable.variables)
            variables.add(new Variable(v.getIdentifier()));
        results = new ArrayList<>();
        for (int i = 0; i < truthTable.getResultCount(); i++) {
            Result result = truthTable.results.get(i);
            addResult(result.getName(), new BoolTableByteArray(result.values));
        }
        this.modelAnalyzerInfo = truthTable.modelAnalyzerInfo;
    }

    /**
     * Clears the table and sets the given variables
     *
     * @param vars the variables to use
     */
    public void clear(Collection<? extends Variable> vars) {
        variables.clear();
        variables.addAll(vars);
        results.clear();
        bitSetter = null;
    }

    /**
     * Returns the number of rows
     *
     * @return the number of rows
     */
    public int getRows() {
        return 1 << variables.size();
    }

    /**
     * Adds a new result row
     *
     * @param name   name of the result column
     * @param values the values
     * @return this for call chaining
     */
    public TruthTable addResult(String name, BoolTable values) {
        results.add(new Result(name, values));
        return this;
    }

    /**
     * Adds a new column
     *
     * @return this for call chaining
     */
    public TruthTable addResult() {
        char v = 'Y';
        String var;
        do {
            var = "" + v;
            v--;
        } while (resultsContains(var) && v >= 'Q');
        return addResult(var);
    }

    /**
     * Adds a new column
     *
     * @param name name of result column
     * @return this for call chaining
     */
    public TruthTable addResult(String name) {
        results.add(new Result(name, new BoolTableByteArray(getRows())));
        return this;
    }

    private boolean resultsContains(String var) {
        for (Result r : results)
            if (r.getName().equals(var))
                return true;
        return false;
    }

    /**
     * Adds a new variable
     */
    public void addVariable() {
        char v = 'A';
        Variable var;
        do {
            var = new Variable("" + v);
            v++;
        } while (variables.contains(var) && v <= 'Z');
        addVariable(var);
    }

    /**
     * Adds a variable
     *
     * @param name name of the variable
     * @return this for chained calls
     */
    public TruthTable addVariable(String name) {
        return addVariable(new Variable(name));
    }

    /**
     * Adds a variable
     *
     * @param var the variable to add
     * @return this for chained calls
     */
    public TruthTable addVariable(Variable var) {
        variables.add(var);
        for (Result r : results)
            r.setValues(BoolTableByteArray.createDoubledValues(r.getValues()));

        bitSetter = null;
        return this;
    }

    private BitSetter getBitSetter() {
        if (bitSetter == null)
            bitSetter = new DummyBitSetter(variables.size());
        return bitSetter;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Variable s : variables)
            sb.append(s.getIdentifier()).append("\t");
        for (Result s : results)
            sb.append(s.getName()).append("\t");

        if (getRows() <= 256) {
            sb.append('\n');
            for (int row = 0; row < getRows(); row++) {
                for (int col = 0; col < variables.size(); col++) {
                    if (getBitSetter().getBit(row, col))
                        sb.append("1\t");
                    else
                        sb.append("0\t");
                }
                for (int col = 0; col < results.size(); col++) {
                    switch (results.get(col).getValues().get(row)) {
                        case one:
                            sb.append("1\t");
                            break;
                        case zero:
                            sb.append("0\t");
                            break;
                        default:
                            sb.append("x\t");
                            break;
                    }
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * @return number of collumns
     */
    public int getCols() {
        return variables.size() + results.size();
    }

    /**
     * Returns the name of the column with the given index
     *
     * @param columnIndex the col index
     * @return the name
     */
    public String getColumnName(int columnIndex) {
        if (columnIndex < variables.size())
            return variables.get(columnIndex).getIdentifier();
        else
            return results.get(columnIndex - variables.size()).getName();
    }

    /**
     * Returns the value of the table as a int value
     *
     * @param rowIndex    the row
     * @param columnIndex the col
     * @return the table value (2 means "don't care")
     */
    public int getValue(int rowIndex, int columnIndex) {
        if (columnIndex < variables.size()) {
            if (getBitSetter().getBit(rowIndex, columnIndex)) return 1;
            else return 0;
        } else
            return results.get(columnIndex - variables.size()).getValues().get(rowIndex).asInt();
    }

    /**
     * Returns true if given column is editable
     *
     * @param columnIndex the column
     * @return thrue if editable
     */
    public boolean isEditable(int columnIndex) {
        if (columnIndex < variables.size())
            return false;
        else {
            BoolTable v = results.get(columnIndex - variables.size()).getValues();
            return v instanceof BoolTableByteArray;
        }
    }

    /**
     * Sets modifies the table
     *
     * @param rowIndex    the row
     * @param columnIndex the column
     * @param aValue      the new value
     */
    public void setValue(int rowIndex, int columnIndex, int aValue) {
        if (columnIndex >= variables.size()) {
            BoolTable v = results.get(columnIndex - variables.size()).getValues();
            if (v instanceof BoolTableByteArray)
                ((BoolTableByteArray) v).set(rowIndex, aValue);
        }
    }

    /**
     * Sets the column name
     *
     * @param columnIndex the column
     * @param name        the new name
     */
    public void setColumnName(int columnIndex, String name) {
        if (columnIndex < variables.size())
            variables.set(columnIndex, new Variable(name));
        else {
            results.get(columnIndex - variables.size()).setName(name);
        }
    }

    /**
     * @return the used variables
     */
    public ArrayList<Variable> getVars() {
        return variables;
    }

    /**
     * Gets the value which is determined by the actual context state
     *
     * @param result  the result index
     * @param context the context
     * @return the table value
     * @throws ExpressionException ExpressionException
     */
    public int getByContext(int result, Context context) throws ExpressionException {
        return results.get(result).getValues().get(getIndexByContext(context)).asInt();
    }

    /**
     * Sets the value which is determined by the actual context state
     *
     * @param result  the result index
     * @param context the context
     * @param value   the new value
     * @throws ExpressionException ExpressionException
     */
    public void setByContext(int result, Context context, int value) throws ExpressionException {
        BoolTable v = results.get(result).getValues();
        if (v instanceof BoolTableByteArray)
            ((BoolTableByteArray) v).set(getIndexByContext(context), value);
    }

    private int getIndexByContext(Context context) throws ExpressionException {
        int mask = 1 << (variables.size() - 1);
        int index = 0;
        for (int i = 0; i < variables.size(); i++) {
            if (context.get(variables.get(i))) {
                index |= mask;
            }
            mask >>= 1;
        }
        return index;
    }

    /**
     * @return the number of results
     */
    public int getResultCount() {
        return results.size();
    }

    /**
     * Returns the result with the given index
     *
     * @param result the result index
     * @return the table representing the result
     */
    public BoolTable getResult(int result) {
        return results.get(result).getValues();
    }

    /**
     * Returns the result with the given name
     *
     * @param resultName the result name
     * @return the table representing the result or null if not found
     */
    public BoolTable getResult(String resultName) {
        for (Result r : results)
            if (r.getName().equals(resultName))
                return r.getValues();
        return null;
    }

    /**
     * Returns the results name
     *
     * @param result index of result
     * @return the name
     */
    public String getResultName(int result) {
        return results.get(result).getName();
    }

    /**
     * Removes the given result column
     *
     * @param i the index of the result to remove
     */
    public void removeResult(int i) {
        results.remove(i);
    }

    /**
     * Modifies all column in the table
     *
     * @param m the modifier to use
     * @return this for chained calls
     */
    public TruthTable modifyValues(BoolTableByteArray.TableModifier m) {
        for (Result r : results) {
            BoolTable bt = r.getValues();
            if (bt instanceof BoolTableByteArray)
                ((BoolTableByteArray) bt).modify(m);
        }
        return this;
    }

    /**
     * Sets additional data obtained from the model
     *
     * @param modelAnalyzerInfo the data obtained from the model
     */
    public void setModelAnalyzerInfo(ModelAnalyserInfo modelAnalyzerInfo) {
        this.modelAnalyzerInfo = modelAnalyzerInfo;
    }

    /**
     * returns additional model infos
     *
     * @return infos obtained from the analysed model, maybe null
     */
    public ModelAnalyserInfo getModelAnalyzerInfo() {
        return modelAnalyzerInfo;
    }

    @Override
    public TruthTable createDeepCopy() {
        return new TruthTable(this);
    }

    /**
     * @return the names of all input variables
     */
    public ArrayList<String> getVarNames() {
        ArrayList<String> names = new ArrayList<>();
        for (Variable v : variables)
            names.add(v.getIdentifier());
        return names;
    }

    /**
     * @return the names of al result variables
     */
    public ArrayList<String> getResultNames() {
        ArrayList<String> names = new ArrayList<>();
        for (Result r : results)
            names.add(r.getName());
        return names;
    }

    /**
     * A single result column
     */
    private static final class Result {
        private String name;
        private BoolTable values;

        /**
         * Creates a new instance
         *
         * @param name   the name of the result
         * @param values the result values
         */
        Result(String name, BoolTable values) {
            this.name = name;
            this.values = values;
        }

        /**
         * @return the result values name
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the values name
         *
         * @param name the values name
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * @return the result values
         */
        public BoolTable getValues() {
            return values;
        }

        /**
         * Sets new values
         *
         * @param values the values to set
         */
        public void setValues(BoolTable values) {
            this.values = values;
        }

    }

    private static final class DummyBitSetter extends BitSetter {
        private DummyBitSetter(int bitCount) {
            super(bitCount);
        }

        @Override
        public void setBit(int row, int bit, boolean value) {
        }
    }
}
