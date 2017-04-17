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

package br.ufrj.cos.language.parser;

import br.ufrj.cos.logic.Clause;
import br.ufrj.cos.logic.parser.example.ExampleParser;
import br.ufrj.cos.logic.parser.knowledge.KnowledgeParser;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
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

    @Test
    public void TEST_KNOWLEDGE_PARSER() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(KNOWLEDGE_TEST_FILE), INPUT_ENCODE));
            KnowledgeParser parser = new KnowledgeParser(reader);
            List<Clause> clauses = parser.parseKnowledge();
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void TEST_EXAMPLE_PARSER() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(EXAMPLE_TEST_FILE), INPUT_ENCODE));
            ExampleParser parser = new ExampleParser(reader);
            parser.parseExamples();
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail(ex.getMessage());
        }
    }

}
