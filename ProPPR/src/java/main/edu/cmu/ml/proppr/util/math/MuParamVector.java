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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import edu.cmu.ml.proppr.util.TimestampedWeight;

/**
 * A version of the parameter vector which also tracks the last update time of each key
 * @author "Kathryn Mazaitis <krivard@cs.cmu.edu>"
 *
 */
public class MuParamVector<F> extends ParamVector<F,TimestampedWeight> {
	private ConcurrentHashMap<F,TimestampedWeight> backingStore;
	private long count=0;
	
	public MuParamVector() {
		this.backingStore = new ConcurrentHashMap<F,TimestampedWeight>();
	}
	public MuParamVector(Map<F,Double> store) {
		this();
		for(Map.Entry<F, Double> e : store.entrySet()) {
			this.backingStore.put(e.getKey(), newValue(e.getValue()));
		}
	}
	
	@Override
	protected ConcurrentHashMap<F, TimestampedWeight> getBackingStore() {
		return this.backingStore;
	}

	@Override
	protected TimestampedWeight newValue(Double value) {
		return new TimestampedWeight(value,this.count);
	}

	@Override
	protected Double getWeight(TimestampedWeight value) {
		if (value==null) {
			throw new IllegalStateException("null?");
		}
		return value.wt;
	}
	
	@Override
	public Set<java.util.Map.Entry<F, Double>> entrySet() {
		return new MuSet(this.backingStore.entrySet());
	}

	public int getLast(F key) {
		if (!this.backingStore.containsKey(key)) 
			return 0;
		return (int) -(this.backingStore.get(key).k - this.count);
	}
	
	public void setLast(Set<F> keys) {
		// not synchronized for now...
		for(F s : keys) {
			this.backingStore.get(s).k = this.count;
		}
	}
	
	public void count() {
		this.count++;
	}
	
	@Override
	public ParamVector<F,TimestampedWeight> copy () {
		MuParamVector copy = new MuParamVector();
        copy.putAll(this);
        return copy;
    }
	
	/** Utility class so we can fake entry iteration over <String,Double>
	 * 
	 * @author "Kathryn Mazaitis <krivard@cs.cmu.edu>"
	 *
	 */
	public static class MuSet<F> implements Set<java.util.Map.Entry<F,Double>>, Iterator<java.util.Map.Entry<F, Double>> {
		private Iterator<Entry<F, TimestampedWeight>> entries;

		private MuSet(Set<java.util.Map.Entry<F,TimestampedWeight>> entries) {
			this.entries = entries.iterator();
		}

		@Override
		public Iterator<java.util.Map.Entry<F, Double>> iterator() {
			return this;
		}

		@Override
		public boolean hasNext() {
			return this.entries.hasNext();
		}

		@Override
		public java.util.Map.Entry<F, Double> next() {
			java.util.Map.Entry<F, TimestampedWeight> next = this.entries.next();
			return new MuEntry(next);
		}
		
		// ***************** Dummy methods, never used:
		
		@Override
		public boolean add(java.util.Map.Entry<F, Double> arg0) {
			throw new UnsupportedOperationException("Not yet implemented!");
		}

		@Override
		public boolean addAll(
				Collection<? extends java.util.Map.Entry<F, Double>> arg0) {
			throw new UnsupportedOperationException("Not yet implemented!");
		}

		@Override
		public void clear() {
			throw new UnsupportedOperationException("Not yet implemented!");
		}

		@Override
		public boolean contains(Object arg0) {
			throw new UnsupportedOperationException("Not yet implemented!");
		}

		@Override
		public boolean containsAll(Collection<?> arg0) {
			throw new UnsupportedOperationException("Not yet implemented!");
		}

		@Override
		public boolean isEmpty() {
			throw new UnsupportedOperationException("Not yet implemented!");
		}

		@Override
		public boolean remove(Object arg0) {
			throw new UnsupportedOperationException("Not yet implemented!");
		}

		@Override
		public boolean removeAll(Collection<?> arg0) {
			throw new UnsupportedOperationException("Not yet implemented!");
		}

		@Override
		public boolean retainAll(Collection<?> arg0) {
			throw new UnsupportedOperationException("Not yet implemented!");
		}

		@Override
		public int size() {
			throw new UnsupportedOperationException("Not yet implemented!");
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
		public void remove() {
			throw new UnsupportedOperationException("Not yet implemented!");
			
		}
		public static class MuEntry<F> implements java.util.Map.Entry<F,Double> {
			java.util.Map.Entry<F, TimestampedWeight> source;
			private MuEntry(java.util.Map.Entry<F, TimestampedWeight> src) {
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
