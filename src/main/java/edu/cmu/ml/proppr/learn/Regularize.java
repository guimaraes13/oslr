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
import edu.cmu.ml.proppr.util.SRWOptions;
import edu.cmu.ml.proppr.util.SymbolTable;
import edu.cmu.ml.proppr.util.math.MuParamVector;
import edu.cmu.ml.proppr.util.math.ParamVector;
import gnu.trove.map.TIntDoubleMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Regularize {

    private static final Logger log = LogManager.getLogger(Regularize.class);

    /**
     * This is a normal update where each global feature is
     * regularized at every example. Override to provide a particular
     * regularization function.
     */
    protected void synchronousUpdate(SRWOptions c, ParamVector<String, ?> params, String f,
                                     TIntDoubleMap gradient, LossData loss, SymbolTable<String> featureLibrary) {
    }

    /**
     * This is a lazy update, where the features in a particular
     * example are regularized in a batch, applying the regularization
     * as many times as since the last time that feature was
     * updated. Override to proved a particular regularization
     * function.
     */
    protected void lazyUpdate(SRWOptions c, MuParamVector<String> params,
                              ParamVector<String, ?> apply, String f, LossData loss, double learningRate) {
    }

    /**
     * Utility function to avoid race conditions that put lazy
     * regularization in an invalid state.
     */
    protected int getGap(MuParamVector<String> params, String f) {
        int gap = params.getLast(f);
        int tries = 0;
        while (gap < 0) { // Can't figure out why gap is showing up < 0 :(
            try {
                Thread.sleep(10);
                gap = params.getLast(f);
                tries++;
            } catch (InterruptedException e) {}
        }
        if (tries > 1 && log.isInfoEnabled()) {
            log.info("Took " + (tries + 1) + " tries to get a valid gap measure @ " + f);
        }
        return gap;
    }
}
