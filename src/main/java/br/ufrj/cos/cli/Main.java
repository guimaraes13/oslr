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

import br.ufrj.cos.util.TimeMeasure;
import com.esotericsoftware.yamlbeans.YamlReader;
import edu.cmu.ml.proppr.Grounder;
import edu.cmu.ml.proppr.QueryAnswerer;
import edu.cmu.ml.proppr.Trainer;
import edu.cmu.ml.proppr.graph.ArrayLearningGraphBuilder;
import edu.cmu.ml.proppr.prove.InnerProductWeighter;
import edu.cmu.ml.proppr.util.*;
import edu.cmu.ml.proppr.util.math.ParamVector;
import edu.cmu.ml.proppr.util.math.SimpleParamVector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.StringReader;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created on 25/03/17.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings("ALL")
public class Main {

    public static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) {
        test();
        System.exit(0);
        logger.info("Begin Program!");
        Locale.setDefault(new Locale("en", "us"));

//        Smokers Experiment
        String prefix = "/Users/Victor/Documents/Universidade Federal do Rio de " +
                "Janeiro/Research/Models/ProPPR_Smokers/";
        String grounded = new File(prefix, "smokers_train.data.grounded").getAbsolutePath();

        String[] groundingArguments = new String[]{"--programFiles", new File(prefix, "smokers.wam").getAbsolutePath
                () + ":" + new File(prefix, "smokers.graph").getAbsolutePath() + ":" + new File(prefix, "smokers" +
                ".cfacts").getAbsolutePath(), "--queries", new File(prefix, "smokers_train.data").getAbsolutePath(),
                "--grounded", grounded, "--prover", "dpr:0.03:0.2",
//                "alph=0.3"
//                "--epochs",
//                "20"
        };

        String[] trainingArguments = new String[]{"--train", grounded, "--params", new File(prefix, "smokers.wts")
                .getAbsolutePath(),};

        String[] inferenceArguments = new String[]{"--programFiles", new File(prefix, "smokers.wam").getAbsolutePath
                () + ":" + new File(prefix, "smokers.graph").getAbsolutePath() + ":" + new File(prefix, "smokers" +
                ".cfacts").getAbsolutePath(), "--queries", new File(prefix, "smokers_test2.data").getAbsolutePath(),
                "--solutions", new File(prefix, "pre2.training.solutions.txt").getAbsolutePath(), "--prover", "dpr"};

//        grounder(1e-2, 0.1, new String[]{"--help"});
//        grounder(1e-2, 0.1, groundingArguments);
//        trainer(1e-2, 0.1, trainingArguments);
        inference(1e-2, 0.1, inferenceArguments);
        logger.info("End Program!");
    }

    public static void test() {
        String anchor = "- &var1 X1\n- *var1";

        YamlReader reader = null;
        try {
            reader = new YamlReader(new StringReader(anchor));

            List<String> variables = (List<String>) reader.read();
            System.out.println("Variable1:\t\"" + variables.get(0) + "\"");
            System.out.println("Variable2:\t\"" + variables.get(1) + "\"");

            if (variables.get(0) == variables.get(1)) {
                System.out.println("[ OK  ] - The variables represent the same object!");
            } else {
                System.out.println("[ERROR] - The variables represent different objects!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void inference(double epsilon, double alpha, String[] args) {
        org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(QueryAnswerer.class);
        double MIN_FEATURE_TRANSFER = .1;
        try {
            int inputFiles = Configuration.USE_QUERIES | Configuration.USE_PARAMS;
            int outputFiles = Configuration.USE_ANSWERS;
            int modules = Configuration.USE_PROVER | Configuration.USE_SQUASHFUNCTION;
            int constants = Configuration.USE_WAM | Configuration.USE_THREADS | Configuration.USE_ORDER;
            QueryAnswerer.QueryAnswererConfiguration c = new QueryAnswerer.QueryAnswererConfiguration(args,
                                                                                                      inputFiles,
                                                                                                      outputFiles,
                                                                                                      constants,
                                                                                                      modules);
            c.apr.epsilon = epsilon;
            c.apr.alpha = alpha;
//			c.squashingFunction = new Exp();
            System.out.println(c.toString());
            QueryAnswerer qa = new QueryAnswerer(c.apr, c.program, c.plugins, c.prover, c.normalize, c.nthreads, c
                    .topk);
            if (log.isInfoEnabled()) {
                log.info("Running queries from " + c.queryFile + "; saving results to " + c.solutionsFile);
            }
            if (c.paramsFile != null) {
                ParamsFile file = new ParamsFile(c.paramsFile);
                qa.addParams(c.prover, new SimpleParamVector<String>(Dictionary.load(file, new
                        ConcurrentHashMap<String, Double>())), c.squashingFunction);
                file.check(c);
            }
            long start = System.currentTimeMillis();
            qa.findSolutions(c.queryFile, c.solutionsFile, c.maintainOrder);
            if (c.prover.getWeighter() instanceof InnerProductWeighter) {
                InnerProductWeighter w = (InnerProductWeighter) c.prover.getWeighter();
                int n = w.getWeights().size();
                if (((double) w.seenKnownFeatures() / n) < MIN_FEATURE_TRANSFER) {
                    log.warn("Only saw " + w.seenKnownFeatures() + " of " + n + " known features (" + ((double) w
                            .seenKnownFeatures() / n * 100) + "%) -- test data may be too different from training " +
                                     "data");
                }
                if (w.seenUnknownFeatures() > w.seenKnownFeatures()) {
                    log.warn("Saw more unknown features (" + w.seenUnknownFeatures() + ") than known features (" + w
                            .seenKnownFeatures() + ") -- test data may be too different from training data");
                }
            }
            System.out.println("Query-answering time: " + (System.currentTimeMillis() - start));

        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(-1);
        }

    }

    protected static void runExperiment(String dataSet, String[] groundingArguments, String[] trainingArguments) {
        logger.fatal("Begin");
        long begin = TimeMeasure.getNanoTime();
        grounder(1e-2, 0.1, groundingArguments);
        long endGrounding = TimeMeasure.getNanoTime();
        trainer(1e-2, 0.1, trainingArguments);
        long end = TimeMeasure.getNanoTime();
        long groundingTime = endGrounding - begin;
        long trainingTime = end - endGrounding;
        long totalTime = end - begin;

        logger.fatal("Program {} finished running.\nGrounding time was:\t{}.\nTraining time was:\t{}.\nTotal time " +
                             "was:\t{}.", dataSet, TimeMeasure.formatNanoDifference(groundingTime), TimeMeasure
                             .formatNanoDifference(trainingTime), TimeMeasure.formatNanoDifference(totalTime));
    }

    public static void grounder(double epsilon, double alpha, String[] args) {
        try {
            int inputFiles = Configuration.USE_QUERIES | Configuration.USE_PARAMS;
            int outputFiles = Configuration.USE_GROUNDED;
            int constants = Configuration.USE_WAM | Configuration.USE_THREADS | Configuration.USE_ORDER |
                    Configuration.USE_EMPTYGRAPHS;
            int modules = Configuration.USE_GROUNDER | Configuration.USE_PROVER | Configuration.USE_SQUASHFUNCTION;

            Grounder.ExampleGrounderConfiguration c = new Grounder.ExampleGrounderConfiguration(args, inputFiles,
                                                                                                outputFiles,
                                                                                                constants, modules);

//            c.aprOptions.epsilon = epsilon;
//            c.aprOptions.alpha = alpha;

            System.out.println(c.toString());

            if (c.getCustomSetting("graphKey") != null) {
                c.grounder.useGraphKeyFile((File) c.getCustomSetting("graphKey"));
            }
            if (c.paramsFile != null) {
                ParamsFile file = new ParamsFile(c.paramsFile);
                c.grounder.addParams(new SimpleParamVector<>(Dictionary.load(file)), c.squashingFunction);
                file.check(c);
            }

            long start = System.currentTimeMillis();
            c.grounder.groundExamples(c.queryFile, c.groundedFile, c.maintainOrder);
            System.out.println("Grounding time: " + (System.currentTimeMillis() - start));
            System.out.println("Done.");

        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(-1);
        }
    }

    public static void trainer(double epsilon, double alpha, String[] args) {
        org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Trainer.class);
        try {
            int inputFiles = Configuration.USE_TRAIN | Configuration.USE_INIT_PARAMS;
            int outputFiles = Configuration.USE_PARAMS;
            int constants = Configuration.USE_EPOCHS | Configuration.USE_FORCE | Configuration.USE_THREADS |
                    Configuration.USE_FIXEDWEIGHTS;
            int modules = Configuration.USE_TRAINER | Configuration.USE_SRW | Configuration.USE_SQUASHFUNCTION;
            ModuleConfiguration c = new ModuleConfiguration(args, inputFiles, outputFiles, constants, modules);

            c.apr.epsilon = epsilon;
            c.apr.alpha = alpha;

            log.info(c.toString());

            String groundedFile = c.queryFile.getPath();
            if (!c.queryFile.getName().endsWith(Grounder.GROUNDED_SUFFIX)) {
                throw new IllegalStateException("Run Grounder on " + c.queryFile.getName() + " first. Ground+Train " +
                                                        "in" + " one go is not supported yet.");
            }
            SymbolTable<String> masterFeatures = new SimpleSymbolTable<String>();
            File featureIndex = new File(groundedFile + Grounder.FEATURE_INDEX_EXTENSION);
            if (featureIndex.exists()) {
                log.info("Reading feature index from " + featureIndex.getName() + "...");
                for (String line : new ParsedFile(featureIndex)) {
                    masterFeatures.insert(line.trim());
                }
            }
            log.info("Training model parameters on " + groundedFile + "...");
            long start = System.currentTimeMillis();
            ParamVector<String, ?> params = c.trainer.train(masterFeatures, new ParsedFile(groundedFile), new
                    ArrayLearningGraphBuilder(), c.initParamsFile, c.epochs);
            System.out.println("Training time: " + (System.currentTimeMillis() - start));

            if (c.paramsFile != null) {
                log.info("Saving parameters to " + c.paramsFile + "...");
                ParamsFile.save(params, c.paramsFile, c);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(-1);
        }
    }

}
