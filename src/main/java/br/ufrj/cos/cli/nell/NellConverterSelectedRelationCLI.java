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

package br.ufrj.cos.cli.nell;

import br.ufrj.cos.cli.CommandLineInterface;
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.Predicate;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Set;

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
    @SuppressWarnings("unused")
    public Set<String> targetPredicates;

    /**
     * The main method
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CommandLineInterface instance = new NellConverterSelectedRelationCLI();
        mainProgram(instance, logger, args);
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
