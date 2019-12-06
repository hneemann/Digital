/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.jedec;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.Not;
import de.neemann.digital.analyse.expression.Operation;
import de.neemann.digital.analyse.expression.Variable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Fills an equation into a fuse map
 * Assumes that all product terms follow each other directly in the fuse map.
 * Assumes that the fuse and not fuse follow each other.
 */
public class FuseMapFiller {

    private final FuseMap fuseMap;
    private final int varsConnectedToMap;
    private final HashMap<Variable, MapEntry> varMap;

    /**
     * Creates a new instance
     *
     * @param fuseMap            the fuse map to fill
     * @param varsConnectedToMap the number variables available in matrix
     */
    public FuseMapFiller(FuseMap fuseMap, int varsConnectedToMap) {
        this.fuseMap = fuseMap;
        this.varsConnectedToMap = varsConnectedToMap;
        varMap = new HashMap<>();
    }

    /**
     * Adds a variable to the matrix
     *
     * @param index number in matrix
     * @param var   the variable
     * @return this for chained calls
     */
    public FuseMapFiller addVariable(int index, Variable var) {
        varMap.put(var, new MapEntry(index, false));
        return this;
    }

    /**
     * Adds a variable to the matrix
     * In difference to addVariable() the inverted column comes first and the non inverted column follows.
     * So the both columns are in reverse order compared to addVariable()
     *
     * @param index number in matrix
     * @param var   the variable
     * @return this for chained calls
     */
    public FuseMapFiller addVariableReverse(int index, Variable var) {
        varMap.put(var, new MapEntry(index, true));
        return this;
    }


    /**
     * Fills an expression to the fuse map
     *
     * @param offs         number of first fuse of first product term to use
     * @param exp          the expression
     * @param productTerms the number of product terms available
     * @throws FuseMapFillerException EquationHandlerException
     */
    public void fillExpression(int offs, Expression exp, int productTerms) throws FuseMapFillerException {
        ArrayList<Expression> terms;

        if (exp instanceof Operation.Or) {
            Operation.Or or = (Operation.Or) exp;
            terms = or.getExpressions();
        } else {
            terms = new ArrayList<>();
            terms.add(exp);
        }

        if (terms.size() > productTerms)
            throw new FuseMapFillerException("only " + productTerms + " product terms supported but " + terms.size() + " needed!");

        int fusesInTerm = varsConnectedToMap * 2;

        for (Expression e : terms) {

            for (int i = 0; i < fusesInTerm; i++)
                fuseMap.setFuse(offs + i, true);

            ArrayList<Expression> ands;

            if (e instanceof Operation.And) {
                ands = ((Operation.And) e).getExpressions();
            } else {
                ands = new ArrayList<>();
                ands.add(e);
            }

            for (Expression v : ands) {

                Variable var;
                boolean invert = false;

                if (v instanceof Variable)
                    var = (Variable) v;
                else if (v instanceof Not) {
                    Expression n = ((Not) v).getExpression();
                    if (n instanceof Variable) {
                        var = (Variable) n;
                        invert = true;
                    } else {
                        throw new FuseMapFillerException("NOT does not contain a variable!");
                    }
                } else
                    throw new FuseMapFillerException("only VAR or NOT VAR allowed!");

                MapEntry entry = varMap.get(var);

                if (entry == null)
                    throw new FuseMapFillerException("VAR " + var + " not found in term list!");

                int fuse = entry.getFuse(invert);

                fuseMap.setFuse(offs + fuse, false);
            }
            offs += fusesInTerm;
        }


    }

    private static class MapEntry {
        private final int index;
        private final boolean swap;

        MapEntry(int index, boolean swap) {
            this.index = index;
            this.swap = swap;
        }

        int getFuse(boolean invert) {
            int fuse=index*2;
            if (swap) {
                if (!invert) fuse++;
            } else
                if (invert) fuse++;
            return fuse;
        }
    }
}
