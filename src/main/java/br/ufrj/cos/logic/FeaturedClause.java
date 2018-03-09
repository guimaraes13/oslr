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

/**
 * Represents a featured clause. A clause with features from ProPPR.
 * <p>
 * Created on 14/04/17.
 *
 * @author Victor Guimarães
 */
public class FeaturedClause extends HornClause {

    protected final Features features;

    /**
     * Constructs with the head and features.
     *
     * @param head     the head
     * @param features the features
     */
    public FeaturedClause(Atom head, Features features) {
        super(head);
        this.features = features;
    }

    /**
     * Constructs with all parameters.
     *
     * @param head     the head
     * @param body     the body
     * @param features the features
     */
    public FeaturedClause(Atom head, Conjunction body, Features features) {
        super(head, body);
        this.features = features;
    }

    /**
     * Constructs with only the head.
     *
     * @param head the head
     */
    public FeaturedClause(Atom head) {
        super(head);
        this.features = null;
    }

    /**
     * Constructs with the head and the body.
     *
     * @param head the head
     * @param body the body
     */
    public FeaturedClause(Atom head, Conjunction body) {
        super(head, body);
        this.features = null;
    }

    /**
     * Gets the {@link Features} of the {@link Clause}.
     *
     * @return the {@link Features}
     */
    public Features getFeatures() {
        return features;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (features != null ? features.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FeaturedClause)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        FeaturedClause that = (FeaturedClause) o;

        return features != null ? features.equals(that.features) : that.features == null;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(super.toString());
        if (stringBuilder.toString().endsWith(LanguageUtils.CLAUSE_END_OF_LINE)) {
            stringBuilder.delete(stringBuilder.length() - LanguageUtils.CLAUSE_END_OF_LINE.length(), stringBuilder
                    .length());
        }
        stringBuilder.append(" ");

        stringBuilder.append(LanguageUtils.FEATURES_OPEN_ARGUMENT_CHARACTER);
        stringBuilder.append(features != null ? features.toString() : "");
        stringBuilder.append(LanguageUtils.FEATURES_CLOSE_ARGUMENT_CHARACTER);
        stringBuilder.append(LanguageUtils.CLAUSE_END_OF_LINE);

        return stringBuilder.toString().trim();
    }

}
