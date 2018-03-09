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

package edu.cmu.ml.proppr.learn.tools;

import edu.cmu.ml.proppr.util.Dictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LossData {

    private static final Logger log = LogManager.getLogger(LossData.class);
    public Map<LOSS, Double> loss = new ConcurrentHashMap<LOSS, Double>();

    public synchronized void add(LossData that) {
        for (Map.Entry<LOSS, Double> x : that.loss.entrySet()) {
            Dictionary.increment(loss, x.getKey(), x.getValue());
        }
    }

    public synchronized void add(LOSS type, double loss) {
        if (loss < 0) {
            log.warn("SUSPICIOUS: decreasing " + type + " loss? " + loss, new RuntimeException());
        }
        if (log.isDebugEnabled()) { log.debug("Adding " + loss + " to " + type); }
        Dictionary.increment(this.loss, type, loss);
    }

    public void clear() {
        this.loss.clear();
    }

    public void convertCumulativesToAverage(int numExamples) {
        // normalize losses by number of examples to get avg loss
        for (Map.Entry<LOSS, Double> e : loss.entrySet()) {
            e.setValue(e.getValue() / numExamples);
        }
    }

    public double total() {
        double total = 0;
        for (Double d : loss.values()) { total += d; }
        return total;
    }

    /**
     * Return a new LossData containing (this.loss.get(x) - that.loss.get(x)) for every loss type x in either object.
     *
     * @param that
     * @return
     */
    public LossData diff(LossData that) {
        LossData diff = new LossData();
        for (LOSS x : this.loss.keySet()) {
            diff.loss.put(x, this.loss.get(x) - Dictionary.safeGet(that.loss, x, 0.0));
        }
        for (Map.Entry<LOSS, Double> x : that.loss.entrySet()) {
            if (!this.loss.containsKey(x.getKey())) { diff.loss.put(x.getKey(), -x.getValue()); }
        }
        return diff;
    }

    /**
     * Return a deep copy of this LossData.
     *
     * @return
     */
    public LossData copy() {
        LossData copy = new LossData();
        copy.loss.putAll(this.loss);
        return copy;
    }

    public enum LOSS {
        REGULARIZATION,
        LOG,
        L2
    }
}
