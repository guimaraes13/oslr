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
import br.ufrj.cos.cli.CommandLineInterrogationException;
import br.ufrj.cos.knowledge.example.AtomExample;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.example.Examples;
import br.ufrj.cos.knowledge.example.ProPprExample;
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.Predicate;
import br.ufrj.cos.logic.Term;
import br.ufrj.cos.logic.Variable;
import br.ufrj.cos.logic.parser.knowledge.ParseException;
import br.ufrj.cos.util.*;
import br.ufrj.cos.util.time.TimeUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

import static br.ufrj.cos.cli.CommandLineOptions.*;
import static br.ufrj.cos.cli.CommandLineOptions.NEGATIVE_EXTENSION;
import static br.ufrj.cos.cli.CommandLineOptions.POSITIVE_EXTENSION;
import static br.ufrj.cos.cli.LearningFromIterationsCLI.DEFAULT_EXAMPLES_FILE_EXTENSION;
import static br.ufrj.cos.util.log.GeneralLog.TOTAL_PROGRAM_TIME;
import static br.ufrj.cos.util.log.NellConverterLog.*;

/**
 * Class to convert examples from the logic representation to the ProPPR's representation.
 * <p>
 * Created on 04/08/17.
 *
 * @author Victor Guimarães
 */
public class LogicToProPprConverter extends CommandLineInterface {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    /**
     * The default variable name to be used at the examples format.
     */
    public static final String DEFAULT_VARIABLE_NAME = "X1";
    /**
     * The variable name to be used at the examples format.
     */
    @SuppressWarnings("CanBeFinal")
    public String variableName = DEFAULT_VARIABLE_NAME;
    /**
     * The data directory path. The directory to find the iterations in.
     */
    public String dataDirectoryPath;
    /**
     * The positive output extension.
     */
    public String positiveExtension = NellBaseConverterCLI.DEFAULT_POSITIVE_EXTENSION;
    /**
     * The negative output extension.
     */
    public String negativeExtension = NellBaseConverterCLI.DEFAULT_NEGATIVE_EXTENSION;
    /**
     * The example file extension.
     */
    public String examplesFileExtension = DEFAULT_EXAMPLES_FILE_EXTENSION;

    /**
     * If it is to force the index. If the value is positive, force this index of the predicate's term to be become a
     * variable. If it is negative, find the term if the smallest number of substitutions.
     */
    public int forceIndex = -1;

    /**
     * Filters to remove examples that only has the negative part. If {@code true}, examples with only negative part
     * will be removed from the output.
     */
    public boolean filterOnlyNegativeExamples = false;

    protected AtomFactory atomFactory;
    protected String[] logicFiles;
    protected NumberFormat numberFormat;
    protected File dataDirectory;

    /**
     * The main method
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CommandLineInterface instance = new LogicToProPprConverter();
        mainProgram(instance, logger, args);
    }

    @Override
    public void initialize() throws InitializationException {
        numberFormat = NumberFormat.getIntegerInstance();
        atomFactory = new AtomFactory();
        dataDirectory = new File(dataDirectoryPath);
        logicFiles = dataDirectory.list((dir, name) -> name.endsWith(positiveExtension));
        buildOutputDirectory("");
    }

    @Override
    protected void initializeOptions() {
        super.initializeOptions();
        if (options == null) { options = new Options(); }

        options.addOption(DATA_DIRECTORY.getOption());
        options.addOption(OUTPUT_DIRECTORY.getOption());

        options.addOption(POSITIVE_EXTENSION.getOption());
        options.addOption(NEGATIVE_EXTENSION.getOption());
        options.addOption(EXAMPLES_EXTENSION.getOption());

        options.addOption(FILTER_ONLY_NEGATIVES.getOption());
        options.addOption(FORCE_INDEX.getOption());
    }

    @Override
    public CommandLineInterface parseOptions(CommandLine commandLine) throws CommandLineInterrogationException {
        super.parseOptions(commandLine);
        dataDirectoryPath = commandLine.getOptionValue(DATA_DIRECTORY.getOptionName());
        outputDirectoryPath = commandLine.getOptionValue(OUTPUT_DIRECTORY.getOptionName(), dataDirectoryPath);

        positiveExtension = commandLine.getOptionValue(POSITIVE_EXTENSION.getOptionName(), positiveExtension);
        negativeExtension = commandLine.getOptionValue(NEGATIVE_EXTENSION.getOptionName(), negativeExtension);
        examplesFileExtension = commandLine.getOptionValue(EXAMPLES_EXTENSION.getOptionName(), examplesFileExtension);

        if (commandLine.hasOption(FORCE_INDEX.getOptionName())) {
            forceIndex = Integer.parseInt(commandLine.getOptionValue(FORCE_INDEX.getOptionName()));
        }

        filterOnlyNegativeExamples = commandLine.hasOption(FILTER_ONLY_NEGATIVES.getOptionName());
        return this;
    }

    @Override
    public void run() {
        long begin = TimeUtils.getNanoTime();
        processFiles();
        long end = TimeUtils.getNanoTime();
        logger.warn(TOTAL_PROGRAM_TIME.toString(), TimeUtils.formatNanoDifference(begin, end));
    }

    /**
     * Process the found files.
     */
    protected void processFiles() {
        try {
            String filePrefix;
            for (String fileName : logicFiles) {
                filePrefix = fileName.substring(0, fileName.length() - positiveExtension.length());
                logger.info(PROCESSING_FILE.toString(), filePrefix);
                convertExamplesFromLogic(dataDirectory, filePrefix);
                logger.info(EMPTY);
            }
        } catch (IOException | ParseException e) {
            logger.error(ExceptionMessages.GENERAL_ERROR.toString(), e);
        }
    }

