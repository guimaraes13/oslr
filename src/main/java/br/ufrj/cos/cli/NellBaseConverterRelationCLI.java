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

package br.ufrj.cos.cli;

import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.Clause;
import br.ufrj.cos.logic.Predicate;
import br.ufrj.cos.logic.parser.knowledge.KnowledgeParser;
import br.ufrj.cos.logic.parser.knowledge.ParseException;
import br.ufrj.cos.util.LogMessages;
import br.ufrj.cos.util.nell.converter.AtomProcessor;
import br.ufrj.cos.util.nell.converter.FilterAtomProcessor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static br.ufrj.cos.util.LogMessages.*;

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
        Locale.setDefault(new Locale(DEFAULT_LANGUAGE, DEFAULT_COUNTRY));
        try {
            CommandLineInterface main = new NellBaseConverterRelationCLI();
            main = main.parseOptions(args);
            run(main, args);
        } catch (Exception e) {
            logger.error(ERROR_MAIN_PROGRAM, e);
        } finally {
            logger.fatal(PROGRAM_END);
        }
    }

    @Override
    protected void processFiles() throws IOException, NoSuchAlgorithmException {
        initializeTargetPredicates();
        while (!targetPredicates.isEmpty()) {
            currentPredicate = targetPredicates.remove();
            logger.info(LogMessages.PROCESSING_RELATION.toString(), currentPredicate.getName());
            super.processFiles();
            logger.info(LogMessages.DONE_RELATION.toString(), currentPredicate.getName());
        }
    }

    /**
     * Filters the atoms from the previous iteration removing the atom that already appears on older iterations.
     *
     * @param index the index of the previous iteration
     * @throws IOException if an I/O error has occurred
     */
    @Override
    protected void filterPreviousAtoms(int index) throws IOException {
        FilterAtomProcessor atomProcessor = new FilterAtomProcessor(previousAtoms);
        for (int i = 0; i < index - 1; i++) {
            logger.debug(LogMessages.FILTERING_ITERATION.toString(), index, i);
            for (Predicate predicate : getPositives(previousAtoms).keySet()) {
                filterPreviousAtoms(i, predicate, atomProcessor, true);
            }
            for (Predicate predicate : getNegatives(previousAtoms).keySet()) {
                filterPreviousAtoms(i, predicate, atomProcessor, false);
            }
        }
        previousSkippedAtoms[index] += atomProcessor.getNumberOfFilteredAtoms();
    }

    /**
     * Creates the stream of the input file with the zip stream if needed.
     *
     * @param index         the index of the {@link #nellInputFilePaths}
     * @param predicate     the predicate
     * @param atomProcessor the {@link AtomProcessor}
     * @param positive      if is positive or negative
     * @throws IOException if an I/O error has occurred
     */
    protected void filterPreviousAtoms(int index, Predicate predicate, AtomProcessor atomProcessor,
                                       boolean positive) throws IOException {
        File iterationDirectory = getIterationDirectory(index);
        String extension = positive ? positiveOutputExtension : negativeOutputExtension;
        final File relationFile = new File(iterationDirectory, predicate.getName() + extension);
        if (!relationFile.exists()) { return; }
        InputStream stream = new FileInputStream(relationFile);
        processLogicFile(stream, atomProcessor, positive);
    }

    /**
     * Process the input file reading the line and applying and {@link AtomProcessor} for each read atom.
     *
     * @param stream        the stream
     * @param atomProcessor the {@link AtomProcessor}
     * @param positive      if the example is positive
     * @throws UnsupportedEncodingException if the encode is not supported
     */
    protected void processLogicFile(InputStream stream, AtomProcessor atomProcessor, boolean positive)
            throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(stream, fileEncode);
        Pair<Atom, Boolean> pair;
        try {
            KnowledgeParser parser = new KnowledgeParser(inputStreamReader);
            List<Clause> clauses = parser.parseKnowledge();
            for (Clause clause : clauses) {
                pair = new ImmutablePair<>((Atom) clause, positive);
                if (isToProcessAtom(pair)) { atomProcessor.isAtomProcessed(pair); }
            }
        } catch (ParseException | ClassCastException e) {
            logger.error(ERROR_READING_FILE.toString(), e);
        } finally {
            stream.close();
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
