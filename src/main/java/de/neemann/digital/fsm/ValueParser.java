/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm;

import de.neemann.digital.lang.Lang;

import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * Helper to parse value assignments
 */
public class ValueParser {
    private final String values;

    /**
     * Create a new instance
     *
     * @param values the values to parse
     */
    public ValueParser(String values) {
        this.values = values;
    }

    /**
     * Creates a map which contains the values
     *
     * @return the map
     * @throws FiniteStateMachineException FiniteStateMachineException
     */
    public TreeMap<String, Integer> parse() throws FiniteStateMachineException {
        TreeMap<String, Integer> valueMap = new TreeMap<>();
        if (values != null) {
            StringTokenizer st = new StringTokenizer(values, ";,");
            while (st.hasMoreTokens()) {
                String tok = st.nextToken();
                int p = tok.indexOf('=');
                if (p < 0)
                    throw new FiniteStateMachineException(Lang.get("err_fsmInvalidOutputAssignment_N", values));
                String key = tok.substring(0, p).trim();
                String valStr = tok.substring(p + 1).trim().toLowerCase();
                switch (valStr) {
                    case "0":
                        valueMap.put(key, 0);
                        break;
                    case "1":
                        valueMap.put(key, 1);
                        break;
                    case "x":
                        valueMap.put(key, 2);
                        break;
                    default:
                        throw new FiniteStateMachineException(Lang.get("err_fsmInvalidOutputAssignment_N", valStr));
                }
            }
        }
        return valueMap;
    }
}
