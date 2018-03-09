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

package edu.cmu.ml.proppr.util;

import java.util.concurrent.ConcurrentHashMap;

/**
 * A "symbol table" mapping arbitrary objects (called 'symbols' in a
 * nod to LISP) to and from 'ids', i.e., integers in the range 1..N
 * (inclusive.)  This is based on ConcurrentHashMap objects so it will
 * hopefully be easy to share among different threads.
 *
 * @author wcohen
 */
public class ConcurrentSymbolTable<T> implements SymbolTable<T> {

    protected ConcurrentHashMap<Object, Integer[]> symbol2Id = new ConcurrentHashMap<Object, Integer[]>();
    protected ConcurrentHashMap<Integer, T> id2symbol = new ConcurrentHashMap<Integer, T>();
    protected HashingStrategy<T> hashingStrategy;
    protected int nextId = 0;

    public ConcurrentSymbolTable(HashingStrategy<T> strategy) {
        this.init(strategy);
    }

    private void init(HashingStrategy<T> strategy) {
        this.hashingStrategy = strategy == null ? new DefaultHashingStrategy<T>() : strategy;
    }

    public ConcurrentSymbolTable() {
        this(HASHING_STRATEGIES.hashCode);
    }

    public ConcurrentSymbolTable(HASHING_STRATEGIES h) {
        switch (h) {
            case hashCode:
                init(new DefaultHashingStrategy<T>());
                break;
            case identity:
                init(new IdentityHashingStrategy<T>());
                break;
        }
    }

    // simple command-line test
    public static void main(String[] argv) {
        ConcurrentSymbolTable stab = new ConcurrentSymbolTable<String>();
        for (int i = 0; i < argv.length; i++) {
            if (stab.hasId(argv[i])) {
                System.out.println("duplicate: " + argv[i] + " has id " + stab.getId(argv[i]));
            }
            stab.insert(argv[i]);
        }
        for (int i = 1; i <= stab.size(); i++) {
            System.out.println(i + ":\t" + stab.getSymbol(i));
        }
    }

    public HashingStrategy<T> getHashingStrategy() {
        return hashingStrategy;
    }

    private void putSymbol(Object h, int id) {
//		symbol2Id.put(h,id);
        if (!symbol2Id.containsKey(h)) {
            symbol2Id.put(h, new Integer[]{0, id});
        } else {
            Integer[] cur = symbol2Id.get(h);
            if (cur[0] == 0) {
                // then there are no more free slots and we need to retabulate
                Integer[] now = new Integer[(cur.length - 1) * 2 + 1];
                for (int i = 0; i < cur.length; i++) {now[i] = cur[i];}
                now[0] = now.length - cur.length;
                symbol2Id.put(h, now);
                cur = now;
            }
            cur[cur.length - cur[0]] = id;
            cur[0]--;
        }
    }

    private int symbolGet(T symbol) {
        Object h = hashingStrategy.computeKey(symbol);
        Integer[] ids = symbol2Id.get(h);
        for (int i = ids.length - 1 - ids[0]; i > 0; i--) {
            if (hashingStrategy.equals(id2symbol.get(ids[i]), symbol)) { return ids[i]; }
        }
        throw new IllegalStateException("Symbol " + symbol + " not found in ConcurrentSymbolTable");
    }

    private boolean symbolContains(T symbol) {
        Object h = hashingStrategy.computeKey(symbol);
        if (!symbol2Id.containsKey(h)) { return false; }
        Integer[] ids = symbol2Id.get(h);
        for (int i = ids.length - 1 - ids[0]; i > 0; i--) {
            T candidate = id2symbol.get(ids[i]);
            // occasionally the value here comes up null, even though
            // the synchronized block on update means that should
            // never happen (?)
            // skipping the id may generate a false negative, but
            // it's been harmless in tests so far.
            // worth revisiting if bugs return.
            if (candidate == null) { continue; }
            if (hashingStrategy.equals(candidate, symbol)) { return true; }
        }
        return false;
    }

    /**
     * Ensure that a 'symbol' is in the table.
     *
     * @param symbol
     */
    @Override
    public void insert(T symbol) {
        //check collision
        if (symbolContains(symbol)) { return; }
        synchronized (this) {
            if (symbolContains(symbol)) { return; }
            Object h = hashingStrategy.computeKey(symbol);
            int newId = ++nextId;
//				symbol2Id.put(h,newId);
            putSymbol(h, newId);
            id2symbol.put(newId, symbol);
        }
    }

    /**
     * Return the numeric id, between 1 and N, of a symbol, inserting it if
     * needed.
     *
     * @param symbol
     */
    @Override
    public int getId(T symbol) {
        insert(symbol);
        return symbolGet(symbol);
    }

    /**
     * Test if the symbol has been previously inserted.
     */
    @Override
    public boolean hasId(T symbol) {
        return symbolContains(symbol);
    }

    /**
     * Get the symbol that corresponds to an id.  Returns null of the
     * symbol has not yet been inserted.
     */
    @Override
    public T getSymbol(int id) {
        return this.id2symbol.get(id);
    }

    /**
     * Return N, the largest id.
     */
    @Override
    public int size() {
        return this.id2symbol.size();
    }

    public enum HASHING_STRATEGIES {
        hashCode,
        identity
    }

    /**
     * Analogous to a gnu.trove hashing strategy.  Objects will be
     * assigned distinct id's iff they have different hash codes.
     **/
    public static interface HashingStrategy<T> {

        public Object computeKey(T symbol);

        public boolean equals(T o1, T o2);
    }

    public class DefaultHashingStrategy<T> implements HashingStrategy<T> {

        @Override
        public Integer computeKey(T symbol) {
            return symbol.hashCode();
        }

        @Override
        public boolean equals(T o1, T o2) {
            if (o1 == null) { return false; }
            return o1.equals(o2);
        }
    }

    public class IdentityHashingStrategy<T> implements HashingStrategy<T> {

        @Override
        public T computeKey(T symbol) {
            return symbol;
        }

        @Override
        public boolean equals(T o1, T o2) {
            if (o1 == null) { return false; }
            return o1.equals(o2);
        }
    }
}

