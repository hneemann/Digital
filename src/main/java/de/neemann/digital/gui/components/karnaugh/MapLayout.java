/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.karnaugh;

/**
 * Used to store the user defined layout of the k-map
 */
public class MapLayout {
    private int mode;
    private int[] swap;

    MapLayout(int size) {
        checkSize(size);
    }

    int indexOf(int var) {
        for (int i = 0; i < swap.length; i++)
            if (swap[i] == var)
                return i;
        return -1;
    }

    private void swapVars(int startVar, int endVar) {
        int t = swap[startVar];
        swap[startVar] = swap[endVar];
        swap[endVar] = t;
    }

    private void toggleInvert(int n) {
        mode ^= (1 << n);
    }

    /**
     * Checks is the given swap list is valid (not null and of the correct size).
     * If so, the given list is preserved, if not, a simple, non swapping default swap
     * list is created.
     *
     * @param size the required size of the list
     */
    void checkSize(int size) {
        if (swap != null && swap.length == size)
            return;

        swap = new int[size];
        for (int i = 0; i < swap.length; i++) swap[i] = i;
        mode = 0;
    }

    boolean swapByDragAndDrop(VarRectList.VarRect startVar, VarRectList.VarRect endVar) {
        if (startVar == null || endVar == null || startVar.equals(endVar))
            return false;

        int start = indexOf(startVar.getVar());
        int end = indexOf(endVar.getVar());
        if (start != end)
            swapVars(start, end);

        if (startVar.getInvert() != endVar.getInvert())
            toggleInvert(end);

        return true;
    }

    boolean getInvert(int n) {
        return (mode & (1 << n)) > 0;
    }

    int get(int i) {
        return swap[i];
    }
}
