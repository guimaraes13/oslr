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

package edu.cmu.ml.proppr.prove;

import java.util.ArrayDeque;

import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;;

import edu.cmu.ml.proppr.prove.wam.LogicProgramException;
import edu.cmu.ml.proppr.util.Dictionary;

public class Backtrace<T> {
	private Logger log;
	public Backtrace(Logger parent) { 
		this.log = parent; 
	}
	protected ArrayDeque<T> backtrace;
	public void start() {
		this.backtrace = new ArrayDeque<T>();
	}
	public void push(T state) {
//		if(log.isDebugEnabled()) log.debug("push "+state);
		this.backtrace.push(state);
	}
	public void pop(T state) {
		T p = this.backtrace.pop();
//		if(log.isDebugEnabled()) log.debug("pop "+state);
		if (!p.equals(state)) log.error("popped unexpected state\nexpected "+state+"\ngot"+p);
	}
	public void rethrow(LogicProgramException e) {
		StringBuilder sb = new StringBuilder(e.getMessage()+"\nLogic program backtrace:\n");
		Dictionary.buildString(this.backtrace, sb, "\n");
		throw new IllegalStateException(sb.toString(),e);
	}
}
