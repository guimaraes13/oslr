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

import org.junit.Test;

import edu.cmu.ml.proppr.prove.wam.ConstantArgument;
import edu.cmu.ml.proppr.prove.wam.Query;
import edu.cmu.ml.proppr.prove.wam.VariableArgument;

public class QueryParserTest {

	public void checkTo0(Query q) {
		assertEquals("query length",1,q.getRhs().length);
		assertEquals("head functor","bob",q.getRhs()[0].getFunctor());
	}
	
	@Test
	public void testArity0() {
		Query q = Query.parse("bob");
		checkTo0(q);
		assertEquals("head arity",0,q.getRhs()[0].getArity());
	}
	
	@Test
	public void testArity1() {
		Query q = Query.parse("bob(joe)");
		System.out.println(q);
		checkTo0(q);
		assertEquals("head arity",1,q.getRhs()[0].getArity());
		assertEquals("head arg0",new ConstantArgument("joe"),q.getRhs()[0].getArg(0));
	}
	
	@Test
	public void testArity2() {
		Query q = Query.parse("bob(joe,X)");
		q.variabilize();
		System.out.println(q);
		checkTo0(q);
		assertEquals("head arity",2,q.getRhs()[0].getArity());
		assertEquals("head arg0",new ConstantArgument("joe"),q.getRhs()[0].getArg(0));
		assertEquals("head arg1",new VariableArgument(-1),q.getRhs()[0].getArg(1));
	}
}
