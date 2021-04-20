/*
 * Copyright (c) 2021 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.ATF150x;

import java.util.ArrayList;

public class PL {

    private final ArrayList<Integer> pinList;

    public PL() {
        pinList = new ArrayList<>();
    }

    public PL p(int... p) {
        for (int j : p) pinList.add(j);
        return this;
    }

    public PL e(int... p) {
        for (int j : p) pinList.remove((Object) j);
        return this;
    }

    public PL pi(int a, int b) {
        for (int i = a; i <= b; i++)
            pinList.add(i);
        return this;
    }

    public int[] pins() {
        int[] pins = new int[pinList.size()];
        for (int i = 0; i < pinList.size(); i++)
            pins[i] = pinList.get(i);
        return pins;
    }
}
