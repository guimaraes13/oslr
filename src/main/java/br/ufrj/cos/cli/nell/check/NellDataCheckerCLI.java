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

package br.ufrj.cos.cli.nell.check;

import br.ufrj.cos.cli.CommandLineInterface;
import br.ufrj.cos.logic.Clause;
import br.ufrj.cos.logic.parser.knowledge.KnowledgeParser;
import br.ufrj.cos.logic.parser.knowledge.ParseException;
import br.ufrj.cos.util.FileIOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static br.ufrj.cos.util.log.FileIOLog.ERROR_READING_FILE;

/**
 * Created on 31/07/17.
 *
 * @author Victor Guimarães
 */
public class NellDataCheckerCLI extends NellHashCheckerCLI {

    /**
     * The main method
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CommandLineInterface instance = new NellDataCheckerCLI();
        mainProgram(instance, logger, args);
    }

    @SuppressWarnings("HardCodedStringLiteral")
    @Override
    protected boolean isEquals(File iteration1, File relation1, File iteration2) throws NoSuchAlgorithmException {
        File relation2 = new File(iteration2, relation1.getName());
        if (!relation2.exists()) {
            logger.debug("File {} exists in {} but does not in {}",
                         relation1.getName(), iteration1, iteration2);
            return true;
        }
        Collection<? extends Clause> clauses1 = readLogicFile(relation1);
        Collection<? extends Clause> clauses2 = readLogicFile(relation2);
        //noinspection ConstantConditions
        if (clauses1.equals(clauses2)) {
            logger.trace("File {} is equal in the two directories.", relation1.getName());
            return true;
        } else {
            logger.debug("File {} is different in each directory.", relation1.getName());
            return false;
        }
    }

    /**
     * Reads the logic file.
     *
     * @param file the logic file
     * @return the clauses
     */
    protected static Collection<Clause> readLogicFile(File file) {
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), FileIOUtils
                .DEFAULT_INPUT_ENCODE)) {
            KnowledgeParser parser = new KnowledgeParser(reader);
            Set<Clause> clauses = new HashSet<>();
            parser.parseKnowledgeAppend(clauses);
            return clauses;
        } catch (ParseException | IOException e) {
            logger.error(ERROR_READING_FILE, e);
            return null;
        }
    }

}
