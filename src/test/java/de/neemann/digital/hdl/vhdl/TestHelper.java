/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.vhdl;

import java.util.StringTokenizer;

public class TestHelper {

    static public String removeCommentLines(String code) {
        return removeCommentLines(code, false);
    }

    static public String removeCommentLines(String code, boolean normalizeWhiteSpace) {
        StringBuilder sb = new StringBuilder();
        StringTokenizer st = new StringTokenizer(code, "\n");
        while (st.hasMoreTokens()) {
            String line = st.nextToken();
            String testLine = line.trim();
            if (!(testLine.length() == 0 || (testLine.length() >= 2 && testLine.startsWith("--")))) {
                if (sb.length() > 0)
                    sb.append("\n");
                if (normalizeWhiteSpace)
                    normalizeWhiteSpaces(sb, line);
                else
                    sb.append(line);
            }
        }
        return sb.toString();
    }

    private static void normalizeWhiteSpaces(StringBuilder sb, String line) {
        boolean wasBlank = true;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == ' ' || c == '\t') {
                wasBlank = true;
            } else {
                if (wasBlank) {
                    sb.append(' ');
                    wasBlank = false;
                }
                sb.append(c);
            }
        }
    }

}
