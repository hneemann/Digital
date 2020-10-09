/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.lang;

import de.neemann.gui.language.Bundle;
import de.neemann.gui.language.Language;
import de.neemann.gui.language.Resources;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashSet;

public class TestSyntax extends TestCase {

    public void testSyntax() {
        Bundle bundle = new Bundle("lang/lang");
        ArrayList<Resources> list = new ArrayList<>();
        Resources en = null;
        Resources de = null;
        for (Language l : bundle.getSupportedLanguages()) {
            Resources resources = bundle.getResources(l.getName());
            if (l.getName().equals("en"))
                en = resources;
            else if (l.getName().equals("de"))
                de = resources;
            else
                list.add(resources);
        }

        for (String key : en.getKeys()) {
            final String msg = en.get(key);
            int paramCount = getParamCount(msg);
            check(msg, paramCount);
            assertEquals(key, paramCount, getParamCount(de.get(key)));
            check(de.get(key), paramCount);

            for (Resources r : list) {
                final String m = r.get(key);
                if (m != null) {
                    check(m, paramCount);
                    if (paramCount != getParamCount(m)) {
                        String message = "Param count does not match: " + key + " " + m;
                        System.out.println("WARNING: " + message);
                    }
                }
            }
        }


    }

    private int getParamCount(String msg) {
        HashSet<Integer> numSet = new HashSet<>();
        int pos = 0;
        while (true) {
            pos = msg.indexOf("{", pos);
            if (pos < 0) {
                for (int i = 0; i < numSet.size(); i++)
                    assertTrue(numSet.contains(i));

                return numSet.size();
            }
            int p = pos + 1;
            while (msg.charAt(p) != '}') p++;
            String numStr = msg.substring(pos + 1, p);
            if (!numStr.contains("background-color:")) {
                int num = Integer.parseInt(numStr);
                numSet.add(num);
            }
            pos = p;
        }
    }

    private void check(String message, int paramCount) {
        checkEscapedParam(message, paramCount);

    }

    private void checkEscapedParam(String message, int paramCount) {
        if (paramCount > 0) {
            boolean lastWasFirstSingleQ = false;
            for (int p = 0; p < message.length(); p++) {
                if (message.charAt(p) == '\'') {
                    lastWasFirstSingleQ = !lastWasFirstSingleQ;
                } else {
                    assertFalse("quote error: " + message, lastWasFirstSingleQ);
                }
            }
        }
    }
}
