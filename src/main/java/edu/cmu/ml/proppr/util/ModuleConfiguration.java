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

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.cmu.ml.proppr.util;

import edu.cmu.ml.proppr.CachingTrainer;
import edu.cmu.ml.proppr.Grounder;
import edu.cmu.ml.proppr.Trainer;
import edu.cmu.ml.proppr.learn.*;
import edu.cmu.ml.proppr.learn.tools.*;
import edu.cmu.ml.proppr.prove.*;
import edu.cmu.ml.proppr.util.multithreading.Multithreading;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import java.io.IOException;

public class ModuleConfiguration extends Configuration {

    public static final String SQUASHFUNCTION_MODULE_OPTION = "squashingFunction";
    private static final String SEED_CONST_OPTION = "seed";
    private static final String SRW_MODULE_OPTION = "srw";
    private static final String TRAINER_MODULE_OPTION = "trainer";
    private static final String GROUNDER_MODULE_OPTION = "grounder";
    private static final String OLD_SQUASHFUNCTION_MODULE_OPTION = "weightingScheme";
    private static final String PROVER_MODULE_OPTION = "prover";
    public Grounder<?> grounder;
    public SRW srw;
    public Trainer trainer;
    public SquashingFunction squashingFunction;
    public Prover<?> prover;

    public ModuleConfiguration(String[] args, int inputFiles, int outputFiles, int constants, int modules) {
        super(args, inputFiles, outputFiles, constants, modules);
    }

