/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.vhdl;

import java.util.StringTokenizer;

public class TestHelper {

    static public String removeCommentLines(String code) {
        StringBuilder sb = new StringBuilder();
        StringTokenizer st = new StringTokenizer(code, "\n");
        while (st.hasMoreTokens()) {
            String line = st.nextToken();
            String testLine = line.trim();
            if (!(testLine.length() == 0 || (testLine.length() >= 2 && testLine.startsWith("--")))) {
                if (sb.length() > 0)
                    sb.append("\n");
                sb.append(line);
            }
        }
        return sb.toString();
    }

}
