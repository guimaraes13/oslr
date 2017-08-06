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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to measure time between point in the code.
 * <p>
 * Created on 05/08/17.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings("unused")
public class TimeMeasure {

    /**
     * The first time stamp.
     */
    public static final String FIRST_TIME_STAMP = "BEGIN";
    /**
     * The begin index.
     */
    public static final int BEGIN_INDEX = 0;

    protected final List<Long> timeStamps;
    protected final Map<String, Integer> stampsByName;
    protected int lastIndex = 0;

    /**
     * Gets a instance from the time starting with the first measure called {@link #FIRST_TIME_STAMP}.
     *
     * @return a new instance of the time measure
     */
    public static TimeMeasure startTimeMeasure() {
        return new TimeMeasure();
    }

    /**
     * Default constructor.
     */
    protected TimeMeasure() {
        this.timeStamps = new ArrayList<>();
        this.stampsByName = new HashMap<>();

        measure(FIRST_TIME_STAMP);
    }

    /**
     * Adds a time stamp with the name.
     *
     * @param name the name of the time stamp.
     * @return return the stamp index
     */
    public int measure(String name) {
        lastIndex = timeStamps.size();
        stampsByName.put(name, lastIndex);
        timeStamps.add(TimeUtils.getNanoTime());
        return lastIndex;
    }

    /**
     * Adds a nameless time stamp.
     *
     * @return return the stamp index
     */
    public int measure() {
        lastIndex = timeStamps.size();
        timeStamps.add(TimeUtils.getNanoTime());
        return lastIndex;
    }

    /**
     * Returns the elapsed time until the addition of the last stamp, i.e. the call to {@link #measure(String)}. The
     * time is represented in nano seconds, and work properly with {@link TimeUtils} standards.
     *
     * @return the time in nano seconds
     */
    public long elapsedTimeUntilLastStamp() {
        return elapsedTimeUntilStamp(lastIndex);
    }

    /**
     * Returns the elapsed time until the addition of the stamp with the given index.
     * The time is represented in nano seconds, and work properly with {@link TimeUtils} standards.
     *
     * @param index the index of the stamp
     * @return the time in nano seconds
     */
    public long elapsedTimeUntilStamp(int index) {
        return TimeUtils.getNanoTime() - timeStamps.get(index);
    }

    /**
     * Returns the elapsed time until the addition of the stamp with the given game.
     * The time is represented in nano seconds, and work properly with {@link TimeUtils} standards.
     *
     * @param stamp the name of the stamp
     * @return the time in nano seconds
     */
    public long elapsedTimeUntilStamp(String stamp) {
        final int stampIndex = stampsByName.getOrDefault(stamp, BEGIN_INDEX);
        return elapsedTimeUntilStamp(stampIndex);
    }

    /**
     * Returns the elapsed time between the first and the last stamp.
     * The time is represented in nano seconds, and work properly with {@link TimeUtils} standards.
     *
     * @return the time in nano seconds
     */
    public long elapsedTimeFirstAndLastStamp() {
        return elapsedTimeBetweenStamps(BEGIN_INDEX, lastIndex);
    }

    /**
     * Returns the elapsed time between two stamps.
     * The time is represented in nano seconds, and work properly with {@link TimeUtils} standards.
     *
     * @param stamp0 the index of the stamp 0
     * @param stamp1 the index of the stamp 1
     * @return the time in nano seconds
     */
    public long elapsedTimeBetweenStamps(int stamp0, int stamp1) {
        return timeStamps.get(stamp1) - timeStamps.get(stamp0);
    }

    /**
     * Returns the elapsed time between two stamps.
     * The time is represented in nano seconds, and work properly with {@link TimeUtils} standards.
     *
     * @param stamp0 the index of the stamp 0
     * @param stamp1 the index of the stamp 1
     * @return the time in nano seconds
     */
    public long elapsedTimeBetweenStamps(String stamp0, String stamp1) {
        return timeStamps.get(stampsByName.getOrDefault(stamp1, lastIndex)) -
                timeStamps.get(stampsByName.getOrDefault(stamp0, BEGIN_INDEX));
    }

    /**
     * Returns the elapsed time between the given stamp and the last saved one.
     * The time is represented in nano seconds, and work properly with {@link TimeUtils} standards.
     *
     * @param stamp the index of the stamp
     * @return the time in nano seconds
     */
    public long elapsedTimeFromIndexToLastStamp(int stamp) {
        return elapsedTimeBetweenStamps(stamp, lastIndex);
    }

    /**
     * Returns the elapsed time between the given stamp and the last saved one.
     * The time is represented in nano seconds, and work properly with {@link TimeUtils} standards.
     *
     * @param stamp the index of the stamp
     * @return the time in nano seconds
     */
    public long elapsedTimeFromIndexToLastStamp(String stamp) {
        return elapsedTimeBetweenStamps(stampsByName.getOrDefault(stamp, BEGIN_INDEX), lastIndex);
    }

    @Override
    public String toString() {
        return TimeUtils.formatNanoDifference(elapsedTimeFirstAndLastStamp());
    }

}
