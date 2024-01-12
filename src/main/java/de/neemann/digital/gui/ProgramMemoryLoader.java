/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.core.memory.ProgramMemory;
import de.neemann.digital.core.memory.importer.Importer;
import de.neemann.digital.core.memory.importer.MultiValueArray;
import de.neemann.digital.lang.Lang;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Modifier that loads a given rom file to the program memory of the model.
 */
public class ProgramMemoryLoader implements ModelModifier {
    private final File romHex;
    private final boolean bigEndian;

    /**
     * Creates a new rom modifier
     *
     * @param romHex    the file to load
     * @param bigEndian reads the file in big endian mode
     */
    public ProgramMemoryLoader(File romHex, boolean bigEndian) {
        this.romHex = romHex;
        this.bigEndian = bigEndian;
    }

    @Override
    public void preInit(Model model) throws NodeException {
        List<Node> progMem = model.findNode(n -> n instanceof ProgramMemory && ((ProgramMemory) n).isProgramMemory());

        try {
            switch (progMem.size()) {
                case 0:
                    throw new NodeException(Lang.get("err_noRomFound"));
                case 1:
                    final ProgramMemory memory = (ProgramMemory) progMem.get(0);
                    memory.setProgramMemory(Importer.read(romHex, memory.getDataBits(), bigEndian));
                default:
                    final Comparator<Node> comparator = Comparator.comparing(n -> ((ProgramMemory) n).getLabel());

                    for (Node n : progMem)
                        for (Node m : progMem)
                            if ((n != m) && comparator.compare(n, m) == 0)
                                throw new NodeException(Lang.get("err_ProgMemLabelsNotDifferent"));

                    progMem.sort(comparator);
                    MultiValueArray.Builder builder = new MultiValueArray.Builder();
                    HashMap<ProgramMemory, DataField> memMap = new HashMap<>();
                    for (Node n : progMem) {
                        final ProgramMemory mem = (ProgramMemory) n;
                        DataField df = new DataField(1024);
                        builder.add(df, mem.getDataBits());
                        memMap.put(mem, df);
                    }
                    Importer.read(romHex, builder.build(), bigEndian);
                    for (Map.Entry<ProgramMemory, DataField> e : memMap.entrySet())
                        e.getKey().setProgramMemory(e.getValue());
            }
        } catch (IOException | MultiValueArray.ValueArrayException e) {
            throw new NodeException(Lang.get("err_errorLoadingRomData"), e);
        }
    }
}
