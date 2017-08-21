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

package br.ufrj.cos.util;

import br.ufrj.cos.cli.CommandLineOptions;
import br.ufrj.cos.knowledge.example.AtomExample;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.example.Examples;
import br.ufrj.cos.knowledge.example.ProPprExample;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.Clause;
import br.ufrj.cos.logic.HornClause;
import br.ufrj.cos.logic.parser.example.ExampleParser;
import br.ufrj.cos.logic.parser.knowledge.KnowledgeParser;
import br.ufrj.cos.logic.parser.knowledge.ParseException;
import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static br.ufrj.cos.util.LanguageUtils.CLAUSE_END_OF_LINE;
import static br.ufrj.cos.util.log.FileIOLog.ERROR_READING_FILE;
import static br.ufrj.cos.util.log.FileIOLog.READING_INPUT_FILES;
import static br.ufrj.cos.util.log.IterationLog.ERROR_WRITING_ITERATION_INFERENCE_FILE;
import static br.ufrj.cos.util.log.SystemLog.EXAMPLES_SIZE;
import static br.ufrj.cos.util.log.SystemLog.READ_CLAUSE_SIZE;

/**
 * Class to centralize useful method with respect with to the file IO.
 * <p>
 * Created on 07/08/17.
 *
 * @author Victor Guimarães
 */
public final class FileIOUtils {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    /**
     * The default encode of the input.
     */
    public static final String DEFAULT_INPUT_ENCODE = "UTF8";
    /**
     * The separator of values in the inference files.
     */
    public static final String INFERENCE_FILE_SEPARATOR = "\t";
    /**
     * The positive value of a examples.
     */
    public static final String POSITIVE_VALUE = "1.0";
    /**
     * The negative value of a examples.
     */
    public static final String NEGATIVE_VALUE = "0.0";
    /**
     * The unknown value of a examples.
     */
    public static final Double UNKNOWN_VALUE = -1.0;
    /**
     * The parameter mark from the log's format
     */
    public static final String LOG_PARAMETER_MARK = "{}";
    private static final File[] FILES = new File[0];

    private FileIOUtils() {
    }

    /**
     * Reads a file to a {@link String}
     *
     * @param filePath the file's path
     * @return the content of the file
     * @throws IOException if an error occurs during the reading
     */
    public static String readFileToString(String filePath) throws IOException {
        return readFileToString(new File(filePath));
    }

