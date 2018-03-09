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
 * Represents a Horn clause.
 * <p>
 * Created on 14/04/17.
 *
 * @author Victor Guimarães
 */
public class HornClause extends Clause {

    protected final Atom head;
    protected Conjunction body;

    /**
     * Constructs a empty body {@link HornClause}
     *
     * @param head the head
     */
    public HornClause(Atom head) {
        this.head = head;
    }

    /**
     * Constructs a {@link HornClause}
     *
     * @param head the head
     * @param body the body
     */
    public HornClause(Atom head, Conjunction body) {
        this.head = head;
        this.body = body;
    }

    /**
     * Gets the head of the {@link HornClause}
     *
     * @return the head
     */
    public Atom getHead() {
        return head;
    }

    /**
     * Gets the body of the {@link HornClause}
     *
     * @return the body
     */
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
    public int hashCode() {
        int result = head != null ? head.hashCode() : 0;
        result = 31 * result + (body != null ? body.hashCode() : 0);
        return result;
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

        if (head != null ? head.equals(that.head) : that.head == null) {
            if (body != null ? body.equals(that.body)
                    : that.body == null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return LanguageUtils.formatHornClause(this);
    }

}
