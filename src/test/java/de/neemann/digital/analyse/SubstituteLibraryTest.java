/*
 * Copyright (c) 2021 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse;

import de.neemann.digital.analyse.expression.format.FormatToExpression;
import de.neemann.digital.core.Bits;
import de.neemann.digital.core.IntFormat;
import de.neemann.digital.core.arithmetic.BarrelShifterMode;
import de.neemann.digital.core.arithmetic.LeftRightFormat;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.Rotation;
import de.neemann.digital.core.extern.Application;
import de.neemann.digital.core.io.CommonConnectionType;
import de.neemann.digital.core.io.InValue;
import de.neemann.digital.core.io.ProbeMode;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.core.memory.rom.ROMManager;
import de.neemann.digital.core.memory.rom.ROMManagerFile;
import de.neemann.digital.draw.graphics.Orientation;
import de.neemann.digital.draw.model.InverterConfig;
import de.neemann.digital.draw.shapes.CustomCircuitShapeType;
import de.neemann.digital.draw.shapes.custom.CustomShapeDescription;
import de.neemann.digital.gui.components.data.ScopeTrigger;
import de.neemann.digital.testing.parser.ParserException;
import de.neemann.gui.language.Language;
import junit.framework.TestCase;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import static de.neemann.digital.analyse.SubstituteLibrary.doImplicitTypeCasts;

public class SubstituteLibraryTest extends TestCase {

    private final HashSet<Class<?>> typeSet = new HashSet<>();

    @Override
    public void setUp() {
        for (Key<?> k : Keys.getKeys()) {
            Class<?> aClass = k.getDefault().getClass();
            // ignore settings
            if (aClass == ROMManagerFile.class
                    || aClass == CustomShapeDescription.class
                    || aClass == Language.class)
                continue;
            typeSet.add(aClass);
        }
    }

    public void testDoImplicitTypeCasts() throws Bits.NumberFormatException, IOException, ParserException {
        check(1, doImplicitTypeCasts(Integer.class, 1L));
        check(2L, doImplicitTypeCasts(Long.class, 2));
        check("zz", doImplicitTypeCasts(String.class, "zz"));
        check(true, doImplicitTypeCasts(Boolean.class, 1));

        check(IntFormat.decSigned, doImplicitTypeCasts(IntFormat.class, 2));
        check(IntFormat.def, doImplicitTypeCasts(IntFormat.class, -2));
        check(IntFormat.floating, doImplicitTypeCasts(IntFormat.class, 9));
        check(IntFormat.def, doImplicitTypeCasts(IntFormat.class, 10));
        check(Orientation.RIGHTBOTTOM, doImplicitTypeCasts(Orientation.class, 2));
        check(Application.Type.GHDL, doImplicitTypeCasts(Application.Type.class, 1));
        check(LeftRightFormat.right, doImplicitTypeCasts(LeftRightFormat.class, 1));
        check(BarrelShifterMode.rotate, doImplicitTypeCasts(BarrelShifterMode.class, 1));
        check(CustomCircuitShapeType.SIMPLE, doImplicitTypeCasts(CustomCircuitShapeType.class, 1));
        check(CommonConnectionType.anode, doImplicitTypeCasts(CommonConnectionType.class, 1));
        check(FormatToExpression.DERIVE, doImplicitTypeCasts(FormatToExpression.class, 1));
        check(ScopeTrigger.Trigger.falling, doImplicitTypeCasts(ScopeTrigger.Trigger.class, 1));
        check(ProbeMode.VALUE, doImplicitTypeCasts(ProbeMode.class, 0));

        check(new Color(0xf0, 0xf0, 0xf0), doImplicitTypeCasts(Color.class, 0xf0f0f0));
        check(new Rotation(2), doImplicitTypeCasts(Rotation.class, 2));
        check(new File("zz"), doImplicitTypeCasts(File.class, "zz"));
        check(new InverterConfig.Builder().add("A").build(), doImplicitTypeCasts(InverterConfig.class, Arrays.asList("A")));
        check(new InValue(2), doImplicitTypeCasts(InValue.class, 2));
        check(new InValue("z"), doImplicitTypeCasts(InValue.class, "z"));
        check(new de.neemann.digital.testing.TestCaseDescription("A B\n1 1"), doImplicitTypeCasts(de.neemann.digital.testing.TestCaseDescription.class, "A B\n1 1"));
        check(new DataField(new long[]{1, 2, 3}), doImplicitTypeCasts(DataField.class, Arrays.asList(1, 2, 3)));

        assertTrue(typeSet.toString(), typeSet.isEmpty());
    }

    private void check(Object expected, Object found) {
        Class<?> aClass = expected.getClass();
        assertEquals(expected, found);
        typeSet.remove(aClass);
    }
}