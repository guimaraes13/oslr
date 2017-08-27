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

package br.ufrj.cos.util.statistics;

import br.ufrj.cos.knowledge.theory.evaluation.metric.TheoryMetric;
import br.ufrj.cos.util.LanguageUtils;
import br.ufrj.cos.util.time.TimeMeasure;

import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class to hold the statistics of an experiment. The idea is to serialize this class using the Yaml
 * library in order to be able to consult the statistics of a run in a way that is both human and machine friendly.
 * <p>
 * Created on 02/08/17.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings("HardCodedStringLiteral")
public class RunStatistics<T> {

    /**
     * The number format.
     */
    public static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance();

    protected int maximumMetricSize;

    protected TimeMeasure<T> timeMeasure;

    protected int knowledgeSize;
    protected int examplesSize;
    protected int testSize;

    protected Map<TheoryMetric, Double> trainEvaluation;
    protected Map<TheoryMetric, Double> testEvaluation;

    /**
     * Gets the knowledge base's size.
     *
     * @return the knowledge base's size
     */
    public int getKnowledgeSize() {
        return knowledgeSize;
    }

    /**
     * Sets the knowledge base's size.
     *
     * @param knowledgeSize the knowledge base's size
     */
    public void setKnowledgeSize(int knowledgeSize) {
        this.knowledgeSize = knowledgeSize;
    }

    /**
     * Gets the examples size.
     *
     * @return the examples size
     */
    public int getExamplesSize() {
        return examplesSize;
    }

    /**
     * Sets the examples size.
     *
     * @param examplesSize the examples size
     */
    public void setExamplesSize(int examplesSize) {
        this.examplesSize = examplesSize;
    }

    /**
     * Gets the test size.
     *
     * @return the test size
     */
    public int getTestSize() {
        return testSize;
    }

    /**
     * Sets the test size.
     *
     * @param testSize the test size
     */
    public void setTestSize(int testSize) {
        this.testSize = testSize;
    }

    /**
     * Gets the train evaluations.
     *
     * @return the train evaluations
     */
    public Map getTrainEvaluation() {
        return trainEvaluation;
    }

    /**
     * Sets the train evaluations.
     *
     * @param trainEvaluation the train evaluations
     */
    public void setTrainEvaluation(Map<TheoryMetric, Double> trainEvaluation) {
        this.trainEvaluation = trainEvaluation;
    }

    /**
     * Gets the test evaluations.
     *
     * @return the test evaluations
     */
    public Map getTestEvaluation() {
        return testEvaluation;
    }

    /**
     * Sets the test evaluations.
     *
     * @param testEvaluation the test evaluations
     */
    public void setTestEvaluation(Map<TheoryMetric, Double> testEvaluation) {
        this.testEvaluation = testEvaluation;
    }

    /**
     * Gets the time measure.
     *
     * @return the time measure
     */
    public TimeMeasure getTimeMeasure() {
        return timeMeasure;
    }

    /**
     * Sets the time measure.
     *
     * @param timeMeasure the time measure
     */
    public void setTimeMeasure(TimeMeasure<T> timeMeasure) {
        this.timeMeasure = timeMeasure;
    }

    @Override
    public String toString() {
        final List sortedMetrics = getSortedMetrics();

        StringBuilder description = new StringBuilder();

        description.append("Run Statistics:\n");

        description.append("\t- Knowledge Size:\t").append(NUMBER_FORMAT.format(knowledgeSize)).append("\n");
        description.append("\t- Example Size:\t\t").append(NUMBER_FORMAT.format(examplesSize)).append("\n");
        if (testEvaluation != null) {
            description.append("\t- Test Size:\t\t").append(NUMBER_FORMAT.format(testSize)).append("\n");
        }

        if (trainEvaluation != null) { appendEvaluation(sortedMetrics, description, trainEvaluation, "Train"); }
        if (testEvaluation != null) { appendEvaluation(sortedMetrics, description, testEvaluation, "Test"); }
        description.append("\n");

        description.append("\t").append("Total Run Time:\t").append(timeMeasure).append("\n");

        return description.toString();
    }

    /**
     * Gets the sorted metrics from the evaluation lists.
     *
     * @return the sorted metrics.
     */
    protected List getSortedMetrics() {
        final Set<TheoryMetric> metricSet = new HashSet<>();
        if (trainEvaluation != null) { metricSet.addAll(trainEvaluation.keySet()); }
        if (testEvaluation != null) { metricSet.addAll(testEvaluation.keySet()); }
        maximumMetricSize = metricSet.stream()
                .reduce(0, (max, metric) -> Math.max(max, metric.toString().trim().length() - 1), Math::max);
        return metricSet.stream().sorted(Comparator.comparing(o -> o.getClass().getSimpleName()))
                .collect(Collectors.toList());
    }

    /**
     * Appends the evaluation of the run to the description.
     *
     * @param sortedMetrics the metrics
     * @param description   the description
     * @param evaluation    the run evaluation
     * @param label         the label of the evaluation type {@code {Train, Test}}
     */
    protected void appendEvaluation(List<TheoryMetric> sortedMetrics, StringBuilder description,
                                    Map<TheoryMetric, Double> evaluation, String label) {
        Object value;
        description.append("\t- ").append(label).append(" Evaluation:\n");
        for (TheoryMetric key : sortedMetrics) {
            value = evaluation.get(key);
            if (value != null) {
                final String metricName = key.toString().trim();
                description.append("\t\t- ").append(metricName)
                        .append(":").append(LanguageUtils.getTabulation(metricName, maximumMetricSize))
                        .append(value).append("\n");
            }
        }
    }

}
