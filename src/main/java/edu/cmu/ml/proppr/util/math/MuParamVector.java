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

import edu.cmu.ml.proppr.util.TimestampedWeight;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A version of the parameter vector which also tracks the last update time of each key
 *
 * @author "Kathryn Mazaitis <krivard@cs.cmu.edu>"
 */
public class MuParamVector<F> extends ParamVector<F, TimestampedWeight> {

    private final ConcurrentHashMap<F, TimestampedWeight> backingStore;
    private long count = 0;

    public MuParamVector(Map<F, Double> store) {
        this();
        for (Map.Entry<F, Double> e : store.entrySet()) {
            this.backingStore.put(e.getKey(), newValue(e.getValue()));
        }
    }

    public MuParamVector() {
        this.backingStore = new ConcurrentHashMap<F, TimestampedWeight>();
    }

    @Override
    protected TimestampedWeight newValue(Double value) {
        return new TimestampedWeight(value, this.count);
    }

    @Override
    protected ConcurrentHashMap<F, TimestampedWeight> getBackingStore() {
        return this.backingStore;
    }

    @Override
    protected Double getWeight(TimestampedWeight value) {
        if (value == null) {
            throw new IllegalStateException("null?");
        }
        return value.wt;
    }

    @Override
    public Set<Map.Entry<F, Double>> entrySet() {
        return new MuSet(this.backingStore.entrySet());
    }

    @Override
    public ParamVector<F, TimestampedWeight> copy() {
        MuParamVector copy = new MuParamVector();
        copy.putAll(this);
        return copy;
    }

    public int getLast(F key) {
        if (!this.backingStore.containsKey(key)) { return 0; }
        return (int) -(this.backingStore.get(key).k - this.count);
    }

    public void setLast(Set<F> keys) {
        // not synchronized for now...
        for (F s : keys) {
            this.backingStore.get(s).k = this.count;
        }
    }

    public void count() {
        this.count++;
    }

    /**
     * Utility class so we can fake entry iteration over <String,Double>
     *
     * @author "Kathryn Mazaitis <krivard@cs.cmu.edu>"
     */
    public static class MuSet<F> implements Set<Map.Entry<F, Double>>, Iterator<Map.Entry<F, Double>> {

        private final Iterator<Entry<F, TimestampedWeight>> entries;

        private MuSet(Set<Map.Entry<F, TimestampedWeight>> entries) {
            this.entries = entries.iterator();
        }

        @Override
        public boolean hasNext() {
            return this.entries.hasNext();
        }

        @Override
        public Map.Entry<F, Double> next() {
            Map.Entry<F, TimestampedWeight> next = this.entries.next();
            return new MuEntry(next);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not yet implemented!");

        }

        // ***************** Dummy methods, never used:

        @Override
        public int size() {
            throw new UnsupportedOperationException("Not yet implemented!");
        }

        @Override
        public boolean isEmpty() {
            throw new UnsupportedOperationException("Not yet implemented!");
        }

        @Override
        public boolean contains(Object arg0) {
            throw new UnsupportedOperationException("Not yet implemented!");
        }

        @Override
        public Iterator<Map.Entry<F, Double>> iterator() {
            return this;
        }

        @Override
        public Object[] toArray() {
            throw new UnsupportedOperationException("Not yet implemented!");
        }

        @Override
        public <T> T[] toArray(T[] arg0) {
            throw new UnsupportedOperationException("Not yet implemented!");
        }

        @Override
        public boolean add(Map.Entry<F, Double> arg0) {
            throw new UnsupportedOperationException("Not yet implemented!");
        }

        @Override
        public boolean remove(Object arg0) {
            throw new UnsupportedOperationException("Not yet implemented!");
        }

        @Override
        public boolean containsAll(Collection<?> arg0) {
            throw new UnsupportedOperationException("Not yet implemented!");
        }

        @Override
        public boolean addAll(
                Collection<? extends Map.Entry<F, Double>> arg0) {
            throw new UnsupportedOperationException("Not yet implemented!");
        }

        @Override
        public boolean retainAll(Collection<?> arg0) {
            throw new UnsupportedOperationException("Not yet implemented!");
        }

        @Override
        public boolean removeAll(Collection<?> arg0) {
            throw new UnsupportedOperationException("Not yet implemented!");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("Not yet implemented!");
        }

        public static class MuEntry<F> implements Map.Entry<F, Double> {

            Map.Entry<F, TimestampedWeight> source;

            private MuEntry(Map.Entry<F, TimestampedWeight> src) {
                this.source = src;
            }

            @Override
            public F getKey() {
                return this.source.getKey();
            }

            @Override
            public Double getValue() {
                return this.source.getValue().wt;
            }

            @Override
            public Double setValue(Double value) {
                this.source.getValue().wt = value;
                return value;
            }

        }
    }
}
