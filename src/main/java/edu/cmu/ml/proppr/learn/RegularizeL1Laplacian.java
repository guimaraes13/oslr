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

import edu.cmu.ml.proppr.learn.tools.LossData;
import edu.cmu.ml.proppr.learn.tools.LossData.LOSS;
import edu.cmu.ml.proppr.util.Dictionary;
import edu.cmu.ml.proppr.util.SRWOptions;
import edu.cmu.ml.proppr.util.math.MuParamVector;
import edu.cmu.ml.proppr.util.math.ParamVector;

import java.util.List;

public class RegularizeL1Laplacian extends RegularizeL1 {

    @Override
    protected void lazyUpdate(SRWOptions c, MuParamVector<String> params, ParamVector<String, ?> apply, String f,
                              LossData loss, double learningRate) {
        int gap = getGap(params, f);
        if (gap == 0) { return; }
        double value = Dictionary.safeGet(params, f);

        double laplacian = 0;
        double sumofsquares = 0;

        String target = "#" + f;
        if (c.diagonalDegree.containsKey(target)) {
            double positive = c.diagonalDegree.get(target) * value;
            double negativeSum = 0;
            sumofsquares = value * value;
            List<String> sims = c.affinity.get(target);
            for (String s : sims) {
                double svalue = Dictionary.safeGet(params, s);
                negativeSum -= svalue;
                sumofsquares = sumofsquares + svalue * svalue;
            }
            laplacian = positive + negativeSum;
            //System.out.println("f: " + f +" laplacian:" + laplacian);
        }

        //Laplacian
        double powerTerm = Math.pow(1 - 2 * c.zeta * learningRate * laplacian, gap);
        double weightDecay = laplacian * (powerTerm - 1);
        Dictionary.increment(params, f, weightDecay);
        loss.add(LOSS.REGULARIZATION, gap * c.zeta * Math.pow(value, 2));

        //L1 with a proximal operator
        //signum(w) * max(0.0, abs(w) - shrinkageVal)

        double shrinkageVal = gap * learningRate * c.mu;
        if ((c.mu != 0) && (!Double.isInfinite(shrinkageVal))) {
            weightDecay = Math.signum(value) * Math.max(0.0, Math.abs(value) - shrinkageVal);
            Dictionary.set(params, f, weightDecay);
            //FIXME: why is this being set instead of incremented?
            //FIXME: opportunity for out-of-date `value`; probably out to convert to a try loop
            loss.add(LOSS.REGULARIZATION, gap * c.mu);
        }
    }
}
