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

/* KnowledgeParserTokenManager.java */
/* Generated By:JavaCC: Do not edit this line. KnowledgeParserTokenManager.java */


package br.ufrj.cos.logic.parser.knowledge;

import java.io.IOException;
import java.io.PrintStream;

/**
 * Token Manager.
 */
public class KnowledgeParserTokenManager implements KnowledgeParserConstants {

    /**
     * Token literal values.
     */
    public static final String[] jjstrLiteralImages = {
            "", null, null, null, null, null, null, "\156\157\164", null, null, "\50",
            "\51", "\54", "\72\72", "\173", "\175", "\72\55", "\56", null,};
    /**
     * Lexer state names.
     */
    public static final String[] lexStateNames = {
            "DEFAULT",
    };
    /**
     * Lex State array.
     */
    public static final int[] jjnewLexState = {
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    };
    static final long[] jjbitVec0 = {
            0xfffffffffffffffeL, 0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffffffffffffffL
    };
    static final long[] jjbitVec2 = {
            0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
    };
    static final int[] jjnextStates = {
            14, 2, 3, 9, 11, 12,
    };
    static final long[] jjtoToken = {
            0x7ffc1L,
    };
    static final long[] jjtoSkip = {
            0x3eL,
    };
    static final long[] jjtoSpecial = {
            0x0L,
    };
    static final long[] jjtoMore = {
            0x0L,
};
    private final int[] jjrounds = new int[15];
    private final int[] jjstateSet = new int[2 * 15];
    private final StringBuilder jjimage = new StringBuilder();
    private final StringBuilder image = jjimage;
    /**
     * Debug output.
     */
    public  PrintStream debugStream = System.out;
    protected SimpleCharStream  input_stream;
    protected int curChar;
int curLexState = 0;
int defaultLexState = 0;
int jjnewStateCnt;
int jjround;
int jjmatchedPos;
int jjmatchedKind;
    private int jjimageLen;
    private int lengthOfMatch;

    /** Constructor. */
    public KnowledgeParserTokenManager(SimpleCharStream stream){

        if (SimpleCharStream.staticFlag)
            throw new Error("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");

        input_stream = stream;
    }

    /**
     * Constructor.
     */
    public KnowledgeParserTokenManager(SimpleCharStream stream, int lexState) {
        ReInit(stream);
        SwitchTo(lexState);
    }

    /**
     * Reinitialise parser.
     */

    public void ReInit(SimpleCharStream stream) {

        jjmatchedPos =
                jjnewStateCnt =
                        0;
        curLexState = defaultLexState;
        input_stream = stream;
        ReInitRounds();
    }