    @Override
    protected void retrieveSettings(CommandLine line, int[] allFlags, Options options) throws IOException {
        super.retrieveSettings(line, allFlags, options);

        int flags;
        // modules
        flags = modules(allFlags);

        if (isOn(flags, USE_PROVER)) {
            if (!line.hasOption(PROVER_MODULE_OPTION)) {
                // default:
                this.prover = new DprProver(apr);
            } else {
                String[] values = line.getOptionValue(PROVER_MODULE_OPTION).split(":");
                boolean proverSupportsPruning = false;
                switch (PROVERS.valueOf(values[0])) {
                    case ippr:
                        this.prover = new IdPprProver(apr);
                        break;
                    case ppr:
                        this.prover = new PprProver(apr);
                        break;
                    case dpr:
                        this.prover = new DprProver(apr);
                        break;
                    case idpr:
                        this.prover = new IdDprProver(apr);
                        break;
                    case p_idpr:
                        if (prunedPredicateRules == null) {
                            log.warn("option --" + PRUNEDPREDICATE_CONST_OPTION + " not set");
                        }
                        this.prover = new PruningIdDprProver(apr, prunedPredicateRules);
                        proverSupportsPruning = true;
                        break;
                    case qpr:
                        this.prover = new PriorityQueueProver(apr);
                        break;
                    case pdpr:
                        this.prover = new PathDprProver(apr);
                        break;
                    case dfs:
                        this.prover = new DfsProver(apr);
                        break;
                    case tr:
                        this.prover = new TracingDfsProver(apr);
                        if (this.nthreads > 1) {
                            usageOptions(options, allFlags, "Tracing prover is not multithreaded. Remove --threads " +
                                    "option or use --threads 1.");
                        }
                        break;
                    default:
                        usageOptions(options, allFlags, "No prover definition for '" + values[0] + "'");
                }
                if (prunedPredicateRules != null && !proverSupportsPruning) {
                    log.warn("option --" + PRUNEDPREDICATE_CONST_OPTION + " is ignored by this prover");
                }
                if (values.length > 1) {
                    for (int i = 1; i < values.length; i++) {
                        this.prover.configure(values[i]);
                    }
                }
            }
        }

        if (anyOn(flags, USE_SQUASHFUNCTION | USE_PROVER | USE_SRW)) {
            if (!line.hasOption(SQUASHFUNCTION_MODULE_OPTION)) {
                // default:
                this.squashingFunction = SRW.DEFAULT_SQUASHING_FUNCTION();
            } else {
                switch (SQUASHFUNCTIONS.valueOf(line.getOptionValue(SQUASHFUNCTION_MODULE_OPTION))) {
                    case linear:
                        squashingFunction = new Linear();
                        break;
                    case sigmoid:
                        squashingFunction = new Sigmoid();
                        break;
                    case tanh:
                        squashingFunction = new Tanh();
                        break;
                    case tanh1:
                        squashingFunction = new Tanh1();
                        break;
                    case ReLU:
                        squashingFunction = new ReLU();
                        break;
                    case LReLU:
                        squashingFunction = new LReLU();
                        break;
                    case exp:
                        squashingFunction = new Exp();
                        break;
                    case clipExp:
                        squashingFunction = new ClippedExp();
                        break;
                    default:
                        this.usageOptions(options, allFlags, "Unrecognized squashing function " + line.getOptionValue
                                (SQUASHFUNCTION_MODULE_OPTION));
                }
            }
        }

        if (isOn(flags, Configuration.USE_GROUNDER)) {
            if (!line.hasOption(GROUNDER_MODULE_OPTION)) {
                this.grounder = new Grounder(nthreads, Multithreading.DEFAULT_THROTTLE, apr, prover, program, plugins);
            } else {
                String[] values = line.getOptionValues(GROUNDER_MODULE_OPTION);
                int threads = nthreads;
                if (values.length > 1) { threads = Integer.parseInt(values[1]); }
                int throttle = Multithreading.DEFAULT_THROTTLE;
                if (values.length > 2) { throttle = Integer.parseInt(values[2]); }
                this.grounder = new Grounder(threads, throttle, apr, prover, program, plugins);
            }
            this.grounder.includeUnlabeledGraphs(includeEmptyGraphs);
        }
        if (isOn(flags, USE_TRAIN)) {
            this.setupSRW(line, flags, options);
            seed(line);
            if (isOn(flags, USE_TRAINER)) {
                // set default stopping criteria
                double percent = StoppingCriterion.DEFAULT_MAX_PCT_IMPROVEMENT;
                int stableEpochs = StoppingCriterion.DEFAULT_MIN_STABLE_EPOCHS;

                TRAINERS type = TRAINERS.cached;
                if (line.hasOption(TRAINER_MODULE_OPTION)) {
                    type = TRAINERS.valueOf(line.getOptionValues(TRAINER_MODULE_OPTION)[0]);
                }
                switch (type) {
                    case streaming:
                        this.trainer = new Trainer(this.srw, this.nthreads, this.throttle);
                        break;
                    case caching: //fallthrough
                    case cached:
                        boolean shuff = CachingTrainer.DEFAULT_SHUFFLE;
                        if (line.hasOption(TRAINER_MODULE_OPTION)) {
                            for (String val : line.getOptionValues(TRAINER_MODULE_OPTION)) {
                                if (val.startsWith("shuff")) {
                                    shuff = Boolean.parseBoolean(val.substring(val.indexOf("=") + 1));
                                }
                            }
                        }
                        this.trainer = new CachingTrainer(this.srw, this.nthreads, this.throttle, shuff);
                        break;
                    case adagrad:
                        this.usageOptions(options, allFlags, "Trainer 'adagrad' no longer necessary. Use '--srw " +
                                "adagrad' for adagrad descent method.");
                    default:
                        this.usageOptions(options, allFlags, "Unrecognized trainer " + line.getOptionValue
                                (TRAINER_MODULE_OPTION));
                }

                if (this.srw instanceof AdaGradSRW) {
                    stableEpochs = 2; // override default
                }

                // now get stopping criteria from command line
                if (line.hasOption(TRAINER_MODULE_OPTION)) {
                    for (String val : line.getOptionValues(TRAINER_MODULE_OPTION)) {
                        if (val.startsWith("pct")) {
                            percent = Double.parseDouble(val.substring(val.indexOf("=") + 1));
                        } else if (val.startsWith("stableEpochs")) {
                            stableEpochs = Integer.parseInt(val.substring(val.indexOf("=") + 1));
                        }
                    }
                }
                this.trainer.setStoppingCriteria(stableEpochs, percent);
            }
        }

        if (isOn(flags, USE_SRW) && this.srw == null) { this.setupSRW(line, flags, options); }
    }

