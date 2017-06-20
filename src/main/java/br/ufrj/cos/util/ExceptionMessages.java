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

import java.util.List;

/**
 * Centralizes all the exception messages from the system.
 * <p>
 * Created on 29/04/17.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings({"JavaDoc", "HardCodedStringLiteral"})
public enum ExceptionMessages {
    //No additional parameters to format
    GENERATED_RULE_NOT_SAVE("Error when generating a new rule, the generate rule can not be made safe."),
    ERROR_DURING_THEORY_COPY("Error when copying the theory."),
    ERROR_GETTING_CLASS_BY_NAME("Error when getting the class by its name."),

    //One additional parameters to format
    ERROR_ANSWERING_QUERY("Error when answering the query {}."),
    ERROR_RESET_FIELD_NOT_ALLOWED("Reset {} is not allowed."),
    ERROR_GETTING_VARIABLE_GENERATOR_CLASS("Error when getting the variable generator class by its name:\t{}"),
//    ERROR_CREATING_DIRECTORY("Error creating directory(is):\t{}"),
ERROR_UNBOUNDED_RANGE_METRIC("Error the unbounded range metric {} is not by the class {}."),

    //Two additional parameters to format
    FILE_NOT_EXISTS("File {} for {} does not exists.");

    protected final String message;

    ExceptionMessages(String message) {
        this.message = message;
    }

    public static String errorFieldsSet(Object clazz, List<String> fields) {
        StringBuilder message = new StringBuilder();
        int i = 0;
        for (; i < fields.size() - 1; i++) {
            message.append(fields.get(i));
        }
        if (fields.size() > 1) {
            message.append(" and ");
        }
        message.append(fields.get(i));
        message.append(", at class ");
        message.append(clazz.getClass().getSimpleName());
        message.append(", must be set prior initialize.");
        return message.toString();
    }

    public static String errorFieldsSet(Object clazz, String field) {
        return field + ", at class " + clazz.getClass().getSimpleName() + ", must be set prior initialize.";
    }

    @Override
    public String toString() {
        return message;
    }

}
