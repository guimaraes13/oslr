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

import java.util.Map;
import java.util.TreeMap;

/**
 * Lightweight implementations of sparse vectors and matrices.
 *
 * @author wcohen
 */

public class SimpleSparse {

    /**
     * A vector of floats, internally represented as two parallel
     * arrays, one (index) with the indices of the non-zero values, and
     * one (val) with the corresponding values.
     * <p>
     * Conceptually this represents a vector V for for all i,
     * V[vec.index[i]] == vec.val[i]
     * <p>
     * A typical use would be to * iterate over the non-zero values like
     * this:
     * <p>
     * for (int i=0; i<vec.index.length; i++)
     * doSomethingWith(index[i],vec.val[i]);
     */
    public static class FloatVector {

        public int[] index;
        public float[] val;

        public FloatVector(int[] index, float[] val) {
            this.index = index.clone();
            this.val = val.clone();
        }

        /**
         * Create a float vector that can store n
         * non-zero values.
         **/
        public FloatVector(int n) {
            this.index = new int[n];
            this.val = new float[n];
        }

        /**
         * Return the dot product with a second vector
         * which is a LongDense.FloatVector
         */
        public double dot(LongDense.AbstractFloatVector vec, float dflt) {
            double result = 0.0;
            for (int i = 0; i < index.length; i++) {
                result += val[i] * (vec.definesIndex(index[i]) ? vec.get(index[i]) : dflt);
            }
            return result;
        }
    }

    /**
     * A sparse matrix of floats.  The internal representation is
     * analgous to a FloatVector except that M[mat.index[i]] is a row of
     * the matrix which contains non-zeros, encoded as a
     * SimpleSparse.FloatVector.
     * <p>
     * A typical use would be
     * <p>
     * for (int i=0; i<mat.index.length; i++) {
     * int r = mat.index[i];
     * SimpleSparse.FloatVector row = mat.val[i];
     * for (int j=0; j<row.index.length; j++) {
     * int c = row.index[i];
     * float M_rc = row[val];
     * // M_rc is value of mat[r][c]
     * doSomething(...);
     * }
     * }
     */
    public static class FloatMatrix {

        public int[] index;
        public FloatVector[] val;

        public FloatMatrix(int[] index, FloatVector[] val) {
            this.index = index.clone();
            this.val = val.clone();
        }

        /**
         * A FloatMatrix that can store n non-zero rows
         **/
        public FloatMatrix(int n) {
            this.index = new int[n];
            this.val = new FloatVector[n];
        }

        /**
         * Sort the index and value arrays so that Arrays.binarysearch(r,
         * mat.index) will quickly return the location in mat.index[] of
         * row r.
         */
        public void sortIndex() {
            TreeMap<Integer, FloatVector> buf = new TreeMap<Integer, FloatVector>();
            for (int i = 0; i < index.length; i++) {
                buf.put(index[i], val[i]);
            }
            int k = 0;
            for (Map.Entry<Integer, FloatVector> e : buf.entrySet()) {
                index[k] = e.getKey().intValue();
                val[k] = e.getValue();
                k++;
            }
        }
    }
}