    /**
     * Converts the examples from the logic files, saving them into the files named by filePrefix +
     * {@link #examplesFileExtension}, and saving it beside the relation file.
     *
     * @param dataDirectory the dataDirectory
     * @param filePrefix    the target relation
     * @throws ParseException if a parser error occurs
     * @throws IOException    if an I/O error has occurred
     */
    protected void convertExamplesFromLogic(File dataDirectory, String filePrefix)
            throws IOException, ParseException {
        File positiveFile = new File(dataDirectory, filePrefix + positiveExtension);
        File negativeFile = new File(dataDirectory, filePrefix + negativeExtension);

        List<Atom> positives = new ArrayList<>();
        FileIOUtils.readAtomKnowledgeFromFile(positiveFile, positives, atomFactory);

        List<Atom> negatives = new ArrayList<>();
        FileIOUtils.readAtomKnowledgeFromFile(negativeFile, negatives, atomFactory);
        logger.debug(TOTAL_NUMBER_POSITIVES.toString(), numberFormat.format(positives.size()));
        logger.debug(TOTAL_NUMBER_NEGATIVES.toString(), numberFormat.format(negatives.size()));
        final Collection<? extends Example> examples = convertAtomToExamples(positives, negatives);
        logger.debug(TOTAL_NUMBER_EXAMPLES_PROPPR.toString(), numberFormat.format(examples.size()));
        final File outputFile = new File(outputDirectory, filePrefix + examplesFileExtension);
        FileIOUtils.saveExamplesToFile(examples, outputFile);
        logger.debug(EXAMPLES_SAVING.toString(), outputFile.getName());
    }

    /**
     * Creates the empty proppr examples with the correct goal.
     *
     * @param atomsByPredicate     the atoms by predicate
     * @param predicateVariableMap the map of the index of the term that must become a variable
     * @return the empty proppr examples with the correct goal
     */
    protected Map<Predicate, Set<ProPprExample>> getProPprGoals(Map<Predicate, Set<Atom>> atomsByPredicate,
                                                                Map<Predicate, Integer> predicateVariableMap) {
        Set<ProPprExample> values;
        List<Term> terms;
        Map<Predicate, Set<ProPprExample>> examplesByPredicate = new HashMap<>();
        final Variable variable = new Variable(variableName);
        for (Map.Entry<Predicate, Set<Atom>> entry : atomsByPredicate.entrySet()) {
            values = new LinkedHashSet<>();
            examplesByPredicate.put(entry.getKey(), values);
            for (Atom atom : entry.getValue()) {
                terms = new ArrayList<>(atom.getTerms());
                terms.set(predicateVariableMap.get(atom.getPredicate()), variable);
                values.add(new ProPprExample(new Atom(entry.getKey(), terms), new ArrayList<>()));
            }
        }
        return examplesByPredicate;
    }

