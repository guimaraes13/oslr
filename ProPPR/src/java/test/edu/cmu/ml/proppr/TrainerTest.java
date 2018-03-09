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

package edu.cmu.ml.proppr;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.io.File;

import org.junit.Before;
import org.junit.Test;

import edu.cmu.ml.proppr.examples.PosNegRWExample;
import edu.cmu.ml.proppr.graph.ArrayLearningGraphBuilder;
import edu.cmu.ml.proppr.learn.RegularizationSchedule;
import edu.cmu.ml.proppr.learn.RegularizeL2;
import edu.cmu.ml.proppr.learn.SRW;
import edu.cmu.ml.proppr.learn.tools.ReLU;
import edu.cmu.ml.proppr.util.Dictionary;
import edu.cmu.ml.proppr.util.SimpleSymbolTable;
import edu.cmu.ml.proppr.util.math.ParamVector;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.hash.TIntDoubleHashMap;

public class TrainerTest extends RedBlueGraph {
	public Trainer trainer;
	public SRW srw;
	public TIntDoubleMap query;
	public ArrayList<String> examples;
	
	public TrainerTest() {
		super(10);
	}
	
	public void initTrainer() {
		this.trainer = new Trainer(this.srw);
	}
	
	@Before
	public void setup() {
		super.setup();
		this.srw = new SRW();
		this.srw.setRegularizer(new RegularizationSchedule(this.srw, new RegularizeL2()));
		this.srw.setSquashingFunction(new ReLU<String>());
		this.initTrainer();
		
		query = new TIntDoubleHashMap();
		query.put(nodes.getId("r0"),1.0);
		examples = new ArrayList<String>();
		for (int k=0;k<this.magicNumber;k++) {
			for (int p=0;p<this.magicNumber;p++) {
				examples.add(new PosNegRWExample(brGraph, query, 
						new int[]{nodes.getId("b"+k)},
						new int[]{nodes.getId("r"+p)}).serialize());
			}
		}
	}
	
	public ParamVector<String,?> train() {
		File nullFile = null;
		return this.trainer.train(new SimpleSymbolTable<String>(), examples, new ArrayLearningGraphBuilder(), nullFile, 5);
	}

	@Test
	public void test() {
		ParamVector<String,?> params = train();
		System.err.println(Dictionary.buildString(params,new StringBuilder(),"\n"));
		for (Object o : params.keySet()) {
			String f = (String) o;
			if (f.equals("tob")) assertTrue("tob "+f,params.get(o) >= this.srw.getSquashingFunction().defaultValue());
			if (f.equals("tor")) assertTrue("tor "+f,params.get(o) <= this.srw.getSquashingFunction().defaultValue());
		}
	}

}
