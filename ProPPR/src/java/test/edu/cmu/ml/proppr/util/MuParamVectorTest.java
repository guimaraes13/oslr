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

package edu.cmu.ml.proppr.util;

import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Test;

import edu.cmu.ml.proppr.util.math.MuParamVector;

public class MuParamVectorTest {
	private static final double EPS=1e-6;

	@Test
	public void testMap() {
		MuParamVector foo = new MuParamVector();
		foo.put("abc",1.0);
		foo.put("def",10.0);
		
		assertEquals(2,foo.size());
		assertEquals(1.0,foo.get("abc"),EPS);
		assertEquals(10.0,foo.get("def"),EPS);
	}
	
	@Test
	public void testTimestamp() {
		MuParamVector foo = new MuParamVector();
		foo.put("abc",1.0);
		foo.put("def",10.0);
		
		assertEquals(0,foo.getLast("abc"));
		foo.count();
		assertEquals(1,foo.getLast("abc"));
		assertEquals(1,foo.getLast("def"));
		foo.setLast(Collections.singleton("abc"));
		assertEquals(0,foo.getLast("abc"));
		assertEquals(1,foo.getLast("def"));
		
	}

}
