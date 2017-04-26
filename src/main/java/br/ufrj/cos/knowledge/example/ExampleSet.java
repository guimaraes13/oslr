/*
 * Probabilist Logic Learner is a system to learn probabilistic logic
 * programs from data and use its learned programs to make inference
 * and answer queries.
 *
 * Copyright (C) 2017 Victor Guimarães
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

package br.ufrj.cos.knowledge.example;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible to hold the read examples from the input.
 * <p>
 * Created on 25/04/17.
 *
 * @author Victor Guimarães
 */
public class ExampleSet {

    List<Example> probLogExamples;
    List<ProPprExampleSet> proPprExamples;

    public ExampleSet() {
        this.probLogExamples = new ArrayList<>();
        this.proPprExamples = new ArrayList<>();
    }

    public ExampleSet(List<Example> probLogExamples, List<ProPprExampleSet> proPprExamples) {
        this.probLogExamples = probLogExamples;
        this.proPprExamples = proPprExamples;
    }

}
