/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.extern;

import junit.framework.TestCase;

import java.util.ArrayList;

public class OptionsTest extends TestCase {

    public void testSimple() {
        check(new Options().addString("-a -e -u=zzz"), "-a", "-e", "-u=zzz");
        check(new Options().addString("-a -e -u=\"Hello World\""), "-a", "-e", "-u=\"Hello World\"");
        check(new Options().addString("-a -u=\"Hello World\" -e"), "-a", "-u=\"Hello World\"", "-e");

        // escape
        check(new Options().addString("-a -u=Hello\\ World -e"), "-a", "-u=Hello World", "-e");
        check(new Options().addString("-a -u=Hello\\nWorld -e"), "-a", "-u=Hello\nWorld", "-e");
        check(new Options().addString("-a -u=Hello\\tWorld -e"), "-a", "-u=Hello\tWorld", "-e");
        check(new Options().addString("-a -u=Hello\\rWorld -e"), "-a", "-u=Hello\rWorld", "-e");
        check(new Options().addString("-a -u=Hello\\\\World -e"), "-a", "-u=Hello\\World", "-e");
        check(new Options().addString("-a -u=Hello\\ World -e\\"), "-a", "-u=Hello World", "-e\\");
        check(new Options().addString("-a -u=Hello\\\"Wo rld -e"), "-a", "-u=Hello\"Wo", "rld", "-e");
        check(new Options().addString("-a \"-u=Hello World\" -e"), "-a", "-u=Hello World", "-e");
        check(new Options().addString("-a \"-u=Hello\\ World\" -e"), "-a", "-u=Hello\\ World", "-e");
        check(new Options().addString("-a \\\"-u=Hello\\ World\\\" -e"), "-a", "\"-u=Hello World\"", "-e");
    }

    private void check(Options options, String... opt) {
        ArrayList<String> l = options.getList();
        assertEquals(opt.length, l.size());
        for (int i = 0; i < opt.length; i++)
            assertEquals(opt[i], l.get(i));
    }
}