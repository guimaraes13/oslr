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

package edu.cmu.ml.proppr;

import edu.cmu.ml.proppr.examples.InferenceExample;
import edu.cmu.ml.proppr.learn.tools.SquashingFunction;
import edu.cmu.ml.proppr.prove.InnerProductWeighter;
import edu.cmu.ml.proppr.prove.Prover;
import edu.cmu.ml.proppr.prove.wam.*;
import edu.cmu.ml.proppr.prove.wam.plugins.WamPlugin;
import edu.cmu.ml.proppr.util.*;
import edu.cmu.ml.proppr.util.math.ParamVector;
import edu.cmu.ml.proppr.util.math.SimpleParamVector;
import edu.cmu.ml.proppr.util.multithreading.Multithreading;
import edu.cmu.ml.proppr.util.multithreading.Transformer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contains a main() which executes a series of queries against a
 * ProPPR and saves the results in an output file. Each query should
 * be a single ProPPR goal, but may include other content after a
 * <TAB> character (as in a training file).  The format of the output
 * file is one line for each query, in the following format:
 * <p>
 * # proved Q# <TAB> QUERY <TAB> TIME-IN-MILLISEC msec
 * <p>
 * followed by one line for each solution, in the format:
 * <p>
 * RANK <TAB> SCORE <TAB> VARIABLE-BINDINGS
 */

public class QueryAnswerer<P extends ProofGraph> {

    private static final Logger log = LogManager.getLogger(QueryAnswerer.class);
    private static final double MIN_FEATURE_TRANSFER = .1;
    protected WamProgram program;
    protected WamPlugin[] plugins;
    protected Prover<P> prover;
    protected APROptions apr;
    protected boolean normalize;
    protected int nthreads;
    protected int numSolutions;
    protected SymbolTable<Feature> featureTable = new ConcurrentSymbolTable<Feature>();
    protected StatusLogger status = new StatusLogger();

    public QueryAnswerer(APROptions apr, WamProgram program, WamPlugin[] plugins, Prover<P> prover, boolean normalize,
                         int threads, int topk) {
        this.apr = apr;
        this.program = program;
        this.plugins = plugins;
        this.prover = prover;
        this.normalize = normalize;
        this.nthreads = Math.max(1, threads);
        this.numSolutions = topk;
    }

