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