    @Override
    protected void addOptions(Options options, int[] allFlags) {
        super.addOptions(options, allFlags);
        int flags;

        //modules
        flags = modules(allFlags);
        if (isOn(flags, USE_SQUASHFUNCTION)) {
            options.addOption(checkOption(
                    OptionBuilder
                            .withLongOpt(SQUASHFUNCTION_MODULE_OPTION)
                            .withArgName("w")
                            .hasArg()
                            .withDescription("Default: clipExp\n"
                                                     + "Available options:\n"
                                                     + Dictionary.buildString(SQUASHFUNCTIONS.values(), new
                                    StringBuilder(), ", "))
//							+ "linear\n"
//							+ "sigmoid\n"
//							+ "tanh\n"
//							+ "ReLU\n"
//							+ "LReLU (leaky ReLU)\n"
//							+ "exp\n"
//							+ "clipExp (clipped to e*x @x=1)")
                            .create()));
        }
        if (isOn(flags, USE_PROVER)) {
            options.addOption(checkOption(
                    OptionBuilder
                            .withLongOpt(PROVER_MODULE_OPTION)
                            .withArgName("class[:arg:...:arg]")
                            .hasArg()
                            .withDescription("Default: dpr\n"
                                                     + "Available options:\n"
                                                     + Dictionary.buildString(PROVERS.values(), new StringBuilder(),
                                                                              ", "))
//							+ "ippr\n"
//							+ "ppr\n"
//							+ "dpr\n"
//							+ "idpr\n"
//							+ "qpr\n"
//							+ "pdpr\n"
//							+ "dfs\n"
//							+ "tr")
                            .create()));
        }
        if (isOn(flags, USE_GROUNDER)) {
            options.addOption(checkOption(
                    OptionBuilder
                            .withLongOpt(GROUNDER_MODULE_OPTION)
                            .withArgName("class[:arg]")
                            .hasArgs()
                            .withValueSeparator(':')
                            .withDescription("Default: g:3\n"
                                                     + "Available options:\n"
                                                     + "g[:threads[:throttle]] (default threads=3,throttle=-1)")
                            .create()));
        }
        if (isOn(flags, USE_TRAINER)) {
            options.addOption(checkOption(
                    OptionBuilder
                            .withLongOpt(TRAINER_MODULE_OPTION)
                            .withArgName("class")
                            .hasArgs()
                            .withValueSeparator(':')
                            .withDescription("Default: cached:shuff=true:pct=1.0:stableEpochs=3\n"
                                                     + "Available trainers:\n"
                                                     + "cached[:shuff={true|false}] (faster)\n"
                                                     + "streaming                   (large dataset)\n"
                                                     + "adagrad\n"
                                                     + "Available parameters:\n"
                                                     + "pct - stopping criterion max % improvement\n"
                                                     + "stableEpochs - stopping criterion")
                            .create()));
        }
        if (isOn(flags, USE_SRW)) {
            options.addOption(checkOption(
                    OptionBuilder
                            .withLongOpt(SRW_MODULE_OPTION)
                            .withArgName("class")
                            .hasArgs()
                            .withValueSeparator(':')
                            .withDescription("Default: ppr:reg=l2:sched=global:loss=posneg\n"
                                                     + "Syntax: srw:param=value:param=value...\n"
                                                     + "Available srws: ppr,dpr,adagrad\n"
                                                     + "Available [reg]ularizers: l1,l1laplacian,l1grouplasso,l2\n"
                                                     + "Available [sched]ules: global,local\n"
                                                     + "Available [loss] functions: " + Dictionary.buildString
                                    (LOSSFUNCTIONS.values(), new StringBuilder(), ",") + "\n"
                                                     + "Other parameters:\n"
                                                     + "mu,eta,delta,zeta,affinityFile\n"
                                                     + "Default mu=.001\n"
                                                     + "Default eta=1.0")
                            .create()));
        }
        if (isOn(flags, USE_SRW)) {
            options.addOption(checkOption(
                    OptionBuilder
                            .withLongOpt(SEED_CONST_OPTION)
                            .withArgName("s")
                            .hasArg()
                            .withDescription("Seed the SRW random number generator")
                            .create()));
        }
    }

