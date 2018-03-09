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
    public static final int DEFAULT_FEATURE_NAME_SIZE = 4;

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
