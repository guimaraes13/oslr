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

package br.ufrj.cos.cli.util;

import br.ufrj.cos.cli.CommandLineInterface;
import br.ufrj.cos.cli.CommandLineInterrogationException;
import br.ufrj.cos.knowledge.example.AtomExample;
import br.ufrj.cos.knowledge.example.ProPprExample;
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.Predicate;
import br.ufrj.cos.logic.Term;
import br.ufrj.cos.logic.parser.knowledge.ParseException;
import br.ufrj.cos.util.FileIOUtils;
import br.ufrj.cos.util.LanguageUtils;
import br.ufrj.cos.util.time.TimeUtils;
import org.apache.commons.cli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static br.ufrj.cos.cli.CommandLineOptions.PERCENTAGE_FILTER;
import static br.ufrj.cos.util.log.GeneralLog.TOTAL_PROGRAM_TIME;
import static br.ufrj.cos.util.log.NellConverterLog.*;

/**
 * Class to convert examples from the logic representation to the ProPPR's representation generating the negative
 * examples using the Local Close World Assumption.
 * <p>
 * Created on 04/08/17.
 *
 * @author Victor Guimarães
 */
public class LogicToProPprConverterLCWA extends LogicToProPprConverter {

    /**
     * The maximum number of attempts to create a corrupt version of the current example.
     */
    public static final int MAXIMUM_ATTEMPTS = 100;
    /**
     * The default value of the percentage of examples to be used.
     */
    public static final double DEFAULT_PERCENTAGE_FILTER = 1.0;
    /**
     * The percentage of examples to be used.
     */
    public double percentageFilter = DEFAULT_PERCENTAGE_FILTER;

    /**
     * The main method
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CommandLineInterface instance = new LogicToProPprConverterLCWA();
        mainProgram(instance, logger, args);
    }

    @Override
    public void run() {
        long begin = TimeUtils.getNanoTime();
        processFiles();
        long end = TimeUtils.getNanoTime();
        logger.warn(TOTAL_PROGRAM_TIME.toString(), TimeUtils.formatNanoDifference(begin, end));
    }

    @Override
    protected void initializeOptions() {
        super.initializeOptions();
        options.addOption(PERCENTAGE_FILTER.getOption());
    }

    @Override
    public CommandLineInterface parseOptions(CommandLine commandLine) throws CommandLineInterrogationException {
        super.parseOptions(commandLine);
        if (commandLine.hasOption(PERCENTAGE_FILTER.getOptionName())) {
            percentageFilter = Double.parseDouble(commandLine.getOptionValue(PERCENTAGE_FILTER.getOptionName()));
        }
        return this;
    }

    @Override
    protected void convertExamplesFromLogic(File dataDirectory, String filePrefix)
            throws IOException, ParseException {
        File positiveFile = new File(dataDirectory, filePrefix + positiveExtension);

        List<Atom> positives = new ArrayList<>();
        FileIOUtils.readAtomKnowledgeFromFile(positiveFile, positives, atomFactory);

        logger.debug(TOTAL_NUMBER_POSITIVES.toString(), numberFormat.format(positives.size()));
        positives = filterAtRandom(positives);
        logger.debug(TOTAL_NUMBER_POSITIVES.toString(), numberFormat.format(positives.size()));
        Collection<? extends ProPprExample> examples = convertAtomToExamples(positives, Collections.emptySet());
        logger.debug(TOTAL_NUMBER_EXAMPLES_PROPPR.toString(), numberFormat.format(examples.size()));
        if (negativePortionFilter >= 0.0) { examples = generateNegatives(positives, examples); }
        if (shuffle) { examples = shuffleOutput(examples); }
        final File outputFile = new File(outputDirectory, filePrefix + examplesFileExtension);
        FileIOUtils.saveExamplesToFile(examples, outputFile);
        logger.debug(EXAMPLES_SAVING.toString(), outputFile.getName());
    }

    /**
     * Filter the elements at random returning a set of size collection * {@link #percentageFilter}, if
     * {@link #percentageFilter} < 1.0; or collection, otherwise.
     *
     * @param collection the initial collection
     * @param <E>        the type of the elements in collection
     * @return the new filtered collection
     */
    protected <E> List<E> filterAtRandom(List<E> collection) {
        if (percentageFilter < 1.0) {
            final List<E> elements = new ArrayList<>();
            if (percentageFilter > 0.0) {
                logger.debug(FILTERING_EXAMPLES.toString(), percentageFilter * 100);
                Random rand = getRandom();
                for (E e : collection) {
                    if (rand.nextDouble() < percentageFilter) { elements.add(e); }
                }
            }
            return elements;
        } else {
            return collection;
        }
    }

