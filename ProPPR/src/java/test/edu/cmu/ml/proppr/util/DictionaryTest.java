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

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;

import edu.cmu.ml.proppr.util.Dictionary;

public class DictionaryTest {
	Map<String,Double> map;
	Map<String,Map<String,Double>> mapmap;
	static final double EPSILON = 1e-10;
	@Before
	public void setup() {
		map = new TreeMap<String,Double>();
		map.put("one", 1.0);
		map.put("zero",0.0);
		mapmap = new TreeMap<String,Map<String,Double>>();
		mapmap.put("a",map);
	}

	@Test
	public void testIncrement() {
		Dictionary.increment(map, "one", 1.0);
		assertEquals("one",2,map.get("one"),EPSILON);
		
		Dictionary.increment(map, "zero", 1.0);
		assertEquals("zero",1,map.get("zero"),EPSILON);
		
		Dictionary.increment(map, "foo",1.0);
		assertEquals("foo",1,map.get("foo"),EPSILON);
	}
	
	@Test
	public void testNestedIncrement() {
		Dictionary.increment(mapmap, "a", "one", 1.0);
		assertEquals(2.0,Dictionary.safeGetGet(mapmap,"a","one"),EPSILON);
		
		Dictionary.increment(mapmap, "b", "one", 1.0);
		assertEquals(1.0,Dictionary.safeGetGet(mapmap,"b","one"),EPSILON);
	}

	@Test
	public void testSort() {
		List<Map.Entry<String,Double>> items = Dictionary.sort(map);
		assertEquals("one",items.get(0).getKey());
		assertEquals("zero",items.get(1).getKey());
	}
	
	@Test
	public void testSave() throws IOException {
		StringWriter writer = new StringWriter();
		TreeMap<String,Double> map = new TreeMap<String,Double>();
		map.put("big", Double.MAX_VALUE);
		map.put("small", Double.MIN_NORMAL);
		map.put("typical",1.0);
		map.put("typicalbig", 3141.59265358979);
		map.put("typicalsmall", 0.0000000031415);
		Dictionary.save(map,writer);
		System.err.println(writer.toString());
	}
}
