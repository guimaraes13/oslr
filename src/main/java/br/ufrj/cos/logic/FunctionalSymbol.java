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

package br.ufrj.cos.logic;

import br.ufrj.cos.util.LanguageUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.function.Function;

/**
 * Represents a Functional symbol.
 * <p>
 * Created on 19/05/18.
 *
 * @author Victor Guimarães
 */
public class FunctionalSymbol extends Clause implements Function<String[], Boolean> {

    /**
     * The script engine name.
     */
    public static final String ENGINE_NAME = "nashorn";
    /**
     * The java function format.
     */
    public static final String JAVA_FUNCTION_FORMAT = "new java.util.function.Function(function(a) %s)";
    /**
     * The compiler engine
     */
    public static final ScriptEngine COMPILER_ENGINE = new ScriptEngineManager().getEngineByName(ENGINE_NAME);

    protected final Predicate head;
    protected final String function;
    protected Function<String[], Boolean> compiledFunction;

    /**
     * Constructs a {@link FunctionalSymbol}
     *
     * @param head     the head
     * @param function the function
     * @throws ScriptException if an error occurs during the compilation of the function.
     */
    public FunctionalSymbol(Predicate head, String function) throws ScriptException {
        this.head = head;
        this.function = function;
        this.compiledFunction = compileFunction(function);
    }

    /**
     * Compiles a {@link Function} from a string.
     *
     * @param function string function
     * @return a compiled {@link Function} from a string
     * @throws ScriptException if an error occurs during the compilation of the function.
     */
    public static Function<String[], Boolean> compileFunction(String function) throws ScriptException {
        //noinspection unchecked
        return (Function<String[], Boolean>) COMPILER_ENGINE.eval(String.format(JAVA_FUNCTION_FORMAT, function));
    }

    @Override
    public boolean isGrounded() {
        return false;
    }

    @Override
    public boolean isFact() {
        return false;
    }

    @Override
    public int hashCode() {
        int result = head.hashCode();
        result = 31 * result + compiledFunction.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof FunctionalSymbol)) { return false; }

        FunctionalSymbol that = (FunctionalSymbol) o;

        if (!head.equals(that.head)) { return false; }
        return compiledFunction.equals(that.compiledFunction);
    }

    @Override
    public String toString() {
        return LanguageUtils.formatFunctionalSymbol(this);
    }

    /**
     * Gets the read of the {@link FunctionalSymbol}
     *
     * @return the head
     */
    public Predicate getHead() {
        return head;
    }

    /**
     * Gets the function string representation of the {@link FunctionalSymbol}
     *
     * @return the function string representation
     */
    public String getFunction() {
        return function;
    }

    @Override
    public Boolean apply(String[] strings) {
        return compiledFunction.apply(strings);
    }

}
