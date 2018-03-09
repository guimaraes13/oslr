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

package edu.cmu.ml.proppr.util;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.custom_hash.TObjectIntCustomHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.strategy.HashingStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * A symbol table mapping strings to/from integers in the range
 * 1..N inclusive.
 *
 * @author wcohen, krivard
 */
public class SimpleSymbolTable<T> implements SymbolTable<T> {

    protected List<T> symbolList = new ArrayList<T>();
    protected int nextId = 0;
    protected TObjectIntMap<T> idDict;

    public SimpleSymbolTable() {
        this.idDict = new TObjectIntHashMap<T>();
    }

    public SimpleSymbolTable(HashingStrategy<T> strat) {
        this.idDict = new TObjectIntCustomHashMap<T>(strat);
    }

    public List<T> getSymbolList() {
        return this.symbolList;
    }	/* (non-Javadoc)
     * @see edu.cmu.ml.proppr.util.ISymbolTable#insert(T)
	 */

    @Override
    public void insert(T symbol) {
        if (this.idDict.containsKey(symbol)) { return; }
        synchronized (this) {
            if (!this.idDict.containsKey(symbol)) {
                this.nextId += 1;
                this.idDict.put(symbol, this.nextId);
                this.symbolList.add(symbol);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SymbolTable:");
        for (int i = 0; i < this.symbolList.size(); i++) {
            sb.append(" ").append(this.symbolList.get(i)).append(":").append(i + 1);
        }
        return sb.toString();
    }	/* (non-Javadoc)
	 * @see edu.cmu.ml.proppr.util.ISymbolTable#getId(T)
	 */

    @Override
    public int getId(T symbol) {
        this.insert(symbol);
        // FIXME this may be slow
        return this.idDict.get(symbol);
    }

    /* (non-Javadoc)
     * @see edu.cmu.ml.proppr.util.ISymbolTable#hasId(T)
     */
    @Override
    public boolean hasId(T symbol) {
        return this.idDict.containsKey(symbol);
    }

    /* (non-Javadoc)
     * @see edu.cmu.ml.proppr.util.ISymbolTable#getSymbol(int)
     */
    @Override
    public T getSymbol(int id) {
        return this.symbolList.get(id - 1);
    }

    /* (non-Javadoc)
     * @see edu.cmu.ml.proppr.util.ISymbolTable#size()
     */
    @Override
    public int size() {
        return this.symbolList.size();
    }

}
