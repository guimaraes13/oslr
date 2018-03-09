/*
 * Probabilist Logic Learner is a system to learn probabilistic logic
 * programs from data and use its learned programs to make inference
 * and answer queries.
 *
 * Copyright (C) 2018 Victor Guimarães
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

package edu.cmu.ml.proppr.prove;

import edu.cmu.ml.proppr.learn.tools.FixedWeightRules;
import edu.cmu.ml.proppr.prove.wam.CachingIdProofGraph;
import edu.cmu.ml.proppr.prove.wam.CallStackFrame;
import edu.cmu.ml.proppr.prove.wam.State;
import edu.cmu.ml.proppr.util.APROptions;
import edu.cmu.ml.proppr.util.StatusLogger;
import edu.cmu.ml.proppr.util.math.LongDense;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 */
public class PruningIdDprProver extends IdDprProver {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();
    private final CachingIdProofGraph.VisibilityFilter test;
    private final FixedWeightRules prunedPredicateRules;

    public PruningIdDprProver(APROptions apr, FixedWeightRules prunedPredicateRules) {
        super(false, apr);
        this.test = new PredicatePruner(prunedPredicateRules);
        this.prunedPredicateRules = prunedPredicateRules;
    }

    @Override
    public String toString() {
        return String.format("p_idpr:%.6g:%g", apr.epsilon, apr.alpha);
    }

    @Override
    public Prover<CachingIdProofGraph> copy() {
        PruningIdDprProver copy = new PruningIdDprProver(this.apr, this.prunedPredicateRules);
        copy.params = this.params;
        if (this.parent != null) { copy.parent = this.parent; } else { copy.parent = this; }
        return copy;
    }

    @Override
    public Map<State, Double> prove(CachingIdProofGraph pg, StatusLogger status) {
        //logger.info("calling Prunedpredicaterules.prove");
        LongDense.FloatVector p = new LongDense.FloatVector();
        prove(pg, p, status);
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

    public static class PredicatePruner implements CachingIdProofGraph.VisibilityFilter {

        private final FixedWeightRules rules;

        public PredicatePruner(FixedWeightRules rules) {
            this.rules = rules;
        }

        @Override
        public boolean visible(State state) {
            if (rules == null) { return true; }
            // test to see if any 'pruned' predicate is on the stack
            String jumpTo = state.getJumpTo();
            if (jumpTo != null && rules.isFixed(state.getJumpTo())) {
                return false;
            }
            for (CallStackFrame frame : state.getCalls()) {
                jumpTo = frame.getJumpTo();
                if (jumpTo != null && rules.isFixed(jumpTo)) {
                    return false;
                }
            }
            return true;
        }
    }
}
