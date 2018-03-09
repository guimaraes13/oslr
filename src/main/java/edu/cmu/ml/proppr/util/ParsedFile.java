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

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.cmu.ml.proppr.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Iterator;

/**
 * File utility with support for automatically skipping blank lines and #-comments.
 *
 * @author "Kathryn Mazaitis <krivard@cs.cmu.edu>"
 */
public class ParsedFile implements Iterable<String>, Iterator<String>, FileBackedIterable {

    private static final Logger log = LogManager.getLogger(ParsedFile.class);
    private static final boolean DEFAULT_STRICT = true;
    private boolean cheating = false;
    private String filename;
    private LineNumberReader reader;
    private String last;
    private String peek;
    private int dataLine;
    private boolean closed;
    private boolean strict;

    public ParsedFile(String filename) {
        this(filename, DEFAULT_STRICT);
    }

    public ParsedFile(String filename, boolean strict) {
        this.strict = strict;
        this.init(filename);
    }

    private void init(String filename) {
        this.filename = filename;
        this.dataLine = -2;
        try {
            reader = new LineNumberReader(new FileReader(filename));
            closed = false;
            this.next();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    protected boolean isComment(String line) {
        return line.startsWith("#");
    }

    protected void processComment(String line) {
    }

    public ParsedFile(File file) {
        this(file, DEFAULT_STRICT);
    }

    public ParsedFile(File file, boolean strict) {
        this.strict = strict;
        try {
            this.init(file.getCanonicalPath());
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Used primarily for unit tests (only supports up to 1024 bytes)
     *
     * @param stringReader
     */
    public ParsedFile(StringReader stringReader) {
        this.filename = stringReader.getClass().getCanonicalName() + stringReader.hashCode();
        this.reader = new LineNumberReader(stringReader);

        this.cheating = true;
        try {
            this.reader.mark(1024);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.next();
    }

    public void parseError() {
        parseError(null);
    }

    public void parseError(String msg) {
        if (this.strict) {
            throw new IllegalArgumentException("Unparsable line " + filename + ":" + reader.getLineNumber() + ":"
                                                       + (msg != null ? ("\n" + msg) : "")
                                                       + "\n" + last);
        }
        log.error("Unparsable line " + filename + ":" + reader.getLineNumber() + ":"
                          + (msg != null ? ("\n" + msg) : "")
                          + "\n" + last);
    }

    @Override
    public void finalize() {
        this.close();
    }

    public void close() {
        if (reader != null) {
            try {
                reader.close();
                this.closed = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isClosed() {
        return this.closed;
    }

    @Override
    public Iterator<String> iterator() {
        this.reset();
        return this;
    }

    /**
     * Reset the iterator back to the beginning of the file
     */
    public void reset() {
        if (this.cheating) {
            try {
                this.reader.reset();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            this.close();
            this.init(this.filename);
        }
    }

    @Override
    public boolean hasNext() {
        return peek != null;
    }

    @Override
    public String next() {
        last = peek;
        try {
            peek = reader.readLine();
            for (boolean skip = true; peek != null; ) {
                skip = skip && ((peek = peek.trim()).length() == 0);
                if (isComment(peek)) {
                    skip = true;
                    processComment(peek);
                }
                if (!skip) { break; }
                peek = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        dataLine++;
        return last;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Can't remove a line from a file, silly");
    }

    /**
     * The current line number, ignoring comments and blank lines.
     *
     * @return
     */
    public int getLineNumber() {
        return dataLine;
    }

    /**
     * The current line number, including comments and blank lines.
     *
     * @return
     */
    public int getAbsoluteLineNumber() {
        return this.reader.getLineNumber() - 1;
    }

    public String getFileName() {
        return this.filename;
    }

    @Override
    public void wrap() {
        this.reset();
    }

}
