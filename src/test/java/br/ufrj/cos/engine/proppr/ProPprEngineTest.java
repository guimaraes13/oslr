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

package br.ufrj.cos.engine.proppr;

import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.logic.Clause;
import br.ufrj.cos.logic.HornClause;
import br.ufrj.cos.logic.parser.knowledge.KnowledgeParser;
import edu.cmu.ml.proppr.prove.wam.WamProgram;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 02/05/17.
 *
 * @author Victor Guimarães
 */
public class ProPprEngineTest {

    public static final String LOGIC_FILE = "src/test/resources/smokers.ppr";
    public static final String WAN_FILE = "src/test/resources/smokers.wam";
    public static final String INPUT_ENCODE = "UTF8";

    @Test
    public void THEORY_COMPILE_TEST() {

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(LOGIC_FILE),
                                                                             INPUT_ENCODE));
            KnowledgeParser parser = new KnowledgeParser(reader);
            List<Clause> clauses = parser.parseKnowledge();
            Theory theory = new Theory(new ArrayList<>());
            theory.addAll(clauses, HornClause.class);
            WamProgram wamProgram = ProPprUtils.compileTheory(theory);
            WamProgram wamProgramExpected = WamProgram.load(new File(WAN_FILE));
            for (int i = 0; i < 29; i++) {
                Assert.assertEquals("Instructions does not match!",
                                    wamProgramExpected.getInstruction(i).toString(),
                                    wamProgram.getInstruction(i).toString());
            }
        } catch (Exception e) {
            Assert.fail();
        }

    }

}
