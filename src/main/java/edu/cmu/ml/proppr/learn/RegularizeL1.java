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
import edu.cmu.ml.proppr.util.SymbolTable;
import edu.cmu.ml.proppr.util.math.MuParamVector;
import edu.cmu.ml.proppr.util.math.ParamVector;
import gnu.trove.map.TIntDoubleMap;

public class RegularizeL1 extends Regularize {

    /**
     * L1 loss is mu * abs( theta_f )
     * d/df L1 loss is then sign(theta_f) * max( abs(theta_f), mu ), where theta_f != 0
     * <p>
     * though non-continuous, the d/df of L1 can be approximated by mu.
     * the proximal operator implementation in localL1 is more stable.
     *
     * @param f
     * @param paramVec
     * @return
     */
    @Override
    protected void synchronousUpdate(SRWOptions c, ParamVector<String, ?> params, String f,
                                     TIntDoubleMap gradient, LossData loss, SymbolTable<String> featureLibrary) {
        double value = Dictionary.safeGet(params, f);
        // want to take theta toward zero, but not past it: gradient can't be bigger than theta

        double ret = Math.signum(value) * Math.min(Math.abs(value), c.mu);
        loss.add(LOSS.REGULARIZATION, c.mu * Math.abs(value));
        gradient.adjustOrPutValue(featureLibrary.getId(f), ret, ret);
    }

    /**
     * We want to do g regularization updates simultaneously, as if they had been applied in previous gradient updates.
     * <p>
     * A single full regularization update for L1 is:
     * theta_f' = theta_f - learningRate * (sign(theta_f) * min(abs(theta_f), mu))
     * <p>
     * So we take at most learningRate * mu off the value of theta. In other words,
     * theta_f' = theta_f - (sign(theta_f) * min(abs(theta_f), learningRate * mu))
     * <p>
     * This makes it easier to take g updates -- if we do that g times, we'll take at most g * learningRate * mu off
     * the value of theta.
     * theta_f' = theta_f - (sign(theta_f) * min(abs(theta_f), g * learningRate * mu))
     *
     * @param params
     * @param apply
     * @param f
     */
    @Override
    protected void lazyUpdate(SRWOptions c, MuParamVector<String> params, ParamVector<String, ?> apply, String f,
                              LossData loss, double learningRate) {
//		if (!parent.trainable(f)) return;
        int gap = getGap(params, f);
        if (gap == 0) { return; }

        //L1 with a proximal operator
        //
        //signum(w) * max(0.0, abs(w) - shrinkageVal)

        double shrinkageVal = gap * learningRate * c.mu;
        double weightDecay;
        if ((c.mu != 0) && (!Double.isInfinite(shrinkageVal))) {
            double value = Dictionary.safeGet(params, f);
            weightDecay = Math.signum(value) * Math.min(Math.abs(value), shrinkageVal);
            apply.adjustValue(f, weightDecay);
            //FIXME: opportunity for out-of-date `value`; may want to convert to a try loop

            loss.add(LOSS.REGULARIZATION, gap * c.mu * Math.abs(value));
        }
    }
}
