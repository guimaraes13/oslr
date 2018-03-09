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

package edu.cmu.ml.proppr.learn;

import edu.cmu.ml.proppr.examples.PosNegRWExample;
import edu.cmu.ml.proppr.learn.tools.LossData;
import edu.cmu.ml.proppr.util.SRWOptions;
import edu.cmu.ml.proppr.util.math.ParamVector;
import gnu.trove.map.TIntDoubleMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;

public abstract class LossFunction {

    protected static final double BOUND = 1.0e-15; //Prevent infinite log loss.
    protected static final Logger log = LogManager.getLogger(SRW.class);

    public double clip(double prob) {
        if (prob <= 0) { return BOUND; }
        return prob;
    }

    public abstract int computeLossGradient(ParamVector params, PosNegRWExample example, TIntDoubleMap gradient,
                                            LossData lossdata, SRWOptions c);

    @Override
    protected LossFunction clone() throws CloneNotSupportedException {
        Class<? extends LossFunction> clazz = this.getClass();
        try {
            LossFunction copy = clazz.getConstructor().newInstance();
            return copy;
        } catch (InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
        throw new CloneNotSupportedException("Programmer error in LossDate subclass " + clazz.getName()
                                                     + ": Must provide the standard LossData constructor signature, or else override clone()");
    }

}
