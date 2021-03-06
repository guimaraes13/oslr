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

/* ExampleParser.java */
/* Generated By:JavaCC: Do not edit this line. ExampleParser.java */


package br.ufrj.cos.logic.parser.example;

import br.ufrj.cos.knowledge.example.AtomExample;
import br.ufrj.cos.knowledge.example.ProPprExample;
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.Predicate;
import br.ufrj.cos.logic.Term;
import br.ufrj.cos.logic.Variable;
import br.ufrj.cos.util.AtomFactory;

import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 15/04/17.
 *
 * @author Victor Guimarães
 */
public class ExampleParser implements ExampleParserConstants {

    private static int[] jj_la1_0;

    static {
        jj_la1_init_0();
    }

    private final int[] jj_la1 = new int[11];
    private final List<int[]> jj_expentries = new ArrayList<int[]>();
    private final int trace_indent = 0;
    public AtomFactory factory = new AtomFactory();
    /**
     * Generated Token Manager.
     */
    public ExampleParserTokenManager token_source;
    /**
     * Current token.
     */
    public Token token;
    /**
     * Next token.
     */
    public Token jj_nt;
    protected List probLogFormatExamples;
    protected List proPprFormatExamples;
    SimpleCharStream jj_input_stream;
    private int jj_ntk;
    private int jj_gen;
    private int[] jj_expentry;
    private int jj_kind = -1;
    private boolean trace_enabled;

    /**
     * Constructor with InputStream.
     */
    public ExampleParser(InputStream stream) {
        this(stream, null);
    }

    /**
     * Constructor with InputStream and supplied encoding
     */
    public ExampleParser(InputStream stream, String encoding) {
        try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        token_source = new ExampleParserTokenManager(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 11; i++) { jj_la1[i] = -1; }
    }

    /**
     * Constructor.
     */
    public ExampleParser(Reader stream) {
        jj_input_stream = new SimpleCharStream(stream, 1, 1);
        token_source = new ExampleParserTokenManager(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 11; i++) { jj_la1[i] = -1; }
    }

