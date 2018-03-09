/*
 * Probabilist Logic Learner is a system to learn probabilistic logic
 * programs from data and use its learned programs to make inference
 * and answer queries.
 *
 * Copyright (C) 2018 Victor Guimarães
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

package edu.cmu.ml.proppr.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;;

/**
 * File utility with support for automatically skipping blank lines and #-comments.
 * 
 * @author "Kathryn Mazaitis <krivard@cs.cmu.edu>"
 *
 */
public class ParsedFile implements Iterable<String>, Iterator<String>, FileBackedIterable {
	private static final Logger log = LogManager.getLogger(ParsedFile.class);
	private static final boolean DEFAULT_STRICT=true;
	private boolean cheating=false;
	private String filename;
	private LineNumberReader reader;
	private String last;
	private String peek;
	private int dataLine;
	private boolean closed;
	private boolean strict;
	public ParsedFile(String filename) {
		this(filename,DEFAULT_STRICT);
	}
	public ParsedFile(String filename, boolean strict) {
		this.strict = strict;
		this.init(filename);
	}
	
	public ParsedFile(File file) {
		this(file,DEFAULT_STRICT);
	}
	public ParsedFile(File file, boolean strict) {
		this.strict=strict;
		try {
			this.init(file.getCanonicalPath());
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	private void init(String filename) {
		this.filename = filename;
		this.dataLine=-2;
		try {
			reader = new LineNumberReader(new FileReader(filename));
			closed = false;
			this.next();
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Used primarily for unit tests (only supports up to 1024 bytes)
	 * @param stringReader
	 */
	public ParsedFile(StringReader stringReader) {
		this.filename = stringReader.getClass().getCanonicalName() + stringReader.hashCode();
		this.reader = new LineNumberReader(stringReader);

		this.cheating=true;
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
		if (this.strict)
			throw new IllegalArgumentException("Unparsable line "+filename+":"+reader.getLineNumber()+":"
					+ (msg!=null ? ("\n"+msg) : "")
					+ "\n"+last);
		log.error("Unparsable line "+filename+":"+reader.getLineNumber()+":"
				+ (msg!=null ? ("\n"+msg) : "")
				+ "\n"+last);
	}
	
	@Override
	public void finalize() {
		this.close();
	}
	
	public void close() {
		if (reader != null)
			try {
				reader.close();
				this.closed = true;
			} catch (IOException e) {
				e.printStackTrace();
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

	@Override
	public boolean hasNext() {
		return peek != null;
	}
	
	/**
	 * The current line number, ignoring comments and blank lines.
	 * @return
	 */
	public int getLineNumber() {
		return dataLine;
	}
	
	/**
	 * The current line number, including comments and blank lines.
	 * @return
	 */
	public int getAbsoluteLineNumber() {
		return this.reader.getLineNumber()-1;
	}
	
	protected boolean isComment(String line) {
		return line.startsWith("#");
	}
	protected void processComment(String line) {}

	@Override
	public String next() {
		last = peek;
		try {
			peek = reader.readLine();
			for(boolean skip=true; peek != null; ) {
				skip = skip && ((peek=peek.trim()).length() == 0);
				if (isComment(peek)) {
					skip = true;
					processComment(peek);
				}
				if (!skip) break;
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

	public String getFileName() {
		return this.filename;
	}
	
	/** Reset the iterator back to the beginning of the file */
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
	public void wrap() {
		this.reset();
	}

}
