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

package edu.cmu.ml.proppr.util.math;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ParamVector<F, T> implements Map<F, Double> {

    @Override
    public int size() {
        return getBackingStore().size();
    }

    @Override
    public boolean isEmpty() {
        return getBackingStore().isEmpty();
    }

    @Override
    public boolean containsKey(Object arg0) {
        return getBackingStore().containsKey(arg0);
    }

    @Override
    public boolean containsValue(Object value) {
        return getBackingStore().containsValue(value);
    }

    @Override
    public Double get(Object key) {
        return getWeight(getBackingStore().get(key));
    }

    @Override
    public Double put(F key, Double value) {
        T nv = this.newValue(value);
        this.getBackingStore().put(key, nv);
        return getWeight(nv);
    }

    @Override
    public Double remove(Object key) {
        return getWeight(getBackingStore().remove(key));
    }

    @Override
    public synchronized void putAll(Map<? extends F, ? extends Double> m) {
        // synchronized to match the behavior of ConcurrentHashMap.putAll()
        Map<F, T> back = getBackingStore();
        for (Map.Entry<? extends F, ? extends Double> e : m.entrySet()) {
            back.put(e.getKey(), newValue(e.getValue()));
        }
    }

    protected abstract T newValue(Double value);

    @Override
    public void clear() {
        getBackingStore().clear();
    }

    @Override
    public Set<F> keySet() {
        return getBackingStore().keySet();
    }

    protected abstract ConcurrentHashMap<F, T> getBackingStore();

    @Override
    public Collection<Double> values() {
        Map<F, T> back = getBackingStore();
        ArrayList<Double> result = new ArrayList<Double>(back.size());
        for (T value : back.values()) { result.add(getWeight(value)); }
        return result;
    }

    protected abstract Double getWeight(T value);

    @Override
    public abstract Set<Map.Entry<F, Double>> entrySet();

    public abstract ParamVector<F, T> copy();

    public void adjustValue(F key, double value) {
        this.safeAdjustValue(key, value);
        //this.hogwildAdjustValue(key, value);
    }

    private void safeAdjustValue(F key, double value) {
        T oldvalue = getBackingStore().get(key);
        if (oldvalue == null) {
            getBackingStore().putIfAbsent(key, newValue(0.0));
            oldvalue = getBackingStore().get(key);
        }
        while (!getBackingStore().replace(key, oldvalue, newValue(getWeight(oldvalue) + value))) {
            oldvalue = getBackingStore().get(key);
            if (oldvalue == null) { oldvalue = newValue(0.0); }
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {}
        }
    }

    private void hogwildAdjustValue(F key, double value) {
        this.put(key, this.get(key) + value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        for (Map.Entry<F, Double> e : entrySet()) {
            if (sb.length() > 1) { sb.append(", "); }
            sb.append(e.getKey()).append(":").append(e.getValue());
        }
        sb.append("}");
        return sb.toString();
    }
}
