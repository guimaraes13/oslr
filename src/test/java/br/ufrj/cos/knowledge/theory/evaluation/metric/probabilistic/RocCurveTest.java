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

package br.ufrj.cos.knowledge.theory.evaluation.metric.probabilistic;

import br.ufrj.cos.knowledge.example.AtomExample;
import br.ufrj.cos.logic.Predicate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created on 20/08/17.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings("ALL")
public class RocCurveTest {

    @Test
    public void TEST_AREA_UNDER_ROC() {
        List<Pair<Double, Double>> points = new ArrayList<>();
        points.add(new ImmutablePair<>(0.0, 0.0));
        points.add(new ImmutablePair<>(0.03185343170044681, 0.04939547101684706));
        points.add(new ImmutablePair<>(0.0539891013126141, 0.0781732229877477));
        points.add(new ImmutablePair<>(0.1538976515811945, 0.13264763636321975));
        points.add(new ImmutablePair<>(0.25010475666488463, 0.218951816976695));
        points.add(new ImmutablePair<>(0.3073152584687257, 0.3136242514208931));
        points.add(new ImmutablePair<>(0.33584189128393477, 0.3595861415342123));
        points.add(new ImmutablePair<>(0.3854615307025594, 0.3659866327431083));
        points.add(new ImmutablePair<>(0.42088082646597114, 0.4252550364221374));
        points.add(new ImmutablePair<>(0.4735346925952219, 0.48420268693252355));
        points.add(new ImmutablePair<>(0.5345068618196882, 0.5622196802904462));
        points.add(new ImmutablePair<>(0.5612625443771511, 0.65691089864799));
        points.add(new ImmutablePair<>(0.5808087943017545, 0.7234134996085725));
        points.add(new ImmutablePair<>(0.5808379821948763, 0.7776343127827225));
        points.add(new ImmutablePair<>(0.5957629470659391, 0.8213524627969195));
        points.add(new ImmutablePair<>(0.6428295240120546, 0.8546822935692172));
        points.add(new ImmutablePair<>(0.658867151305349, 0.9074423490320891));
        points.add(new ImmutablePair<>(1.0, 1.0));

        double expectedAreaUnderRoc = 0.563010497079;
        double calculatedArea = RocCurveMetric.integrateRocCurve(points);
        Assert.assertEquals(expectedAreaUnderRoc, calculatedArea, 1e-4);
    }

