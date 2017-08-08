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
import com.esotericsoftware.yamlbeans.YamlException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static br.ufrj.cos.util.log.GeneralLog.ERROR_MAIN_PROGRAM;
import static br.ufrj.cos.util.log.GeneralLog.PROGRAM_END;
import static br.ufrj.cos.util.log.InferenceLog.EVALUATION_UNDER_METRIC;

/**
 * Class to evaluate the model trained from a {@link LearningFromFilesCLI} into a test set.
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

    protected LearningFromFilesCLI learningFromFilesCLI;
    protected Examples examples;

    /**
     * The main method
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Locale.setDefault(new Locale(DEFAULT_LANGUAGE, DEFAULT_COUNTRY));
        try {
            CommandLineInterface main = new TestCLI();
            main = main.parseOptions(args);
            run(main, args);
        } catch (Exception e) {
            logger.error(ERROR_MAIN_PROGRAM, e);
        } finally {
            logger.fatal(PROGRAM_END);
        }
    }

    @Override
    public void initialize() throws InitializationException {
        try {
            addAppender(new File(inputDirectoryPath, STDOUT_LOG_FILE_NAME).getAbsolutePath());
            learningFromFilesCLI = initializeLearningCLI();
            learningFromFilesCLI.outputDirectory = new File(inputDirectoryPath);
            learningFromFilesCLI.initialize();
            learningFromFilesCLI.theoryFilePaths = new String[]{
                    new File(inputDirectoryPath, LearningFromFilesCLI.THEORY_FILE_NAME).getAbsolutePath()
            };
            learningFromFilesCLI.loadedPreTrainedParameters = LOAD_PARAMETERS;
            learningFromFilesCLI.build();
            examples = LearningFromFilesCLI.buildExampleSet(exampleFilePaths);
        } catch (Exception e) {
            throw new InitializationException(e);
        }
    }

    /**
     * Initializes a {@link LearningFromFilesCLI} from its arguments.
     *
     * @return the {@link LearningFromFilesCLI}
     * @throws IOException if the an error occurs when reading the file
     */
    protected LearningFromFilesCLI initializeLearningCLI() throws IOException {
        learningFromFilesCLI = new LearningFromFilesCLI();
        String arguments = FileIOUtils.readFileToString(new File(inputDirectoryPath,
                                                                 learningFromFilesCLI.getArgumentFileName()));
        String[] fields = arguments.split(LanguageUtils.ARGUMENTS_SEPARATOR);
        if (fields[0].endsWith(LearningFromFilesCLI.class.getSimpleName())) {
            fields = Arrays.copyOfRange(fields, 1, fields.length);
        }
        return (LearningFromFilesCLI) learningFromFilesCLI.parseOptions(fields);
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
            cli.exampleFilePaths = getFilesFromOption(commandLine, CommandLineOptions.EXAMPLES.getOptionName());
            cli.inputDirectoryPath = commandLine.getOptionValue(CommandLineOptions.INPUT_DIRECTORY.getOptionName());
            return cli;
        } catch (FileNotFoundException | YamlException e) {
            throw new CommandLineInterrogationException(e);
        }
    }

    @SuppressWarnings("unused")
    @Override
    public void run() {
        Map<Example, Map<Atom, Double>> inferredExamples = learningFromFilesCLI.learningSystem.inferExamples(examples);
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
                Plot2D plot2D = Plot2D.createRocPlot(RocCurveMetric.buildRocCurve(pairs));
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
