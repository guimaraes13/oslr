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

/**
 * Class to measure execution time of methods.
 * <p>
 * Created on 29/03/17.
 *
 * @author Victor Guimarães
 */
public class TimeMeasure {

    /**
     * The format of the timestamp.
     */
    @SuppressWarnings("SpellCheckingInspection")
    public static final String HOUR_DEFAULT_FORMATTER = "%dh%dmin%dsec";

    /**
     * A constant to divide a number in nano seconds scale and get a number in seconds scale.
     */
    public static final long NANO_TO_SECONDS_DENOMINATOR = 1000000000L;

    /**
     * A constant to divide a number in seconds scale and get a number in minutes scale.
     */
    public static final int SECONDS_TO_MINUTES_DENOMINATOR = 60;

    /**
     * A constant to divide a number in minutes scale and get a number in hours scale.
     */
    public static final int MINUTES_TO_HOURS_DENOMINATOR = 60;

    /**
     * A constant to multiply a number in seconds scale and get a number in milliseconds scale.
     */
    public static final int SECONDS_TO_MILLISECONDS_MULTIPLIER = 1000;

    /**
     * Gets the time of the System in nano time.
     *
     * @return the nano time of the System
     */
    public static long getNanoTime() {
        return System.nanoTime();
    }

    /**
     * Gets formatted timestamp from a time interval from begin to end.
     *
     * @param begin the begin of the interval
     * @param end   the end of the interval
     * @return the formatted timestamp
     */
    public static String formatNanoDifference(long begin, long end) {
        return formatNanoDifference(begin, end, HOUR_DEFAULT_FORMATTER);
    }

    /**
     * Gets formatted timestamp from a time interval from begin to end, with a given format.
     *
     * @param begin  the begin of the interval
     * @param end    the end of the interval
     * @param format the given format
     * @return the formatted timestamp
     */
    @SuppressWarnings("SameParameterValue")
    public static String formatNanoDifference(long begin, long end, String format) {
        return formatNanoDifference(end - begin, format);
    }

    /**
     * Gets formatted timestamp from a time interval of size elapsedTime, with a given format.
     *
     * @param elapsedTime the size of the interval
     * @param format      the given format
     * @return the formatted timestamp
     */
    public static String formatNanoDifference(long elapsedTime, String format) {
        long elapsed = elapsedTime / NANO_TO_SECONDS_DENOMINATOR;

        int minutes = (int) (elapsed / SECONDS_TO_MINUTES_DENOMINATOR);
        int seconds = (int) (elapsed % SECONDS_TO_MINUTES_DENOMINATOR);

        int hours = minutes / MINUTES_TO_HOURS_DENOMINATOR;
        minutes = minutes % MINUTES_TO_HOURS_DENOMINATOR;

        return String.format(format, hours, minutes, seconds);
    }

    /**
     * Gets formatted timestamp from a time interval of size elapsedTime.
     *
     * @param elapsedTime the size of the interval
     * @return the formatted timestamp
     */
    public static String formatNanoDifference(long elapsedTime) {
        return formatNanoDifference(elapsedTime, HOUR_DEFAULT_FORMATTER);
    }

}
