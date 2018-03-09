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

package br.ufrj.cos.language.parser;

import br.ufrj.cos.logic.Clause;
import br.ufrj.cos.logic.parser.example.ExampleParser;
import br.ufrj.cos.logic.parser.knowledge.KnowledgeParser;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 15/04/17.
 *
 * @author Victor Guimarães
 */
public class ParserTest {

    public static final String KNOWLEDGE_TEST_FILE = "src/test/resources/KnowledgeParserTest.pl";
    public static final String EXAMPLE_TEST_FILE = "src/test/resources/ExampleParserTest.pl";
    public static final String INPUT_ENCODE = "UTF8";

    public static final String DIFFERENT_PARSED_LIST_ERROR = "Different parsed lists.";

    @Test
    public void TEST_KNOWLEDGE_PARSER() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(KNOWLEDGE_TEST_FILE)
                    , INPUT_ENCODE));
            KnowledgeParser parser = new KnowledgeParser(reader);
            List<Clause> clauses = parser.parseKnowledge();
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void TEST_KNOWLEDGE_PARSER_APPEND() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(KNOWLEDGE_TEST_FILE)
                    , INPUT_ENCODE));
            KnowledgeParser parser = new KnowledgeParser(reader);
            List<Clause> clauses = parser.parseKnowledge();

            reader = new BufferedReader(new InputStreamReader(new FileInputStream(KNOWLEDGE_TEST_FILE), INPUT_ENCODE));
            parser = new KnowledgeParser(reader);
            List<Clause> clauses1 = new ArrayList<>();
            parser.parseKnowledgeAppend(clauses1);
            Assert.assertEquals(DIFFERENT_PARSED_LIST_ERROR, clauses.size(), clauses1.size());
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void TEST_EXAMPLE_PARSER() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(EXAMPLE_TEST_FILE),
                                                                             INPUT_ENCODE));
            ExampleParser parser = new ExampleParser(reader);
            parser.parseExamples();
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void TEST_EXAMPLE_PARSER_APPEND() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(EXAMPLE_TEST_FILE),
                                                                             INPUT_ENCODE));
            ExampleParser parser = new ExampleParser(reader);
            parser.parseExamples();

            List probExamples = parser.getProbLogFormatExamples();
            List propExamples = parser.getProPprFormatExamples();

            List probExamples1 = new ArrayList();
            List propExamples1 = new ArrayList();

            reader = new BufferedReader(new InputStreamReader(new FileInputStream(EXAMPLE_TEST_FILE), INPUT_ENCODE));
            parser = new ExampleParser(reader);
            parser.parseExamplesAppend(probExamples1, propExamples1);
            Assert.assertEquals(DIFFERENT_PARSED_LIST_ERROR, probExamples.size(), probExamples1.size());
            Assert.assertEquals(DIFFERENT_PARSED_LIST_ERROR, propExamples.size(), propExamples1.size());
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail(ex.getMessage());
        }
    }

}
