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

package edu.cmu.ml.proppr.util.math;

import java.util.Arrays;

/**
 * Encodes dense vectors V of unknown size, with constant-time access
 * to the components V[k].  Unlike an array, the maximum dimension
 * doesn't need to be specified in advance - the underlying array will
 * be resized as needed.
 *
 * @author wcohen
 */

public class LongDense {

    public static void main(String[] argv) throws Exception {
        LongDense.FloatVector v = new LongDense.FloatVector();
        for (int i = 0; i < argv.length; i += 2) {
            int k = Integer.parseInt(argv[i]);
            double d = Double.parseDouble(argv[i + 1]);
            v.inc(k, d);
        }
        for (int i = 0; i < v.size(); i++) {
            System.out.println(i + ":\t" + v.get(i));
        }
    }

    /**
     * A vector of floats.
     **/

    public abstract static class AbstractFloatVector {

        /**
         * Get the k-th component of the vector.
         * I.e., v.get(k) conceptually returns V[k]
         */
        public abstract float get(int k);

        @Override
        public abstract String toString();

        public abstract boolean definesIndex(int i);
    }

    public static class UnitVector extends AbstractFloatVector {

        /**
         * A unit vector has value 1.0 at every component.
         * todo: should probably be called Ones, not UnitVector
         */
        @Override
        public float get(int k) {
            return 1.0f;
        }

        @Override
        public String toString() {
            return "1.0...";
        }

        @Override
        public boolean definesIndex(int i) {
            return true;
        }
    }

    /**
     * A float vector in which arbitrary floats can be stored.
     */
    public static class FloatVector extends AbstractFloatVector {

        public float[] val;
        int maxIndex = -1;  // largest index actually used
        float dflt; // when asked for a value out of bounds

        public FloatVector() {
            this(10);
        }

        public FloatVector(int sizeHint) {
            this(new float[sizeHint], -1);
        }

        public FloatVector(float[] v, int mi) {
            this(v, mi, 0.0f);
        }

        public FloatVector(float[] v, int mi, float dflt) {
            this.val = v;
            this.maxIndex = mi;//this.val.length-1;
            this.dflt = dflt;
        }

        /**
         * The size of the smallest float[] array
         * that could store this information.
         */
        public int size() {
            return maxIndex + 1;
        }

        /**
         * Set all components to zero.
         */
        public void clear() {
            for (int k = 0; k <= maxIndex; k++) {
                val[k] = 0;
            }
        }

        /**
         * Increment V[k] by delta
         **/
        public void inc(int k, double delta) {
            growIfNeededTo(k);
            val[k] += delta;
            maxIndex = Math.max(maxIndex, k);
        }

        @Override
        public boolean definesIndex(int i) {
            return i <= maxIndex;
        }

        /**
         * Set V[k] to v
         **/
        public void set(int k, double v) {
            growIfNeededTo(k);
            val[k] = (float) v;
            maxIndex = Math.max(maxIndex, k);
        }

        /**
         * Return V[k]
         **/
        @Override
        public float get(int k) {
            growIfNeededTo(k);
            return val[k];
        }

        private void growIfNeededTo(int k) {
            // resize the underlying array if needed
            if (val.length <= k) {
                int n = Math.max(2 * val.length, k + 1);
                float[] tmp = new float[n];
                System.arraycopy(val, 0, tmp, 0, maxIndex + 1);
                Arrays.fill(tmp, maxIndex + 1, n, this.dflt);
                val = tmp;
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i <= maxIndex; i++) { sb.append("\n").append(val[i]); }
            return sb.substring(1);
        }
    }

    /**
     * A vector of arbitrary objects.
     **/
    public static class ObjVector<T> {

        public Object[] val = new Object[10];
        int maxIndex = 0;

        /**
         * The size of the smallest array that could store this
         * information.
         */

        public int size() {
            return maxIndex + 1;
        }

        /**
         * Return V[k]
         **/
        public T get(int k) {
            growIfNeededTo(k);
            return (T) val[k];
        }

        /**
         * Resize the underlying storage as needed
         **/
        private void growIfNeededTo(int k) {
            if (val.length <= k) {
                int n = Math.max(2 * val.length, k + 1);
                Object[] tmp = new Object[n];
                System.arraycopy(val, 0, tmp, 0, maxIndex + 1);
                val = tmp;
            }
        }

        /**
         * Store newval in V[k]
         **/
        public void set(int k, T newval) {
            growIfNeededTo(k);
            val[k] = newval;
            maxIndex = Math.max(maxIndex, k);
        }
    }
}
