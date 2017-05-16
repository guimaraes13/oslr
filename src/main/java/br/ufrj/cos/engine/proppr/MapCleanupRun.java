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

import br.ufrj.cos.util.LogMessages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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
        } catch (InterruptedException | ExecutionException e) {
            logger.trace(LogMessages.ERROR_PROVING_GOAL.toString(), id);
        }
    }

}
