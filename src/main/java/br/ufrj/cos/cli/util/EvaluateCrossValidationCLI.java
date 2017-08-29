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

package br.ufrj.cos.cli.util;

import br.ufrj.cos.cli.CommandLineInterface;
import br.ufrj.cos.cli.CommandLineInterrogationException;
import br.ufrj.cos.cli.CommandLineOptions;
import br.ufrj.cos.cli.LearningFromIterationsCLI;
import br.ufrj.cos.knowledge.example.AtomExample;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.example.Examples;
import br.ufrj.cos.knowledge.example.ProPprExample;
import br.ufrj.cos.knowledge.theory.evaluation.metric.TheoryMetric;
import br.ufrj.cos.knowledge.theory.evaluation.metric.logic.AccuracyMetric;
import br.ufrj.cos.knowledge.theory.evaluation.metric.logic.F1ScoreMetric;
import br.ufrj.cos.knowledge.theory.evaluation.metric.logic.PrecisionMetric;
import br.ufrj.cos.knowledge.theory.evaluation.metric.logic.RecallMetric;
import br.ufrj.cos.knowledge.theory.evaluation.metric.probabilistic.RocCurveMetric;
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.Predicate;
import br.ufrj.cos.util.ExceptionMessages;
import br.ufrj.cos.util.LanguageUtils;
import br.ufrj.cos.util.time.TimeUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import static br.ufrj.cos.util.FileIOUtils.DEFAULT_INPUT_ENCODE;
import static br.ufrj.cos.util.FileIOUtils.FILES;
import static br.ufrj.cos.util.log.GeneralLog.TOTAL_PROGRAM_TIME;
import static br.ufrj.cos.util.log.NellConverterLog.EMPTY;
import static br.ufrj.cos.util.log.UtilsLog.*;

/**
 * Class to consolidate the evaluation of several folds from a cross validation run.
 * <p>
 * Created on 28/08/17.
 *
 * @author Victor Guimarães
 */
public class EvaluateCrossValidationCLI extends CommandLineInterface {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    /**
     * The default fold prefix.
     */
    public static final String DEFAULT_FOLD_PREFIX = "FOLD_";

    /**
     * The test file name.
     */
    public static final String TEST_FILE_NAME = "inference.test.tsv";

    /**
     * The default {@link TheoryMetric}s.
     */
    protected static final TheoryMetric[] DEFAULT_METRICS;
    protected static final int MAXIMUM_METRIC_SIZE;

    static {
        DEFAULT_METRICS = new TheoryMetric[]{
                new AccuracyMetric(),
                new PrecisionMetric(),
                new RecallMetric(),
                new F1ScoreMetric(),
                new RocCurveMetric()
        };
        int auxiliary = 0;
        for (TheoryMetric metric : DEFAULT_METRICS) {
            auxiliary = Math.max(auxiliary, metric.toString().trim().length());
        }
        MAXIMUM_METRIC_SIZE = auxiliary - 1;
    }

    protected File dataDirectory = null;
    protected String foldPrefix = DEFAULT_FOLD_PREFIX;

    /**
     * The main method
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CommandLineInterface instance = new EvaluateCrossValidationCLI();
        mainProgram(instance, logger, args);
    }

    @Override
    public void run() {
        if (dataDirectory == null) { return; }
        try {
            final long begin = TimeUtils.getNanoTime();
            File[] folds = findFolds();
            final List<Pair<Map<Example, Map<Atom, Double>>, Collection<? extends Example>>> evaluations
                    = evaluateFolds(folds);
            evaluateMicroMetrics(evaluations);
            evaluatedAverage(evaluations);
            final long end = TimeUtils.getNanoTime();
            logger.warn(TOTAL_PROGRAM_TIME.toString(), TimeUtils.formatNanoDifference(begin, end));
        } catch (IOException e) {
            logger.error(ExceptionMessages.GENERAL_ERROR.toString(), e);
        }
    }

    /**
     * Evaluates the micro metrics, i.e. the evaluation of the concatenation of all the folds.
     *
     * @param evaluations the evaluation of each fold.
     */
    protected static void evaluateMicroMetrics(
            List<Pair<Map<Example, Map<Atom, Double>>, Collection<? extends Example>>> evaluations) {
        Map<Example, Map<Atom, Double>> inferredExample = new HashMap<>();
        Collection<Example> examples = new HashSet<>();
        for (Pair<Map<Example, Map<Atom, Double>>, Collection<? extends Example>> pair : evaluations) {
            inferredExample.putAll(pair.getKey());
            examples.addAll(pair.getValue());
        }
        logger.info(MICRO_EVALUATION);
        evaluateExamples(inferredExample, examples);
        logger.info(EMPTY);
    }

    /**
     * Finds the folds.
     *
     * @return the found folds
     */
    protected File[] findFolds() {
        Pattern pattern = Pattern.compile(foldPrefix + LearningFromIterationsCLI.NUMERIC_SUFFIX_PATTERN);
        File[] directories = dataDirectory.listFiles((dir, name) -> pattern.matcher(name).matches());
        if (directories == null) { return FILES; }
        Arrays.sort(directories,
                    Comparator.comparingInt(i -> LearningFromIterationsCLI.getIterationNumber(foldPrefix, i)));
        logger.info(FOLD_FOUND.toString(), directories.length);
        if (logger.isDebugEnabled()) {
            for (File directory : directories) {
                logger.debug(directory);
            }
        }
        return directories;
    }

