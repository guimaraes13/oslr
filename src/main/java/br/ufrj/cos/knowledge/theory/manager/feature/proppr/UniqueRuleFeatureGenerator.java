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
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.FeaturedClause;
import br.ufrj.cos.logic.Features;
import br.ufrj.cos.logic.HornClause;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.Collection;

/**
 * Creates a unique features for each rule, based on its hash.
 * <p>
 * Created on 13/10/2017.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings("unused")
public class UniqueRuleFeatureGenerator extends FeatureGenerator {

    /**
     * The default feature prefix.
     */
    public static final String DEFAULT_FEATURE_PREFIX = "w_";
    /**
     * The default feature name size.
     */
    public static final int DEFAULT_FEATURE_NAME_SIZE = 0;

    /**
     * The feature name size.
     */
    public int featureNameSize = DEFAULT_FEATURE_NAME_SIZE;

    @Override
    public HornClause createFeatureForRule(HornClause rule, Collection<? extends Example> examples) {
        final Features features = new Features(1);
        features.add(new Atom(createFeatureName(rule)));
        return new FeaturedClause(rule.getHead(), rule.getBody(), features);
    }

    /**
     * Creates the feature name.
     *
     * @param rule the rule
     * @return the feature name
     */
    protected String createFeatureName(HornClause rule) {
        String shaHex = DigestUtils.shaHex(String.valueOf(rule.hashCode()));
        if (featureNameSize > 0) {
            shaHex = shaHex.substring(0, Math.min(featureNameSize, shaHex.length()));
        }
        return DEFAULT_FEATURE_PREFIX + shaHex;
    }

}
