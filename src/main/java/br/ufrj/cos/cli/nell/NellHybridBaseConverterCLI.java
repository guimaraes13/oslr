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

package br.ufrj.cos.cli.nell;

import br.ufrj.cos.cli.CommandLineInterface;
import br.ufrj.cos.logic.Clause;
import br.ufrj.cos.logic.Predicate;
import br.ufrj.cos.logic.parser.knowledge.KnowledgeParser;
import br.ufrj.cos.logic.parser.knowledge.ParseException;
import br.ufrj.cos.util.InitializationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Function;

import static br.ufrj.cos.util.log.FileIOLog.ERROR_READING_FILE;
import static br.ufrj.cos.util.log.NellConverterLog.*;

/**
 * This class mixes the processing of the nell base by iteration and by relation.
 * <p>
 * It first saves each iteration to disk, splitting the relations into positive and negative temporary files.
 * <p>
 * Then, it goes through each relation temporary file, removing facts from same class in previous iterations and from
 * the opposite class in the next iteration, saving it to final files.
 * <p>
 * Created on 30/07/17.
 *
 * @author Victor Guimarães
 */
public class NellHybridBaseConverterCLI extends NellBaseConverterCLI {

    private static final int NELL_ARITY = 2;
    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();
    /**
     * Temporary extension.
     */
    public static final String TEMPORARY_EXTENSION = ".tmp";
    /**
     * If it is to process the iteration first, or the relation first.
     * <p>
     * If {@code true}, it will process each iteration at a time. If {@code false} it will be each relation each a time.
     */
    @SuppressWarnings("CanBeFinal")
    public boolean iterationFirst = false;
    /**
     * If it is to save the iterations to logic files before begin. It is generally the desired behavior, except when
     * the logic files were already saved by another run.
     */
    @SuppressWarnings("CanBeFinal")
    public boolean saveIterationToLogic = true;

    protected String finalPositiveExtension;
    protected String finalNegativeExtension;
    protected boolean correctingPhase;

    /**
     * The main method
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CommandLineInterface instance = new NellHybridBaseConverterCLI();
        mainProgram(instance, logger, args);
    }

    @Override
    public void initialize() throws InitializationException {
        super.initialize();
        this.finalPositiveExtension = positiveOutputExtension;
        this.finalNegativeExtension = negativeOutputExtension;
        this.positiveOutputExtension = positiveOutputExtension + TEMPORARY_EXTENSION;
        this.negativeOutputExtension = negativeOutputExtension + TEMPORARY_EXTENSION;
    }

    @Override
    protected void processFiles() throws IOException, NoSuchAlgorithmException {
        correctingPhase = false;
        if (saveIterationToLogic) { saveIterationFiles(); }
        correctingPhase = true;
        if (iterationFirst) { filterFactsIterationFirst(); } else { filterFactsPredicateFirst(); }
    }

    @Override
    protected String getOutputExtension(boolean positive) {
        if (correctingPhase) {
            return positive ? finalPositiveExtension : finalNegativeExtension;
        } else {
            return super.getOutputExtension(positive);
        }
    }

    /**
     * Process the input files.
     *
     * @throws IOException              if an I/O error has occurred
     * @throws NoSuchAlgorithmException if no Provider supports a MessageDigestSpi implementation for the specified
     *                                  algorithm.
     */
    protected void saveIterationFiles() throws IOException, NoSuchAlgorithmException {
        previousAtoms = buildMapPair();
        if (startIndex > 0) {
            readFile(startIndex - 1);
            previousAtoms = currentAtoms;
        }
        // read the first file to current
        readFile(startIndex);

        int i;
        for (i = startIndex; i < nellInputFilePaths.length; i++) {
            previousAtoms = buildMapPair();
            readFile(i);                        // read the next file to current, filtering it by the current
            previousAtoms = currentAtoms;       // makes the previous the current
            initializeOutputHashMaps(i);
            saveIteration(i);           // saves the previous to files
            atomFactory.clearConstantMap();
        }
    }

    /**
     * Filter the facts from the relations in the iterations, removing the facts of same class in previous iteration
     * and of different class in the next iteration.
     *
     * @throws IOException              if an I/O error has occurred
     * @throws NoSuchAlgorithmException if no Provider supports a MessageDigestSpi implementation for the specified
     *                                  algorithm.
     */
    protected void filterFactsIterationFirst() throws IOException, NoSuchAlgorithmException {
        for (int i = startIndex; i < nellInputFilePaths.length; i++) {
            logger.info(PROCESSING_ITERATION.toString(), i);
            initializeOutputHashMaps(i);
            for (Predicate predicate : getPredicates()) {
                logger.trace(PROCESSING_RELATION_ITERATION.toString(), predicate.getName(), i);
                processRelationOfIteration(predicate, i, true);
                processRelationOfIteration(predicate, i, false);
            }
            logger.info(DONE_ITERATION.toString(), i);
        }
    }

    /**
     * Gets the collections of predicates.
     *
     * @return the collections of predicates
     */
    protected Collection<Predicate> getPredicates() {
        if (saveIterationToLogic) {
            return atomFactory.getPredicates();
        } else {
            return loadRelationFromFiles();
        }
    }