    public static void main(String[] args) {
        try {
            int inputFiles = Configuration.USE_QUERIES | Configuration.USE_PARAMS;
            int outputFiles = Configuration.USE_ANSWERS;
            int modules = Configuration.USE_PROVER | Configuration.USE_SQUASHFUNCTION;
            int constants = Configuration.USE_WAM | Configuration.USE_THREADS | Configuration.USE_ORDER;
            QueryAnswererConfiguration c = new QueryAnswererConfiguration(
                    args,
                    inputFiles, outputFiles, constants, modules);
//			c.squashingFunction = new Exp();
            System.out.println(c);
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
                int m = w.seenKnownFeatures() + w.seenUnknownFeatures();
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

    public void addParams(Prover<P> prover, ParamVector<String, ?> params, SquashingFunction<Goal> f) {
        InnerProductWeighter w = InnerProductWeighter.fromParamVec(params, f);
        prover.setWeighter(w);
        for (Feature g : w.getWeights().keySet()) { this.featureTable.insert(g); }
    }

    public void findSolutions(File queryFile, File outputFile, boolean maintainOrder) throws IOException {
        Multithreading<Query, String> m = new Multithreading<Query, String>(log, status, maintainOrder);
        m.executeJob(
                this.nthreads,
                new QueryStreamer(queryFile),
                new Transformer<Query, String>() {
                    @Override
                    public Callable<String> transformer(Query in, int id) {
                        return new Answer(in, id);
                    }
                },
                outputFile,
                Multithreading.DEFAULT_THROTTLE);
    }

    public String findSolutions(WamProgram program, WamPlugin[] plugins, Prover<P> prover, Query query,
                                boolean normalize, int id) throws LogicProgramException {
        P pg = prover.makeProofGraph(new InferenceExample(query, null, null), apr, featureTable, program, plugins);
        if (log.isDebugEnabled()) { log.debug("Querying: " + query); }
        long start = System.currentTimeMillis();
        Map<State, Double> dist = getSolutions(prover, pg);
        long end = System.currentTimeMillis();
        Map<Query, Double> solutions = new TreeMap<Query, Double>();
        for (Map.Entry<State, Double> s : dist.entrySet()) {
            if (s.getKey().isCompleted()) {
                Query x = pg.fill(s.getKey());
                solutions.put(x, s.getValue());
                if (log.isDebugEnabled()) {
                    log.debug(x + "\t" + s.getValue());
                }
            } else if (log.isDebugEnabled()) {
                log.debug(s + "\t" + s.getValue());
            }
        }
        if (normalize) {
            log.debug("normalizing");
            solutions = Dictionary.normalize(solutions);
        } else {
            log.debug("not normalizing");
        }
        List<Map.Entry<Query, Double>> solutionDist = Dictionary.sort(solutions);
        if (log.isDebugEnabled()) { log.debug("Writing " + solutionDist.size() + " solutions..."); }
        StringBuilder sb = new StringBuilder("# proved ").append(String.valueOf(id)).append("\t").append(query)
                .append("\t").append((end - start) + " msec\n");
        int rank = 0;
        double lastScore = 0;
        int displayrank = 0;
        for (Map.Entry<Query, Double> soln : solutionDist) {
            ++rank;
            if (soln.getValue() != lastScore) { displayrank = rank; }
            if (numSolutions > 0 && rank > numSolutions) { break; }
            sb.append(displayrank + "\t").append(soln.getValue()).append("\t").append(soln.getKey()).append("\n");
            lastScore = soln.getValue();
        }
        return sb.toString();
    }

    public Map<State, Double> getSolutions(Prover<P> prover, P pg) throws LogicProgramException {
        return prover.prove(pg, status);
    }

    //////////////////////// Multithreading scaffold ///////////////////////////

    public static class QueryAnswererConfiguration extends ModuleConfiguration {

        public boolean normalize;
        public int topk;

        public QueryAnswererConfiguration(String[] args, int inputFiles, int outputFiles, int constants, int modules) {
            super(args, inputFiles, outputFiles, constants, modules);
        }

        @Override
        protected void retrieveSettings(CommandLine line, int[] flags, Options options) throws IOException {
            super.retrieveSettings(line, flags, options);
            this.normalize = !line.hasOption("unnormalized");
            if (!line.hasOption(Configuration.QUERIES_FILE_OPTION)) {
                usageOptions(options, flags, "Missing required option: " + Configuration.QUERIES_FILE_OPTION);
            }
            this.topk = -1;
            if (line.hasOption("top")) { this.topk = Integer.parseInt(line.getOptionValue("top")); }
        }

        @Override
        protected void addOptions(Options options, int[] flags) {
            super.addOptions(options, flags);
            options.addOption(
                    OptionBuilder
                            .withLongOpt("unnormalized")
                            .withDescription("Show unnormalized scores for answers")
                            .create());
            options.addOption(
                    OptionBuilder
                            .withLongOpt("top")
                            .withArgName("k")
                            .hasArg()
                            .withDescription("Print only the top k solutions for each query")
                            .create());
        }
    }

    /**
     * Transforms from inputs to outputs
     *
     * @author "Kathryn Mazaitis <krivard@cs.cmu.edu>"
     */
    private class Answer implements Callable<String> {

        Query query;
        int id;

        public Answer(Query query, int id) {
            this.query = query;
            this.id = id;
        }

        @Override
        public String call() throws Exception {
            try {
                return findSolutions(program, plugins, prover.copy(), query, normalize, id);
            } catch (LogicProgramException e) {
                throw new LogicProgramException("on query " + id, e);
            }
        }
    }

    private class QueryStreamer implements Iterable<Query>, Iterator<Query> {

        ParsedFile reader;

        public QueryStreamer(File queryFile) {
            reader = new ParsedFile(queryFile);
        }

        @Override
        public boolean hasNext() {
            return reader.hasNext();
        }

        @Override
        public Query next() {
            String queryString = reader.next().split("\t")[0];
            Query query = Query.parse(queryString);
            return query;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Can't remove from a file");
        }

        @Override
        public Iterator<Query> iterator() {
            return this;
        }

    }
}
