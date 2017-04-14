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

package br.ufrj.cos.language;

import br.ufrj.cos.util.LanguageUtils;

/**
 * Created on 14/04/17.
 *
 * @author Victor Guimarães
 */
public class FeaturedClause extends HornClause {

    protected Features features;

    public FeaturedClause(Atom head, Features features) {
        super(head);
        this.features = features;
    }

    public FeaturedClause(Atom head, Conjunction body, Features features) {
        super(head, body);
        this.features = features;
    }

    public FeaturedClause(Atom head) {
        super(head);
    }

    public FeaturedClause(Atom head, Conjunction body) {
        super(head, body);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(super.toString());
        if (stringBuilder.toString().endsWith(LanguageUtils.CLAUSE_END_OF_LINE)) {
            stringBuilder.delete(stringBuilder.length() - LanguageUtils.CLAUSE_END_OF_LINE.length(),
                                 stringBuilder.length());
        }

        stringBuilder.append(LanguageUtils.FEATURES_OPEN_ARGUMENT_CHARACTER);
        stringBuilder.append(features.toString());
        stringBuilder.append(LanguageUtils.FEATURES_CLOSE_ARGUMENT_CHARACTER);
        stringBuilder.append(LanguageUtils.CLAUSE_END_OF_LINE);

        return stringBuilder.toString().trim();
    }

}
