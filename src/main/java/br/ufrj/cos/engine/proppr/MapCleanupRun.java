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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static br.ufrj.cos.util.log.InferenceLog.ERROR_PROVING_GOAL;

/**
 * Implements the {@link Runnable} to put into the {@link Map}.
 * <p>
 * Created on 05/05/17.
 *
 * @author Victor Guimarães
 */
public class MapCleanupRun<Result> implements Runnable {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    protected final Map<Integer, Result> integerMap;
    protected final Future<Result> input;
    protected final Integer id;

    /**
     * Constructor with the minimal parameters.
     *
     * @param integerMap the {@link Map}
     * @param input      the input {@link Result}
     * @param id         the input's id
     */
    public MapCleanupRun(Map<Integer, Result> integerMap, Future<Result> input, Integer id) {
        this.integerMap = integerMap;
        this.input = input;
        this.id = id;
    }

    @Override
    public void run() {
        try {
            integerMap.put(id, input.get());
        } catch (InterruptedException | ExecutionException ignored) {
            logger.trace(ERROR_PROVING_GOAL.toString(), id);
        }
    }

}
