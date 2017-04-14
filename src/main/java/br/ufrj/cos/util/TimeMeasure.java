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
 * Created on 29/03/17.
 *
 * @author Victor Guimarães
 */
public class TimeMeasure {

    public static final String HOUR_DEFAULT_FORMATTER = "%dh%dmin%dsec";

    public static final long NANO_TO_SECONDS_DENOMINATOR = 1000000000L;
    public static final int SECONDS_TO_MINUTES_DENOMINATOR = 60;
    public static final int MINUTES_TO_HOURS_DENOMINATOR = 60;

    public static long getNanoTime() {
        return System.nanoTime();
    }

    public static String formatNanoDifference(long elapsedTime, String format) {
        long elapsed = elapsedTime / NANO_TO_SECONDS_DENOMINATOR;

        int minutes = (int) (elapsed / SECONDS_TO_MINUTES_DENOMINATOR);
        int seconds = (int) (elapsed % SECONDS_TO_MINUTES_DENOMINATOR);

        int hours = minutes / MINUTES_TO_HOURS_DENOMINATOR;
        minutes = minutes % MINUTES_TO_HOURS_DENOMINATOR;

        return String.format(format, hours, minutes, seconds);
    }

    public static String formatNanoDifference(long begin, long end) {
        return formatNanoDifference(begin, end, HOUR_DEFAULT_FORMATTER);
    }

    public static String formatNanoDifference(long begin, long end, String format) {
        return formatNanoDifference(end - begin, HOUR_DEFAULT_FORMATTER);
    }

    public static String formatNanoDifference(long elapsedTime) {
        return formatNanoDifference(elapsedTime, HOUR_DEFAULT_FORMATTER);
    }

}
