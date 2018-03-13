/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog;

import java.util.StringTokenizer;

public class TestHelper {

    public static String removeCommentLines(String code) {
        StringBuilder sb = new StringBuilder();
        StringTokenizer st = new StringTokenizer(code, "\n");
        boolean inComment = false;

        while (st.hasMoreTokens()) {
            String line = st.nextToken();
            String testLine = line.trim();

            if (testLine.startsWith("/*") && !inComment) {
                inComment = true;
            } else if (inComment) {
                if (testLine.equals("*/")) {
                    inComment = false;
                }
            } else if (!(testLine.length() == 0 || testLine.startsWith("//"))) {
                if (sb.length() > 0)
                    sb.append("\n");
                sb.append(line);
            }
        }
        return sb.toString();
    }

}
