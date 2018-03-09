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

public class APROptions {

    public static final double EPS_DEFAULT = 1e-4, MINALPH_DEFAULT = 0.1;
    public static final int MAXDEPTH_DEFAULT = 20;
    public static final int TRACEDEPTH_DEFAULT = 0;
    public static final int TRACEROOT_DEFAULT = 1;
    public static final int STOPEARLY_DEFAULT = -1;
    public int maxDepth;
    public double alpha;
    public double epsilon;
    public int traceDepth, traceRoot;
    public int stopEarly;

    public APROptions(String... optionValues) {
        this();
        for (String o : optionValues) {
            this.set(o.split("="));
        }
    }

    public APROptions() {
        this(EPS_DEFAULT, MINALPH_DEFAULT, MAXDEPTH_DEFAULT, TRACEDEPTH_DEFAULT, TRACEROOT_DEFAULT, STOPEARLY_DEFAULT);
    }

    public void set(String... setting) {
        switch (names.valueOf(setting[0])) {
            case eps:
            case epsilon:
                this.epsilon = Double.parseDouble(setting[1]);
                return;
            case alph:
            case alpha:
                this.alpha = Double.parseDouble(setting[1]);
                return;
            case depth:
                this.maxDepth = Integer.parseInt(setting[1]);
                return;
            case traceDepth:
                this.traceDepth = Integer.parseInt(setting[1]);
                return;
            case traceRoot:
                this.traceRoot = Integer.parseInt(setting[1]);
                return;
            case stop:
            case stopEarly:
                this.stopEarly = Integer.parseInt(setting[1]);
                return;
            default:
                throw new IllegalArgumentException("No option to set '" + setting[0] + "'");
        }
    }

    public APROptions(double eps, double minalph, int depth, int traceDepth, int traceRoot, int stopEarly) {
        this.epsilon = eps;
        this.alpha = minalph;
        this.maxDepth = depth;
        this.traceDepth = traceDepth;
        this.traceRoot = traceRoot;
        this.stopEarly = stopEarly;
    }

    private enum names {
        eps,
        epsilon,
        alph,
        alpha,
        depth,
        stop,
        stopEarly,
        traceDepth,
        traceRoot
    }
}
