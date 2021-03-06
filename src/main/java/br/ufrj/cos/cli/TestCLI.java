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

package br.ufrj.cos.cli;

import br.ufrj.cos.knowledge.example.AtomExample;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.example.Examples;
import br.ufrj.cos.knowledge.theory.evaluation.metric.TheoryMetric;
import br.ufrj.cos.knowledge.theory.evaluation.metric.probabilistic.RocCurveMetric;
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.util.FileIOUtils;
import br.ufrj.cos.util.InitializationException;
import br.ufrj.cos.util.LanguageUtils;
import br.ufrj.cos.util.Plot2D;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static br.ufrj.cos.util.log.InferenceLog.EVALUATION_UNDER_METRIC;

/**
 * Class to evaluate the model trained from a {@link LearningFromBatchCLI} into a test set.
 * <p>
 * Created on 21/05/17.
 *
 * @author Victor Guimarães
 */
public class TestCLI extends CommandLineInterface {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();
    /**
     * The default yaml configuration file.
     */
    public static final String DEFAULT_YAML_CONFIGURATION_FILE = "src/main/resources/test.yml";
    /**
     * The name of the file that logs the output.
     */
    public static final String STDOUT_LOG_FILE_NAME = "evaluation.txt";
    /**
     * If it is to load the trained parameters before the evaluation.
     */
    public static final boolean LOAD_PARAMETERS = true;
    /**
     * The test examples paths
     */
    public String[] exampleFilePaths;
    /**
     * The {@link TheoryMetric}s.
     */
    @SuppressWarnings("unused")
    public TheoryMetric[] theoryMetrics;
    /**
     * The input directory to read the files from.
     */
    public String inputDirectoryPath;
    /**
     * If it is to draw the ROC curve, if it exists.
     */
    @SuppressWarnings("CanBeFinal")
    public boolean drawRocCurve = false;

    protected LearningFromBatchCLI learningFromBatchCLI;
    protected Examples examples;

    /**
     * The main method
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CommandLineInterface instance = new TestCLI();
        mainProgram(instance, logger, args);
    }

    @Override
    public void initialize() throws InitializationException {
        try {
            addAppender(new File(inputDirectoryPath, STDOUT_LOG_FILE_NAME).getAbsolutePath());
            learningFromBatchCLI = initializeLearningCLI();
            learningFromBatchCLI.outputDirectory = new File(inputDirectoryPath);
            learningFromBatchCLI.initialize();
            learningFromBatchCLI.theoryFilePaths = new String[]{
                    new File(inputDirectoryPath, LearningFromBatchCLI.THEORY_FILE_NAME).getAbsolutePath()
            };
            learningFromBatchCLI.loadedPreTrainedParameters = LOAD_PARAMETERS;
            learningFromBatchCLI.build();
            examples = FileIOUtils.buildExampleSet(exampleFilePaths);
        } catch (Exception e) {
            throw new InitializationException(e);
        }
    }

    /**
     * Initializes a {@link LearningFromBatchCLI} from its arguments.
     *
     * @return the {@link LearningFromBatchCLI}
     * @throws IOException if the an error occurs when reading the file
     */
    protected LearningFromBatchCLI initializeLearningCLI() throws IOException {
        learningFromBatchCLI = new LearningFromBatchCLI();
        String arguments = FileIOUtils.readFileToString(new File(inputDirectoryPath,
                                                                 learningFromBatchCLI.getArgumentFileName()));
        String[] fields = arguments.split(LanguageUtils.ARGUMENTS_SEPARATOR);
        if (fields[0].endsWith(LearningFromBatchCLI.class.getSimpleName())) {
            fields = Arrays.copyOfRange(fields, 1, fields.length);
        }
        return (LearningFromBatchCLI) learningFromBatchCLI.parseOptions(fields);
    }

    @Override
    protected void initializeOptions() {
        super.initializeOptions();
        if (options == null) { options = new Options(); }
        options.addOption(CommandLineOptions.INPUT_DIRECTORY.option);
        options.addOption(CommandLineOptions.EXAMPLES.option);
        options.addOption(CommandLineOptions.YAML.option);
    }

    @Override
    protected CommandLineInterface parseOptions(CommandLine commandLine) throws CommandLineInterrogationException {
        try {
            super.parseOptions(commandLine);
            TestCLI cli = readYamlFile(commandLine, TestCLI.class, DEFAULT_YAML_CONFIGURATION_FILE);
            cli.exampleFilePaths = getFilesFromOption(commandLine, CommandLineOptions.EXAMPLES.getOptionName(),
                                                      cli.exampleFilePaths);
            cli.inputDirectoryPath = commandLine.getOptionValue(CommandLineOptions.INPUT_DIRECTORY.getOptionName());
            return cli;
        } catch (IOException e) {
            throw new CommandLineInterrogationException(e);
        }
    }

    @SuppressWarnings("unused")
    @Override
    public void run() {
        Map<Example, Map<Atom, Double>> inferredExamples = learningFromBatchCLI.learningSystem.inferExamples(examples);
        Map<TheoryMetric, Double> evaluations = new HashMap<>();
        RocCurveMetric rocCurveMetric = null;
        double evaluation;
        for (TheoryMetric metric : theoryMetrics) {
            evaluation = metric.evaluate(inferredExamples, examples);
            logger.warn(EVALUATION_UNDER_METRIC.toString(), metric, evaluation);
            if (metric instanceof RocCurveMetric) {
                rocCurveMetric = (RocCurveMetric) metric;
            }
        }

        if (drawRocCurve) { drawRocCurve(rocCurveMetric, inferredExamples); }
    }

    /**
     * Draws the ROC curve from the inferred examples.
     *
     * @param rocCurveMetric   the {@link RocCurveMetric} instance
     * @param inferredExamples the inferred examples
     */
    protected void drawRocCurve(RocCurveMetric rocCurveMetric, Map<Example, Map<Atom, Double>> inferredExamples) {
        if (rocCurveMetric != null) {
            List<Pair<AtomExample, Double>> pairs;
            pairs = rocCurveMetric.calculateEvaluation(inferredExamples, examples);
            if (pairs != null) {
                Plot2D plot2D = Plot2D.createRocPlot(rocCurveMetric.buildCurve(pairs));
                plot2D.plot();
            }
        }
    }

    @Override
    public String toString() {
        String itemPrefix = "\t\t\t-\t";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\t").append("Settings:");
        stringBuilder.append("\n").append("\t").append("Input directory:").append("\n");
        stringBuilder.append(itemPrefix).append(inputDirectoryPath).append("\n");
        stringBuilder.append("\n").append("\t").append("Example files:").append("\n");
        for (String examplesFilePath : exampleFilePaths) {
            stringBuilder.append(itemPrefix).append(examplesFilePath).append("\n");
        }
        return stringBuilder.toString();
    }

}