    @Override
    protected void constructUsageSyntax(StringBuilder syntax, int[] allFlags) {
        super.constructUsageSyntax(syntax, allFlags);
        int flags;

        //modules
        flags = modules(allFlags);
        if (isOn(flags, USE_PROVER)) {
            syntax.append(" [--").append(PROVER_MODULE_OPTION).append(" ippr | ppr | dpr | pdpr | dfs | tr ]");
        }
        if (isOn(flags, USE_SQUASHFUNCTION)) {
            syntax.append(" [--").append(SQUASHFUNCTION_MODULE_OPTION).append(" linear | sigmoid | tanh | ReLU | exp]");
        }
        if (isOn(flags, USE_TRAINER)) {
            syntax.append(" [--").append(TRAINER_MODULE_OPTION).append(" cached|streaming]");
        }
    }

    @Override
    public String toString() {
        String superString = super.toString();
        if (superString == null) { superString = "unknownConfigClass"; }
        StringBuilder sb = new StringBuilder(superString).append("\n");
        if (trainer != null) {
            sb.append(String.format(FORMAT_STRING, "Trainer")).append(": ").append(trainer.getClass()
                                                                                           .getCanonicalName())
                    .append("\n");
        }
        if (prover != null) {
            sb.append(String.format(FORMAT_STRING, "Prover")).append(": ").append(prover.getClass().getCanonicalName
                    ()).append("\n");
        }
        if (srw != null) {
            sb.append(String.format(FORMAT_STRING, "Walker")).append(": ").append(srw.getClass().getCanonicalName()).append("\n");
            sb.append(String.format(FORMAT_STRING, "Regularizer")).append(": ").append(srw.getRegularizer().description()).append("\n");
            sb.append(String.format(FORMAT_STRING, "Loss FunctionalSymbol")).append(": ").append(srw.getLossFunction
                    ().getClass().getCanonicalName()).append("\n");
        }
        if (squashingFunction != null) {
            sb.append(String.format(FORMAT_STRING, "Squashing function")).append(": ").append(squashingFunction
                                                                                                      .getClass()
                                                                                                      .getCanonicalName()).append("\n");
        }
        sb.append(String.format(FORMAT_STRING, "APR Alpha")).append(": ").append(apr.alpha).append("\n");
        sb.append(String.format(FORMAT_STRING, "APR Epsilon")).append(": ").append(apr.epsilon).append("\n");
        sb.append(String.format(FORMAT_STRING, "APR Depth")).append(": ").append(apr.maxDepth).append("\n");
        return sb.toString();
    }

