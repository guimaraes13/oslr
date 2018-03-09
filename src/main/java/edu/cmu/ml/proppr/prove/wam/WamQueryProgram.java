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

package edu.cmu.ml.proppr.prove.wam;

import java.util.ArrayList;
import java.util.List;

public class WamQueryProgram extends WamProgram {

    private final WamProgram masterProgram;
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
        if (addr < masterProgram.size()) { return masterProgram.getInstruction(addr); }
        return queryProgram.getInstruction(addr - masterProgram.size());
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
        List<Integer> query = queryProgram.getAddresses(jumpTo);
        if (query == null || query.isEmpty()) { return master; }
        ArrayList<Integer> ret = new ArrayList<Integer>(master.size() + query.size());
        ret.addAll(master);
        int offset = masterProgram.size();
        for (Integer k : query) { ret.add(offset + k); }
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