    /**
     * Reads a file to a {@link String}
     *
     * @param file the file
     * @return the content of the file
     * @throws FileNotFoundException        if the file does not exists
     * @throws UnsupportedEncodingException if the encoding is not supported
     */
    public static String readFileToString(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),
                                                                              DEFAULT_INPUT_ENCODE))) {
            return reader.lines().collect(Collectors.joining("\n")).trim();
        }
    }

    /**
     * Writes a {@link String} to a file.
     *
     * @param content the content of the file
     * @param file    the file to write to
     * @throws IOException if an error occurs during the writing
     */
    public static void writeStringToFile(String content, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),
                                                                               DEFAULT_INPUT_ENCODE))) {
            writer.write(content);
            writer.close();
        }
    }

    /**
     * Reads the file paths to {@link File} objects.
     *
     * @param paths     the file paths
     * @param inputName the input name
     * @return the {@link File}s
     * @throws FileNotFoundException if a file does not exists
     */
    public static File[] readPathsToFiles(String[] paths, String inputName) throws FileNotFoundException {
        if (paths == null) { return FILES; }
        File[] files = new File[paths.length];
        File file;
        for (int i = 0; i < paths.length; i++) {
            file = new File(paths[i]);
            if (file.exists()) {
                files[i] = file;
            } else {
                throw new FileNotFoundException(formatLogMessage(ExceptionMessages.FILE_NOT_EXISTS.toString(), file
                        .getAbsoluteFile(), inputName));
            }
        }

        return files;
    }

    /**
     * Formats a {@link String} replacing the parameter marks from the log format to the objects {@code toString}
     * method.
     *
     * @param message the message with the {@link #LOG_PARAMETER_MARK}
     * @param objects the objects
     * @return the formatted {@link String}
     */
    @SuppressWarnings("DynamicRegexReplaceableByCompiledPattern")
    public static String formatLogMessage(String message, Object... objects) {
        String format = message.replace(LOG_PARAMETER_MARK, LanguageUtils.STRING_PARAMETER_MARK);
        return String.format(format, objects);
    }

    /**
     * Saves the {@link Theory} to the {@link File}
     *
     * @param theory the {@link Theory}
     * @param file   the {@link File}
     * @throws IOException if an error occurs with the file
     */
    public static void saveTheoryToFile(Theory theory, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),
                                                                               DEFAULT_INPUT_ENCODE))) {
            for (HornClause clause : theory) {
                writer.write(clause + "\n");
            }
        }
    }

    /**
     * Saves the {@link Clause}s to the {@link File}
     *
     * @param clauses the {@link Clause}s
     * @param file    the {@link File}
     * @throws IOException if an error occurs with the file
     */
    public static void saveClausesToFile(Collection<? extends Clause> clauses, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),
                                                                               DEFAULT_INPUT_ENCODE))) {
            for (Clause clause : clauses) {
                writer.write(clause.toString());
                if (clause instanceof Atom) {
                    writer.write(CLAUSE_END_OF_LINE);
                }
                writer.write("\n");
            }
        }
    }

    /**
     * Saves the inferred examples to file.
     *
     * @param inferredExamples the inferred examples
     * @param allExamples      the full set of input examples
     * @param file             the file
     */
    public static void saveInferencesToTsvFile(Map<Example, Map<Atom, Double>> inferredExamples,
                                               Collection<? extends Example> allExamples, File file) {
        if (allExamples == null || inferredExamples == null) {
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),
                                                                               DEFAULT_INPUT_ENCODE))) {
            writeTsvInferenceFileHeader(writer);
            Map<Atom, Double> inferredAtoms;
            Double inference;
            for (Example example : allExamples) {
                inferredAtoms = inferredExamples.getOrDefault(example, Collections.emptyMap());
                for (AtomExample atomExample : example.getGroundedQuery()) {
                    // example
                    writer.write(atomExample.getAtom().toString());
                    writer.write(INFERENCE_FILE_SEPARATOR);
                    // expected
                    writer.write(atomExample.isPositive() ? POSITIVE_VALUE : NEGATIVE_VALUE);
                    writer.write(INFERENCE_FILE_SEPARATOR);
                    inference = inferredAtoms.getOrDefault(atomExample.getAtom(), UNKNOWN_VALUE);
                    writer.write(inference.toString());
                    writer.write(INFERENCE_FILE_SEPARATOR);
                    writer.write("\n");
                }
            }
        } catch (IOException e) {
            logger.error(ERROR_WRITING_ITERATION_INFERENCE_FILE, e);
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    /**
     * Writes the inference tsv file header.
     *
     * @param writer the writer
     * @throws IOException if an I/O error has occurred
     */
    @SuppressWarnings("HardCodedStringLiteral")
    protected static void writeTsvInferenceFileHeader(BufferedWriter writer) throws IOException {
        writer.write("Example");
        writer.write(INFERENCE_FILE_SEPARATOR);
        writer.write("Expected");
        writer.write(INFERENCE_FILE_SEPARATOR);
        writer.write("Inference");
        writer.write(" (Positive = " + POSITIVE_VALUE);
        writer.write(", Negative = " + NEGATIVE_VALUE);
        writer.write(", Unknown = " + UNKNOWN_VALUE);
        writer.write(")\n");
    }

    /**
     * Saves the {@link Example}s to the {@link File}.
     *
     * @param examples the {@link Example}s
     * @param file     the {@link File}
     * @throws IOException if an error occurs with the file
     */
    public static void saveExamplesToFile(Collection<? extends Example> examples, File file) throws IOException {
        saveExamplesToFile(examples, file, false);
    }

    /**
     * Saves the {@link Example}s to the {@link File}.
     *
     * @param examples the {@link Example}s
     * @param file     the {@link File}
     * @param append   if is to append the file.
     * @throws IOException if an error occurs with the file
     */
    public static void saveExamplesToFile(Collection<? extends Example> examples, File file, boolean append)
            throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append),
                                                                               DEFAULT_INPUT_ENCODE))) {
            for (Example example : examples) {
                writer.write(example + "\n");
            }
        }
    }

    /**
     * Reads the knowledge base from the iteration file.
     *
     * @param file        the file
     * @param clauses     the clause list to append the read clauses
     * @param atomFactory the atom factory, if wants to save memory by keeping same constants pointing to the same
     *                    object in memory
     * @throws ParseException if a parser error occurs
     * @throws IOException    if an I/O error has occurred
     */
    public static void readAtomKnowledgeFromFile(File file, Collection<Atom> clauses, AtomFactory atomFactory)
            throws IOException, ParseException {
        BufferedReader reader;
        KnowledgeParser parser;
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), DEFAULT_INPUT_ENCODE));
        parser = new KnowledgeParser(reader);
        parser.factory = atomFactory != null ? atomFactory : new AtomFactory();
        parser.parseKnowledgeAppend(clauses);
        reader.close();
    }

    /**
     * Reads the knowledge base from the iteration file.
     *
     * @param file        the file
     * @param clauses     the clause list to append the read clauses
     * @param atomFactory the atom factory, if wants to save memory by keeping same constants pointing to the same
     *                    object in memory
     * @param filter      the predicate to filter the atoms.
     * @throws IOException if an I/O error has occurred
     */
    public static void readFilteredAtomKnowledgeFrom(File file, Collection<Atom> clauses, AtomFactory atomFactory,
                                                     Predicate<? super Atom> filter)
            throws IOException {
        BufferedReader reader;
        KnowledgeParser parser;
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), DEFAULT_INPUT_ENCODE));
        parser = new KnowledgeParser(reader);
        parser.factory = atomFactory != null ? atomFactory : new AtomFactory();
        Atom atom;
        while (parser.hasNext()) {
            atom = (Atom) parser.next();
            if (filter.test(atom)) { clauses.add(atom); }
        }
        reader.close();
    }

    /**
     * Saves the object as a yaml file
     *
     * @param object the object
     * @param file   the output file
     * @throws IOException if an I/O error has occurred
     */
    public static void writeObjectToYamlFile(Object object, File file) throws IOException {
        writeObjectToYamlFile(object, file, false);
    }

    /**
     * Saves the object as a yaml file
     *
     * @param object       the object
     * @param file         the output file
     * @param isAutoAnchor if is to use auto anchor
     * @throws IOException if an I/O error has occurred
     */
    public static void writeObjectToYamlFile(Object object, File file, boolean isAutoAnchor) throws IOException {
        YamlConfig config = new YamlConfig();
        config.writeConfig.setIndentSize(2);
        config.writeConfig.setKeepBeanPropertyOrder(true);
        config.writeConfig.setAutoAnchor(isAutoAnchor);
        try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),
                                                                                       DEFAULT_INPUT_ENCODE))) {
            YamlWriter writer = new YamlWriter(bufferedWriter, config);
            writer.write(object);
            writer.close();
        }
    }

    /**
     * Reads the input {@link File}s to a {@link List} of {@link Clause}s.
     *
     * @param inputFiles the input {@link File}s
     * @return the {@link List} of {@link Clause}s
     */
    public static List<Clause> readInputKnowledge(File[] inputFiles) {
        List<Clause> clauses = new ArrayList<>();
        if (inputFiles == null) { return clauses; }
        logger.trace(READING_INPUT_FILES);
        for (File file : inputFiles) {
            readClausesToList(file, clauses);
        }
        logger.debug(READ_CLAUSE_SIZE.toString(), clauses.size());
        return clauses;
    }

    /**
     * Parses the {@link File}'s {@link Clause}s and appends they to the {@link List}.
     *
     * @param file    the {@link File} to parse
     * @param clauses the {@link List} to append to
     */
    public static void readClausesToList(File file, List<Clause> clauses) {
        try {
            BufferedReader reader;
            KnowledgeParser parser;
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), DEFAULT_INPUT_ENCODE));
            parser = new KnowledgeParser(reader);
            parser.parseKnowledgeAppend(clauses);
        } catch (UnsupportedEncodingException | FileNotFoundException | ParseException e) {
            logger.error(ERROR_READING_FILE.toString(), e);
        }
    }

    /**
     * Builds the {@link Examples} from the input files.
     *
     * @param exampleFilePaths the examples paths
     * @return the examples
     * @throws InstantiationException if an error occurs when instantiating a new set
     * @throws IllegalAccessException if an error occurs when instantiating a new set
     * @throws FileNotFoundException  if a file does not exists
     */
    public static Examples buildExampleSet(String[] exampleFilePaths) throws InstantiationException,
            IllegalAccessException, FileNotFoundException {
        List<AtomExample> atomExamples = new ArrayList<>();
        List<ProPprExample> proPprExamples = new ArrayList<>();
        logger.trace(READING_INPUT_FILES);
        File[] files = readPathsToFiles(exampleFilePaths, CommandLineOptions.EXAMPLES.getOptionName());
        for (File file : files) {
            readExamplesToLists(file, atomExamples, proPprExamples);
        }
        logger.info(EXAMPLES_SIZE.toString(), atomExamples.size() + proPprExamples.size());

        return new Examples(proPprExamples, atomExamples);
    }

    /**
     * Parses the {@link File}'s iterator and appends they to the correspondent {@link List}.
     *
     * @param file           the {@link File} to parse
     * @param atomExamples   the {@link List} to the ProbLog like iterator
     * @param proPprExamples the {@link List} to the ProPPR like iterator
     */
    public static void readExamplesToLists(File file,
                                           List<AtomExample> atomExamples,
                                           List<ProPprExample> proPprExamples) {
        try {
            BufferedReader reader;
            ExampleParser parser;
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),
                                                              DEFAULT_INPUT_ENCODE));
            parser = new ExampleParser(reader);
            parser.parseExamplesAppend(atomExamples, proPprExamples);
        } catch (UnsupportedEncodingException | FileNotFoundException | br.ufrj.cos.logic.parser.example
                .ParseException e) {
            logger.error(ERROR_READING_FILE.toString(), e);
        }
    }
}