    /**
     * Generates the negative examples so the portion of negatives become approximately {@link #negativePortionFilter}
     * times the portion of positive.
     *
     * @param positives the set of positive atoms
     * @param examples  the examples to filter
     * @return the generated examples
     */
    protected Collection<? extends ProPprExample> generateNegatives(Collection<? extends Atom> positives,
                                                                    Collection<? extends ProPprExample> examples) {
        Map<Predicate, Set<Atom>> atomsByPredicate = new HashMap<>();
        LanguageUtils.splitAtomsByPredicate(positives, atomsByPredicate, Atom::getPredicate);
        Map<Predicate, Integer> predicateVariableMap = calculateIndexToVariable(atomsByPredicate);
        final Map<Predicate, List<Term>> possibleTermsByPredicate = buildTermsByPredicate(positives,
                                                                                          predicateVariableMap);

        return generateNegativeExamples(positives, examples, possibleTermsByPredicate, predicateVariableMap);
    }

    /**
     * Builds the sets of possible term to substitute the variable from the goal of the examples by each predicate.
     * These terms are the terms that appears in the same position of the variable in a positive example of the
     * predicate.
     *
     * @param positives            the set of possible examples
     * @param predicateVariableMap the position of the variable term for each predicate
     * @return the set of possible term for each predicate
     */
    protected static Map<Predicate, List<Term>> buildTermsByPredicate(Collection<? extends Atom> positives,
                                                                      Map<Predicate, Integer> predicateVariableMap) {
        Map<Predicate, Set<Term>> termByPredicate = new HashMap<>();

        for (Atom atom : positives) {
            final Predicate predicate = atom.getPredicate();
            termByPredicate.computeIfAbsent(predicate, k -> new HashSet<>())
                    .add(atom.getTerms().get(predicateVariableMap.get(predicate)));
        }

        Map<Predicate, List<Term>> answer = new HashMap<>();
        for (Map.Entry<Predicate, Set<Term>> entry : termByPredicate.entrySet()) {
            answer.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return answer;
    }

    @Override
    public String toString() {
        return super.toString() + "\tExamples Extension:\t" + examplesFileExtension + "\n";
    }

    @SuppressWarnings({"OverlyLongMethod", "OverlyComplexMethod"})
    private Collection<? extends ProPprExample> generateNegativeExamples(Collection<? extends Atom> positives,
                                                                         Collection<? extends ProPprExample> examples,
                                                                         Map<Predicate, List<Term>> termsByPredicate,
                                                                         Map<Predicate, Integer> predicateVariableMap) {
        final Random exampleRandom = getRandom();
        final Random termRandom = getRandom();
        final Set<ProPprExample> examplesWithNegatives = new LinkedHashSet<>();
        int totalOfNegatives = 0;
        final int totalOfExamples = examples.size();
        final double percentage = totalOfExamples / 100.0;
        int processedExamples = 0;
        for (ProPprExample example : examples) {
            final int positiveCount = (int) example.getGroundedQuery().stream().filter(AtomExample::isPositive).count();
            final int negativeExamples = (int) (negativePortionFilter * positiveCount);
            final double probabilityExample = (negativePortionFilter * positiveCount) - negativeExamples;
            final Set<Atom> negatives = new HashSet<>();
            final int examplesToAdd = negativeExamples + (exampleRandom.nextDouble() < probabilityExample ? 1 : 0);
            int addedExamples = 0;
            int attempt = 0;
            final Predicate predicate = example.getGoal().getPredicate();
            while (addedExamples < examplesToAdd && attempt < MAXIMUM_ATTEMPTS) {
                final List<Term> terms = new ArrayList<>(example.getGoal().getTerms());
                final List<Term> possibleTerms = termsByPredicate.get(predicate);
                terms.set(predicateVariableMap.get(predicate),
                          possibleTerms.get(termRandom.nextInt(possibleTerms.size())));
                final Atom negative = new Atom(predicate, terms);
                if (!positives.contains(negative) && !negatives.contains(negative)) {
                    negatives.add(negative);
                    attempt = 0;
                    addedExamples++;
                } else {
                    attempt++;
                }
            }
            totalOfNegatives += addedExamples;
            List<AtomExample> atomExamples = new ArrayList<>(example.getGroundedQuery().size() + addedExamples);
            atomExamples.addAll(example.getGroundedQuery());
            for (Atom negative : negatives) {
                atomExamples.add(new AtomExample(predicate, negative.getTerms(), false));
            }
            examplesWithNegatives.add(new ProPprExample(example.getGoal(), atomExamples));
            processedExamples++;
            if (processedExamples % percentage == 0) {
                logger.debug(PROCESSED_NUMBER_EXAMPLES.toString(),
                             numberFormat.format(processedExamples), numberFormat.format(totalOfExamples));
            }
        }
        logger.debug(TOTAL_NUMBER_NEGATIVES.toString(), numberFormat.format(totalOfNegatives));
        return examplesWithNegatives;
    }

}
