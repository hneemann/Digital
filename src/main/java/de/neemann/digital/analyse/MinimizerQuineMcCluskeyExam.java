package de.neemann.digital.analyse;

import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.quinemc.QuineMcCluskey;
import de.neemann.digital.analyse.quinemc.QuineMcCluskeyExam;

import java.util.List;

/**
 * The QMC-Minimizer used for exam correction.
 * Should only be used if there are not more then 4 variables.
 * Created by hneemann on 13.03.17.
 */
public class MinimizerQuineMcCluskeyExam extends MinimizerQuineMcCluskey {
    @Override
    protected QuineMcCluskey createQuineMcCluskey(List<Variable> vars) {
        return new QuineMcCluskeyExam(vars);
    }
}
