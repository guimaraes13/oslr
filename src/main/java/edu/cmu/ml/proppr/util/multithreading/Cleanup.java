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

package edu.cmu.ml.proppr.util.multithreading;

import org.apache.logging.log4j.Logger;

import java.util.concurrent.*;

public abstract class Cleanup<Result> {

    private static final int LOGUPDATE_MS = 5000;
    protected int count = 0;

    public Logger getLog() {
        return null;
    }

    public Runnable cleanup(Future<Result> in, ExecutorService cleanupPool, int id) {
        return new TryCleanup(in, cleanup(in, id), cleanupPool, id);
    }

    public abstract Runnable cleanup(Future<Result> in, int id);

    public class TryCleanup implements Runnable {

        Runnable wrapped;
        ExecutorService cleanupPool;
        int id;
        Future<Result> input;

        public TryCleanup(Future<Result> in, Runnable r, ExecutorService p, int id) {
            this.input = in;
            this.wrapped = r;
            this.cleanupPool = p;
            this.id = id;
        }

        @Override
        public void run() {
            if (cleanupPool != null) {
                try {
                    Result result = input.get(50, TimeUnit.MILLISECONDS);
                } catch (TimeoutException e) {
                    // if we timeout, resubmit the job
                    if (getLog() != null && getLog().isDebugEnabled()) { getLog().debug("Rescheduling #" + id); }
                    cleanupPool.submit(this);
                    return;
                } catch (InterruptedException | ExecutionException e) { return; }
            }

            // otherwise pass to the wrapped runnable:
            wrapped.run();
        }
    }
}
