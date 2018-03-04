/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.quinemc;


import de.neemann.digital.analyse.expression.*;
import de.neemann.digital.analyse.quinemc.primeselector.PrimeSelector;
import de.neemann.digital.analyse.quinemc.primeselector.PrimeSelectorDefault;
import de.neemann.digital.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static de.neemann.digital.analyse.expression.Operation.or;

/**
 * The algorithm of Quine and McCluskey
 */
public class QuineMcCluskey {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuineMcCluskey.class);

    private final List<Variable> variables;
    private final ArrayList<TableRow> primes;
    private TableRows rows;

    /**
     * Creates a new instance
     *
     * @param variables the variables to use
     */
    public QuineMcCluskey(List<Variable> variables) {
        this.variables = variables;
        this.rows = new TableRows();
        this.primes = new ArrayList<>();
    }

    QuineMcCluskey(List<Variable> variables, TableRows rows, ArrayList<TableRow> primes) {
        this.variables = variables;
        this.rows = rows;
        this.primes = primes;
    }

    /**
     * Creates a new instance.
     * The Bool table is build using the given expression
     *
     * @param expression the expression used to build the table
     * @throws ExpressionException ExpressionException
     */
    public QuineMcCluskey(Expression expression) throws ExpressionException {
        ContextFiller context = new ContextFiller(expression);
        variables = context.getVariables();
        rows = new TableRows();
        fillTableWith(new BoolTableExpression(expression, context));
        primes = new ArrayList<>();
    }

    /**
     * Fills the instance with the given values
     *
     * @param values the values
     * @return this for chained calls
     * @throws ExpressionException ExpressionException
     */
    public QuineMcCluskey fillTableWith(BoolTable values) throws ExpressionException {
        int n = 1 << variables.size();
        if (n != values.size())
            throw new ExpressionException(Lang.get("err_exact_N0_valuesNecessaryNot_N1", n, values.size()));
        for (int i = 0; i < n; i++) {
            ThreeStateValue value = values.get(i);
            if (!value.equals(ThreeStateValue.zero)) {
                add(i, value.equals(ThreeStateValue.dontCare));
            }
        }
        return this;
    }


    private void add(int i, boolean dontCare) {
        rows.add(new TableRow(variables.size(), i, rows.size() + 1, dontCare));
    }

    /**
     * Simplifies the given expression.
     * If no simplification was found, the original expression is returned unchanged.
     *
     * @param expression the expression to simplify
     * @return the simplified expression
     * @throws ExpressionException ExpressionException
     */
    public static Expression simplify(Expression expression) throws ExpressionException {
        int initialCplx = expression.traverse(new ComplexityInclNotVisitor()).getComplexity();

        Expression newExp = new QuineMcCluskey(expression)
                .simplify()
                .getExpression();

        int newCplx = newExp.traverse(new ComplexityInclNotVisitor()).getComplexity();

        if (newCplx < initialCplx)
            return newExp;
        else
            return expression;
    }

    /**
     * Simplifies the table the the default {@link PrimeSelector}
     *
     * @return the simplified QMC instance
     */
    public QuineMcCluskey simplify() {
        return simplify(new PrimeSelectorDefault());
    }

    /**
     * Simplifies the table the the given {@link PrimeSelector}
     *
     * @param ps the prime selector
     * @return the simplified QMC instance
     */
    public QuineMcCluskey simplify(PrimeSelector ps) {
        while (!isFinished()) {
            LOGGER.debug("QMC rows " + rows.size());
            simplifyStep();
        }
        simplifyPrimes(ps);

        return this;
    }


    /**
     * a single simplification iteration
     */
    public void simplifyStep() {
        TableRows newRows = new TableRows();

        for (TableRows.InnerList list : rows.listIterable())
            for (int i = 0; i < list.size() - 1; i++)
                for (int j = i + 1; j < list.size(); j++) {
                    TableRow r1 = list.get(i);
                    TableRow r2 = list.get(j);

                    int index = r1.checkCompatible(r2);
                    if (index >= 0) {
                        // can optimize;
                        TableRow newRow = new TableRow(r1);
                        newRow.setToOptimized(index);

                        if (!newRows.contains(newRow)) {
                            newRow.addSource(r1.getSource());
                            newRow.addSource(r2.getSource());
                            newRows.add(newRow);
                        }
                        r1.setUsed();
                        r2.setUsed();
                    }
                }

        for (TableRow row : rows)
            if (!row.isUsed() && row.getSource().size() > 0)
                primes.add(row);

        rows = newRows;
    }

    /**
     * @return true id simplification is complete
     */
    public boolean isFinished() {
        return rows.isEmpty();
    }

    /**
     * @return the actual table rows
     */
    public TableRows getRows() {
        return rows;
    }

    /**
     * Sets the table rows.
     *
     * @param rows the rows to use
     */
    public void setRows(TableRows rows) {
        this.rows = rows;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        ArrayList<TableRow> newList = new ArrayList<>();
        for (TableRow r : rows) {
            newList.add(r);
        }
        Collections.sort(newList);
        for (TableRow r : newList) {
            sb.append(r.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * @return the final primes
     */
    public ArrayList<TableRow> getPrimes() {
        return primes;
    }

    /**
     * @return the variables used
     */
    public List<Variable> getVariables() {
        return variables;
    }

    /**
     * @return the simplified expression which represent this table
     */
    public Expression getExpression() {
        if (primes.isEmpty() && rows.isEmpty())
            return Constant.ZERO;

        Expression e = addAnd(null, primes, variables);
        return addAnd(e, rows, variables);
    }

    /**
     * Creates the final expression
     *
     * @param e         the expression to complete
     * @param rows      the rows to add
     * @param variables the variables to use to build the expression
     * @return the expression
     */
    public static Expression addAnd(Expression e, Iterable<TableRow> rows, List<Variable> variables) {
        for (TableRow r : rows) {
            Expression n = r.getExpression(variables);
            if (e == null)
                e = n;
            else
                e = or(e, n);
        }
        return e;
    }

    /**
     * Simplify the primes
     *
     * @param primeSelector the prime selector to use
     */
    public void simplifyPrimes(PrimeSelector primeSelector) {

        TreeSet<Integer> columns = new TreeSet<>();
        for (TableRow r : primes)
            columns.addAll(r.getSource());

        LOGGER.debug("initial primes " + primes.size());

        // remove all primes which are easy to remove
        while (true) {
            // find rows to delete
            HashSet<TableRow> rowsToDelete = new HashSet<>();
            for (TableRow r1 : primes)
                for (TableRow r2 : primes) {
                    if ((r1 != r2) && !rowsToDelete.contains(r1) && r1.getSource().containsAll(r2.getSource()))
                        rowsToDelete.add(r2);
                }

            primes.removeAll(rowsToDelete);

            // find the cols to delete
            HashSet<Integer> colsToDelete = new HashSet<>();
            for (int c1 : columns) {
                for (int c2 : columns) {
                    if ((c1 != c2) && !colsToDelete.contains(c1) && smaller(c1, c2, primes))
                        colsToDelete.add(c2);
                }
            }

            if (colsToDelete.isEmpty() && rowsToDelete.isEmpty())
                break;

            for (TableRow p : primes)
                p.getSource().removeAll(colsToDelete);

            columns.removeAll(colsToDelete);
        }

        LOGGER.debug("residual primes " + primes.size());

        // try to reduce the number of primes needed
        if (primeSelector != null && !columns.isEmpty()) {
            ArrayList<TableRow> availPrimes = new ArrayList<>(primes.size());
            availPrimes.addAll(primes);
            primes.clear();
            primeSelector.select(primes, availPrimes, columns);
            LOGGER.debug("final primes " + primes.size());
        }
    }

    private boolean smaller(int c1, int c2, ArrayList<TableRow> primes) {
        for (TableRow r : primes) {
            Collection<Integer> s = r.getSource();
            if (s.contains(c1) && !s.contains(c2))
                return false;
        }
        return true;
    }
}
