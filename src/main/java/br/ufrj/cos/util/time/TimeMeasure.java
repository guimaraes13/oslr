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

package br.ufrj.cos.util.time;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Class to measure time between point in the code.
 * <p>
 * Created on 05/08/17.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings("unused")
public class TimeMeasure<T> {

    /**
     * The begin index.
     */
    public static final int BEGIN_INDEX = 0;

    /**
     * The time stamps.
     */
    public List<Long> timeStamps;
    /**
     * The stamps tags.
     */
    public Map<T, Integer> stampsByName;
    /**
     * The index of the last saved stamp.
     */
    public int lastIndex = BEGIN_INDEX;
    /**
     * If this class is finished to saving stamps.
     */
    public boolean isEnded;

    /**
     * Default constructor.
     */
    public TimeMeasure() {
        this.timeStamps = new ArrayList<>();
        this.stampsByName = new LinkedHashMap<>();
        this.isEnded = false;
    }

    /**
     * Converts this time measure of type {@link T} to a time measure of type {@link R}.
     *
     * @param function the function of {@link T} to {@link R}
     * @param <R>      the output type of the {@link TimeMeasure}
     * @return the {@link TimeMeasure} of type {@link R}
     */
    public <R> TimeMeasure<R> convertTimeMeasure(Function<T, R> function) {
        TimeMeasure<R> timeMeasure = new TimeMeasure<>();
        timeMeasure.timeStamps.addAll(this.timeStamps);
        this.stampsByName.forEach((key, value) -> timeMeasure.stampsByName.put(function.apply(key), value));
        timeMeasure.isEnded = this.isEnded;
        timeMeasure.lastIndex = this.lastIndex;
        return timeMeasure;
    }

    /**
     * Returns the elapsed time until the addition of the last stamp, i.e. the call to {@link #measure(T)}. The
     * time is represented in nano seconds, and work properly with {@link TimeUtils} standards.
     *
     * @return the time in nano seconds
     */
    public long timeUntilLastStamp() {
        return timeUntilStamp(lastIndex);
    }

    /**
     * Returns the elapsed time until the addition of the stamp with the given index.
     * The time is represented in nano seconds, and work properly with {@link TimeUtils} standards.
     *
     * @param index the index of the stamp
     * @return the time in nano seconds
     */
    public long timeUntilStamp(int index) {
        return TimeUtils.getNanoTime() - timeStamps.get(index);
    }

    /**
     * Returns the elapsed time until the addition of the stamp with the given game.
     * The time is represented in nano seconds, and work properly with {@link TimeUtils} standards.
     *
     * @param stamp the tag of the stamp
     * @return the time in nano seconds
     */
    public long timeUntilStamp(T stamp) {
        final int stampIndex = stampsByName.getOrDefault(stamp, BEGIN_INDEX);
        return timeUntilStamp(stampIndex);
    }

    /**
     * Returns the elapsed time between two stamps, formatted with {@link TimeUtils}.
     * The time is represented in nano seconds, and work properly with {@link TimeUtils} standards.
     *
     * @param stamp0 the tag of the stamp 0
     * @param stamp1 the tag of the stamp 1
     * @return the time in nano seconds
     */
    public String textTimeBetweenStamps(T stamp0, T stamp1) {
        return TimeUtils.formatNanoDifference(timeBetweenStamps(stamp0, stamp1));
    }

    /**
     * Returns the elapsed time between two stamps.
     * The time is represented in nano seconds, and work properly with {@link TimeUtils} standards.
     *
     * @param stamp0 the tag of the stamp 0
     * @param stamp1 the tag of the stamp 1
     * @return the time in nano seconds
     */
    public long timeBetweenStamps(T stamp0, T stamp1) {
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
    public long timeFromIndexToLastStamp(int stamp) {
        return timeBetweenStamps(stamp, lastIndex);
    }

    /**
     * Returns the elapsed time between two stamps.
     * The time is represented in nano seconds, and work properly with {@link TimeUtils} standards.
     *
     * @param stamp0 the index of the stamp 0
     * @param stamp1 the index of the stamp 1
     * @return the time in nano seconds
     */
    public long timeBetweenStamps(int stamp0, int stamp1) {
        return timeStamps.get(stamp1) - timeStamps.get(stamp0);
    }

    /**
     * Returns the elapsed time between the given stamp and the last saved one.
     * The time is represented in nano seconds, and work properly with {@link TimeUtils} standards.
     *
     * @param stamp the tag of the stamp
     * @return the time in nano seconds
     */
    public long timeFromIndexToLastStamp(T stamp) {
        return timeBetweenStamps(stampsByName.getOrDefault(stamp, BEGIN_INDEX), lastIndex);
    }

    /**
     * Adds the last time stamp and returns its index. After calling this method, no more timestamps will be accept.
     *
     * @param lastTag the last tag
     * @return the index of the last time stamp.
     */
    public int endMeasure(T lastTag) {
        final int index = measure(lastTag);
        isEnded = true;
        return index;
    }

    /**
     * Adds a time stamp with the tag.
     *
     * @param tag the tag of the time stamp.
     * @return return the stamp index
     */
    public int measure(T tag) {
        final int index = measure();
        stampsByName.put(tag, index);
        return index;
    }

    /**
     * Adds a nameless time stamp.
     *
     * @return return the stamp index
     */
    public int measure() {
        if (isEnded) { return lastIndex; }
        lastIndex = timeStamps.size();
        timeStamps.add(TimeUtils.getNanoTime());
        return lastIndex;
    }

    @Override
    public String toString() {
        return TimeUtils.formatNanoDifference(timeFirstAndLastStamp());
    }

    /**
     * Returns the elapsed time between the first and the last stamp.
     * The time is represented in nano seconds, and work properly with {@link TimeUtils} standards.
     *
     * @return the time in nano seconds
     */
    public long timeFirstAndLastStamp() {
        return timeBetweenStamps(BEGIN_INDEX, lastIndex);
    }

}
