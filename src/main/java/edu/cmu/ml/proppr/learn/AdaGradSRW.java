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
import edu.cmu.ml.proppr.util.SRWOptions;
import edu.cmu.ml.proppr.util.StatusLogger;
import edu.cmu.ml.proppr.util.math.ParamVector;
import edu.cmu.ml.proppr.util.math.SimpleParamVector;
import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.map.TIntDoubleMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;

/**
 * The AdaGrad version of SRW (see below for description)
 *
 * @author rosecatherinek
 * <p>
 * <p>
 * Random walk learning
 * <p>
 * Flow of information:
 * <p>
 * Train on example =
 * load (initialize example parameters and compute M/dM)
 * inference (compute p/dp)
 * sgd (compute empirical loss gradient and apply to parameters)
 * <p>
 * Accumulate gradient =
 * load  (initialize example parameters and compute M/dM)
 * inference (compute p/dp)
 * gradient (compute empirical loss gradient)
 * @author krivard
 */
public class AdaGradSRW extends SRW {

    private static final Logger log = LogManager.getLogger(AdaGradSRW.class);
    private static final double MIN_GRADIENT = Math.sqrt(Double.MIN_VALUE) * 10;
    // This makes AdaGradSRW stateful, but SRW should only ever be used by one thread at a time
    private ParamVector<String, ?> totSqGrad = null;

    public AdaGradSRW() {
        super(new SRWOptions());
    }

    public AdaGradSRW(SRWOptions params) {
        super(params);
        totSqGrad = new SimpleParamVector<String>(new ConcurrentHashMap<String, Double>());
    }

    /**
     * Modify the parameter vector by taking a gradient step along the dir suggested by this example.
     * <p>
     * AdaGrad: use the adaptive learning rate
     *
     * @param params
     * @param example
     */
    @Override
    public void trainOnExample(ParamVector<String, ?> params, PosNegRWExample example, StatusLogger status) {
        if (log.isDebugEnabled()) {
            log.debug("Training on " + example);
        } else if (log.isInfoEnabled() && status.due(2)) {
            log.info(Thread.currentThread() + " Training on " + example);
        }

        initializeFeatures(params, example.getGraph());
        regularizer.prepareForExample(params, example.getGraph(), params);
        load(params, example);
        inference(params, example, status);
        agd(params, example);
    }

    @Override
    protected double learningRate(String feature) {
        if (!totSqGrad.containsKey(feature)) { return 0.0; }
        return c.eta / Math.sqrt(this.totSqGrad.get(feature));
    }

    @Override
    public SRW copy() {
        SRW cop = super.copy();
        ((AdaGradSRW) cop).setTotSqGrad(this.totSqGrad);
        return cop;
    }

    public void setTotSqGrad(ParamVector<String, ?> t) {
        this.totSqGrad = t;
    }

    /**
     * AdaGrad Descent Algo
     * <p>
     * edits params using totSqGrad as well
     *
     * @author rosecatherinek
     */
    protected void agd(ParamVector<String, ?> params, PosNegRWExample ex) {
        TIntDoubleMap gradient = gradient(params, ex);
        // apply gradient to param vector
        for (TIntDoubleIterator grad = gradient.iterator(); grad.hasNext(); ) {
            grad.advance();
            // avoid underflow since we're summing the square
            if (Math.abs(grad.value()) < MIN_GRADIENT) { continue; }
            String feature = ex.getGraph().featureLibrary.getSymbol(grad.key());

            if (trainable(feature)) {
                Double g = grad.value();

                //first update the running total of the square of the gradient
                totSqGrad.adjustValue(feature, g * g);

                //now get the running total
//				Double rt = totSqGrad.get(feature);

                //w_{t+1, i} = w_{t, i} - \eta * g_{t,i} / \sqrt{ G,i }

//				Double descentVal = - c.eta * g / Math.sqrt(rt);

                params.adjustValue(feature, -learningRate(feature) * g);

                if (params.get(feature).isInfinite()) {
                    log.warn("Infinity at " + feature + "; gradient " + grad.value() + "; rt " + totSqGrad.get(feature));
                }
            }
        }
    }

}
