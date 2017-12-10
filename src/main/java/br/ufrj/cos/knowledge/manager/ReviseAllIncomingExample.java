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
import br.ufrj.cos.knowledge.theory.manager.revision.point.RelevantSampleSelector;
import br.ufrj.cos.knowledge.theory.manager.revision.point.RevisionExamples;
import br.ufrj.cos.util.LanguageUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static br.ufrj.cos.util.log.IncomingExampleLog.CALLING_REVISION_OF_EXAMPLE;

/**
 * Class to getBestRevisionOperator all incoming examples as they arrive.
 * <p>
 * Created on 08/05/17.
 *
 * @author Victor Guimarães
 */
public class ReviseAllIncomingExample extends IncomingExampleManager {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    protected RevisionExamples revisionExamples;
    protected List<RevisionExamples> singleton;

    /**
     * Default constructor.
     */
    public ReviseAllIncomingExample() {
        super();
    }

    /**
     * Constructs a {@link ReviseAllIncomingExample} with its fields.
     *
     * @param learningSystem the {@link LearningSystem}
     * @param sampleSelector the {@link RelevantSampleSelector}
     */
    public ReviseAllIncomingExample(LearningSystem learningSystem, RelevantSampleSelector sampleSelector) {
        super(learningSystem, sampleSelector);
    }

    @Override
    public void incomingExamples(Iterable<? extends Example> examples) {
        this.revisionExamples = new RevisionExamples(learningSystem, sampleSelector);
        this.singleton = Collections.singletonList(revisionExamples);
        int size = 0;
        for (Example example : convertToProPprExamples(examples)) {
            revisionExamples.addExample(example);
            size++;
        }
        logger.info(CALLING_REVISION_OF_EXAMPLE.toString(), size);
        learningSystem.reviseTheory(singleton);
    }

    /**
     * Converts a {@link Collection} of {@link Example}s to a {@link Collection} of {@link ProPprExample}.
     *
     * @param examples the {@link Collection} of {@link Example}s
     * @return the {@link Collection} of {@link ProPprExample}
     */
    public static Collection<ProPprExample> convertToProPprExamples(Iterable<? extends Example> examples) {
        Collection<ProPprExample> proPprExamples = new LinkedHashSet<>();
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

    @Override
    public Collection<? extends Example> getRemainingExamples() {
        return Collections.emptySet();
    }

}
