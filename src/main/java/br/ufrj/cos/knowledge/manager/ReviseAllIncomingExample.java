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

package br.ufrj.cos.knowledge.manager;

import br.ufrj.cos.core.LearningSystem;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.example.ProPprExample;
import br.ufrj.cos.knowledge.theory.manager.revision.TheoryRevisionException;
import br.ufrj.cos.util.Initializable;
import br.ufrj.cos.util.LanguageUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

/**
 * Class to getBestRevisionOperator all incoming examples as they arrive.
 * <p>
 * Created on 08/05/17.
 *
 * @author Victor Guimarães
 */
public class ReviseAllIncomingExample extends IncomingExampleManager {

    /**
     * Default constructor to be in compliance to {@link Initializable} interface.
     */
    public ReviseAllIncomingExample() {
        super();
    }

    /**
     * Constructs a {@link ReviseAllIncomingExample} with its fields.
     *
     * @param learningSystem the {@link LearningSystem}
     */
    public ReviseAllIncomingExample(LearningSystem learningSystem) {
        super(learningSystem);
    }

    @Override
    public void incomingExamples(Collection<? extends Example> examples) throws TheoryRevisionException {
        learningSystem.getExamples().addAll(convertToProPprExamples(examples));
        learningSystem.reviseTheory(Collections.singletonList(examples));
    }

    /**
     * Converts a {@link Collection} of {@link Example}s to a {@link Collection} of {@link ProPprExample}.
     *
     * @param examples the {@link Collection} of {@link Example}s
     * @return the {@link Collection} of {@link ProPprExample}
     */
    public static Collection<ProPprExample> convertToProPprExamples(Collection<? extends Example> examples) {
        Collection<ProPprExample> proPprExamples = new LinkedHashSet<>(examples.size());
        for (Example example : examples) {
            if (example instanceof ProPprExample) {
                proPprExamples.add((ProPprExample) example);
            } else {
                proPprExamples.add(
                        new ProPprExample(LanguageUtils.toVariableAtom(example.getGoalQuery()),
                                          new ArrayList<>(example.getGroundedQuery())));
            }
        }
        return proPprExamples;
    }

}
