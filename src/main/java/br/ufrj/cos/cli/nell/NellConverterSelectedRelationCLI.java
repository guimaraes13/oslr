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

package br.ufrj.cos.cli.nell;

import br.ufrj.cos.cli.CommandLineInterface;
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.Predicate;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Locale;
import java.util.Set;

import static br.ufrj.cos.util.LogMessages.ERROR_MAIN_PROGRAM;
import static br.ufrj.cos.util.LogMessages.PROGRAM_END;

/**
 * Class to convert a Knowledge base from Nell's csv files to a set of logic files.
 * <p>
 * This class does the process relation by relation, only by the specified relations, using less main memory but doing
 * more access to the secondary memory.
 * <p>
 * Created on 30/07/17.
 *
 * @author Victor Guimarães
 */
public class NellConverterSelectedRelationCLI extends NellBaseConverterRelationCLI {

    private static final int NELL_ARITY = 2;

    /**
     * The target predicates to process.
     */
    public Set<String> targetPredicates;

    /**
     * The main method
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Locale.setDefault(new Locale(DEFAULT_LANGUAGE, DEFAULT_COUNTRY));
        try {
            CommandLineInterface main = new NellConverterSelectedRelationCLI();
            main = main.parseOptions(args);
            run(main, args);
        } catch (Exception e) {
            logger.error(ERROR_MAIN_PROGRAM, e);
        } finally {
            logger.fatal(PROGRAM_END);
        }
    }

    @Override
    protected void initializeTargetPredicates() throws IOException {
        super.targetPredicates = new ArrayDeque<>();
        targetPredicates.stream().map(p -> new Predicate(p, NELL_ARITY)).forEach(e -> super.targetPredicates.add(e));
    }

    @Override
    protected boolean isToProcessAtom(Pair<Atom, Boolean> pair) {
        return pair != null && pair.getLeft().getPredicate().equals(currentPredicate);
    }

}
