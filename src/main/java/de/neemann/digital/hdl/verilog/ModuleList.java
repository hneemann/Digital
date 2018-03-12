/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog;

import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * @author ideras
 */
public class ModuleList {
    private final HashSet<String> moduleNameList;
    private final ArrayList<String> moduleCodeList;

    /**
     * Creates a new instance
     */
    public ModuleList() {
        moduleNameList = new HashSet<>();
        moduleCodeList = new ArrayList<>();
    }

    /**
     * Register a new module with its generated verilog code.
     *
     * @param elemName the name of the module
     * @param moduleCode the generated verilog code
     */
    public void registerModule(String elemName, String moduleCode) {
        if (!moduleNameList.contains(elemName)) {
            moduleNameList.add(elemName);
            moduleCodeList.add(moduleCode);
        }
    }

    /**
     * Checks if a module is already registered in the list
     *
     * @param elemName the module name
     * @return true is the module is already registered, false otherwise.
     */
    public boolean isModuleRegistered(String elemName) {
        return moduleNameList.contains(elemName);
    }

    /**
     * Return the list of generated modules code.
     * @return the list
     */
    public ArrayList<String> getModuleCodeList() {
        return moduleCodeList;
    }

    /**
     * Checks is the list of modules is empty
     *
     * @return true is the list is empty, false otherwise.
     */
    public boolean isEmpty() {
        return moduleNameList.isEmpty();
    }

    /**
     * Returns the number of registered modules
     *
     * @return the number of registered modules
     */
    public int size() {
        return moduleCodeList.size();
    }
}
