/*
 * Probabilist Logic Learner is a system to learn probabilistic logic
 * programs from data and use its learned programs to make inference
 * and answer queries.
 *
 * Copyright (C) 2017 Victor Guimarães
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package br.ufrj.cos.knowledge.theory.manager;

import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.knowledge.theory.manager.revision.RevisionOperatorEvaluator;
import br.ufrj.cos.knowledge.theory.manager.revision.RevisionOperatorSelector;
import br.ufrj.cos.knowledge.theory.manager.revision.TheoryRevisionException;
import br.ufrj.cos.knowledge.theory.manager.revision.point.RevisionExamples;
import br.ufrj.cos.util.ExceptionMessages;
import br.ufrj.cos.util.FileIOUtils;
import br.ufrj.cos.util.InitializationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Collection;
import java.util.function.Function;

import static br.ufrj.cos.util.log.PosRevisionLog.DELTA_VALUE_NOT_UPDATED;
import static br.ufrj.cos.util.log.PosRevisionLog.DELTA_VALUE_UPDATED;
import static br.ufrj.cos.util.log.PreRevisionLog.*;

/**
 * Responsible to decide when to revise the {@link Theory} based on the Hoeffding's bound, with confidence delta.
 * <p>
 * It that the {@link Theory} will only be updated if, with confidence of 1 - delta, the improve on the sample
 * represents the real improvement over the population.
 * <p>
 * Created on 16/06/17.
 *
 * @author Victor Guimarães
 */
public class HoeffdingBoundTheoryManager extends TheoryRevisionManager {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    /**
     * The default value of delta.
     */
    public static final double DEFAULT_DELTA = 1.0e-6;
    /**
     * The default delta update expression.
     */
    public static final String DEFAULT_DELTA_UPDATE_FUNCTION_EXPRESSION = "d";
    /**
     * The java function format.
     */
    public static final String JAVA_FUNCTION_FORMAT = "new java.util.function.Function(function(d) %s)";
    /**
     * The script engine name.
     */
    @SuppressWarnings("SpellCheckingInspection")
    public static final String ENGINE_NAME = "nashorn";

    /**
     * The delta update expression. This is a mathematical expression that has a variable d as the current
     * delta value and its result will be set as the new delta value.
     */
    @SuppressWarnings("CanBeFinal")
    public String deltaUpdateExpression = DEFAULT_DELTA_UPDATE_FUNCTION_EXPRESSION;

    protected Function<? super Double, Double> deltaUpdateFunction;
    protected double delta = DEFAULT_DELTA;

    @Override
    public void initialize() throws InitializationException {
        super.initialize();
        if (Double.isInfinite(theoryMetric.getRange()) || Double.isNaN(theoryMetric.getRange())) {
            throw new InitializationException(
                    FileIOUtils.formatLogMessage(ExceptionMessages.ERROR_UNBOUNDED_RANGE_METRIC.toString(),
                                                 theoryMetric.getClass().getSimpleName(),
                                                 this.getClass().getSimpleName()));
        }
        compileDeltaUpdateFunction();
    }

    /**
     * Compiles the delta update function.
     *
     * @throws InitializationException if an error occurs during the compilation of the update function.
     */
    protected void compileDeltaUpdateFunction() throws InitializationException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName(ENGINE_NAME);
        try {
            final String functionExpression = String.format(JAVA_FUNCTION_FORMAT, deltaUpdateExpression);
            //noinspection unchecked
            deltaUpdateFunction = (Function<? super Double, Double>) engine.eval(functionExpression);
        } catch (ScriptException | ClassCastException e) {
            throw new InitializationException(String.format(ERROR_COMPILING_DELTA_UPDATE_FUNCTION.toString(), e), e);
        }
    }

    @Override
    public boolean applyRevision(RevisionOperatorSelector operatorSelector,
                                 RevisionExamples examples) throws TheoryRevisionException {
        double epsilon = calculateHoeffdingBound(theoryMetric.getRange(), examples.getRelevantSampleSize());
        theoryEvaluation = evaluateCurrentTheory(examples);
        double bestPossibleImprovement = theoryMetric.bestPossibleImprovement(theoryEvaluation);
        // tests if the best possible improvement is enough to pass the Hoeffding's threshold
        if (bestPossibleImprovement >= epsilon) {
            Collection<? extends Example> targets = examples.getTrainingExamples(trainUsingAllExamples);
            // calls the revision on the right threshold
            logger.debug(CALLING_REVISION_ON_EXAMPLES.toString(),
                         examples.getTrainingExamples(trainUsingAllExamples).size());
            RevisionOperatorEvaluator operatorEvaluator = operatorSelector.selectOperator(targets, theoryMetric);
            logger.debug(SELECTED_OPERATOR.toString(), operatorEvaluator);
            if (operatorEvaluator == null) { return false; }
            final boolean revised = applyRevision(operatorEvaluator, examples, theoryEvaluation, epsilon);
            if (revised) { updateDelta(); }
            return revised;
        } else {
            logger.trace(SKIPPING_REVISION_ON_EXAMPLES);
        }
        return false;
    }

    /**
     * Calculates the Hoeffding's bound value of epsilon. The value is given by the formula:
     * <p>
     * \epsilon = \sqrt{\frac{R^2 * ln(1/\delta)}{2n}}
     * <p>
     * Where R is the range of the random variable and n is the sample size.
     *
     * @param range      the range of the random variable
     * @param sampleSize the sample size
     * @return the Hoeffding's bound value of epsilon
     */
    protected double calculateHoeffdingBound(double range, int sampleSize) {
        // equivalent form to \sqrt{frac{R^2 * ln(1/δ)}{2n}}
        return StrictMath.sqrt((range * range * -StrictMath.log(delta)) / (2 * sampleSize));
    }

    /**
     * Updates the {@link #delta} each time a revision is accepted, given a specified function f: R -> R.
     */
    public void updateDelta() {
        final double oldDelta = getDelta();
        final Double newDelta = deltaUpdateFunction.apply(oldDelta);
        if (setDelta(newDelta)) {
            logger.debug(DELTA_VALUE_UPDATED.toString(), oldDelta, getDelta());
        } else {
            logger.debug(DELTA_VALUE_NOT_UPDATED.toString(), getDelta());
        }
    }

    /**
     * Gets the delta.
     *
     * @return the delta
     */
    public double getDelta() {
        return delta;
    }

    /**
     * Sets the delta.
     *
     * @param delta the delta
     * @return return {@code true} if the delta has changed, {@code false} otherwise, because of range constraints.
     */
    @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
    public boolean setDelta(double delta) {
        if (delta > 0.0 && delta < 1.0) {
            this.delta = delta;
            return true;
        }
        return false;
    }

}