    /**
     * Constructor with generated Token Manager.
     */
    public ExampleParser(ExampleParserTokenManager tm) {
        token_source = tm;
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 11; i++) { jj_la1[i] = -1; }
    }

    private static void jj_la1_init_0() {
        jj_la1_0 = new int[]{0x10040, 0x10040, 0x10040, 0x10040, 0x180, 0x3000, 0x3000, 0x800, 0x200, 0x38000,
                0x30000,};
    }

    public List getProbLogFormatExamples() {
        return probLogFormatExamples;
    }

    public List getProPprFormatExamples() {
        return proPprFormatExamples;
    }

    public final void parseExamples() throws ParseException {
        probLogFormatExamples = new ArrayList();
        proPprFormatExamples = new ArrayList();
        label_1:
        while (true) {
            switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
                case PROBLOG_EXAMPLE_PREFIX:
                case CONSTANT: {
                    break;
                }
                default:
                    jj_la1[0] = jj_gen;
                    break label_1;
            }
            switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
                case PROBLOG_EXAMPLE_PREFIX: {
                    readProbLogExample(probLogFormatExamples);
                    break;
                }
                case CONSTANT: {
                    readProPprExample(proPprFormatExamples);
                    break;
                }
                default:
                    jj_la1[1] = jj_gen;
                    jj_consume_token(-1);
                    throw new ParseException();
            }
        }
        jj_consume_token(0);
    }

    public final void parseExamplesAppend(List probLogFormatExamples, List proPprFormatExamples) throws ParseException {
        label_2:
        while (true) {
            switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
                case PROBLOG_EXAMPLE_PREFIX:
                case CONSTANT: {
                    break;
                }
                default:
                    jj_la1[2] = jj_gen;
                    break label_2;
            }
            switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
                case PROBLOG_EXAMPLE_PREFIX: {
                    readProbLogExample(probLogFormatExamples);
                    break;
                }
                case CONSTANT: {
                    readProPprExample(proPprFormatExamples);
                    break;
                }
                default:
                    jj_la1[3] = jj_gen;
                    jj_consume_token(-1);
                    throw new ParseException();
            }
        }
        jj_consume_token(0);
    }

    public final void readProbLogExample(List examples) throws ParseException {
        Atom atom;
        boolean positive;
        jj_consume_token(PROBLOG_EXAMPLE_PREFIX);
        jj_consume_token(OPEN_PREDICATE_ARGUMENT);
        atom = readAtom();
        jj_consume_token(LIST_SEPARATOR);
        switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
            case TRUE_SIGN: {
                jj_consume_token(TRUE_SIGN);
                positive = true;
                break;
            }
            case FALSE_SIGN: {
                jj_consume_token(FALSE_SIGN);
                positive = false;
                break;
            }
            default:
                jj_la1[4] = jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
        }
        jj_consume_token(CLOSE_PREDICATE_ARGUMENT);
        jj_consume_token(END_OF_LINE_CHARACTER);
        examples.add(new AtomExample(atom, positive));
    }

    public final void readProPprExample(List proPprExamples) throws ParseException {Atom goal;
        Atom example;
        boolean positive;
        List examples = new ArrayList();
        goal = readAtom();
        label_3:
        while (true) {
            switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
                case POSTIVE_SIGN: {
                    jj_consume_token(POSTIVE_SIGN);
                    positive = true;
                    break;
                }
                case NEGATIVE_SIGN: {
                    jj_consume_token(NEGATIVE_SIGN);
                    positive = false;
                    break;
                }
                default:
                    jj_la1[5] = jj_gen;
                    jj_consume_token(-1);
                    throw new ParseException();
            }
            example = readAtom();
            examples.add(new AtomExample(example, positive));
            switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
                case POSTIVE_SIGN:
                case NEGATIVE_SIGN: {
                    break;
                }
                default:
                    jj_la1[6] = jj_gen;
                    break label_3;
            }
        }
        proPprExamples.add(new ProPprExample(goal, examples));
}

    public final Atom readAtom() throws ParseException {
        String predicate;
        List terms = new ArrayList();
        predicate = readPredicate();
        switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
            case OPEN_PREDICATE_ARGUMENT: {
                jj_consume_token(OPEN_PREDICATE_ARGUMENT);
                readTerm(terms);
                label_4:
                while (true) {
                    switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
                        case LIST_SEPARATOR: {
                            break;
                        }
                        default:
                            jj_la1[7] = jj_gen;
                            break label_4;
                    }
                    jj_consume_token(LIST_SEPARATOR);
                    readTerm(terms);
                }
                jj_consume_token(CLOSE_PREDICATE_ARGUMENT);
                break;
            }
            default:
                jj_la1[8] = jj_gen;
        }
        Predicate value = factory.getPredicate(predicate, terms.size());
        {if ("" != null) { return new Atom(value, terms); }}
        throw new Error("Missing return statement in function");
    }

    public final String readPredicate() throws ParseException {
        Token predicate;
        predicate = jj_consume_token(CONSTANT);
        {if ("" != null) { return predicate.image; }}
        throw new Error("Missing return statement in function");
    }

    public final void readTerm(List<Term> terms) throws ParseException {
        Term term;
        switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
            case CONSTANT:
            case QUOTED: {
                term = readConstant();
                terms.add(term);
                break;
            }
            case VARIABLE: {
                term = readVariable();
                terms.add(term);
                break;
            }
            default:
                jj_la1[9] = jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
        }
    }

    public final Term readConstant() throws ParseException {
        Token constant;
        switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
            case CONSTANT: {
                constant = jj_consume_token(CONSTANT);
                break;
            }
            case QUOTED: {
                constant = jj_consume_token(QUOTED);
                token.image = token.image.substring(1, token.image.length() - 1);
                break;
            }
            default:
                jj_la1[10] = jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
        }
        {if ("" != null) { return factory.getConstant(token.image); }}
        throw new Error("Missing return statement in function");
    }

    public final Term readVariable() throws ParseException {
        Token variable;
        variable = jj_consume_token(VARIABLE);
        {if ("" != null) { return new Variable(variable.image); }}
        throw new Error("Missing return statement in function");
    }

    /**
     * Reinitialise.
     */
    public void ReInit(InputStream stream) {
        ReInit(stream, null);
    }

    /**
     * Reinitialise.
     */
    public void ReInit(InputStream stream, String encoding) {
        try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        token_source.ReInit(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 11; i++) { jj_la1[i] = -1; }
    }

    /**
     * Reinitialise.
     */
    public void ReInit(Reader stream) {
        if (jj_input_stream == null) {
            jj_input_stream = new SimpleCharStream(stream, 1, 1);
        } else {
            jj_input_stream.ReInit(stream, 1, 1);
        }
        if (token_source == null) {
            token_source = new ExampleParserTokenManager(jj_input_stream);
        }

        token_source.ReInit(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 11; i++) { jj_la1[i] = -1; }
    }

    /**
     * Reinitialise.
     */
    public void ReInit(ExampleParserTokenManager tm) {
        token_source = tm;
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 11; i++) { jj_la1[i] = -1; }
    }

    private Token jj_consume_token(int kind) throws ParseException {
        Token oldToken;
        if ((oldToken = token).next != null) { token = token.next; } else {
            token = token.next = token_source.getNextToken();
        }
        jj_ntk = -1;
        if (token.kind == kind) {
            jj_gen++;
            return token;
        }
        token = oldToken;
        jj_kind = kind;
        throw generateParseException();
    }

    /**
     * Get the next Token.
     */
    public final Token getNextToken() {
        if (token.next != null) { token = token.next; } else { token = token.next = token_source.getNextToken(); }
        jj_ntk = -1;
        jj_gen++;
        return token;
    }

    /**
     * Get the specific Token.
     */
    public final Token getToken(int index) {
        Token t = token;
        for (int i = 0; i < index; i++) {
            if (t.next != null) { t = t.next; } else { t = t.next = token_source.getNextToken(); }
        }
        return t;
    }

    private int jj_ntk_f() {
        if ((jj_nt = token.next) == null) { return (jj_ntk = (token.next = token_source.getNextToken()).kind); } else {
            return (jj_ntk = jj_nt.kind);
        }
    }

    /**
     * Generate ParseException.
     */
    public ParseException generateParseException() {
        jj_expentries.clear();
        boolean[] la1tokens = new boolean[18];
        if (jj_kind >= 0) {
            la1tokens[jj_kind] = true;
            jj_kind = -1;
        }
        for (int i = 0; i < 11; i++) {
            if (jj_la1[i] == jj_gen) {
                for (int j = 0; j < 32; j++) {
                    if ((jj_la1_0[i] & (1 << j)) != 0) {
                        la1tokens[j] = true;
                    }
                }
            }
        }
        for (int i = 0; i < 18; i++) {
            if (la1tokens[i]) {
                jj_expentry = new int[1];
                jj_expentry[0] = i;
                jj_expentries.add(jj_expentry);
            }
        }
        int[][] exptokseq = new int[jj_expentries.size()][];
        for (int i = 0; i < jj_expentries.size(); i++) {
            exptokseq[i] = jj_expentries.get(i);
        }
        return new ParseException(token, exptokseq, tokenImage);
    }

    /**
     * Trace enabled.
     */
    public final boolean trace_enabled() {
        return trace_enabled;
    }

    /**
     * Enable tracing.
     */
    public final void enable_tracing() {
    }

    /** Disable tracing. */
    public final void disable_tracing() {
    }

}