    /**
     * Loads the collections of predicates.
     *
     * @return the collections of predicates
     */
    protected Collection<Predicate> loadRelationFromFiles() {
        Collection<Predicate> predicates = new HashSet<>();
        FilenameFilter filenameFilter = (dir, name) -> name.endsWith(positiveOutputExtension) ||
                name.endsWith(negativeOutputExtension);
        Function<String, String> nameMap = n -> n.replace(positiveOutputExtension, "")
                .replace(negativeOutputExtension, "");
        File iterationDirectory;
        String[] relationFileNames;
        for (int i = startIndex; i < nellInputFilePaths.length; i++) {
            iterationDirectory = getIterationDirectory(i);
            relationFileNames = iterationDirectory.list(filenameFilter);
            if (relationFileNames == null) {
                continue;
            }
            Arrays.stream(relationFileNames).map(nameMap).forEach(n -> predicates.add(new Predicate(n, NELL_ARITY)));
        }
        return predicates;
    }

    /**
     * Filter the facts from the relations in the iterations, removing the facts of same class in previous iteration
     * and of different class in the next iteration.
     *
     * @throws IOException              if an I/O error has occurred
     * @throws NoSuchAlgorithmException if no Provider supports a MessageDigestSpi implementation for the specified
     *                                  algorithm.
     */
    protected void filterFactsPredicateFirst() throws IOException, NoSuchAlgorithmException {
        for (Predicate predicate : getPredicates()) {
            logger.info(PROCESSING_RELATION.toString(), predicate.getName());
            for (int i = startIndex; i < nellInputFilePaths.length; i++) {
                logger.trace(PROCESSING_RELATION_ITERATION.toString(), predicate.getName(), i);
                initializeOutputHashMaps(i);
                processRelationOfIteration(predicate, i, true);
                processRelationOfIteration(predicate, i, false);
            }
            logger.info(DONE_RELATION.toString(), predicate.getName());
        }
    }

    /**
     * Process the relation of the iteration, removing the facts of same class in previous iteration and of
     * different class in the next iteration.
     *
     * @param index     the index of the iteration
     * @param predicate the predicate
     * @param positive  if the atoms are positive or negative
     * @throws IOException              if an I/O error has occurred
     * @throws NoSuchAlgorithmException if no Provider supports a MessageDigestSpi implementation for the specified
     *                                  algorithm.
     */
    protected void processRelationOfIteration(Predicate predicate, int index, boolean positive) throws IOException,
            NoSuchAlgorithmException {
        Set<Clause> currentAtoms = new LinkedHashSet<>();
        File iterationDirectory = getIterationDirectory(index);
        String extension = positive ? finalPositiveExtension : finalNegativeExtension;
        File relationFile = new File(iterationDirectory, predicate.getName() + extension + TEMPORARY_EXTENSION);
        if (!relationFile.exists()) { return; }
        processLogicFile(new FileInputStream(relationFile), currentAtoms);
        previousSkippedAtoms[index] = currentAtoms.size();
        for (int j = 0; j < index; j++) {
            filterAtoms(currentAtoms, predicate, j, extension);
        }
        previousSkippedAtoms[index] -= currentAtoms.size();
        if (index < nellInputFilePaths.length - 1) {
            String oppositeExtension = positive ? negativeOutputExtension : positiveOutputExtension;
            removedAtoms[index] = currentAtoms.size();
            filterAtoms(currentAtoms, predicate, index + 1, oppositeExtension);
            removedAtoms[index] -= currentAtoms.size();
        }
        writePredicateToFile(index, predicate, currentAtoms, positive);
        deleteDataDirectory(relationFile);
    }

    /**
     * Reads a logic file of facts.
     *
     * @param stream  the stream
     * @param clauses a collection of clauses to append to
     * @return a collection of clauses
     * @throws UnsupportedEncodingException if the encode is not supported
     */
    protected Collection processLogicFile(InputStream stream, Collection<Clause> clauses) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(stream, fileEncode);
        try {
            KnowledgeParser parser = new KnowledgeParser(inputStreamReader);
            return parser.parseKnowledgeAppend(clauses);
        } catch (ParseException | ClassCastException e) {
            logger.error(ERROR_READING_FILE.toString(), e);
        } finally {
            stream.close();
        }
        return null;
    }

    /**
     * Filters the atoms from the other found in the iteration of index, relation and positive.
     *
     * @param atoms     the atom to filter
     * @param predicate the predicate to filter
     * @param index     the iteration to find the atoms that should be filtered.
     * @param extension the extension of the file
     * @throws IOException if an I/O error has occurred
     */
    protected void filterAtoms(Set<Clause> atoms, Predicate predicate, int index, String extension) throws IOException {
        File previousIterationDirectory;
        File relationFile;
        previousIterationDirectory = getIterationDirectory(index);
        relationFile = new File(previousIterationDirectory, predicate.getName() + extension);
        if (!relationFile.exists()) { return; }
        Set<Clause> otherAtoms = new HashSet<>();
        processLogicFile(new FileInputStream(relationFile), otherAtoms);
        atoms.removeAll(otherAtoms);
    }

}
