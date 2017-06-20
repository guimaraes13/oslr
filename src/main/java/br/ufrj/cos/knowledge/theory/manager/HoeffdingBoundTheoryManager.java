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
import br.ufrj.cos.knowledge.theory.manager.revision.TheoryRevisionException;
import br.ufrj.cos.knowledge.theory.manager.revision.operator.RevisionOperatorEvaluator;
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.Term;
import br.ufrj.cos.util.ExceptionMessages;
import br.ufrj.cos.util.InitializationException;
import br.ufrj.cos.util.LanguageUtils;
import br.ufrj.cos.util.LogMessages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
@SuppressWarnings("unused")
public class HoeffdingBoundTheoryManager extends TheoryRevisionManager {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();
    /**
     * The default value of delta.
     */
    public static final double DEFAULT_DELTA = 0.01;
    /**
     * The depth of the relevant breadth first search.
     */
    public int relevantDepth = 0;
    protected double delta = DEFAULT_DELTA;

    @Override
    public void initialize() throws InitializationException {
        super.initialize();
        if (Double.isInfinite(theoryMetric.getRange()) || Double.isNaN(theoryMetric.getRange())) {
            throw new InitializationException(
                    LanguageUtils.formatLogMessage(ExceptionMessages.ERROR_UNBOUNDED_RANGE_METRIC.toString(),
                                                   theoryMetric.getClass().getSimpleName(),
                                                   this.getClass().getSimpleName()));
        }
    }

    @Override
    public void revise(Iterable<? extends Example> targets) throws TheoryRevisionException {
        Collection<? extends Example> sample = getIndependentSample(targets);
        double epsilon = calculateHoeffdingBound(theoryMetric.getRange(), sample.size());

        //QUESTION: train on the sample or on all the targets?
        RevisionOperatorEvaluator revisionOperator = revisionManager.getBestRevisionOperator(targets);

        //TODO: check if the difference between the best possible value of the metric and the current is bigger than
        // epsilon
        applyRevision(revisionOperator, sample, epsilon);
    }

    /**
     * Gets an independent sample of example from the targets. Two example is said to be independent, in a given
     * distance, if they do not share a common relevant in the given distance.
     *
     * @param targets the target {@link Example}s
     * @return the independent sample
     */
    protected Collection<? extends Example> getIndependentSample(Iterable<? extends Example> targets) {
        Set<Atom> previousRelevants = new HashSet<>();
        Collection<Example> examples = new HashSet<>();
        Set<Atom> currentRelevants;
        Set<Term> terms;
        int counter = 0;
        for (Example example : targets) {
            terms = example.getGoalQuery().getTerms().stream().filter(Term::isConstant).collect(Collectors.toSet());
            currentRelevants = learningSystem.relevantsBreadthFirstSearch(terms, relevantDepth, false);
            if (Collections.disjoint(previousRelevants, currentRelevants)) {
                examples.add(example);
                previousRelevants.addAll(currentRelevants);
            }
            counter++;
            //QUESTION: Another way would be to add the current to the previous ones even if the sets are not disjoint
        }
        logger.debug(LogMessages.SAMPLING_FROM_TARGETS.toString(), examples.size(), counter);
        return examples;
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
     */
    public void setDelta(double delta) {
        if (delta > 0.0 && delta < 1.0) { this.delta = delta; }
    }
}
