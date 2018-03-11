/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.table;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.format.FormatterException;
import de.neemann.digital.analyse.parser.ParseException;
import de.neemann.digital.analyse.parser.Parser;
import junit.framework.TestCase;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by helmut.neemann on 01.12.2016.
 */
public class ExpressionListenerOptimizeJKTest extends TestCase {

    static class TestEL implements ExpressionListener {
        private ArrayList<Expression> list = new ArrayList<>();

        @Override
        public void resultFound(String name, Expression expression) throws FormatterException, ExpressionException {
            list.add(expression);
        }

        @Override
        public void close() throws FormatterException, ExpressionException {
        }

        public ArrayList<Expression> getList() {
            return list;
        }
    }

    public void testSimple() throws IOException, ParseException, FormatterException, ExpressionException {
        TestEL exp = new TestEL();

        ExpressionListener elojk = new ExpressionListenerOptimizeJK(exp);
        Expression e1,e2;
        elojk.resultFound("Sn+1", e1=new Parser("(Sn*A)+(!Sn*B)").parse().get(0));
        elojk.resultFound("Sn+1", e2=new Parser("(Sn*A)+(!C*B)").parse().get(0));
        elojk.close();

        assertEquals(1, exp.getList().size());
        assertEquals(e1, exp.getList().get(0));
    }

    public void testSimpleSwap() throws IOException, ParseException, FormatterException, ExpressionException {
        TestEL exp = new TestEL();

        ExpressionListener elojk = new ExpressionListenerOptimizeJK(exp);
        Expression e1,e2;
        elojk.resultFound("Sn+1", e2=new Parser("(Sn*A)+(!C*B)").parse().get(0));
        elojk.resultFound("Sn+1", e1=new Parser("(Sn*A)+(!Sn*B)").parse().get(0));
        elojk.close();

        assertEquals(1, exp.getList().size());
        assertEquals(e1, exp.getList().get(0));
    }

}
