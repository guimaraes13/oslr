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

package br.ufrj.cos.knowledge.theory.manager.revision;

import br.ufrj.cos.knowledge.KnowledgeException;

/**
 * Represents an {@link Exception} in the revision process.
 * <p>
 * Created on 29/04/17.
 *
 * @author Victor Guimarães
 */
public class TheoryRevisionException extends KnowledgeException {

    /**
     * Constructs with a message.
     *
     * @param message the message
     */
    public TheoryRevisionException(String message) {
        super(message);
    }

    /**
     * Constructs with message and cause.
     *
     * @param message the message
     * @param cause   the cause.  (A {@code null} value is permitted,
     *                and indicates that the cause is nonexistent or unknown.)
     */
    public TheoryRevisionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs with a cause.
     *
     * @param cause the cause.  (A {@code null} value is permitted,
     *              and indicates that the cause is nonexistent or unknown.)
     */
    public TheoryRevisionException(Throwable cause) {
        super(cause);
    }

    /**
     * Full constructor.
     *
     * @param message            the message
     * @param cause              the cause.  (A {@code null} value is permitted,
     *                           and indicates that the cause is nonexistent or unknown.)
     * @param enableSuppression  whether or not suppression is enabled
     *                           or disabled
     * @param writableStackTrace whether or not the stack trace should
     *                           be writable
     */
    public TheoryRevisionException(String message, Throwable cause, boolean enableSuppression,
                                   boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