    @Test
    public void TEST_ALL_RIGHT_ROC() {
        double expectedAreaUnderRoc = 1.0;

        Predicate predicate = new Predicate("dumb");
        List<Pair<AtomExample, Double>> pairs = new ArrayList<>();
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, false), 0.17678417));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, false), 0.23162987));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, false), 0.27172821));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, false), 0.28730114));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, false), 0.29486018));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.35861145));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.37020265));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.4659956));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.8009047));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.91826629));

        List<Pair<Double, Double>> points = RocCurveMetric.buildRocCurve(pairs);
        double calculatedArea = RocCurveMetric.integrateRocCurve(points);

        Assert.assertEquals(expectedAreaUnderRoc, calculatedArea, 1e-4);
    }

    @Test
    public void TEST_ALL_WRONG_ROC() {
        double expectedAreaUnderRoc = 0.0;

        Predicate predicate = new Predicate("dumb");
        List<Pair<AtomExample, Double>> pairs = new ArrayList<>();
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.17678417));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.23162987));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.27172821));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.28730114));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.29486018));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, false), 0.35861145));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, false), 0.37020265));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, false), 0.4659956));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, false), 0.8009047));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, false), 0.91826629));

        List<Pair<Double, Double>> points = RocCurveMetric.buildRocCurve(pairs);
        double calculatedArea = RocCurveMetric.integrateRocCurve(points);

        Assert.assertEquals(expectedAreaUnderRoc, calculatedArea, 1e-4);
    }

    @Test
    public void TEST_ALL_TRUE_ROC() {
        double expectedAreaUnderRoc = 1.0;

        Predicate predicate = new Predicate("dumb");
        List<Pair<AtomExample, Double>> pairs = new ArrayList<>();
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.17678417));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.23162987));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.27172821));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.28730114));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.29486018));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.35861145));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.37020265));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.4659956));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.8009047));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.91826629));

        List<Pair<Double, Double>> points = RocCurveMetric.buildRocCurve(pairs);
        double calculatedArea = RocCurveMetric.integrateRocCurve(points);

        Assert.assertEquals(expectedAreaUnderRoc, calculatedArea, 1e-4);
    }

    @Test
    public void TEST_ALL_FALSE_ROC() {
        double expectedAreaUnderRoc = 0.0;

        Predicate predicate = new Predicate("dumb");
        List<Pair<AtomExample, Double>> pairs = new ArrayList<>();
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, false), 0.17678417));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, false), 0.23162987));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, false), 0.27172821));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, false), 0.28730114));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, false), 0.29486018));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, false), 0.35861145));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, false), 0.37020265));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, false), 0.4659956));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, false), 0.8009047));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, false), 0.91826629));

        List<Pair<Double, Double>> points = RocCurveMetric.buildRocCurve(pairs);
        double calculatedArea = RocCurveMetric.integrateRocCurve(points);

        Assert.assertEquals(expectedAreaUnderRoc, calculatedArea, 1e-4);
    }

    @Test
    public void TEST_HALF_RIGHT_ROC() {
        double expectedAreaUnderRoc = 0.6;

        Predicate predicate = new Predicate("dumb");
        List<Pair<AtomExample, Double>> pairs = new ArrayList<>();
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, false), 0.17678417));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.23162987));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, false), 0.27172821));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.28730114));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, false), 0.29486018));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.35861145));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, false), 0.37020265));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.4659956));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, false), 0.8009047));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.91826629));

        List<Pair<Double, Double>> points = RocCurveMetric.buildRocCurve(pairs);
        double calculatedArea = RocCurveMetric.integrateRocCurve(points);

        Assert.assertEquals(expectedAreaUnderRoc, calculatedArea, 1e-4);
    }

    @Test
    public void TEST_HALF_RIGHT_ROC2() {
        double expectedAreaUnderRoc = 0.4;

        Predicate predicate = new Predicate("dumb");
        List<Pair<AtomExample, Double>> pairs = new ArrayList<>();
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.17678417));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, false), 0.23162987));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.27172821));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, false), 0.28730114));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.29486018));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, false), 0.35861145));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.37020265));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, false), 0.4659956));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.8009047));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, false), 0.91826629));

        List<Pair<Double, Double>> points = RocCurveMetric.buildRocCurve(pairs);
        double calculatedArea = RocCurveMetric.integrateRocCurve(points);

        Assert.assertEquals(expectedAreaUnderRoc, calculatedArea, 1e-4);
    }

    @Test
    public void TEST_LAST_FALSE_ROC() {
        double expectedAreaUnderRoc = 0.0;

        Predicate predicate = new Predicate("dumb");
        List<Pair<AtomExample, Double>> pairs = new ArrayList<>();
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.17678417));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.23162987));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.27172821));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.28730114));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.29486018));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.35861145));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.37020265));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.4659956));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.8009047));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, false), 0.91826629));

        List<Pair<Double, Double>> points = RocCurveMetric.buildRocCurve(pairs);
        double calculatedArea = RocCurveMetric.integrateRocCurve(points);

        Assert.assertEquals(expectedAreaUnderRoc, calculatedArea, 1e-4);
    }

    @Test
    public void TEST_MIDDLE_FALSE_ROC() {
        double expectedAreaUnderRoc = 0.55555;

        Predicate predicate = new Predicate("dumb");
        List<Pair<AtomExample, Double>> pairs = new ArrayList<>();
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.17678417));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.23162987));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.27172821));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.28730114));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, false), 0.29486018));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.35861145));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.37020265));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.4659956));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.8009047));
        pairs.add(new ImmutablePair<>(new AtomExample(predicate, true), 0.91826629));

        List<Pair<Double, Double>> points = RocCurveMetric.buildRocCurve(pairs);
        double calculatedArea = RocCurveMetric.integrateRocCurve(points);

        Assert.assertEquals(expectedAreaUnderRoc, calculatedArea, 1e-4);
    }

    @Test
    public void TEST_EMPTY_ROC() {
        double expectedAreaUnderRoc = 1.0;

        List<Pair<Double, Double>> points = RocCurveMetric.buildRocCurve(Collections.emptyList());
        double calculatedArea = RocCurveMetric.integrateRocCurve(points);

        Assert.assertEquals(expectedAreaUnderRoc, calculatedArea, 1e-4);
    }

}