    /**
     * Switch to specified lex state.
     */
    public void SwitchTo(int lexState) {
        if (lexState >= 1 || lexState < 0) {
            throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.",
                                    TokenMgrError.INVALID_LEXICAL_STATE);
        } else { curLexState = lexState; }
    }

    private void ReInitRounds() {
        int i;
        jjround = 0x80000001;
        for (i = 15; i-- > 0; ) { jjrounds[i] = 0x80000000; }
    }

    private static final boolean jjCanMove_0(int hiByte, int i1, int i2, long l1, long l2) {
        switch (hiByte) {
            case 0:
                return ((jjbitVec2[i2] & l2) != 0L);
            default:
                return (jjbitVec0[i1] & l1) != 0L;
        }
    }

    /**
     * Set debug output.
     */
    public void setDebugStream(PrintStream ds) {
        debugStream = ds;
    }

    private final int jjStopStringLiteralDfa_0(int pos, long active0) {
        switch (pos) {
            case 0:
                if ((active0 & 0x80L) != 0L) {
                    jjmatchedKind = 9;
                    return 7;
                }
                if ((active0 & 0x20000L) != 0L) { return 3; }
                return -1;
            case 1:
                if ((active0 & 0x80L) != 0L) {
                    jjmatchedKind = 9;
                    jjmatchedPos = 1;
                    return 7;
                }
                return -1;
            default:
                return -1;
        }
    }

    private final int jjStartNfa_0(int pos, long active0) {
        return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0), pos + 1);
    }

    private int jjStopAtPos(int pos, int kind) {
        jjmatchedKind = kind;
        jjmatchedPos = pos;
        return pos + 1;
    }

    private int jjMoveStringLiteralDfa0_0() {
        switch (curChar) {
            case 40:
                return jjStopAtPos(0, 10);
            case 41:
                return jjStopAtPos(0, 11);
            case 44:
                return jjStopAtPos(0, 12);
            case 46:
                return jjStartNfaWithStates_0(0, 17, 3);
            case 58:
                return jjMoveStringLiteralDfa1_0(0x12000L);
            case 110:
                return jjMoveStringLiteralDfa1_0(0x80L);
            case 123:
                return jjStopAtPos(0, 14);
            case 125:
                return jjStopAtPos(0, 15);
            default:
                return jjMoveNfa_0(0, 0);
        }
    }

    private int jjMoveStringLiteralDfa1_0(long active0) {
        try { curChar = input_stream.readChar(); } catch (IOException e) {
            jjStopStringLiteralDfa_0(0, active0);
            return 1;
        }
        switch (curChar) {
            case 45:
                if ((active0 & 0x10000L) != 0L) { return jjStopAtPos(1, 16); }
                break;
            case 58:
                if ((active0 & 0x2000L) != 0L) { return jjStopAtPos(1, 13); }
                break;
            case 111:
                return jjMoveStringLiteralDfa2_0(active0, 0x80L);
            default:
                break;
        }
        return jjStartNfa_0(0, active0);
    }

    private int jjMoveStringLiteralDfa2_0(long old0, long active0) {
        if (((active0 &= old0)) == 0L) { return jjStartNfa_0(0, old0); }
        try { curChar = input_stream.readChar(); } catch (IOException e) {
            jjStopStringLiteralDfa_0(1, active0);
            return 2;
        }
        switch (curChar) {
            case 116:
                if ((active0 & 0x80L) != 0L) { return jjStartNfaWithStates_0(2, 7, 7); }
                break;
            default:
                break;
        }
        return jjStartNfa_0(1, active0);
    }

    private int jjStartNfaWithStates_0(int pos, int kind, int state) {
        jjmatchedKind = kind;
        jjmatchedPos = pos;
        try { curChar = input_stream.readChar(); } catch (IOException e) { return pos + 1; }
        return jjMoveNfa_0(state, pos + 1);
    }

    private int jjMoveNfa_0(int startState, int curPos) {
        int startsAt = 0;
        jjnewStateCnt = 15;
        int i = 1;
        jjstateSet[0] = startState;
        int kind = 0x7fffffff;
        for (; ; ) {
            if (++jjround == 0x7fffffff) { ReInitRounds(); }
            if (curChar < 64) {
                long l = 1L << curChar;
                do {
                    switch (jjstateSet[--i]) {
                        case 0:
                            if ((0x3ff000000000000L & l) != 0L) {
                                if (kind > 6) { kind = 6; }
                                { jjCheckNAddStates(0, 2); }
                            } else if (curChar == 34) { jjCheckNAddTwoStates(9, 11); } else if (curChar == 46) {
                                jjCheckNAdd(3);
                            } else if (curChar == 37) {
                                if (kind > 5) { kind = 5; }
                                { jjCheckNAdd(1); }
                            }
                            break;
                        case 1:
                            if ((0xffffffffffffdbffL & l) == 0L) { break; }
                            if (kind > 5) { kind = 5; }
                        { jjCheckNAdd(1); }
                        break;
                        case 2:
                            if (curChar == 46) { jjCheckNAdd(3); }
                            break;
                        case 3:
                            if ((0x3ff000000000000L & l) == 0L) { break; }
                            if (kind > 6) { kind = 6; }
                        { jjCheckNAdd(3); }
                        break;
                        case 5:
                            if ((0x3ff200000000000L & l) == 0L) { break; }
                            if (kind > 8) { kind = 8; }
                            jjstateSet[jjnewStateCnt++] = 5;
                            break;
                        case 7:
                            if ((0x3ff200000000000L & l) == 0L) { break; }
                            if (kind > 9) { kind = 9; }
                            jjstateSet[jjnewStateCnt++] = 7;
                            break;
                        case 8:
                            if (curChar == 34) { jjCheckNAddTwoStates(9, 11); }
                            break;
                        case 10: { jjCheckNAddStates(3, 5); }
                        break;
                        case 11:
                            if ((0xfffffffbffffffffL & l) != 0L) { jjCheckNAddStates(3, 5); }
                            break;
                        case 12:
                            if (curChar == 34 && kind > 18) { kind = 18; }
                            break;
                        case 13:
                            if ((0x3ff000000000000L & l) == 0L) { break; }
                            if (kind > 6) { kind = 6; }
                        { jjCheckNAddStates(0, 2); }
                        break;
                        case 14:
                            if ((0x3ff000000000000L & l) != 0L) { jjCheckNAddTwoStates(14, 2); }
                            break;
                        default:
                            break;
                    }
                } while (i != startsAt);
            } else if (curChar < 128) {
                long l = 1L << (curChar & 077);
                do {
                    switch (jjstateSet[--i]) {
                        case 0:
                            if ((0x7fffffe00000000L & l) != 0L) {
                                if (kind > 9) { kind = 9; }
                                { jjCheckNAdd(7); }
                            } else if ((0x7fffffeL & l) != 0L) {
                                if (kind > 8) { kind = 8; }
                                { jjCheckNAdd(5); }
                            }
                            break;
                        case 1:
                            if (kind > 5) { kind = 5; }
                            jjstateSet[jjnewStateCnt++] = 1;
                            break;
                        case 4:
                            if ((0x7fffffeL & l) == 0L) { break; }
                            if (kind > 8) { kind = 8; }
                        { jjCheckNAdd(5); }
                        break;
                        case 5:
                            if ((0x7fffffe87fffffeL & l) == 0L) { break; }
                            if (kind > 8) { kind = 8; }
                        { jjCheckNAdd(5); }
                        break;
                        case 6:
                            if ((0x7fffffe00000000L & l) == 0L) { break; }
                            if (kind > 9) { kind = 9; }
                        { jjCheckNAdd(7); }
                        break;
                        case 7:
                            if ((0x7fffffe87fffffeL & l) == 0L) { break; }
                            if (kind > 9) { kind = 9; }
                        { jjCheckNAdd(7); }
                        break;
                        case 9:
                            if (curChar == 92) { jjstateSet[jjnewStateCnt++] = 10; }
                            break;
                        case 10:
                        case 11: { jjCheckNAddStates(3, 5); }
                        break;
                        default:
                            break;
                    }
                } while (i != startsAt);
            } else {
                int hiByte = (curChar >> 8);
                int i1 = hiByte >> 6;
                long l1 = 1L << (hiByte & 077);
                int i2 = (curChar & 0xff) >> 6;
                long l2 = 1L << (curChar & 077);
                do {
                    switch (jjstateSet[--i]) {
                        case 1:
                            if (!jjCanMove_0(hiByte, i1, i2, l1, l2)) { break; }
                            if (kind > 5) { kind = 5; }
                            jjstateSet[jjnewStateCnt++] = 1;
                            break;
                        case 10:
                        case 11:
                            if (jjCanMove_0(hiByte, i1, i2, l1, l2)) { jjCheckNAddStates(3, 5); }
                            break;
                        default:
                            if (i1 == 0 || l1 == 0 || i2 == 0 || l2 == 0) { break; } else { break; }
                    }
                } while (i != startsAt);
            }
            if (kind != 0x7fffffff) {
                jjmatchedKind = kind;
                jjmatchedPos = curPos;
                kind = 0x7fffffff;
            }
            ++curPos;
            if ((i = jjnewStateCnt) == (startsAt = 15 - (jjnewStateCnt = startsAt))) { return curPos; }
            try { curChar = input_stream.readChar(); } catch (IOException e) { return curPos; }
        }
    }

    protected Token jjFillToken() {
        final Token t;
        final String curTokenImage;
        final int beginLine;
        final int endLine;
        final int beginColumn;
        final int endColumn;
        String im = jjstrLiteralImages[jjmatchedKind];
        curTokenImage = (im == null) ? input_stream.GetImage() : im;
        beginLine = input_stream.getBeginLine();
        beginColumn = input_stream.getBeginColumn();
        endLine = input_stream.getEndLine();
        endColumn = input_stream.getEndColumn();
        t = Token.newToken(jjmatchedKind, curTokenImage);

        t.beginLine = beginLine;
        t.endLine = endLine;
        t.beginColumn = beginColumn;
        t.endColumn = endColumn;

        return t;
    }

    /**
     * Get the next Token.
     */
    public Token getNextToken() {
        Token matchedToken;
        int curPos = 0;

        EOFLoop:
        for (; ; ) {
            try {
                curChar = input_stream.BeginToken();
            } catch (Exception e) {
                jjmatchedKind = 0;
                jjmatchedPos = -1;
                matchedToken = jjFillToken();
                return matchedToken;
            }

            try {
                input_stream.backup(0);
                while (curChar <= 32 && (0x100002600L & (1L << curChar)) != 0L) { curChar = input_stream.BeginToken(); }
            } catch (IOException e1) { continue EOFLoop; }
            jjmatchedKind = 0x7fffffff;
            jjmatchedPos = 0;
            curPos = jjMoveStringLiteralDfa0_0();
            if (jjmatchedKind != 0x7fffffff) {
                if (jjmatchedPos + 1 < curPos) { input_stream.backup(curPos - jjmatchedPos - 1); }
                if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L) {
                    matchedToken = jjFillToken();
                    return matchedToken;
                } else {
                    continue EOFLoop;
                }
            }
            int error_line = input_stream.getEndLine();
            int error_column = input_stream.getEndColumn();
            String error_after = null;
            boolean EOFSeen = false;
            try {
                input_stream.readChar();
                input_stream.backup(1);
            } catch (IOException e1) {
                EOFSeen = true;
                error_after = curPos <= 1 ? "" : input_stream.GetImage();
                if (curChar == '\n' || curChar == '\r') {
                    error_line++;
                    error_column = 0;
                } else { error_column++; }
            }
            if (!EOFSeen) {
                input_stream.backup(1);
                error_after = curPos <= 1 ? "" : input_stream.GetImage();
            }
            throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);
        }
    }

    void SkipLexicalActions(Token matchedToken) {
        switch (jjmatchedKind) {
            default:
                break;
        }
    }

    void MoreLexicalActions() {
        jjimageLen += (lengthOfMatch = jjmatchedPos + 1);
        switch (jjmatchedKind) {
            default:
                break;
        }
    }

    void TokenLexicalActions(Token matchedToken) {
        switch (jjmatchedKind) {
            default:
                break;
        }
    }

    private void jjCheckNAdd(int state) {
        if (jjrounds[state] != jjround) {
            jjstateSet[jjnewStateCnt++] = state;
            jjrounds[state] = jjround;
        }
    }

    private void jjAddStates(int start, int end) {
        do {
            jjstateSet[jjnewStateCnt++] = jjnextStates[start];
        } while (start++ != end);
    }

    private void jjCheckNAddTwoStates(int state1, int state2) {
        jjCheckNAdd(state1);
        jjCheckNAdd(state2);
    }

    private void jjCheckNAddStates(int start, int end) {
        do {
            jjCheckNAdd(jjnextStates[start]);
        } while (start++ != end);
    }

    /**
     * Reinitialise parser.
     */
    public void ReInit(SimpleCharStream stream, int lexState)

    {
        ReInit(stream);
        SwitchTo(lexState);
    }
}
