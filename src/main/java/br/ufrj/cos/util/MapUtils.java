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

package br.ufrj.cos.util;

import java.util.Map;
import java.util.Set;

/**
 * Class to centralize useful method with respect with to {@link Map}s.
 * <p>
 * Created on 29/04/17.
 *
 * @author Victor Guimarães
 */
public final class MapUtils {

    private MapUtils() {
    }

    /**
     * Asserts that the {@link Map} contains a {@link Set} for the key, if it does not, create
     * and empty {@link Set} of the specified class for the given key.
     *
     * @param map   the map
     * @param clazz the class of the new set
     * @param key   the key
     * @param <K>   the key type
     * @param <V>   the {@link Set} type
     * @return the set for the key
     * @throws InstantiationException if an error occurs when instantiating a new set
     * @throws IllegalAccessException if an error occurs when instantiating a new set
     */
    public static <K, V> Set<V> assertExistsSet(Map<K, Set<V>> map, Class<? extends Set> clazz,
                                                K key) throws IllegalAccessException, InstantiationException {
        Set<V> value = map.get(key);
        if (value == null) {
            value = clazz.newInstance();
            map.put(key, value);
        }

        return value;
    }

}
