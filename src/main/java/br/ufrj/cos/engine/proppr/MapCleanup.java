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

package br.ufrj.cos.engine.proppr;

import edu.cmu.ml.proppr.util.multithreading.Cleanup;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * Class to gather a {@link Cleanup}'s result into a {@link Map}.
 * <p>
 * Created on 05/05/17.
 *
 * @author Victor Guimarães
 */
public class MapCleanup<Result> extends Cleanup<Result> {

    protected final Map<Integer, Result> resultMap = new ConcurrentHashMap<>();

    @Override
    public Runnable cleanup(Future<Result> in, int id) {
        return new MapCleanupRun<>(resultMap, in, id);
    }

    /**
     * Gets the {@link Map} of {@link Result} with the ids as keys.
     *
     * @return the {@link Map} of {@link Result}
     */
    public Map<Integer, Result> getResultMap() {
        return resultMap;
    }

}