    /**
     * Evaluates the test folds.
     *
     * @param folds the folds
     * @return a list of pairs of inferences and examples
     * @throws IOException if something goes wrong during the reading of the examples
     */
    protected static List<Pair<Map<Example, Map<Atom, Double>>, Collection<? extends Example>>>
    evaluateFolds(File[] folds) throws IOException {
        List<Pair<Map<Example, Map<Atom, Double>>, Collection<? extends Example>>> examples =
                new ArrayList<>(folds.length);
        Pair<Map<Example, Map<Atom, Double>>, Collection<? extends Example>> pair;
        logger.info(EMPTY);
        for (File fold : folds) {
            pair = readTSVExamples(new File(fold, TEST_FILE_NAME));
            logger.info(EVALUATION_OF_FOLD.toString(), fold.getName());
            evaluateExamples(pair.getKey(), pair.getValue());
            logger.info(EMPTY);
            examples.add(pair);
        }
        return examples;
    }

    private static void evaluateExamples(Map<Example, Map<Atom, Double>> inferredResult,
                                         Collection<? extends Example> examples) {
        String metricName;
        for (TheoryMetric metric : DEFAULT_METRICS) {
            metricName = metric.toString().trim();
            logger.info("{}:{}{}", metricName, LanguageUtils.getTabulation(metricName, MAXIMUM_METRIC_SIZE),
                        metric.evaluate(inferredResult, examples));
        }
    }

    private static void evaluatedAverage(List<Pair<Map<Example, Map<Atom, Double>>, Collection<? extends Example>>>
                                                 evaluations) {
        Map<TheoryMetric, Double> evaluationNumerators = new HashMap<>();
        int evaluationDenominator = 0;
        for (Pair<Map<Example, Map<Atom, Double>>, Collection<? extends Example>> pair : evaluations) {
            for (TheoryMetric metric : DEFAULT_METRICS) {
                Double value = evaluationNumerators.computeIfAbsent(metric, e -> 0.0);
                value += metric.evaluate(pair.getKey(), pair.getValue()) * pair.getValue().size();
                evaluationNumerators.put(metric, value);
            }
            evaluationDenominator += pair.getValue().size();
        }
        String metricName;
        logger.info(AVERAGE_EVALUATION);
        for (TheoryMetric metric : DEFAULT_METRICS) {
            metricName = metric.toString().trim();
            logger.info("{}:{}{}", metricName, LanguageUtils.getTabulation(metricName, MAXIMUM_METRIC_SIZE),
                        evaluationNumerators.get(metric) / evaluationDenominator);
        }
        logger.info(EMPTY);

    }

    /**
     * Reads the examples to evaluate.
     *
     * @param file the tsv file
     * @return the pair of inferences and examples
     * @throws IOException if something goes wrong during the reading of the examples
     */
    protected static Pair<Map<Example, Map<Atom, Double>>, Collection<? extends Example>> readTSVExamples(File file)
            throws IOException {
        Map<Example, Map<Atom, Double>> inferredResult = new HashMap<>();
        Examples examples = new Examples();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),
                                                                              DEFAULT_INPUT_ENCODE))) {
            String line;
            reader.readLine();
            line = reader.readLine();
            String[] fields;
            ProPprExample example;
            List<AtomExample> provedExample;
            while (line != null) {
                fields = line.split(LanguageUtils.EXAMPLE_SEPARATOR_CHARACTER);
                final boolean positive = Double.parseDouble(fields[1].trim()) >= 1.0;
                final double value = Double.parseDouble(fields[2].trim());
                Atom atom = new Atom(new Predicate(fields[0].trim()));
                provedExample = Collections.singletonList(new AtomExample(atom, positive));
                example = new ProPprExample(atom, provedExample);
                inferredResult.put(example,
                                   value < 0.0 ? Collections.emptyMap() : Collections.singletonMap(atom, value));
                examples.add(example);
                line = reader.readLine();
            }
        }

        return new ImmutablePair<>(inferredResult, examples);
    }

    @Override
    protected void initializeOptions() {
        super.initializeOptions();
        if (options == null) { options = new Options(); }

        options.addOption(CommandLineOptions.DATA_DIRECTORY.getOption());
        options.addOption(CommandLineOptions.FOLD_PREFIX.getOption());
    }

    @Override
    protected CommandLineInterface parseOptions(CommandLine commandLine) throws CommandLineInterrogationException {
        super.parseOptions(commandLine);

        String dataDirectoryPath = commandLine.getOptionValue(CommandLineOptions.DATA_DIRECTORY.getOptionName());
        foldPrefix = commandLine.getOptionValue(CommandLineOptions.FOLD_PREFIX.getOptionName(), foldPrefix);
        if (dataDirectoryPath != null) {
            dataDirectory = new File(dataDirectoryPath);
        }

        return this;
    }

    @Override
    public String toString() {
        return "Settings:" +
                "\n" +
                "\t" +
                "Data Directory:\t" +
                (dataDirectory != null ? dataDirectory.getAbsolutePath() : "null") +
                "\n" +
                "\t" +
                "Fold Prefix:\t" + foldPrefix;
    }
}
