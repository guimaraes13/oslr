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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ParamVector<F,T> implements Map<F,Double> {
	
	protected abstract ConcurrentHashMap<F,T> getBackingStore();
	protected abstract Double getWeight(T value);
	protected abstract T newValue(Double value);
	
	@Override
	public Set<F> keySet() {
		return getBackingStore().keySet();
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
	public abstract Set<java.util.Map.Entry<F, Double>> entrySet();
	
	@Override
	public Double get(Object key) {
		return getWeight(getBackingStore().get(key));
	}
	@Override
	public boolean isEmpty() {
		return getBackingStore().isEmpty();
	}
	@Override
	public int size() {
		return getBackingStore().size();
	}
	@Override
	public Collection<Double> values() {
		Map<F,T> back = getBackingStore();
		ArrayList<Double> result = new ArrayList<Double>(back.size());
		for (T value : back.values()) result.add(getWeight(value));
		return result;
	}
	
	@Override
	public void clear() {
		getBackingStore().clear();
	}
	@Override
	public Double put(F key, Double value) {
		T nv = this.newValue(value);
		this.getBackingStore().put(key,nv);
		return getWeight(nv);
	}
	@Override
	public synchronized void putAll(Map<? extends F, ? extends Double> m) {
		// synchronized to match the behavior of ConcurrentHashMap.putAll()
		Map<F,T> back = getBackingStore();
		for (Map.Entry<? extends F, ? extends Double> e : m.entrySet()) {
			back.put(e.getKey(), newValue(e.getValue()));
		}
	}
	@Override
	public Double remove(Object key) {
		return getWeight(getBackingStore().remove(key));
	}

	public abstract ParamVector<F,T> copy ();
	
	public void adjustValue(F key, double value) {
		this.safeAdjustValue(key,value);
		//this.hogwildAdjustValue(key, value);
	}

	private void hogwildAdjustValue(F key, double value) {
	    this.put(key, this.get(key)+value);
	}
		
	private void safeAdjustValue(F key, double value) {
		T oldvalue = getBackingStore().get(key);
		if (oldvalue == null) {
			getBackingStore().putIfAbsent(key, newValue(0.0));
			oldvalue = getBackingStore().get(key);
		}
		while( !getBackingStore().replace(key, oldvalue, newValue(getWeight(oldvalue)+value))) {
			oldvalue = getBackingStore().get(key);
			if (oldvalue == null) oldvalue = newValue(0.0);
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {}
		}
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("{");
		for (Map.Entry<F,Double> e : entrySet()) {
			if (sb.length()>1) sb.append(", ");
			sb.append(e.getKey()).append(":").append(e.getValue());
		}
		sb.append("}");
		return sb.toString();
	}
}
