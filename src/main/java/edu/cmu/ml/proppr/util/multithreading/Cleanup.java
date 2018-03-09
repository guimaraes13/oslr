/*
 * Probabilist Logic Learner is a system to learn probabilistic logic
 * programs from data and use its learned programs to make inference
 * and answer queries.
 *
 * Copyright (C) 2018 Victor Guimarães
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
