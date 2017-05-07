/*
 * Probabilist Logic Learner is a system to learn probabilistic logic
 * programs from data and use its learned programs to make inference
 * and answer queries.
 *
 * Copyright (C) 2017 Victor Guimarães
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

    protected Map<Integer, Result> resultMap = new ConcurrentHashMap<>();

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