    /**
     * Finds the index of the minimum size value in an array of collections.
     *
     * @param values the array of collections
     * @return the index of the minimum size value in an array of collections
     */
    public static int getMinimumArgumentIndex(Collection<?>[] values) {
        int minIndex = 0;
        int minValue;
        int auxiliary;
        minValue = values[minIndex].size();
        for (int i = 1; i < values.length; i++) {
            auxiliary = values[i].size();
            if (auxiliary < minValue) {
                minIndex = i;
                minValue = auxiliary;
            }
        }
        return minIndex;
    }

    /**
     * Converts the atoms to the ProPPR's example format.
     *
     * @param positives the positive atoms
     * @param negatives the positive atoms
     * @return the examples in the ProPPR's format.
     */
    protected Collection<? extends Example> convertAtomToExamples(Collection<? extends Atom> positives,
                                                                  Collection<? extends Atom> negatives) {
        Map<Predicate, Set<Atom>> atomsByPredicate = new HashMap<>();
        LanguageUtils.splitAtomsByPredicate(positives, atomsByPredicate, Atom::getPredicate);
        LanguageUtils.splitAtomsByPredicate(negatives, atomsByPredicate, Atom::getPredicate);
        Map<Predicate, Integer> predicateVariableMap = calculateIndexToVariable(atomsByPredicate);
        Map<Predicate, Set<ProPprExample>> examplesByPredicate = getProPprGoals(atomsByPredicate,
                                                                                predicateVariableMap);

        Collection<AtomExample> atomExamples = new HashSet<>(positives.size() + negatives.size());
        positives.stream().map(e -> new AtomExample(e, true)).forEach(atomExamples::add);
        negatives.stream().map(e -> new AtomExample(e, false)).forEach(atomExamples::add);
        Examples.appendAtomExamplesIntoProPpr(atomExamples, examplesByPredicate);

        final List<ProPprExample> examples;

        if (filterOnlyNegativeExamples) {
            examples = examplesByPredicate.values().stream().flatMap(Collection::stream)
                    .filter(ProPprExample::isPositive).collect(Collectors.toList());
        } else {
            examples = examplesByPredicate.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        }

        return new LinkedHashSet<>(examples);
    }

    /**
     * Calculates the index of the predicates' term to be changed to a variable. The index is the one that has the
     * minimum number of elements among the examples.
     *
     * @param atomsByPredicate the atoms by predicate
     * @return the index of the terms that should be replaced by a variable.
     */
    protected Map<Predicate, Integer> calculateIndexToVariable(Map<Predicate, Set<Atom>> atomsByPredicate) {
        Map<Predicate, Integer> termsByIndex = new HashMap<>();
        Set<Term>[] values;
        int minIndex;
        Predicate predicate;
        for (Map.Entry<Predicate, Set<Atom>> entry : atomsByPredicate.entrySet()) {
            predicate = entry.getKey();
            if (predicate.getArity() == 0) { continue; }
            if (forceIndex > 0) {
                minIndex = forceIndex;
            } else {
                values = new Set[predicate.getArity()];
                Arrays.setAll(values, i -> new HashSet());
                for (Atom atom : entry.getValue()) {
                    for (int i = 0; i < predicate.getArity(); i++) {
                        values[i].add(atom.getTerms().get(i));
                    }
                }
                minIndex = getMinimumArgumentIndex(values);
            }
            termsByIndex.put(predicate, minIndex);
        }

        return termsByIndex;
    }

    @Override
    public String toString() {
        StringBuilder description = new StringBuilder();
        description.append("\t").append("Settings:").append("\n");
        description.append("\t").append("Data Directory:\t\t").append(dataDirectoryPath).append("\n");
        description.append("\t").append("Output Directory:\t").append(outputDirectoryPath).append("\n");
        description.append("\t").append("Positive Extension:\t").append(positiveExtension).append("\n");
        description.append("\t").append("Negative Extension:\t").append(negativeExtension).append("\n");
        description.append("\t").append("Examples Extension:\t").append(examplesFileExtension).append("\n");
        if (forceIndex > 0) {
            description.append("\t").append("Force Index:\t\t").append(forceIndex).append("\n");
        }
        description.append("\t").append("Filter Negatives:\t").append(filterOnlyNegativeExamples).append("\n");
        description.append("\t").append("Logic Files:\n");
        for (String logicFile : logicFiles) {
            description.append("\t\t - ").append(logicFile).append("\n");
        }
        return description.toString();
    }

}
