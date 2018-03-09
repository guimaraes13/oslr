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

import java.util.ArrayList;
import java.util.List;

public class WamQueryProgram extends WamProgram {
	private WamProgram masterProgram;
	private WamProgram queryProgram;
	public WamQueryProgram(WamProgram master) {
		this.masterProgram = master;
	}
	@Override
	public void append(Instruction inst) {
		this.queryProgram.append(inst);
	}
	@Override
	public void setInstruction(int placeToPatch, Instruction instruction) {
		this.queryProgram.setInstruction(placeToPatch, instruction);
	}
	@Override
	public int size() {
		return masterProgram.size() + queryProgram.size();
	}
	@Override
	public Instruction getInstruction(int addr) {
		if (addr < masterProgram.size()) return masterProgram.getInstruction(addr);
		return queryProgram.getInstruction(addr-masterProgram.size());
	}
	@Override
	public void insertLabel(String label) {
		queryProgram.insertLabel(label);
	}
	@Override
	public boolean hasLabel(String jumpTo) {
		return masterProgram.hasLabel(jumpTo) || queryProgram.hasLabel(jumpTo);
	}
	@Override
	public List<Integer> getAddresses(String jumpTo) {
		List<Integer> master = masterProgram.getAddresses(jumpTo);
		List<Integer> query  = queryProgram.getAddresses(jumpTo);
		if (query==null || query.isEmpty()) return master;
		ArrayList<Integer> ret = new ArrayList<Integer>(master.size() + query.size());
		ret.addAll(master);
		int offset = masterProgram.size();
		for (Integer k : query) ret.add(offset + k);
		return ret;
	}
	@Override
	public void save() {
		// do nothing
	}
	@Override
	public void revert() {
		this.queryProgram = new WamBaseProgram();
	}
}
