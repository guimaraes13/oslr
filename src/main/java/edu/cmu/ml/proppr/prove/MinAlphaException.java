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

package edu.cmu.ml.proppr.prove;

import edu.cmu.ml.proppr.prove.wam.State;

public class MinAlphaException extends IllegalStateException {

    public MinAlphaException(double minAlpha, double localAlpha, State u) {
        super("minAlpha too high! Decrease minAlpha by setting it in --apr, or use the boost or adjust alpha " +
                      "strategies in --prover. " +
                      "dpr minAlpha =" + minAlpha + " localAlpha=" + localAlpha + " for state " + u);
    }

    public MinAlphaException(String string) {
        super(string);
    }
}
