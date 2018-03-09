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

import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.List;

public abstract class WamProgram {
	protected Compiler compiler;
	public WamProgram() {
		compiler = new Compiler();
	}
	public void append(Rule r) {
		compiler.compileRule(r, this);
	}
	
	public abstract void append(Instruction inst);

	public abstract void setInstruction(int placeToPatch,
			Instruction instruction);

	public abstract int size();

	public abstract Instruction getInstruction(int addr);

	public abstract void insertLabel(String label);

	public abstract boolean hasLabel(String jumpTo);

	public abstract List<Integer> getAddresses(String jumpTo);

	public abstract void save();

	public abstract void revert();

	public static WamProgram load(File file) throws IOException {
		return WamBaseProgram.load(file);
	}
	public static WamProgram load(LineNumberReader reader) throws IOException {
		return WamBaseProgram.load(reader);
	}
}