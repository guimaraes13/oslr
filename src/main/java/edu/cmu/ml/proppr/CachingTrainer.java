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

package edu.cmu.ml.proppr;

import edu.cmu.ml.proppr.examples.PosNegRWExample;
import edu.cmu.ml.proppr.graph.GraphFormatException;
import edu.cmu.ml.proppr.graph.LearningGraphBuilder;
import edu.cmu.ml.proppr.learn.SRW;
import edu.cmu.ml.proppr.learn.tools.RWExampleParser;
import edu.cmu.ml.proppr.learn.tools.StoppingCriterion;
import edu.cmu.ml.proppr.util.StatusLogger;
import edu.cmu.ml.proppr.util.SymbolTable;
import edu.cmu.ml.proppr.util.math.ParamVector;
import edu.cmu.ml.proppr.util.multithreading.NamedThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class CachingTrainer extends Trainer {

    public static final boolean DEFAULT_SHUFFLE = true;
    private static final Logger log = LogManager.getLogger(CachingTrainer.class);
    private final boolean shuffle;

    public CachingTrainer(SRW learner, int nthreads, int throttle, boolean shuffle) {
        super(learner, nthreads, throttle);
        this.shuffle = shuffle;
    }

    @Override
    public ParamVector<String, ?> train(SymbolTable<String> masterFeatures, Iterable<String> exampleFile,
                                        LearningGraphBuilder builder, ParamVector<String, ?> initialParamVec,
                                        int numEpochs) {
        ArrayList<PosNegRWExample> examples = new ArrayList<PosNegRWExample>();
        RWExampleParser parser = new RWExampleParser();
        if (masterFeatures.size() > 0) { LearningGraphBuilder.setFeatures(masterFeatures); }
        int id = 0;
        StatusLogger stattime = new StatusLogger();
        TrainingStatistics total = new TrainingStatistics();
        boolean logged = false;
        for (String s : exampleFile) {
            total.updateReadingStatistics(stattime.sinceLast());
            id++;
            try {
                stattime.tick();
                PosNegRWExample ex = parser.parse(s, builder, masterLearner);
                total.updateParsingStatistics(stattime.sinceLast());
                examples.add(ex);
                if (status.due()) {
                    log.info("Parsed " + id + " ...");
                    logged = true;
                }
            } catch (GraphFormatException e) {
                log.error("Trouble with #" + id, e);
            }
            stattime.tick();
        }
        if (logged) { log.info("Total parsed: " + id); }
        return trainCached(examples, builder, initialParamVec, numEpochs, total);
    }

    public ParamVector<String, ?> trainCached(List<PosNegRWExample> examples, LearningGraphBuilder builder,
                                              ParamVector<String, ?> initialParamVec, int numEpochs,
                                              TrainingStatistics total) {
        ParamVector<String, ?> paramVec = this.masterLearner.setupParams(initialParamVec);
        NamedThreadFactory trainThreads = new NamedThreadFactory("work-");
        ExecutorService trainPool;
        ExecutorService cleanPool;
        StoppingCriterion stopper = new StoppingCriterion(numEpochs, this.stoppingPercent, this.stoppingEpoch);
        boolean graphSizesStatusLog = true;
        // repeat until ready to stop
        while (!stopper.satisified()) {
            // set up current epoch
            this.epoch++;
            for (SRW learner : this.learners.values()) {
                learner.setEpoch(epoch);
                learner.clearLoss();
            }
            log.info("epoch " + epoch + " ...");
            status.tick();

            // reset counters & file pointers
            this.statistics = new TrainingStatistics();
            trainThreads.reset();

            trainPool = Executors.newFixedThreadPool(this.nthreads, trainThreads);
            cleanPool = Executors.newSingleThreadExecutor();

            // run examples
            int id = 1;
            if (this.shuffle) { Collections.shuffle(examples); }
            for (PosNegRWExample s : examples) {
                Future<ExampleStats> trained = trainPool.submit(new Train(new PretendParse(s), paramVec, id, null));
                cleanPool.submit(new TraceLosses(trained, id));
                id++;
                if (log.isInfoEnabled() && status.due(1)) {
                    log.info("queued: " + id + " trained: " + statistics.exampleSetSize);
                }
            }

            cleanEpoch(trainPool, cleanPool, paramVec, stopper, id, total);
            if (graphSizesStatusLog) {
                log.info("Dataset size stats: " + statistics.totalGraphSize + " total nodes / max " + statistics
                        .maxGraphSize + " / avg " + (statistics.totalGraphSize / id));
                graphSizesStatusLog = false;
            }
        }

        log.info("Reading: " + total.readTime + " Parsing: " + total.parseTime + " Training: " + total.trainTime);
        return paramVec;
    }

    private class PretendParse implements Future<PosNegRWExample> {

        PosNegRWExample e;

        public PretendParse(PosNegRWExample e) {
            this.e = e;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public PosNegRWExample get() throws InterruptedException,
                ExecutionException {
            return this.e;
        }

        @Override
        public PosNegRWExample get(long timeout, TimeUnit unit)
                throws InterruptedException, ExecutionException,
                TimeoutException {
            return this.e;
        }

    }
}
