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

package br.ufrj.cos.knowledge.theory.evaluation;

import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.example.Examples;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.knowledge.theory.evaluation.metric.TheoryMetric;
import br.ufrj.cos.logic.HornClause;
import br.ufrj.cos.util.time.TimeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.concurrent.Callable;

import static br.ufrj.cos.util.log.InferenceLog.ERROR_EVALUATING_CANDIDATE_THEORY;
import static br.ufrj.cos.util.log.InferenceLog.EVALUATION_THEORY_TIMEOUT;

/**
 * Handle a asynchronous execution of evaluation a {@link Theory}. This is useful when a maximum amount of time is
 * specified for the task. In addition, have a timeout, the maximum amount of time the thread is allowed to run.
 * <p>
 * Created on 29/04/17.
 *
 * @author Victor Guimarães
 */
public class AsyncTheoryEvaluator<E> implements Runnable, Callable<AsyncTheoryEvaluator<E>> {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    /**
     * Constant that represent that the thread can run with no time restriction.
     */
    public static final int NO_TIMEOUT = 0;

    protected final TheoryEvaluator theoryEvaluator;
    protected final TheoryMetric theoryMetric;

    protected final Collection<? extends Example> examples;

    protected HornClause hornClause;
    protected E element;

    protected int timeout = NO_TIMEOUT;

    protected double evaluation;

    protected boolean evaluationFinished;
    protected double evaluationTime;
    private long begin;

    /**
     * Constructor with the needed parameters.
     *
     * @param examples        the {@link Examples}
     * @param theoryEvaluator the {@link TheoryEvaluator}
     * @param theoryMetric    the {@link TheoryMetric}
     * @param timeout         the maximum amount of time the thread is allowed to run
     */
    public AsyncTheoryEvaluator(Collection<? extends Example> examples, TheoryEvaluator theoryEvaluator,
                                TheoryMetric theoryMetric,
                                int timeout) {
        this(examples, theoryEvaluator, theoryMetric);
        this.timeout = timeout;
    }

    /**
     * Constructor with the no timeout.
     *
     * @param examples        the {@link Examples}
     * @param theoryEvaluator the {@link TheoryEvaluator}
     * @param theoryMetric    the {@link TheoryMetric}
     */
    public AsyncTheoryEvaluator(Collection<? extends Example> examples, TheoryEvaluator theoryEvaluator,
                                TheoryMetric theoryMetric) {
        this.examples = examples;
        this.theoryEvaluator = theoryEvaluator;
        this.theoryMetric = theoryMetric;
        this.evaluation = theoryMetric.getDefaultValue();
    }

    /**
     * Copies this class into another class of different parametrized type.
     *
     * @param <V> the other type
     * @return the copy
     */
    public <V> AsyncTheoryEvaluator<V> copy() {
        AsyncTheoryEvaluator<V> copy = new AsyncTheoryEvaluator(examples, theoryEvaluator, theoryMetric);
        copy.hornClause = hornClause;
        copy.timeout = timeout;
        copy.evaluation = evaluation;
        copy.evaluationFinished = evaluationFinished;

        return copy;
    }

    /**
     * Use this method to evaluateTheory the {@link Theory} with the given timeout.
     * <p>
     * {@inheritDoc}
     */
    @SuppressWarnings("IntegerMultiplicationImplicitCastToLong")
    @Override
    public AsyncTheoryEvaluator<E> call() {
        Thread thread = new Thread(this);
        try {
            thread.start();
            thread.join(timeout * TimeUtils.SECONDS_TO_MILLISECONDS_MULTIPLIER);
        } catch (InterruptedException e) {
            logger.error(ERROR_EVALUATING_CANDIDATE_THEORY, e);
        } finally {
            if (thread.isAlive()) {
                logger.trace(EVALUATION_THEORY_TIMEOUT.toString(), timeout);
                thread.interrupt();
            } else {
                final long end = TimeUtils.getNanoTime();
                evaluationTime = TimeUtils.elapsedTimeInSeconds(begin, end);
                evaluationFinished = true;
            }
        }
        return this;
    }

    /**
     * Use this method (directly or by starting a new thread) if no timeout will be used. Otherwise, use the
     * {@link #call()} method.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void run() {
        evaluationFinished = false;
        evaluationTime = Double.POSITIVE_INFINITY;
        begin = TimeUtils.getNanoTime();
        evaluation = theoryEvaluator.evaluateTheoryAppendingClauses(theoryMetric, examples, hornClause);
    }

    /**
     * Gets the evaluated value.
     *
     * @return the evaluated value
     */
    public double getEvaluation() {
        return evaluation;
    }

    /**
     * Gets the {@link #hornClause}, the evaluated clause that is added to the {@link Theory}.
     *
     * @return the {@link HornClause}
     */
    public HornClause getHornClause() {
        return hornClause;
    }

    /**
     * Sets the {@link #hornClause}, the evaluated clause that is added to the {@link Theory}.
     *
     * @param hornClause the {@link #hornClause}
     */
    public void setHornClause(HornClause hornClause) {
        this.hornClause = hornClause;
    }

    /**
     * Gets if the evaluation has finished.
     *
     * @return {@code true} if the evaluation has finished, {@code false} otherwise
     */
    public boolean isEvaluationFinished() {
        return evaluationFinished;
    }

    /**
     * Gets the evaluation time, in seconds. If it is infinity, either the evaluation is still running or it has been
     * interrupted.
     *
     * @return the evaluation time, in seconds.
     */
    public double getEvaluationTime() {
        return evaluationTime;
    }

    /**
     * Gets the element.
     *
     * @return the element
     */
    public E getElement() {
        return element;
    }

    /**
     * Sets the element.
     *
     * @param element the element
     */
    public void setElement(E element) {
        this.element = element;
    }

    @Override
    public String toString() {
        return hornClause.toString();
    }

}
