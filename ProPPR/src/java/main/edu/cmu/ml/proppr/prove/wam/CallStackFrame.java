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

/**
 * Enough information to 'return' to a prior state.
 * @author "William Cohen <wcohen@cs.cmu.edu>"
 * @author "Kathryn Mazaitis <krivard@cs.cmu.edu>"
 *
 */
public class CallStackFrame {
	private int heapPointer;
	private int registerPointer;
	private int programCounter;
	private String jumpTo;
	public CallStackFrame(State state) {
		this.heapPointer = state.getHeapSize();
		this.registerPointer = state.getRegisterSize();
		this.programCounter = state.getProgramCounter();
		this.jumpTo = state.getJumpTo();
	}

	public int getHeapPointer() {
		return heapPointer;
	}

	public int getRegisterPointer() {
		return registerPointer;
	}

	public int getProgramCounter() {
		return programCounter;
	}

	public String getJumpTo() {
		return jumpTo;
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("sf:[");
		sb.append(this.heapPointer).append(", ");
		sb.append(this.registerPointer).append(", ");
		sb.append(this.programCounter).append(", ");
		sb.append(this.jumpTo);
		sb.append("]");
		return sb.toString();
	}
	@Override
	public boolean equals(Object o) {
		if (! (o instanceof CallStackFrame)) return false;
		CallStackFrame c = (CallStackFrame) o;
		if (heapPointer != c.heapPointer ||
				registerPointer != c.registerPointer ||
				programCounter != c.programCounter ||
				!(jumpTo == null ? c.jumpTo == null : jumpTo.equals(c.jumpTo)))
			return false;
		return true;
	}
}
