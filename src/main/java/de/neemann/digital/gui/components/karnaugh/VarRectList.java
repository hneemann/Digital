/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.karnaugh;

import de.neemann.digital.draw.graphics.text.formatter.GraphicsFormatter;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Objects;

final class VarRectList {
    private final ArrayList<VarRect> varRectList;
    private int xOffs;
    private int yOffs;

    VarRectList() {
        varRectList = new ArrayList<>();
    }

    public void reset(int xOffs, int yOffs) {
        this.xOffs = xOffs;
        this.yOffs = yOffs;
        varRectList.clear();
    }

    public void add(int var, boolean invert, Rectangle r, GraphicsFormatter.Fragment fr) {
        varRectList.add(new VarRect(var, invert, r, fr));
    }

    VarRect findVarRect(MouseEvent e) {
        int x = e.getX() - xOffs;
        int y = e.getY() - yOffs;
        for (VarRect r : varRectList) {
            if (r.rect.contains(x, y))
                return r;
        }
        return null;
    }

    static final class VarRect {
        private final boolean invert;
        private final Rectangle rect;
        private final int var;
        private final GraphicsFormatter.Fragment fragment;

        private VarRect(int var, boolean invert, Rectangle rect, GraphicsFormatter.Fragment fragment) {
            this.var = var;
            this.invert = invert;
            this.rect = rect;
            this.fragment = fragment;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VarRect varRect = (VarRect) o;
            return invert == varRect.invert && var == varRect.var;
        }

        @Override
        public int hashCode() {
            return Objects.hash(invert, var);
        }

        int getVar() {
            return var;
        }

        boolean getInvert() {
            return invert;
        }

        GraphicsFormatter.Fragment getFragment() {
            return fragment;
        }

        @Override
        public String toString() {
            return "VarRect{"
                    + "invert="
                    + invert
                    + ", var=" + var
                    + '}';
        }
    }
}
