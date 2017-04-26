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

package br.ufrj.cos.logic;

import br.ufrj.cos.util.LanguageUtils;

/**
 * Created on 14/04/17.
 *
 * @author Victor Guimarães
 */
public class HornClause extends Clause {

    protected Atom head;
    protected Conjunction body;

    public HornClause(Atom head) {
        this.head = head;
    }

    public HornClause(Atom head, Conjunction body) {
        this.head = head;
        this.body = body;
    }

    public Atom getHead() {
        return head;
    }

    public Conjunction getBody() {
        return body;
    }

    @Override
    public boolean isGrounded() {
        return head.isGrounded() && body.isGrounded();
    }

    @Override
    public boolean isFact() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HornClause)) {
            return false;
        }

        HornClause that = (HornClause) o;

        if (head != null ? !head.equals(that.head) : that.head != null) {
            return false;
        }
        return body != null ? body.equals(that.body) : that.body == null;
    }

    @Override
    public int hashCode() {
        int result = head != null ? head.hashCode() : 0;
        result = 31 * result + (body != null ? body.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return LanguageUtils.formatHornClause(this);
    }

}
