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

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.cmu.ml.proppr.prove.wam;

import java.util.LinkedList;
import java.util.List;

/**
 * Headless version for queries.
 *
 * @author "Kathryn Mazaitis <krivard@cs.cmu.edu>"
 */
public class Query extends Rule implements Comparable<Query> {

    public Query(Goal... rhs) {
        this(rhs, new Goal[0], new Goal[0]);
    }

    protected Query(Goal[] rhs, Goal[] features, Goal[] findall) {
        super(null, rhs, features, findall);
    }

    /**
     * Input a string in prolog form, e.g. "predict(doc1234,X)"; output a Query object.
     * <p>
     * Note that queries can have multiple goals! "predict(doc1234,X),predict(doc5678,Y)" is a valid query.
     *
     * @param string
     * @return
     */
    public static Query parse(String string) {
        char[] array = string.toCharArray();
        LinkedList<Goal> goals = new LinkedList<Goal>();
        int cursor = 0;
        while (cursor < array.length) {
            cursor = goal(array, cursor, goals);
        }
        return new Query(goals.toArray(new Goal[0]));

    }

    private static int goal(char[] array, int cursor, List<Goal> goals) {
        StringBuilder functor = new StringBuilder();
        cursor = functor(array, cursor, functor);
        if (functor.length() == 0) { return cursor; }
        Argument[] arguments = new Argument[0];
        if (cursor < array.length && array[cursor] != ',') {
            LinkedList<Argument> arglist = new LinkedList<Argument>();
            while (cursor < array.length && array[cursor] != ')') {
                //add another argument
                StringBuilder argument = new StringBuilder();
                cursor = argument(array, cursor, argument);
                if (argument.length() == 0) { continue; }
                arglist.add(new ConstantArgument(argument.toString()));
            }
            arguments = arglist.toArray(arguments);
        }
        goals.add(new Goal(functor.toString(), arguments));
        return cursor;
    }

    private static int functor(char[] array, int cursor, StringBuilder functor) {
        for (int i = cursor; i < array.length; i++) {
            char c = array[i];
            switch (c) {
                case ' ':
                    continue;
                case ',': //fallthrough
                case '(':
                    return i + 1;
                default:
                    functor.append(c);
            }
        }
        return array.length;
    }

    private static int argument(char[] array, int cursor, StringBuilder argument) {
        for (int i = cursor; i < array.length; i++) {
            char c = array[i];
            switch (c) {
                case ',': //fallthrough
                case ')':
                    return i + 1;
                case ' ':
                    if (argument.length() == 0) {
                        continue; //fallthrough
                    }
                default:
                    argument.append(c);
            }
        }
        return array.length;
    }

    public static Goal parseGoal(String string) {
        char[] array = string.toCharArray();
        LinkedList<Goal> goals = new LinkedList<Goal>();
        if (goal(array, 0, goals) != array.length) {
            throw new IllegalArgumentException("Bad syntax for goal " + string);
        }
        return goals.getFirst();
    }

    @Override
    public int compareTo(Query o) {
        for (int i = 0; i < this.body.length; i++) {
            if (i >= o.body.length) { return -1; }
            int j = this.body[i].compareTo(o.body[i]);
            if (j != 0) { return j; }
        }
        return 0;
    }

}
