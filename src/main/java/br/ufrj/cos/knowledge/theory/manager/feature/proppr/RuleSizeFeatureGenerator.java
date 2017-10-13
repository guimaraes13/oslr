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

package br.ufrj.cos.knowledge.theory.manager.feature.proppr;

import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.theory.manager.feature.FeatureGenerator;
import br.ufrj.cos.logic.*;

import java.util.Collection;

/**
 * Creates features for the rules, based on their body sizes.
 * <p>
 * Created on 13/10/2017.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings("unused")
public class RuleSizeFeatureGenerator extends FeatureGenerator {

    @Override
    public HornClause createFeatureForRule(HornClause rule, Collection<? extends Example> examples) {
        final Features features = new Features(1);
        features.add(new Atom(new Predicate(rule.getHead().getPredicate().getName() + rule.getBody().size())));
        return new FeaturedClause(rule.getHead(), rule.getBody(), features);
    }

}
