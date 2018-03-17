/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.vhdl.lib;

import de.neemann.digital.hdl.hgs.HGSMap;

import java.util.HashMap;

/**
 * A easy to populate map.
 * Used to pass parameters to the template.
 */
public class TempParameter implements HGSMap {
    private HashMap<String, Object> map;

    /**
     * creates an empty instance.
     */
    public TempParameter() {
        this.map = new HashMap<>();
    }

    /**
     * Adds avalue to this map
     *
     * @param name  the name
     * @param value the value
     * @return this for chained calls
     */
    public TempParameter put(String name, Object value) {
        map.put(name, value);
        return this;
    }

    @Override
    public Object hgsMapGet(String key) {
        return map.get(key);
    }
}
