/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.quinemc;


import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.format.FormatterException;

/**
 */
public abstract class FullVariantDontCareCreator {

    private final int nmax;
    private final int step;

    public FullVariantDontCareCreator() {
        this(3, 1);
    }

    public FullVariantDontCareCreator(int nmax) {
        this(nmax, 1);
    }

    public FullVariantDontCareCreator(int nmax, int step) {
        this.nmax = nmax;
        this.step = step;
    }

    public void create() throws ExpressionException, FormatterException {
        for (int n = 1; n <= nmax; n++) {
            int tables = 1;
            int c = 1 << n;
            for (int i = 0; i < c; i++) tables *= 3;

            int count = 0;
            byte[] tab = new byte[1 << n];
            for (int i = 0; i < tables; i += step) {
                int value = i;
                for (int j = 0; j < tab.length; j++) {
                    tab[j] = (byte) (value % 3);
                    value /= 3;
                }
                handleTable(n, tab);

                if (count++ > 10000) {
                    System.out.println(i + "/" + tables);
                    count = 0;
                }

            }
        }
    }

    public abstract void handleTable(int n, byte[] tab) throws ExpressionException, FormatterException;
}
