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

package edu.cmu.ml.proppr.learn;

import edu.cmu.ml.proppr.examples.PosNegRWExample;
import edu.cmu.ml.proppr.learn.tools.LossData;
import edu.cmu.ml.proppr.learn.tools.LossData.LOSS;
import edu.cmu.ml.proppr.util.SRWOptions;
import edu.cmu.ml.proppr.util.math.ParamVector;
import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.map.TIntDoubleMap;

public class PosNegLoss extends LossFunction {

    @Override
    public int computeLossGradient(ParamVector params, PosNegRWExample example,
                                   TIntDoubleMap gradient, LossData lossdata, SRWOptions c) {
        PosNegRWExample ex = example;
        int nonzero = 0;

        // add empirical loss gradient term
        // positive examples
        double pmax = 0;
        for (int a : ex.getPosList()) {
            double pa = clip(ex.p[a]);
            if (pa > pmax) { pmax = pa; }
            for (TIntDoubleIterator da = ex.dp[a].iterator(); da.hasNext(); ) {
                da.advance();
                if (da.value() == 0) { continue; }
                nonzero++;
                double aterm = -da.value() / pa;
                gradient.adjustOrPutValue(da.key(), aterm, aterm);
            }
            if (log.isDebugEnabled()) { log.debug("+p=" + pa); }
            lossdata.add(LOSS.LOG, -Math.log(pa));
        }

        //negative instance booster
        double h = pmax + c.delta;
        double beta = 1;
        if (c.delta < 0.5) { beta = (Math.log(1 / h)) / (Math.log(1 / (1 - h))); }

        // negative examples
        for (int b : ex.getNegList()) {
            double pb = clip(ex.p[b]);
            for (TIntDoubleIterator db = ex.dp[b].iterator(); db.hasNext(); ) {
                db.advance();
                if (db.value() == 0) { continue; }
                nonzero++;
                double bterm = beta * db.value() / (1 - pb);
                gradient.adjustOrPutValue(db.key(), bterm, bterm);
            }
            if (log.isDebugEnabled()) { log.debug("-p=" + pb); }
            lossdata.add(LOSS.LOG, -Math.log(1.0 - pb));
        }
        return nonzero;
    }

}
