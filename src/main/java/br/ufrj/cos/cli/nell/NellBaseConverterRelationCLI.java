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

package br.ufrj.cos.cli.nell;

import br.ufrj.cos.cli.CommandLineInterface;
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.Predicate;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import static br.ufrj.cos.util.log.FileIOLog.ERROR_READING_FILE;
import static br.ufrj.cos.util.log.NellConverterLog.DONE_RELATION;
import static br.ufrj.cos.util.log.NellConverterLog.PROCESSING_RELATION;

/**
 * Class to convert a Knowledge base from Nell's csv files to a set of logic files.
 * <p>
 * This class does the process relation by relation, using less main memory but doing more access to the
 * secondary memory.
 * <p>
 * Created on 27/07/17.
 *
 * @author Victor Guimarães
 */
public class NellBaseConverterRelationCLI extends NellBaseConverterCLI {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    protected final Set<Predicate> evaluatedPredicates = new HashSet<>();
    protected Predicate currentPredicate;
    protected Queue<Predicate> targetPredicates;

    /**
     * The main method
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CommandLineInterface instance = new NellBaseConverterRelationCLI();
        mainProgram(instance, logger, args);
    }

    @Override
    protected void processFiles() throws IOException, NoSuchAlgorithmException {
        initializeTargetPredicates();
        while (!targetPredicates.isEmpty()) {
            currentPredicate = targetPredicates.remove();
            logger.info(PROCESSING_RELATION.toString(), currentPredicate.getName());
            super.processFiles();
            logger.info(DONE_RELATION.toString(), currentPredicate.getName());
        }
    }

    /**
     * Initializes the target predicates queue by adding the first predicates found in the first file, that is not
     * skipped.
     *
     * @throws IOException if an I/O error has occurred
     */
    protected void initializeTargetPredicates() throws IOException {
        targetPredicates = new ArrayDeque<>();
        InputStreamReader inputStreamReader = new InputStreamReader(createStream(startIndex), fileEncode);
        Pair<Atom, Boolean> pair;
        try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            String line;
            line = bufferedReader.readLine();
            readHeader(startIndex, line);
            line = bufferedReader.readLine();
            while (line != null) {
                if (line.isEmpty() || line.startsWith(commentCharacter)) { continue; }
                pair = readLine(line, startIndex);
                if (super.isToProcessAtom(pair)) {
                    targetPredicates.add(pair.getLeft().getPredicate());
                    break;
                }
                line = bufferedReader.readLine();
            }
        } catch (IOException e) {
            logger.error(ERROR_READING_FILE.toString(), e);
        }
    }

    @Override
    protected boolean isToProcessAtom(Pair<Atom, Boolean> pair) {
        if (!super.isToProcessAtom(pair)) {
            return false;
        }
        final Predicate predicate = pair.getLeft().getPredicate();
        if (predicate.equals(currentPredicate)) {
            return true;
        } else if (!evaluatedPredicates.contains(predicate)) {
            evaluatedPredicates.add(predicate);
            targetPredicates.add(predicate);
        }
        return false;
    }

}
