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

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Test;

public class StateCallStackTest {

	@Test
	public void test() {
		MutableState state = new MutableState();
		MutableState[] stack = new MutableState[5];
		for (int i=0; i<5; i++) {
			stack[i] = new MutableState();
			stack[i].setJumpTo(String.valueOf(i));
			stack[i].setProgramCounter(i);
		}
		for (int i=5; i>0; i--) {
			state.getCalls().push(new CallStackFrame(stack[i-1]));
		}
		assertTrue("sanity check", state.calls.peekFirst().getProgramCounter() != state.calls.peekLast().getProgramCounter());
		
		ImmutableState test = new ImmutableState(state);
		
		assertEquals("Peek", state.calls.peek(), test.calls.peek());
		
		Iterator<CallStackFrame> si = state.getCalls().iterator();
		Iterator<CallStackFrame> ti = test.calls.iterator();
		
		while(si.hasNext()) {
			assertTrue("Immutable version shorter than source", ti.hasNext());
			CallStackFrame sc = si.next();
			CallStackFrame tc = ti.next();
			assertEquals("Program counter", sc.getProgramCounter(), tc.getProgramCounter());
		}
		
	}

}
