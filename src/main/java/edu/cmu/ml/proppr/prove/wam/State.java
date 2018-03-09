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

package edu.cmu.ml.proppr.prove.wam;

import edu.cmu.ml.proppr.util.Dictionary;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * State of the interpreter.  States are stored and retrieved to allow
 * for backtracking, and to build a proof graph.  You can call
 * <p>
 * savedState = wamInterp.state.save()
 * <p>
 * to save an interpreter state, and
 * <p>
 * wamInterp.state = State.restore(savedState)
 * <p>
 * to restore one. Saved states are immutable and hashable.
 *
 * @author "William Cohen <wcohen@cs.cmu.edu>"
 * @author "Kathryn Mazaitis <krivard@cs.cmu.edu>"
 */
public abstract class State {

    protected int[] heap;
    protected int[] registers;
    protected int pc;
    protected String jumpTo;
    protected boolean completed;
    protected boolean failed;
    protected LinkedList<CallStackFrame> calls;
    protected int canon;      // canonical hash code - for duplicate checking
    protected String canonF;  // canonical format - canon is hash of this

    /**
     * True iff there is a variable at heap position i.
     */
    public boolean hasVariableAt(int i) {
        return heap[i] >= 0;
    }

    /**
     * Get the id, in the Interpreter's constantTable, of the constant
     * stored in this heap position.
     *
     * @param i
     * @return
     */
    public int getIdOfConstantAt(int i) {
        if (heap[i] >= 0) { throw new InvalidHeapException(); }
        return -heap[i];
    }

    /**
     * Create a heap cell that stores a constant with the given id
     */
    public int createConstantCell(int id) {
        if (id < 1) { throw new IllegalArgumentException(); }
        return -id;
    }

    /**
     * Create a heap cell that stores a variable bound to heap position a
     */
    public int createVariableCell(int a) {
        return a;
    }

    /**
     * Create a copy of the constant cell at position i.
     */
    public int copyConstantCell(int i) {
        return heap[i];
    }

    /**
     * Dereference a variable, ie, follow pointers till you reach an
     * unbound variable or a constant.
     *
     * @param i
     * @return
     */
    public int dereference(int heapIndex) {
        while (!hasConstantAt(heapIndex) && !hasFreeAt(heapIndex)) {
            heapIndex = getVariableAt(heapIndex);
        }
        return heapIndex;
    }

    /**
     * True iff there is a constant at heap position i.
     */
    public boolean hasConstantAt(int i) {
        return heap[i] < 0;
    }

    /**
     * True iff there is an unbound variable at heap position i.
     */
    public boolean hasFreeAt(int i) {
        return heap[i] == i;
    }

    /**
     * Get the value of the variable stored at this heap position.
     */
    public int getVariableAt(int i) {
        return heap[i];
    }

    /**
     * Immutable, hashable version of this state.
     */
    public abstract ImmutableState immutableVersion();

    /**
     * Restore from a copy produced by save().
     */
    public abstract MutableState mutableVersion();

    /** */
    public boolean isCompleted() {
        return this.completed;
    }

    public boolean isFailed() {
        return this.failed;
    }

    public int getHeapSize() {
        return heap.length;
    }

    public int getRegisterSize() {
        return registers.length;
    }

    public int getProgramCounter() {
        return pc;
    }

    public String getJumpTo() {
        return jumpTo;
    }

    public List<CallStackFrame> getCalls() {
        return calls;
    }

    public int[] getRegisters() {
        return this.registers;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof State)) { return false; }
        State s = (State) o;
        if (this.calls.size() != s.calls.size() ||
                this.pc != s.pc ||
                this.completed != s.completed ||
                this.failed != s.failed) { return false; }
        if (!Arrays.equals(heap, s.heap)) { return false; }
        if (!Arrays.equals(registers, s.registers)) { return false; }
        Iterator<CallStackFrame> it = this.calls.iterator(),
                sit = s.calls.iterator();
        while (it.hasNext()) {
            CallStackFrame mine = it.next();
            CallStackFrame theirs = sit.next();
            if (!mine.equals(theirs)) { return false; }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("state<");
        buildHeapString(sb);
        sb.append(" ");
        buildRegisterString(sb);
        sb.append(" ");
        buildCallStackString(sb);
        sb.append(" ");
        sb.append(pc).append(" ").append(jumpTo);
        sb.append(">");
        if (completed) { sb.append("*"); }
        if (failed) { sb.append("!"); }
        return sb.toString();
    }

    protected void buildHeapString(StringBuilder sb) {
        sb.append("h[");
        Dictionary.buildString(heap, sb, " ");
        sb.append("]");
    }

    protected void buildRegisterString(StringBuilder sb) {
        sb.append("r[");
        Dictionary.buildString(registers, sb, " ");
        sb.append("]");
    }

    protected void buildCallStackString(StringBuilder sb) {
        sb.append("c[");
        for (CallStackFrame f : this.calls) {
            sb.append(f);
        }
        sb.append("]");
    }

    public int canonicalHash() {
        return canon;
    }

    public String canonicalForm() {
        return canonF;
    }

    public void setCanonicalHash(WamInterpreter interpreter, State startState) {
        try {
//			this.canon = interpreter.canonicalForm(startState, this).hashCode();
            setCanonicalHash(interpreter.canonicalHash(startState, this));
        } catch (LogicProgramException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setCanonicalHash(int i) {
        this.canon = i;
    }

    public void setCanonicalForm(WamInterpreter interpreter, State startState) {
        if (this.canonF != null) { return; }
        try {
            setCanonicalForm(interpreter.canonicalForm(startState, this));
        } catch (LogicProgramException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setCanonicalForm(String s) {
        this.canonF = s;
    }
}
