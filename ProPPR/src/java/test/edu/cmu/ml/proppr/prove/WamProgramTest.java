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

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import edu.cmu.ml.proppr.prove.wam.WamProgram;
import edu.cmu.ml.proppr.prove.wam.WamBaseProgram;
import edu.cmu.ml.proppr.prove.wam.Instruction.OP;

public class WamProgramTest {
	@Test
	public void testLoad() throws IOException {
		WamProgram program = WamBaseProgram.load(new File(SimpleProgramProverTest.PROGRAM));
		OP[] simpleProgram = 
			{
				OP.comment,
				OP.allocate,
				OP.initfreevar,
				OP.initfreevar,
				OP.fclear,
				OP.fpushstart,
				OP.fpushconst,
				OP.fpushconst,
				OP.fpushconst,
				OP.freport,
				OP.pushboundvar,
				OP.pushfreevar,
				OP.callp,
				OP.pushboundvar,
				OP.pushboundvar,
				OP.callp,
				OP.returnp,
				OP.comment,
				OP.allocate,
				OP.initfreevar,
				OP.initfreevar,
				OP.fclear,
				OP.fpushstart,
				OP.fpushconst,
				OP.fpushconst,
				OP.fpushconst,
				OP.freport,
				OP.pushboundvar,
				OP.callp,
				OP.pushboundvar,
				OP.pushboundvar,
				OP.callp,
				OP.returnp,
				OP.comment,
				OP.unifyconst,
				OP.unifyconst,
				OP.fclear,
				OP.fpushstart,
				OP.fpushconst,
				OP.fpushconst,
				OP.fpushconst,
				OP.freport,
				OP.returnp,
				OP.comment,
				OP.unifyconst,
				OP.unifyconst,
				OP.fclear,
				OP.fpushstart,
				OP.fpushconst,
				OP.fpushconst,
				OP.fpushconst,
				OP.freport,
				OP.returnp,
				OP.comment,
				OP.unifyconst,
				OP.fclear,
				OP.fpushstart,
				OP.fpushconst,
				OP.fpushconst,
				OP.fpushconst,
				OP.freport,
				OP.returnp
			};
		assertEquals(simpleProgram.length,program.size());
		for (int i=0; i<simpleProgram.length; i++) {
			assertEquals("Instruction "+i,simpleProgram[i],program.getInstruction(i).opcode);
		}
		assertTrue(program.hasLabel("coworker/2"));
		List<Integer> addr = program.getAddresses("coworker/2");
		assertEquals("coworker/2",1,addr.get(0).intValue());
		assertTrue(program.hasLabel("employee/2"));
		addr = program.getAddresses(("employee/2"));
		assertEquals("employee/2",18,addr.get(0).intValue());
	}

}