    protected void setupSRW(CommandLine line, int flags, Options options) {
        SRWOptions sp = new SRWOptions(apr, this.squashingFunction);

        if (line.hasOption(SRW_MODULE_OPTION)) {
            String[] values = line.getOptionValues(SRW_MODULE_OPTION);
            REGULARIZERS regularizerType = REGULARIZERS.l2;
            REGULARIZERSCHEDULES scheduleType = REGULARIZERSCHEDULES.synch;
            LOSSFUNCTIONS lossType = LOSSFUNCTIONS.posneg;
            boolean namedParameters = false;
            if (values.length > 1 && values[1].contains("=")) { namedParameters = true; }

            if (namedParameters) {
                for (int i = 1; i < values.length; i++) {
                    String[] parts = values[i].split("=");
                    switch (parts[0]) {
                        case "reg":
                            regularizerType = REGULARIZERS.valueOf(parts[1]);
                            break;
                        case "sched":
                            scheduleType = REGULARIZERSCHEDULES.valueOf(parts[1]);
                            break;
                        case "loss":
                            lossType = LOSSFUNCTIONS.valueOf(parts[1]);
                            break;
                        default:
                            sp.set(parts);
                    }
                }
            } else {
                if (values.length > 1) {
                    sp.mu = Double.parseDouble(values[1]);
                }
                if (values.length > 2) {
                    sp.eta = Double.parseDouble(values[2]);
                }
                if (values.length > 3) {
                    sp.delta = Double.parseDouble(values[3]);
                }
                if (values.length > 4) {
                    sp.affinityFile = this.getExistingFile(values[4]);
                }
                if (values.length > 5) {
                    sp.zeta = Double.parseDouble(values[5]);
                }
            }

            SRWS type = SRWS.valueOf(values[0]);
            switch (type) {
                case ppr:
                    this.srw = new SRW(sp);
                    break;
                case dpr:
                    this.srw = new DprSRW(sp, DprSRW.DEFAULT_STAYPROB);
                    break;
                case adagrad:
                    this.srw = new AdaGradSRW(sp);
                    if (this.squashingFunction instanceof ReLU) {
                        log.warn("AdaGrad performs quite poorly with --squashingFunction ReLU. For better results, switch to an exp variant.");
                    }
                    break;
                default:
                    usageOptions(options, -1, -1, -1, flags, "No srw definition for '" + values[0] + "'");
            }
            Regularize reg = null;
            switch (regularizerType) {
                case l1:
                    reg = new RegularizeL1();
                    break;
                case l1laplacian:
                    reg = new RegularizeL1Laplacian();
                    break;
                case l1grouplasso:
                    reg = new RegularizeL1GroupLasso();
                    break;
                case l2:
                    reg = new RegularizeL2();
                    break;
            }
            switch (scheduleType) {
                case global:
                case synch: // fallthrough
                    this.srw.setRegularizer(new RegularizationSchedule(this.srw, reg));
                    break;
                case local:
                case lazy: // fallthrough
                    this.srw.setRegularizer(new LocalRegularizationSchedule(this.srw, reg));
                    break;
            }
            switch (lossType) {
                case posneg:
                    this.srw.setLossFunction(new PosNegLoss());
                    break;
                case normpos:
                    this.srw.setLossFunction(new NormalizedPosLoss());
                    break;
                case pair:
                    this.srw.setLossFunction(new PairwiseL2SqLoss());
                    break;
            }
        } else {
            this.srw = new SRW(sp);
            this.srw.setRegularizer(new RegularizationSchedule(this.srw, new RegularizeL2()));
        }

        if (this.fixedWeightRules != null) { this.srw.setFixedWeightRules(fixedWeightRules); }

    }

    private void seed(CommandLine line) {
        if (!line.hasOption(SEED_CONST_OPTION)) { return; }
        long seed = Long.parseLong(line.getOptionValue(SEED_CONST_OPTION));
        SRW.seed(seed);
    }

    private enum PROVERS {ippr, ppr, qpr, idpr, p_idpr, dpr, pdpr, dfs, tr}

    private enum SQUASHFUNCTIONS {linear, sigmoid, tanh, tanh1, ReLU, LReLU, exp, clipExp}

    private enum TRAINERS {cached, caching, streaming, adagrad}

    private enum SRWS {ppr, dpr, adagrad}

    private enum REGULARIZERS {l1, l1laplacian, l1grouplasso, l2}

    private enum REGULARIZERSCHEDULES {synch, global, lazy, local}

    private enum LOSSFUNCTIONS {posneg, normpos, pair}
}
