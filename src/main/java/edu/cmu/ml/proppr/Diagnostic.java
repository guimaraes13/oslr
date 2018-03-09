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
import edu.cmu.ml.proppr.graph.ArrayLearningGraphBuilder;
import edu.cmu.ml.proppr.graph.GraphFormatException;
import edu.cmu.ml.proppr.graph.LearningGraphBuilder;
import edu.cmu.ml.proppr.learn.SRW;
import edu.cmu.ml.proppr.learn.tools.RWExampleParser;
import edu.cmu.ml.proppr.util.Configuration;
import edu.cmu.ml.proppr.util.ModuleConfiguration;
import edu.cmu.ml.proppr.util.ParsedFile;
import edu.cmu.ml.proppr.util.StatusLogger;
import edu.cmu.ml.proppr.util.math.ParamVector;
import edu.cmu.ml.proppr.util.math.SimpleParamVector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.*;

/*
 * This class intended to be used for one-off debugging.
 * 
 * Functionality not fixed -- do not use in scripts, for experiments, etc
 */
public class Diagnostic {

    private static final Logger log = LogManager.getLogger(Diagnostic.class);

    public static void main(String[] args) {
        StatusLogger status = new StatusLogger();
        try {
            int inputFiles = Configuration.USE_TRAIN;
            int outputFiles = 0;
            int constants = Configuration.USE_THREADS | Configuration.USE_THROTTLE;
            int modules = Configuration.USE_SRW;
            ModuleConfiguration c = new ModuleConfiguration(args, inputFiles, outputFiles, constants, modules);
            log.info(c.toString());

            String groundedFile = c.queryFile.getPath();
            log.info("Parsing " + groundedFile + "...");
            long start = System.currentTimeMillis();

            final ArrayLearningGraphBuilder b = new ArrayLearningGraphBuilder();
            final SRW srw = c.srw;
            final ParamVector<String, ?> params = srw.setupParams(new SimpleParamVector<String>(new ConcurrentHashMap<String, Double>(16, (float) 0.75, 24)));
            srw.setEpoch(1);
            srw.clearLoss();
            srw.fixedWeightRules().addExact("id(restart)");
            srw.fixedWeightRules().addExact("id(trueLoop)");
            srw.fixedWeightRules().addExact("id(trueLoopRestart)");


			/* DiagSrwES: */

            ArrayList<Future<PosNegRWExample>> parsed = new ArrayList<Future<PosNegRWExample>>();
            final ExecutorService trainerPool = Executors.newFixedThreadPool(c.nthreads > 1 ? c.nthreads / 2 : 1);
            final ExecutorService parserPool = Executors.newFixedThreadPool(c.nthreads > 1 ? c.nthreads / 2 : 1);
            int i = 1;
            for (String s : new ParsedFile(groundedFile)) {
                final int id = i++;
                final String in = s;
                parsed.add(parserPool.submit(new Callable<PosNegRWExample>() {
                    @Override
                    public PosNegRWExample call() throws Exception {
                        try {
                            //log.debug("Job start "+id);
                            //PosNegRWExample ret = parser.parse(in, b.copy());
                            log.debug("Parsing start " + id);
                            PosNegRWExample ret = new RWExampleParser().parse(in, b.copy(), srw);
                            log.debug("Parsing done " + id);
                            //log.debug("Job done "+id);
                            return ret;
                        } catch (IllegalArgumentException e) {
                            System.err.println("Problem with #" + id);
                            e.printStackTrace();
                        }
                        return null;
                    }
                }));
            }
            parserPool.shutdown();
            i = 1;
            for (Future<PosNegRWExample> future : parsed) {
                final int id = i++;
                final Future<PosNegRWExample> in = future;
                trainerPool.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            PosNegRWExample ex = in.get();
                            log.debug("Training start " + id);
                            srw.trainOnExample(params, ex, status);
                            log.debug("Training done " + id);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
            trainerPool.shutdown();
            try {
                parserPool.awaitTermination(7, TimeUnit.DAYS);
                trainerPool.awaitTermination(7, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                log.error("Interrupted?", e);
            }
            /* /SrwES */

			/* SrwTtwop: 
			final ExecutorService parserPool = Executors.newFixedThreadPool(c.nthreads>1?c.nthreads/2:1);
			Multithreading<String,PosNegRWExample> m = new Multithreading<String,PosNegRWExample>(log);
			m.executeJob(c.nthreads/2, new ParsedFile(groundedFile), 
					new Transformer<String,PosNegRWExample>() {
						@Override
						public Callable<PosNegRWExample> transformer(final String in, final int id) {
							return new Callable<PosNegRWExample>() {
								@Override
								public PosNegRWExample call() throws Exception {
									try {
									//log.debug("Job start "+id);
									//PosNegRWExample ret = parser.parse(in, b.copy());
									log.debug("Parsing start "+id);
									PosNegRWExample ret = new GroundedExampleParser().parse(in, b.copy());
									log.debug("Parsing done "+id);
									//log.debug("Job done "+id);
									return ret;
									} catch (IllegalArgumentException e) {
										System.err.println("Problem with #"+id);
										e.printStackTrace();
									}
									return null;
								}};
						}}, new Cleanup<PosNegRWExample>() {
							@Override
							public Runnable cleanup(final Future<PosNegRWExample> in, final int id) {
								return new Runnable(){
									@Override
									public void run() {
										try {
											final PosNegRWExample ex = in.get();
											log.debug("Cleanup start "+id);
											trainerPool.submit(new Runnable() {
													@Override
													public void run(){
														log.debug("Training start "+id);
														srw.trainOnExample(params,ex);
														log.debug("Training done "+id);
													}
												});
										} catch (InterruptedException e) {
										    e.printStackTrace(); 
										} catch (ExecutionException e) {
										    e.printStackTrace();
										}
										log.debug("Cleanup done "+id);
									}};
							}}, c.throttle);
			trainerPool.shutdown();
			try {
				trainerPool.awaitTermination(7, TimeUnit.DAYS);
			} catch (InterruptedException e) {
				log.error("Interrupted?",e);
			}

			 /SrwTtwop */

			/* all diag tasks except SrwO: 
			Multithreading<String,PosNegRWExample> m = new Multithreading<String,PosNegRWExample>(log);
			m.executeJob(c.nthreads, new ParsedFile(groundedFile), 
					new Transformer<String,PosNegRWExample>() {
						@Override
						public Callable<PosNegRWExample> transformer(final String in, final int id) {
							return new Callable<PosNegRWExample>() {
								@Override
								public PosNegRWExample call() throws Exception {
									try {
									//log.debug("Job start "+id);
									//PosNegRWExample ret = parser.parse(in, b.copy());
									log.debug("Parsing start "+id);
									PosNegRWExample ret = new GroundedExampleParser().parse(in, b.copy());
									log.debug("Parsing done "+id);
									log.debug("Training start "+id);
									srw.trainOnExample(params,ret);
									log.debug("Training done "+id);
									//log.debug("Job done "+id);
									return ret;
									} catch (IllegalArgumentException e) {
										System.err.println("Problem with #"+id);
										e.printStackTrace();
									}
									return null;
								}};
						}}, new Cleanup<PosNegRWExample>() {
							@Override
							public Runnable cleanup(final Future<PosNegRWExample> in, final int id) {
								return new Runnable(){
									//ArrayList<PosNegRWExample> done = new ArrayList<PosNegRWExample>();
									@Override
									public void run() {
										try {
											//done.add(in.get());
											in.get();
										} catch (InterruptedException e) {
										    e.printStackTrace(); 
										} catch (ExecutionException e) {
										    e.printStackTrace();
										}
										log.debug("Cleanup start "+id);
										log.debug("Cleanup done "+id);
									}};
							}}, c.throttle);
			*/

			/* SrwO:
			   Multithreading<PosNegRWExample,Integer> m = new Multithreading<PosNegRWExample,Integer>(log);
			m.executeJob(c.nthreads, new PosNegRWExampleStreamer(new ParsedFile(groundedFile),new
			ArrayLearningGraphBuilder()),
						 new Transformer<PosNegRWExample,Integer>() {
						@Override
						public Callable<Integer> transformer(final PosNegRWExample in, final int id) {
							return new Callable<Integer>() {
								@Override
								public Integer call() throws Exception {
									try {
									//log.debug("Job start "+id);
									//PosNegRWExample ret = parser.parse(in, b.copy());
									log.debug("Training start "+id);
									srw.trainOnExample(params,in);
									log.debug("Training done "+id);
									//log.debug("Job done "+id);
									} catch (IllegalArgumentException e) {
										System.err.println("Problem with #"+id);
										e.printStackTrace();
									}
									return in.length();
								}};
						}}, new Cleanup<Integer>() {
							@Override
							public Runnable cleanup(final Future<Integer> in, final int id) {
								return new Runnable(){
									//ArrayList<PosNegRWExample> done = new ArrayList<PosNegRWExample>();
									@Override
									public void run() {
										try {
											//done.add(in.get());
											in.get();
										} catch (InterruptedException e) {
										    e.printStackTrace(); 
										} catch (ExecutionException e) {
										    e.printStackTrace();
										}
										log.debug("Cleanup start "+id);
										log.debug("Cleanup done "+id);
									}};
							}}, c.throttle);
			*/

            srw.cleanupParams(params, params);
            log.info("Finished diagnostic in " + (System.currentTimeMillis() - start) + " ms");
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(-1);
        }
    }

    private static class PosNegRWExampleStreamer implements Iterable<PosNegRWExample>, Iterator<PosNegRWExample> {

        Iterator<String> examples;
        ParamVector<String, ?> paramVec;
        LearningGraphBuilder builder;
        SRW srw;
        int id = 0;

        public PosNegRWExampleStreamer(Iterable<String> examples, LearningGraphBuilder builder) {
            this.examples = examples.iterator();
            this.builder = builder;
        }

        @Override
        public Iterator<PosNegRWExample> iterator() {
            return this;
        }

        @Override
        public boolean hasNext() {
            return examples.hasNext();
        }

        @Override
        public PosNegRWExample next() {
            String example = examples.next();
            id++;
            try {
                log.debug("Parsing start " + id);
                PosNegRWExample ret = new RWExampleParser().parse(example, builder, srw);
                log.debug("Parsing done " + id);
                return ret;
            } catch (GraphFormatException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("No removal of examples permitted during training!");
        }
    }

}
