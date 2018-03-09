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
    GENERAL_ERROR("Main program error:\t"),
    ERROR_BUILD_LEARNING_SYSTEM("Error building the Learning System:\t"),
    GENERATED_RULE_NOT_SAVE("Error when generating a new rule, the generate rule can not be made safe."),
    ERROR_DURING_THEORY_COPY("Error when copying the theory."),
    ERROR_REVISING_THE_THEORY("Error when revising the theory."),
    ERROR_REVISING_THE_THEORY_REASON("Error when revising the theory, reason:\t{}"),
    ERROR_GETTING_CLASS_BY_NAME("Error when getting the class by its name."),
    @SuppressWarnings("unused") ERROR_FILE_NOT_IN_CLASS_PATH("Default configuration file not in the class path."),

    //One additional parameters to format
    ERROR_ANSWERING_QUERY("Error when answering the query {}."),
    ERROR_RESET_FIELD_NOT_ALLOWED("Reset {} is not allowed."),
    ERROR_RESET_AFTER_USE("Reset {}, after using it, is not allowed."),
    ERROR_GETTING_VARIABLE_GENERATOR_CLASS("Error when getting the variable generator class by its name:\t{}"),
    //    ERROR_CREATING_DIRECTORY("Error creating directory(is):\t{}"),
    ERROR_UNBOUNDED_RANGE_METRIC("Error the unbounded range metric {} is not by the class {}."),
    ERROR_CREATING_DIRECTORY("Error creating the directory:\t{}"),

    //Two additional parameters to format
    ERROR_APPENDING_LITERAL("Error when appending literal to initial clause, reason:\t{}"),
    ERROR_NO_YAML_FILE("Yaml configuration file is not setted."),
    FILE_NOT_EXISTS("File {} for {} does not exists."),

    INDEXES_NOT_FOUND("Indexes {} was(were) not found in the file {}.");

    public static final String LAST_FIELD_SEPARATOR = " and ";
    public static final String FIELD_SEPARATOR = ", ";

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
            message.append(LAST_FIELD_SEPARATOR);
        }
        message.append(fields.get(i));
        message.append(", at class ");
        message.append(clazz.getClass().getSimpleName());
        message.append(", must be set prior initialize.");
        return message.toString();
    }

    public static String formatList(List list) {
        if (list.isEmpty()) { return ""; }
        if (list.size() == 1) {
            return list.get(0).toString();
        } else if (list.size() == 2) {
            return list.get(0) + LAST_FIELD_SEPARATOR + list.get(1);
        }

        StringBuilder message = new StringBuilder();
        int i;
        for (i = 0; i < list.size() - 1; i++) {
            message.append(list.get(i));
            message.append(FIELD_SEPARATOR);
        }
        message.append(LAST_FIELD_SEPARATOR);
        message.append(list.get(i));
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
