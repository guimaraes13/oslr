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

package edu.cmu.ml.proppr.util.math;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleParamVector<F> extends ParamVector<F, Double> {

    private final ConcurrentHashMap<F, Double> backingStore;

    public SimpleParamVector() {
        this.backingStore = new ConcurrentHashMap<F, Double>();
    }

    public SimpleParamVector(Map<F, Double> map) {

        if (map instanceof ConcurrentHashMap) { this.backingStore = (ConcurrentHashMap) map; } else {
            this.backingStore = new ConcurrentHashMap<F, Double>();
            this.backingStore.putAll(map);
        }
    }

    @Override
    protected Double newValue(Double value) {
        return value;
    }

    @Override
    protected ConcurrentHashMap<F, Double> getBackingStore() {
        return backingStore;
    }

    @Override
    protected Double getWeight(Double value) {
        return value;
    }

    @Override
    public Set<Map.Entry<F, Double>> entrySet() {
        return backingStore.entrySet();
    }

    @Override
    public ParamVector<F, Double> copy() {
        ParamVector<F, Double> copy = new SimpleParamVector<F>();
        copy.putAll(this);
        return copy;
    }

}
