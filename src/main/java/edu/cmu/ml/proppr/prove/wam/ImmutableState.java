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

import java.util.Arrays;
import java.util.LinkedList;

/**
 * An immutable, hashable version of an interpreter state.
 *
 * @author "William Cohen <wcohen@cs.cmu.edu>"
 * @author "Kathryn Mazaitis <krivard@cs.cmu.edu>"
 */
public class ImmutableState extends State {

    private final int hash;

    public ImmutableState(MutableState state) {
        this.heap = Arrays.copyOf(state.heap, state.getHeapSize());//new int[state.getHeapSize()];
//		for (int i=0; i<state.getHeapSize(); i++) this.heap[i] = state.heap[i];
        this.registers = Arrays.copyOf(state.registers, state.getRegisterSize()); //new int[state.getRegisterSize()];
//		for (int i=0; i<state.getRegisterSize(); i++) this.registers[i] = state.registers[i];
        this.calls = new LinkedList<CallStackFrame>();
        this.calls.addAll(state.calls);
        // TODO: varNameList

        this.pc = state.getProgramCounter();
        this.jumpTo = state.getJumpTo();
        this.completed = state.isCompleted();
        this.failed = state.isFailed();

        this.hash = ((Arrays.hashCode(heap) ^ Arrays.hashCode(registers) ^ pc ^ (jumpTo != null ? jumpTo.hashCode() :
                0)) << 2) ^ (completed ? 1 : 0) ^ (failed ? 2 : 0);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public ImmutableState immutableVersion() {
        return this;
    }

    @Override
    public MutableState mutableVersion() {
        MutableState results = new MutableState(this);
        return results;
    }
}
