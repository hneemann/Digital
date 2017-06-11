package de.neemann.digital.analyse;

import de.neemann.digital.core.*;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class DependencyAnalyserTest extends TestCase {

    private static final int[] VAL = new int[]{1, 1, 1, 2, 6, 5, 5, 3, 8, 7, 7, 5, 10, 9, 9, 7, 2, 14, 14, 14, 14, 14, 14, 14, 2, 2};

    public void testAnalyzer() throws Exception {
        Model model = new ToBreakRunner("dig/backtrack/Plexer.dig").getModel();
        ModelAnalyser m = new ModelAnalyser(model);
        DependencyAnalyser da = new DependencyAnalyser(m);

        assertEquals(17, m.getInputs().size());
        assertEquals(26, m.getOutputs().size());
        for (int i = 0; i < m.getOutputs().size(); i++)
            assertEquals("" + i, VAL[i], da.getInputs(m.getOutputs().get(i)).size());
    }

}

/*

ExpressionCreator - p0n+1 reduced from 17 to 1 variables ([Count])
ExpressionCreator - p1n+1 reduced from 17 to 1 variables ([p0n])
ExpressionCreator - Q_1n+1 reduced from 17 to 1 variables ([Q_0n])
ExpressionCreator - Q_0n+1 reduced from 17 to 2 variables ([Q_1n, Q_0n])
ExpressionCreator - C0Q_3n+1 reduced from 17 to 6 variables ([p0n, p1n, C0Q_3n, C0Q_2n, C0Q_1n, C0Q_0n])
ExpressionCreator - C0Q_2n+1 reduced from 17 to 5 variables ([p0n, p1n, C0Q_2n, C0Q_1n, C0Q_0n])
ExpressionCreator - C0Q_1n+1 reduced from 17 to 5 variables ([p0n, p1n, C0Q_3n, C0Q_1n, C0Q_0n])
ExpressionCreator - C0Q_0n+1 reduced from 17 to 3 variables ([p0n, p1n, C0Q_0n])
ExpressionCreator - C1Q_3n+1 reduced from 17 to 8 variables ([p0n, p1n, C0Q_3n, C0Q_0n, C1Q_3n, C1Q_2n, C1Q_1n, C1Q_0n])
ExpressionCreator - C1Q_2n+1 reduced from 17 to 7 variables ([p0n, p1n, C0Q_3n, C0Q_0n, C1Q_2n, C1Q_1n, C1Q_0n])
ExpressionCreator - C1Q_1n+1 reduced from 17 to 7 variables ([p0n, p1n, C0Q_3n, C0Q_0n, C1Q_3n, C1Q_1n, C1Q_0n])
ExpressionCreator - C1Q_0n+1 reduced from 17 to 5 variables ([p0n, p1n, C0Q_3n, C0Q_0n, C1Q_0n])
ExpressionCreator - C2Q_3n+1 reduced from 17 to 10 variables ([p0n, p1n, C0Q_3n, C0Q_0n, C1Q_3n, C1Q_0n, C2Q_3n, C2Q_2n, C2Q_1n, C2Q_0n])
ExpressionCreator - C2Q_2n+1 reduced from 17 to 9 variables ([p0n, p1n, C0Q_3n, C0Q_0n, C1Q_3n, C1Q_0n, C2Q_2n, C2Q_1n, C2Q_0n])
ExpressionCreator - C2Q_1n+1 reduced from 17 to 9 variables ([p0n, p1n, C0Q_3n, C0Q_0n, C1Q_3n, C1Q_0n, C2Q_3n, C2Q_1n, C2Q_0n])
ExpressionCreator - C2Q_0n+1 reduced from 17 to 7 variables ([p0n, p1n, C0Q_3n, C0Q_0n, C1Q_3n, C1Q_0n, C2Q_0n])
ExpressionCreator - s2 reduced from 17 to 2 variables ([Q_1n, Q_0n])
ExpressionCreator - d0 reduced from 17 to 14 variables ([Q_1n, Q_0n, C0Q_3n, C0Q_2n, C0Q_1n, C0Q_0n, C1Q_3n, C1Q_2n, C1Q_1n, C1Q_0n, C2Q_3n, C2Q_2n, C2Q_1n, C2Q_0n])
ExpressionCreator - c0 reduced from 17 to 14 variables ([Q_1n, Q_0n, C0Q_3n, C0Q_2n, C0Q_1n, C0Q_0n, C1Q_3n, C1Q_2n, C1Q_1n, C1Q_0n, C2Q_3n, C2Q_2n, C2Q_1n, C2Q_0n])
ExpressionCreator - b0 reduced from 17 to 14 variables ([Q_1n, Q_0n, C0Q_3n, C0Q_2n, C0Q_1n, C0Q_0n, C1Q_3n, C1Q_2n, C1Q_1n, C1Q_0n, C2Q_3n, C2Q_2n, C2Q_1n, C2Q_0n])
ExpressionCreator - a0 reduced from 17 to 14 variables ([Q_1n, Q_0n, C0Q_3n, C0Q_2n, C0Q_1n, C0Q_0n, C1Q_3n, C1Q_2n, C1Q_1n, C1Q_0n, C2Q_3n, C2Q_2n, C2Q_1n, C2Q_0n])
ExpressionCreator - e0 reduced from 17 to 14 variables ([Q_1n, Q_0n, C0Q_3n, C0Q_2n, C0Q_1n, C0Q_0n, C1Q_3n, C1Q_2n, C1Q_1n, C1Q_0n, C2Q_3n, C2Q_2n, C2Q_1n, C2Q_0n])
ExpressionCreator - f0 reduced from 17 to 14 variables ([Q_1n, Q_0n, C0Q_3n, C0Q_2n, C0Q_1n, C0Q_0n, C1Q_3n, C1Q_2n, C1Q_1n, C1Q_0n, C2Q_3n, C2Q_2n, C2Q_1n, C2Q_0n])
ExpressionCreator - g0 reduced from 17 to 14 variables ([Q_1n, Q_0n, C0Q_3n, C0Q_2n, C0Q_1n, C0Q_0n, C1Q_3n, C1Q_2n, C1Q_1n, C1Q_0n, C2Q_3n, C2Q_2n, C2Q_1n, C2Q_0n])
ExpressionCreator - s1 reduced from 17 to 2 variables ([Q_1n, Q_0n])
ExpressionCreator - s0 reduced from 17 to 2 variables ([Q_1n, Q_0n])

 */
