/*
 * Online Structure Learner by Revision (OSLR) is an online relational
 * learning algorithm that can handle continuous, open-ended
 * streams of relational examples as they arrive. We employ
 * techniques from theory revision to take advantage of the already
 * acquired knowledge as a starting point, find where it should be
 * modified to cope with the new examples, and automatically update it.
 * We rely on the Hoeffding's bound statistical theory to decide if the
 * model must in fact be updated accordingly to the new examples.
 * The system is built upon ProPPR statistical relational language to
 * describe the induced models, aiming at contemplating the uncertainty
 * inherent to real data.
 *
 * Copyright (C) 2017-2018 Victor Guimarães
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

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.cmu.ml.proppr.prove;

import edu.cmu.ml.proppr.learn.tools.FixedWeightRules;
import edu.cmu.ml.proppr.prove.wam.CachingIdProofGraph;
import edu.cmu.ml.proppr.prove.wam.State;
import edu.cmu.ml.proppr.util.APROptions;
import edu.cmu.ml.proppr.util.StatusLogger;
import edu.cmu.ml.proppr.util.math.LongDense;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 */
public class PruningIdPprProver extends IdPprProver {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();
    private final CachingIdProofGraph.VisibilityFilter test;
    private final FixedWeightRules prunedPredicateRules;

    public PruningIdPprProver(APROptions apr, FixedWeightRules prunedPredicateRules) {
        super(apr);
        this.test = new CachingIdProofGraph.PredicatePruner(prunedPredicateRules);
        this.prunedPredicateRules = prunedPredicateRules;
    }

    @Override
    public String toString() {
        return String.format("p_ippr:%.6g:%g", apr.epsilon, apr.alpha);
    }

    @Override
    public Map<State, Double> prove(CachingIdProofGraph pg, StatusLogger status) {
        //logger.info("calling Prunedpredicaterules.prove");
        LongDense.FloatVector p = proveVec(pg, status);
        if (apr.traceDepth != 0) {
            logger.info("== before pruning:  edges/nodes " + pg.edgeSize() + "/" + pg.nodeSize());
            logger.info(pg.treeView(apr.traceDepth, apr.traceRoot, weighter, p));
        }
        LongDense.FloatVector prunedP = pg.prune(params, weighter, test, p);
        //logger.info("== after pruning:  edges/nodes "+pg.edgeSize()+"/"+pg.nodeSize());
        //logger.info(pg.treeView(weighter,prunedP));
        if (apr.traceDepth != 0) {
            logger.info("== after pruning:  edges/nodes " + pg.edgeSize() + "/" + pg.nodeSize());
            logger.info(pg.treeView(apr.traceDepth, apr.traceRoot, weighter, prunedP));
        }
        return pg.asMap(prunedP);
    }

    @Override
    public Prover<CachingIdProofGraph> copy() {
        PruningIdPprProver copy = new PruningIdPprProver(this.apr, this.prunedPredicateRules);
        copy.params = this.params;
        return copy;
    }

}
