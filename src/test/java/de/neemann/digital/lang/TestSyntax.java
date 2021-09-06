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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class TestSyntax extends TestCase {

    public void testSyntax() {
        Bundle bundle = new Bundle("lang/lang");
        HashMap<String, Resources> list = new HashMap<>();
        Resources en = null;
        Resources de = null;
        for (Language l : bundle.getSupportedLanguages()) {
            Resources resources = bundle.getResources(l.getName());
            if (l.getName().equals("en"))
                en = resources;
            else if (l.getName().equals("de"))
                de = resources;
            else
                list.put(l.getName(), resources);
        }

        boolean error = false;
        for (String key : en.getKeys()) {
            final String en_msg = en.get(key);
            final String de_msg = de.get(key);
            int paramCount = getParamCount(key, en_msg, "en");
            assertTrue(paramCount >= 0);
            assertEquals(key, paramCount, getParamCount(key, de_msg, "de"));
            checkSingleQuoteRules(en_msg, key, paramCount);
            checkSingleQuoteRules(de_msg, key, paramCount);

            for (Map.Entry<String, Resources> e : list.entrySet()) {
                Resources r = e.getValue();
                final String m = r.get(key);
                if (m != null) {
                    checkSingleQuoteRules(m, key, paramCount);
                    int pc = getParamCount(key, m, e.getKey());
                    if (pc < 0)
                        error = true;
                    else if (pc != paramCount) {
                        System.out.println(e.getKey() + ": Param count does not match: " + key + "=\"" + m + "\", expected: " + paramCount + ", found " + pc);
                        error = true;
                    }
                }
            }
        }

        assertFalse("Param errors detected!", error);
    }

    private int getParamCount(String key, String msg, String lang) {
        HashSet<Integer> numSet = new HashSet<>();
        int pos = 0;
        while (true) {
            pos = msg.indexOf("{", pos);
            if (pos < 0) {
                for (int i = 0; i < numSet.size(); i++) {
                    if (!numSet.contains(i)) {
                        System.out.println(lang + ": Param " + i + " is missing in: " + key + "=\"" + msg + "\"");
                        return -1;
                    }
                }

                return numSet.size();
            }
            int p = pos + 1;
            while (msg.length() > p && msg.charAt(p) != '}') p++;
            if (p >= msg.length()) {
                System.out.println(lang + ": Missing closing '}': " + key + "=\"" + msg + "\"");
                return -1;
            }
            String numStr = msg.substring(pos + 1, p);
            if (!numStr.contains("background-color:")) {
                int num = Integer.parseInt(numStr);
                numSet.add(num);
            }
            pos = p;
        }
    }

    private void checkSingleQuoteRules(String message, String key, int paramCount) {
        if (paramCount == 0) {
            assertFalse(key + ": quote error: If a single quote is to be used in a string not containing parameters, a doubled single quote is not allowed.", message.contains("''"));
        } else {
            boolean singleQ = false;
            for (int p = 0; p < message.length(); p++) {
                if (message.charAt(p) == '\'')
                    singleQ = !singleQ;
                else
                    assertFalse(key + ": quote error: If a single quote is to be used in a string containing parameters, the single quote must be doubled (e.g. don''t):\n" + message, singleQ);
            }
        }
    }

}
