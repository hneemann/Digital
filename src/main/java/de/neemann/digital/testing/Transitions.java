/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing;

import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.data.Value;
import de.neemann.digital.testing.parser.Context;
import de.neemann.digital.testing.parser.Parser;
import de.neemann.digital.testing.parser.ParserException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Helper to create all possible transitions between states.
 */
public class Transitions {

    private final String text;
    private final ArrayList<Value[]> uniqueLines;
    private final ArrayList<Integer> inVarNum;

    /**
     * Creates a new instance
     *
     * @param text   the test data
     * @param inputs the used inputs
     * @throws IOException     IOException
     * @throws ParserException ParserException
     */
    public Transitions(String text, PinDescription[] inputs) throws IOException, ParserException {
        this.text = text;
        uniqueLines = new ArrayList<>();

        Parser p = new Parser(text).parse();
        inVarNum = new ArrayList<>();
        ArrayList<String> testNames = p.getNames();
        for (int i = 0; i < testNames.size(); i++) {
            String in = testNames.get(i);
            boolean found = false;
            for (PinDescription pin : inputs) {
                if (pin.getName().equals(in)) {
                    found = true;
                    break;
                }
            }
            if (found)
                inVarNum.add(i);
        }

        p.getLines().emitLines(line -> {
            if (isNormal(line)) {
                boolean found = false;
                for (Value[] u : uniqueLines) {
                    if (isInputEqual(line, u)) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    uniqueLines.add(line);
            }
        }, new Context());
    }

    private boolean isInputEqual(Value[] l1, Value[] l2) {
        if (l1.length != l2.length) return false;

        for (int i : inVarNum)
            if (!l1[i].isEqualTo(l2[i]))
                return false;

        return true;
    }

    private boolean isNormal(Value[] line) {
        for (Value v : line)
            if (v.getType().equals(Value.Type.CLOCK) || v.getType().equals(Value.Type.DONTCARE))
                return false;

        return true;
    }

    /**
     * @return the extended test case
     */
    public String getCompletedText() {
        return text + "\n\n\n# transitions\n" + getTransitionTests();
    }

    /**
     * @return is there data to add;
     */
    public boolean isNew() {
        return uniqueLines.size() > 1;
    }

    private String getTransitionTests() {
        ArrayList<Trans> trans = new ArrayList<>();
        for (Value[] a : uniqueLines)
            for (Value[] b : uniqueLines)
                if (!isInputEqual(a, b))
                    trans.add(new Trans(a, b));

        StringBuilder sb = new StringBuilder();
        Value[] last = null;
        while (!trans.isEmpty()) {
            Trans found = null;
            if (last != null) {
                for (Trans t : trans)
                    if (isInputEqual(t.a, last)) {
                        found = t;
                        break;
                    }
            }

            if (found == null) {
                Trans t = trans.remove(0);
                toString(sb, t.a);
                sb.append("\n");
                toString(sb, t.b);
                sb.append("\n");
                last = t.b;
            } else {
                toString(sb, found.b);
                sb.append("\n");
                last = found.b;
                trans.remove(found);
            }
        }
        return sb.toString();
    }

    private void toString(StringBuilder sb, Value[] a) {
        boolean first = true;
        for (Value v : a) {
            if (first)
                first = false;
            else
                sb.append(" ");
            sb.append(v.toString());
        }
    }

    private static final class Trans {
        private final Value[] a;
        private final Value[] b;

        private Trans(Value[] a, Value[] b) {
            this.a = a;
            this.b = b;
        }
    }
}
