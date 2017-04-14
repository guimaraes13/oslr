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

package br.ufrj.cos.main;

import br.ufrj.cos.util.TimeMeasure;
import edu.cmu.ml.proppr.Grounder;
import edu.cmu.ml.proppr.Trainer;
import edu.cmu.ml.proppr.graph.ArrayLearningGraphBuilder;
import edu.cmu.ml.proppr.util.*;
import edu.cmu.ml.proppr.util.math.ParamVector;
import edu.cmu.ml.proppr.util.math.SimpleParamVector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Locale;

/**
 * Created on 25/03/17.
 *
 * @author Victor Guimarães
 */
public class Main {

    public static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) {
        Locale.setDefault(new Locale("en", "us"));

//        Smokers Experiment
//        String prefix = "/Users/Victor/Desktop/ProPPR_Smokers";
//        String grounded = new File(prefix, "smokers_train.data.grounded").getAbsolutePath();
//
//        String[] groundingArguments = new String[]{
//                "--programFiles",
//                new File(prefix, "smokers.wam").getAbsolutePath() + ":" +
//                        new File(prefix, "smokers.graph").getAbsolutePath() + ":" +
//                        new File(prefix, "smokers.cfacts").getAbsolutePath(),
//                "--queries",
//                new File(prefix, "smokers_train.data").getAbsolutePath(),
//                "--grounded",
//                grounded,
//                "--prover",
//                "dpr"
//        };
//
//        String[] trainingArguments = new String[]{
//                "--train",
//                grounded,
//                "--params",
//                new File(prefix, "smokers.wts").getAbsolutePath(),
//        };

//      Google Near 1M Experiment
        String prefix = "/Users/Victor/IdeaProjects/ProPPR/examples/top-1M-near-X";
        String grounded = new File(prefix, "top-1M-near-google.train.examples.grounded").getAbsolutePath();

        String[] groundingArguments = new String[]{
                "--programFiles",
                new File(prefix, "top-1M-near-google-recursive.wam").getAbsolutePath()
                        + ":" + new File(prefix, "top-1M-near-google-fact.graph").getAbsolutePath(),
//                        + ":" + new File(prefix, "smokers.cfacts").getAbsolutePath(),
                "--queries",
                new File(prefix, "top-1M-near-google.train.examples").getAbsolutePath(),
                "--grounded",
                grounded,
                "--prover",
                "dpr"
        };

        String[] trainingArguments = new String[]{
                "--train",
                grounded,
                "--params",
                new File(prefix, "top-1M-near-google.wts").getAbsolutePath(),
        };

        String dataSet = "beatles";
//        Baseball Near 1M Experiment
        prefix = "/Users/Victor/IdeaProjects/ProPPR/examples/top-1M-near-X";
        grounded = new File(prefix, "top-1M-near-" + dataSet + ".train.examples.grounded").getAbsolutePath();

        groundingArguments = new String[]{
                "--programFiles",
                new File(prefix, "top-1M-near-" + dataSet + "-recursive.wam").getAbsolutePath()
                        + ":" + new File(prefix, "top-1M-near-" + dataSet + "-fact.graph").getAbsolutePath(),
//                        + ":" + new File(prefix, "smokers.cfacts").getAbsolutePath(),
                "--queries",
                new File(prefix, "top-1M-near-" + dataSet + ".train.examples").getAbsolutePath(),
                "--grounded",
                grounded,
                "--prover",
                "dpr"
        };

        trainingArguments = new String[]{
                "--train",
                grounded,
                "--params",
                new File(prefix, "top-1M-near-" + dataSet + ".wts").getAbsolutePath(),
        };

        runExperiment(dataSet, groundingArguments, trainingArguments);
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

        logger.fatal("Program {} finished running.\nGrounding time was:\t{}.\nTraining time was:\t{}.\nTotal time was:\t{}.",
                     dataSet,
                     TimeMeasure.formatNanoDifference(groundingTime),
                     TimeMeasure.formatNanoDifference(trainingTime),
                     TimeMeasure.formatNanoDifference(totalTime));
    }

    public static void grounder(double epsilon, double alpha, String[] args) {
        try {
            int inputFiles = Configuration.USE_QUERIES | Configuration.USE_PARAMS;
            int outputFiles = Configuration.USE_GROUNDED;
            int constants = Configuration.USE_WAM | Configuration.USE_THREADS | Configuration.USE_ORDER | Configuration.USE_EMPTYGRAPHS;
            int modules = Configuration.USE_GROUNDER | Configuration.USE_PROVER | Configuration.USE_SQUASHFUNCTION;

            Grounder.ExampleGrounderConfiguration c = new Grounder.ExampleGrounderConfiguration(args, inputFiles, outputFiles, constants, modules);

            c.apr.epsilon = epsilon;
            c.apr.alpha = alpha;

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
            int constants = Configuration.USE_EPOCHS | Configuration.USE_FORCE | Configuration.USE_THREADS | Configuration.USE_FIXEDWEIGHTS;
            int modules = Configuration.USE_TRAINER | Configuration.USE_SRW | Configuration.USE_SQUASHFUNCTION;
            ModuleConfiguration c = new ModuleConfiguration(args, inputFiles, outputFiles, constants, modules);

            c.apr.epsilon = epsilon;
            c.apr.alpha = alpha;

            log.info(c.toString());

            String groundedFile = c.queryFile.getPath();
            if (!c.queryFile.getName().endsWith(Grounder.GROUNDED_SUFFIX)) {
                throw new IllegalStateException("Run Grounder on " + c.queryFile.getName() + " first. Ground+Train in one go is not supported yet.");
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
            ParamVector<String, ?> params = c.trainer.train(
                    masterFeatures,
                    new ParsedFile(groundedFile),
                    new ArrayLearningGraphBuilder(),
                    c.initParamsFile,
                    c.epochs);
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
