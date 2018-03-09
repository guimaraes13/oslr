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

package edu.cmu.ml.proppr.examples;

import edu.cmu.ml.proppr.prove.wam.Query;
import edu.cmu.ml.proppr.util.ParsedFile;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Yields an InferenceExample for each line in a file. The format for each line is:
 * queryGoal <TAB> +posGroundGoal <TAB> ... <TAB> -negGroundGoal
 * where a goal is in the space-delimited format
 * functor arg1 arg2 ...
 *
 * @author wcohen, krivard
 */
public class InferenceExampleStreamer {

    private final File[] files;

    public InferenceExampleStreamer(File... filelist) {
        this.files = filelist;
    }

    public List<InferenceExample> load() {
        List<InferenceExample> examples = new ArrayList<InferenceExample>();
        for (File f : files) {
            ParsedFile file = new ParsedFile(f);
            for (String line : file) {
                examples.add(exampleFromString(line));
            }
        }
        return examples;
    }

    public InferenceExample exampleFromString(String line) {
        String[] parts = line.trim().split("\t");

        Query query = Query.parse(parts[0]);

        List<String> posList = new ArrayList<String>();
        List<String> negList = new ArrayList<String>();
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].startsWith("+")) {
                posList.add(parts[i].substring(1));
            } else if (parts[i].startsWith("-")) {
                negList.add(parts[i].substring(1));
            }
        }

        Query[] posSet = new Query[posList.size()];
        for (int i = 0; i < posList.size(); i++) {
            posSet[i] = Query.parse(posList.get(i));
        }

        Query[] negSet = new Query[negList.size()];
        for (int i = 0; i < negList.size(); i++) {
            negSet[i] = Query.parse(negList.get(i));
        }
        return new InferenceExample(query, posSet, negSet);
    }

    public Iterable<InferenceExample> stream() {
        return new InferenceExampleIterator(files);
    }

    /**
     * See Also ParsedFile for comment syntax
     *
     * @author "Kathryn Mazaitis <krivard@cs.cmu.edu>"
     */
    public class InferenceExampleIterator implements Iterable<InferenceExample>, Iterator<InferenceExample> {

        LineNumberReader reader = null;
        File currentFile;
        File[] fileList;
        int currentFileId;
        String nextLine = null;
        Exception lastException = null;

        public InferenceExampleIterator(File[] files) {
            fileList = files;
            init();
        }

        public InferenceExampleIterator(String[] filenames) {
            fileList = new File[filenames.length];
            for (int i = 0; i < filenames.length; i++) { fileList[i] = new File(filenames[i]); }
            init();
        }

        private void init() {
            currentFileId = -1;
            nextFile();
        }

        protected void nextFile() {
            currentFileId++;
            try {
                if (reader != null) { reader.close(); }

                if (currentFileId < fileList.length) {
                    currentFile = fileList[currentFileId];
                    reader = new LineNumberReader(new FileReader(currentFile));
                    peek();
                }
            } catch (FileNotFoundException e) {
                throw new IllegalStateException(e);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        protected void peek() throws IOException {
            nextLine = reader.readLine();
            if (nextLine == null) {
                nextFile();
                return;
            }
            nextLine = nextLine.trim();
            if (nextLine.isEmpty() || nextLine.startsWith("#")) { peek(); }
        }

        @Override
        public Iterator<InferenceExample> iterator() {
            return this;
        }

        @Override
        public boolean hasNext() {
            if (lastException != null) { throw new IllegalStateException(lastException); }
            return nextLine != null;
        }

        @Override
        public InferenceExample next() {
            InferenceExample next = exampleFromString(nextLine);
            try {
                peek();
            } catch (IOException e) {
                lastException = e;
            }
            return next;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Can't remove from a file-backed iterator");
        }

    }
}
