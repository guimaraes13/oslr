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

package br.ufrj.cos.cli;

import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.Predicate;
import br.ufrj.cos.util.*;
import br.ufrj.cos.util.nell.converter.AddAtomProcessor;
import br.ufrj.cos.util.nell.converter.AtomProcessor;
import br.ufrj.cos.util.nell.converter.FilterAtomProcessor;
import com.esotericsoftware.yamlbeans.YamlException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import static br.ufrj.cos.util.LogMessages.*;

/**
 * Class to convert a Knowledge base from Nell's csv files to a set of logic files.
 * <p>
 * Created on 16/07/17.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings({"CanBeFinal", "OverlyComplexClass"})
public class NellBaseConverterCLI extends CommandLineInterface {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();
    /**
     * GZip short suffix.
     */
    public static final String GZIP_SHORT_SUFFIX = ".gz";
    /**
     * GZip long suffix.
     */
    public static final String GZIP_LONG_SUFFIX = ".gzip";
    /**
     * Zip long suffix.
     */
    public static final String ZIP_SUFFIX = ".zip";
    /**
     * Default hash algorithm.
     */
    public static final String DEFAULT_HASH_ALGORITHM = "SHA-1";
    /**
     * The default substring of the subject name on the header.
     */
    public static final String DEFAULT_SUBJECT_NAME = "entity";
    /**
     * The default substring of the object name on the header.
     */
    public static final String DEFAULT_OBJECT_NAME = "value";
    /**
     * The default substring of the predicate name on the header.
     */
    public static final String DEFAULT_PREDICATE_NAME = "relation";
    /**
     * The default substring of the confidence name on the header.
     */
    public static final String DEFAULT_CONFIDENCE_NAME = "probability";
    /**
     * The default positive extension.
     */
    public static final String DEFAULT_POSITIVE_EXTENSION = ".f";
    /**
     * The default negative extension.
     */
    public static final String DEFAULT_NEGATIVE_EXTENSION = ".n";
    /**
     * The default confidence threshold to split the examples into positives and negatives.
     */
    public static final double DEFAULT_CONFIDENCE_THRESHOLD = 0.75;
    /**
     * The confidence value separator.
     */
    public static final String CONFIDENCE_VALUE_SEPARATOR = ",";
    /**
     * The confidence open array character.
     */
    public static final String CONFIDENCE_OPEN_ARRAY = "[";
    /**
     * The default iteration output directory prefix.
     */
    public static final String DEFAULT_ITERATION_PREFIX = "ITERATION_";
    /**
     * The default data directory name.
     */
    public static final String DEFAULT_DATA_DIRECTORY_NAME = "data";
    /**
     * The multiplier factor to turn a proportion into percentage.
     */
    public static final int PERCENT_FACTOR = 100;
    /**
     * The file encode
     */
    public String fileEncode = LanguageUtils.DEFAULT_INPUT_ENCODE;
    /**
     * Comment character for the input nell file
     */
    public String commentCharacter = "#";
    /**
     * The value separator in the nell's file.
     */
    public String valueSeparator = "\t";
    /**
     * The substring of the subject name on the header.
     */
    public String subjectName = DEFAULT_SUBJECT_NAME;
    /**
     * The substring of the object name on the header.
     */
    public String objectName = DEFAULT_OBJECT_NAME;
    /**
     * The substring of the predicate name on the header.
     */
    public String predicateName = DEFAULT_PREDICATE_NAME;
    /**
     * The substring of the confidence name on the header.
     */
    public String confidenceName = DEFAULT_CONFIDENCE_NAME;
    /**
     * The confidence threshold to split the examples into positives and negatives.
     */
    public double confidenceThreshold = DEFAULT_CONFIDENCE_THRESHOLD;
    /**
     * The positive output extension.
     */
    public String positiveOutputExtension = DEFAULT_POSITIVE_EXTENSION;
    /**
     * The negative output extension.
     */
    public String negativeOutputExtension = DEFAULT_NEGATIVE_EXTENSION;
    /**
     * Hash algorithm.
     */
    public String hashAlgorithm = DEFAULT_HASH_ALGORITHM;
    /**
     * The iteration output directory prefix.
     */
    public String iterationPrefix = DEFAULT_ITERATION_PREFIX;
    /**
     * The set of predicate to skip. This checks occurs before the predicateReplaceMap be applied.
     */
    public Set<String> skipPredicate;
    /**
     * The entity replace map, each key found on the entity (subject or object) string will be replaced by the
     * correspondent value.
     */
    public Map<String, String> entityReplaceMap;
    /**
     * The predicate replace map, each key found on the predicate string will be replaced by the correspondent value.
     */
    public Map<String, String> predicateReplaceMap;
    /**
     * The data directory name, inside the output directory.
     */
    public String dataDirectoryName = DEFAULT_DATA_DIRECTORY_NAME;
    /**
     * The array of nell's input files, sorted by iteration.
     */
    public String[] nellInputFilePaths = null;
    /**
     * The index of the iteration to start form. Useful to the job from another run.
     */
    public int startIndex = 0;

    protected int maxNameSize = confidenceName.length();
    protected int[] subjectIndexes;
    protected int[] objectIndexes;
    protected int[] predicateIndexes;
    protected int[] confidenceIndexes;

    protected Pair<Map<Predicate, Set<Atom>>, Map<Predicate, Set<Atom>>> previousAtoms;
    protected Pair<Map<Predicate, Set<Atom>>, Map<Predicate, Set<Atom>>> currentAtoms;
    protected MessageDigest normalDigest;
    protected MessageDigest zippedDigest;
    protected String[] inputHash;
    protected String[] inputZippedHash;
    protected Map<Predicate, String>[] outputPositiveHash;
    protected Map<Predicate, String>[] outputNegativeHash;
    protected AtomFactory atomFactory = new AtomFactory();
    protected File outputDataDirectory;
    protected final NumberFormat numberFormat;
    protected int[] previousSkippedAtoms;
    protected int[] removedAtoms;
    protected int[] positiveOutputSize;
    protected int[] negativeOutputSize;

    /**
     * Default constructor.
     */
    public NellBaseConverterCLI() {
        numberFormat = NumberFormat.getIntegerInstance();
        entityReplaceMap = new HashMap<>();
        entityReplaceMap.put("\"", "");
        entityReplaceMap.put("\'", "");
        entityReplaceMap.put(":", "_");

        predicateReplaceMap = new HashMap<>();
        //noinspection HardCodedStringLiteral
        predicateReplaceMap.put("^concept", "");
        predicateReplaceMap.put(":", "_");

        skipPredicate = new HashSet<>();
        //noinspection HardCodedStringLiteral
        skipPredicate.add("generalizations");
    }

    /**
     * Gets the negative part of the pair.
     *
     * @param pair the pair
     * @param <V>  the type of the negative part
     * @return the negative part of the pair
     */
    public static <V> V getNegatives(Pair<V, ?> pair) {
        return pair.getLeft();
    }

    /**
     * Gets the positive part of the pair.
     *
     * @param pair the pair
     * @param <V>  the type of the positive part
     * @return the positive part of the pair
     */
    public static <V> V getPositives(Pair<?, V> pair) {
        return pair.getRight();
    }

    /**
     * The main method
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Locale.setDefault(new Locale(DEFAULT_LANGUAGE, DEFAULT_COUNTRY));
        try {
            CommandLineInterface main = new NellBaseConverterCLI();
            main = main.parseOptions(args);
            run(main, args);
        } catch (Exception e) {
            logger.error(ERROR_MAIN_PROGRAM, e);
        } finally {
            logger.fatal(PROGRAM_END);
        }
    }

    @Override
    public void initialize() throws InitializationException {
        saveConfigurations();
    }

    @Override
    protected void initializeOptions() {
        super.initializeOptions();
        if (options == null) { options = new Options(); }
        options.addOption(CommandLineOptions.YAML.getOption());
        options.addOption(CommandLineOptions.OUTPUT_DIRECTORY.getOption());
    }

    @Override
    protected CommandLineInterface parseOptions(CommandLine commandLine) throws CommandLineInterrogationException {
        try {
            super.parseOptions(commandLine);
            NellBaseConverterCLI cli = readYamlFile(commandLine, NellBaseConverterCLI.class, null);
            cli.outputDirectoryPath = commandLine.getOptionValue(CommandLineOptions.OUTPUT_DIRECTORY.getOptionName());
            return cli;
        } catch (FileNotFoundException | YamlException e) {
            throw new CommandLineInterrogationException(e);
        }
    }

    /**
     * Deletes the data directory content.
     *
     * @param directory the directory
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteDataDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteDataDirectory(f);
                } else {
                    f.delete();
                }
            }
        }
        directory.delete();
    }

    /**
     * Builds a pair of maps.
     *
     * @param <K> the key of the map
     * @param <V> the value of the map
     * @return the pair of maps
     */
    protected static <K, V> ImmutablePair<Map<K, V>, Map<K, V>> buildMapPair() {
        Map<K, V> left = new HashMap<>();
        Map<K, V> right = new HashMap<>();
        return new ImmutablePair<>(left, right);
    }

    /**
     * Initializes the fields.
     */
    protected void initializeFields() {
        final int inputFiles = nellInputFilePaths.length;

        inputHash = new String[inputFiles];
        inputZippedHash = new String[inputFiles];

        outputPositiveHash = new HashMap[inputFiles];
        outputNegativeHash = new HashMap[inputFiles];

        outputDataDirectory = new File(outputDirectory, dataDirectoryName);
//        deleteDataDirectory(outputDataDirectory);

        previousSkippedAtoms = new int[inputFiles];
        removedAtoms = new int[inputFiles];
        positiveOutputSize = new int[inputFiles];
        negativeOutputSize = new int[inputFiles];

        initializeHeaderIndexes(inputFiles);
    }

    /**
     * Initializes the arrays to hold the header indexes for each input file
     *
     * @param inputFiles the number of input files
     */
    protected void initializeHeaderIndexes(int inputFiles) {
        subjectIndexes = new int[inputFiles];
        objectIndexes = new int[inputFiles];
        predicateIndexes = new int[inputFiles];
        confidenceIndexes = new int[inputFiles];

        for (int i = 0; i < inputFiles; i++) {
            subjectIndexes[i] = -1;
            objectIndexes[i] = -1;
            predicateIndexes[i] = -1;
            confidenceIndexes[i] = -1;
        }
    }

    @Override
    public void run() {
        try {
            if (nellInputFilePaths.length == 0) { return; }
            long begin = TimeMeasure.getNanoTime();
            initializeFields();

            processFiles();
            saveDescriptionFile();

            long end = TimeMeasure.getNanoTime();
            logger.info(HASH_DISCLAIMER);
            logger.warn(TOTAL_PROGRAM_TIME.toString(), TimeMeasure.formatNanoDifference(begin, end));
        } catch (Exception e) {
            logger.error(ExceptionMessages.GENERAL_ERROR.toString(), e);
        }
    }

    /**
     * Process the input files.
     *
     * @throws IOException              if an I/O error has occurred
     * @throws NoSuchAlgorithmException if no Provider supports a MessageDigestSpi implementation for the specified
     *                                  algorithm.
     */
    protected void processFiles() throws IOException, NoSuchAlgorithmException {
        previousAtoms = buildMapPair();
        if (startIndex > 0) {
            readFile(startIndex - 1);
            previousAtoms = currentAtoms;
        }
        // read the first file to current
        readFile(startIndex);

        int i;
        for (i = startIndex + 1; i < nellInputFilePaths.length; i++) {
            previousAtoms = currentAtoms;       // makes the previous the current
            readFile(i);                        // read the next file to current, filtering it by the current
            filterPreviousAtoms(i - 1);     // filter the previous by already added files
            initializeOutputHashMaps(i - 1);
            saveIteration(i - 1);           // saves the previous to files
            atomFactory.clearConstantMap();
        }
        previousAtoms = currentAtoms;       // makes the previous the current
        filterPreviousAtoms(i - 1);     // filter the previous by already added files
        initializeOutputHashMaps(i - 1);
        saveIteration(i - 1);           // saves the previous to files
    }

    /**
     * Initializes the output hash maps.
     *
     * @param index the index of the hash map
     */
    protected void initializeOutputHashMaps(int index) {
        if (outputPositiveHash[index] == null) { outputPositiveHash[index] = new HashMap<>(); }
        if (outputNegativeHash[index] == null) { outputNegativeHash[index] = new HashMap<>(); }
    }

    /**
     * Formats the name based on the replaceMap. Each occurrence of a key, from the replaceMap, in the name will be
     * replaced by its correspondent value.
     *
     * @param name       the name
     * @param replaceMap the replace map
     * @return the formatted name
     */
    protected static String formatName(String name, Map<String, String> replaceMap) {
        String entity = name;
        for (Map.Entry<String, String> entry : replaceMap.entrySet()) {
            entity = entity.replaceAll(entry.getKey(), entry.getValue());
        }
        return entity;
    }

    /**
     * Calculates the average probability from a string representation of a array of doubles.
     *
     * @param fields the array of doubles
     * @return the average probability
     */
    protected static double averageProbability(String[] fields) {
        OptionalDouble average = Arrays.stream(fields).mapToDouble(s -> Double.parseDouble(s.trim())).average();
        return average.isPresent() ? average.getAsDouble() : 0.0;
    }

    /**
     * Filters the atoms from the previous iteration removing the atom that already appears on older iterations.
     *
     * @param index the index of the previous iteration
     * @throws IOException if an I/O error has occurred
     */
    protected void filterPreviousAtoms(int index) throws IOException {
        FilterAtomProcessor atomProcessor = new FilterAtomProcessor(previousAtoms);
        InputStream stream;
        for (int i = 0; i < index - 1; i++) {
            logger.debug(LogMessages.FILTERING_ITERATION.toString(), index, i);
            stream = createStream(i);
            processInputFile(stream, i, atomProcessor, false);
        }
        previousSkippedAtoms[index] += atomProcessor.getNumberOfFilteredAtoms();
    }

    /**
     * Saves the iteration to file.
     *
     * @param index the index of the iteration
     * @throws NoSuchAlgorithmException if no Provider supports a MessageDigestSpi implementation for the specified
     *                                  algorithm.
     * @throws IOException              if an I/O error has occurred
     */
    protected void saveIteration(int index) throws IOException, NoSuchAlgorithmException {
        File iterationDirectory = getIterationDirectory(index);
        logger.debug(LogMessages.ITERATION_SAVING.toString(), index, iterationDirectory);
        if (!iterationDirectory.exists()) {
            if (!iterationDirectory.mkdirs()) {
                throw new IOException(
                        LanguageUtils.formatLogMessage(ExceptionMessages.ERROR_CREATING_DIRECTORY.toString(),
                                                       iterationDirectory));
            }
        }

        Set<Predicate> predicates = new HashSet<>();
        Map<Predicate, Set<Atom>> positiveMap = getPositives(previousAtoms);
        predicates.addAll(positiveMap.keySet());
        Map<Predicate, Set<Atom>> negativeMap = getNegatives(previousAtoms);
        predicates.addAll(negativeMap.keySet());

        Set<Atom> outputAtoms;
        for (Predicate predicate : predicates) {
            outputAtoms = positiveMap.computeIfAbsent(predicate, k -> new HashSet<>());
            writePredicateToFile(index, predicate, outputAtoms, true);
            outputAtoms = negativeMap.computeIfAbsent(predicate, k -> new HashSet<>());
            writePredicateToFile(index, predicate, outputAtoms, false);
        }
        logger.info(LogMessages.ITERATION_SAVED.toString(), index, iterationDirectory);
    }

    /**
     * Writes the predicate to file.
     *
     * @param index       the index of the iteration
     * @param predicate   the predicate
     * @param outputAtoms the output atoms
     * @param positive    if the atoms are positive or negative
     * @throws IOException              if an I/O error has occurred
     * @throws NoSuchAlgorithmException if no Provider supports a MessageDigestSpi implementation for the specified
     *                                  algorithm.
     */
    protected void writePredicateToFile(int index, Predicate predicate, Set<Atom> outputAtoms, boolean positive)
            throws IOException, NoSuchAlgorithmException {
        File iterationDirectory = getIterationDirectory(index);
        final String outputExtension = positive ? this.positiveOutputExtension : this.negativeOutputExtension;
        File outputFile = new File(iterationDirectory, predicate.getName() + outputExtension);
        String hash = saveIterationPredicate(outputAtoms, outputFile);
        (positive ? positiveOutputSize : negativeOutputSize)[index] += outputAtoms.size();
        (positive ? outputPositiveHash : outputNegativeHash)[index].put(predicate, hash);
    }

    /**
     * Saves the atoms to file.
     *
     * @param atoms the atoms
     * @param file  the file
     * @return the hash of the saved file
     * @throws NoSuchAlgorithmException if no Provider supports a MessageDigestSpi implementation for the specified
     *                                  algorithm.
     * @throws IOException              if an I/O error has occurred
     */
    protected String saveIterationPredicate(Iterable<? extends Atom> atoms, File file)
            throws IOException, NoSuchAlgorithmException {
        OutputStream stream = new FileOutputStream(file);
        MessageDigest digest = MessageDigest.getInstance(hashAlgorithm);
        stream = new DigestOutputStream(stream, digest);
        OutputStreamWriter writer = new OutputStreamWriter(stream, fileEncode);
        try (BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
            for (Atom atom : atoms) {
                bufferedWriter.write(atom.toString());
                bufferedWriter.write(LanguageUtils.CLAUSE_END_OF_LINE);
                bufferedWriter.write("\n");
            }
        } catch (IOException e) {
            logger.error(ERROR_WRITING_FILE.toString(), e);
        }

        return Hex.encodeHexString(digest.digest());
    }

    /**
     * Gets the directory to save the iteration.
     *
     * @param index the index of the input file
     * @return the output directory
     */
    protected File getIterationDirectory(int index) {
        return new File(outputDataDirectory, iterationPrefix + index);
    }

    /**
     * Reads the file to the memory, also calculating the hash of the file.
     *
     * @param index the index of the file
     * @throws NoSuchAlgorithmException if no Provider supports a MessageDigestSpi implementation for the specified
     *                                  algorithm.
     * @throws IOException              if an I/O error has occurred
     */
    protected void readFile(int index) throws NoSuchAlgorithmException, IOException {
        currentAtoms = buildMapPair();
        InputStream stream = createDigestStream(index);
        AddAtomProcessor atomProcessor = new AddAtomProcessor(previousAtoms, currentAtoms);
        processInputFile(stream, index, atomProcessor, true);
        previousSkippedAtoms[index] += atomProcessor.getNumberOfSkippedAtoms();
        if (index > 0) { removedAtoms[index - 1] = atomProcessor.getNumberOfRemovedAtoms(); }
    }

    /**
     * Process the input file reading the line and applying and {@link AtomProcessor} for each read atom.
     *
     * @param stream        the stream
     * @param index         the index of the input file
     * @param atomProcessor the {@link AtomProcessor}
     * @param logHash       if it is to log the hash
     * @throws UnsupportedEncodingException if the encode is not supported
     */
    @SuppressWarnings("OverlyLongMethod")
    protected void processInputFile(InputStream stream, int index, AtomProcessor atomProcessor, boolean logHash) throws
            UnsupportedEncodingException {
        InputStreamReader inputStreamReader = new InputStreamReader(stream, fileEncode);
        Pair<Atom, Boolean> pair;
        try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            logger.info(LogMessages.PROCESSING_FILE.toString(), nellInputFilePaths[index]);
            String line;
            line = bufferedReader.readLine();
            int count = 1;
            if (logHash) {
                logger.trace(LogMessages.PROCESSING_FILE_HEADER.toString(), nellInputFilePaths[index],
                             StringEscapeUtils.escapeJava(line));
            }
            readHeader(index, line);
            line = bufferedReader.readLine();
            while (line != null) {
                count++;
                if (line.isEmpty() || line.startsWith(commentCharacter)) { continue; }
                pair = readLine(line, index);
                if (isToProcessAtom(pair)) { atomProcessor.isAtomProcessed(pair); }
                line = bufferedReader.readLine();
            }
            if (logHash) { logHash(index, count); }
        } catch (IOException e) {
            logger.error(ERROR_READING_FILE.toString(), e);
        }
    }

    /**
     * If it is to skip the current atom.
     *
     * @param pair the atom with the label
     * @return {@code true} if it is to skip {@code false}, otherwise
     */
    protected boolean isToProcessAtom(Pair<Atom, Boolean> pair) {
        return pair != null;
    }

    /**
     * Logs the hash(es) of the input file.
     *
     * @param index the index of the {@link #nellInputFilePaths}
     * @param lines the number of read lines
     */
    protected void logHash(int index, int lines) {
        logger.info(FILE_CONTAINS_LINES.toString(), nellInputFilePaths[index], numberFormat.format(lines));
        String hash = Hex.encodeHexString(normalDigest.digest());
        inputHash[index] = hash;
        logger.info(FILE_NORMAL_HASH.toString(), hashAlgorithm, hash);
        if (zippedDigest != null) {
            String zippedHash = Hex.encodeHexString(zippedDigest.digest());
            inputZippedHash[index] = zippedHash;
            logger.info(FILE_ZIPPED_HASH.toString(), hashAlgorithm, zippedHash);
        }
    }

    /**
     * Creates the stream of the input file with the correspondent digest and saves it at the {@link #normalDigest}.
     * <p>
     * If the file is zipped (.gz, .gzip or .zip), the zipped digest is saved at the {@link #zippedDigest}.
     *
     * @param index the index of the {@link #nellInputFilePaths}
     * @return the {@link InputStream}
     * @throws NoSuchAlgorithmException if no Provider supports a MessageDigestSpi implementation for the specified
     *                                  algorithm.
     * @throws IOException              if an I/O error has occurred
     */
    protected InputStream createDigestStream(int index) throws NoSuchAlgorithmException, IOException {
        String filePath = nellInputFilePaths[index];
        InputStream stream = new FileInputStream(filePath);
        zippedDigest = null;
        if (filePath.endsWith(GZIP_SHORT_SUFFIX) || filePath.endsWith(GZIP_LONG_SUFFIX)) {
            zippedDigest = MessageDigest.getInstance(hashAlgorithm);
            stream = new DigestInputStream(stream, zippedDigest);
            stream = new GZIPInputStream(stream);
        } else if (filePath.endsWith(ZIP_SUFFIX)) {
            zippedDigest = MessageDigest.getInstance(hashAlgorithm);
            stream = new DigestInputStream(stream, zippedDigest);
            //noinspection resource,IOResourceOpenedButNotSafelyClosed
            ZipInputStream zipInputStream = new ZipInputStream(stream);
            zipInputStream.getNextEntry();
            stream = zipInputStream;
        }
        normalDigest = MessageDigest.getInstance(hashAlgorithm);
        stream = new DigestInputStream(stream, normalDigest);
        return stream;
    }

    /**
     * Creates the stream of the input file with the zip stream if needed.
     *
     * @param index the index of the {@link #nellInputFilePaths}
     * @return the {@link InputStream}
     * @throws IOException if an I/O error has occurred
     */
    protected InputStream createStream(int index) throws IOException {
        String filePath = nellInputFilePaths[index];
        InputStream stream = new FileInputStream(filePath);
        if (filePath.endsWith(GZIP_SHORT_SUFFIX) || filePath.endsWith(GZIP_LONG_SUFFIX)) {
            stream = new GZIPInputStream(stream);
        } else if (filePath.endsWith(ZIP_SUFFIX)) {
            stream = new DigestInputStream(stream, zippedDigest);
            //noinspection resource,IOResourceOpenedButNotSafelyClosed
            ZipInputStream zipInputStream = new ZipInputStream(stream);
            zipInputStream.getNextEntry();
            stream = zipInputStream;
        }
        return stream;
    }

    /**
     * Reads the header from the file and load the fields' indexes
     *
     * @param header the header
     * @param index  the index of the {@link #nellInputFilePaths}
     * @throws IOException if one or more indexes can not be found
     */
    @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod"})
    protected void readHeader(int index, String header) throws IOException {
        String filePath = nellInputFilePaths[index];
        String[] fields = header.toLowerCase().split(valueSeparator);
        for (int i = 0; i < fields.length; i++) {
            if (subjectIndexes[index] < 0 && fields[i].contains(subjectName)) {
                subjectIndexes[index] = i;
                logIndexFound(subjectName, subjectIndexes[index]);
                continue;
            }
            if (objectIndexes[index] < 0 && fields[i].contains(objectName)) {
                objectIndexes[index] = i;
                logIndexFound(objectName, objectIndexes[index]);
                continue;
            }
            if (predicateIndexes[index] < 0 && fields[i].contains(predicateName)) {
                predicateIndexes[index] = i;
                logIndexFound(predicateName, predicateIndexes[index]);
                continue;
            }
            if (confidenceIndexes[index] < 0 && fields[i].contains(confidenceName)) {
                confidenceIndexes[index] = i;
                logIndexFound(confidenceName, confidenceIndexes[index]);
            }
        }

        List<String> list = new ArrayList<>();
        if (subjectIndexes[index] < 0) { list.add(subjectName); }
        if (objectIndexes[index] < 0) { list.add(objectName); }
        if (predicateIndexes[index] < 0) { list.add(predicateName); }
        if (confidenceIndexes[index] < 0) { list.add(confidenceName); }

        if (!list.isEmpty()) {
            throw new IOException(LanguageUtils.formatLogMessage(ExceptionMessages.INDEXES_NOT_FOUND.toString(),
                                                                 ExceptionMessages.formatList(list), filePath));
        }
    }

    /**
     * Reads the line into a literal.
     *
     * @param line  the line
     * @param index the index of the current input file
     * @return the atom and if its label
     */
    @SuppressWarnings({"OverlyLongMethod"})
    protected Pair<Atom, Boolean> readLine(String line, int index) {
        String[] fields = line.split(valueSeparator);
        String[] confidences;
        String predicate = fields[predicateIndexes[index]].trim();
        predicate = formatName(predicate, predicateReplaceMap);
        if (skipPredicate.contains(predicate)) { return null; }
        String confidenceString = fields[confidenceIndexes[index]].trim();
        double confidence;
        if (confidenceString.isEmpty()) {
            return null;
        }
        if (confidenceString.startsWith(CONFIDENCE_OPEN_ARRAY)) {
            try {
                confidenceString = confidenceString.substring(CONFIDENCE_OPEN_ARRAY.length(),
                                                              confidenceString.length() -
                                                                      CONFIDENCE_OPEN_ARRAY.length());
                confidences = confidenceString.split(CONFIDENCE_VALUE_SEPARATOR);
                confidence = averageProbability(confidences);
            } catch (NumberFormatException ignore) {
                return null;
            }
        } else {
            confidence = Double.parseDouble(confidenceString);
        }
        String subject = formatName(fields[subjectIndexes[index]].trim(), entityReplaceMap);
        String object = formatName(fields[objectIndexes[index]].trim(), entityReplaceMap);
        Atom atom = atomFactory.createAtom(predicate, subject, object);

        return new ImmutablePair<>(atom, confidence >= confidenceThreshold);
    }

    /**
     * Saves the description files with all the information to reproduce the data.
     *
     * @throws UnsupportedEncodingException if the encoding is not supported
     * @throws NoSuchAlgorithmException     if no Provider supports a MessageDigestSpi implementation for the specified
     *                                      algorithm.
     */
    @SuppressWarnings({"OverlyLongMethod", "NonConstantStringShouldBeStringBuffer"})
    protected void saveDescriptionFile() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest totalHash = MessageDigest.getInstance(hashAlgorithm);
        String hash;
        File iterationDirectory;
        List<Predicate> predicates;
        File outputFile;
        int maxSize = 1;
        DecimalFormat format = new DecimalFormat("0.00");
        MessageDigest digest;
        String positiveHash;
        String negativeHash;
        String positiveRate;
        String negativeRate;
        String positiveExamples;
        String negativeExamples;
        for (int i = startIndex; i < nellInputFilePaths.length; i++) {
            digest = MessageDigest.getInstance(hashAlgorithm);
            iterationDirectory = getIterationDirectory(i);
            logger.info("");
            logger.info(FILE_SAVE.toString(), nellInputFilePaths[i], iterationDirectory.getAbsolutePath());
            hash = inputHash[i];
            logger.info(FILE_NORMAL_HASH.toString(), hashAlgorithm, hash);
            hash = inputZippedHash[i];
            if (hash != null) { logger.info(FILE_ZIPPED_HASH.toString(), hashAlgorithm, hash); }
            predicates = new ArrayList<>(outputPositiveHash[i].keySet());
            predicates.sort(Comparator.comparing(Predicate::getName));
            if (logger.isTraceEnabled()) {
                OptionalInt optionalMaxSize = predicates.stream().mapToInt(p -> p.getName().length()).max();
                maxSize = (optionalMaxSize.isPresent() ? optionalMaxSize.getAsInt() : 1);
                maxSize += Math.max(positiveOutputExtension.length(), negativeOutputExtension.length());
            }
            for (Predicate key : predicates) {
                outputFile = new File(iterationDirectory, key.getName() + positiveOutputExtension);
                positiveHash = outputPositiveHash[i].get(key);
                logger.trace(FILE_HASH_AND_SIZE.toString(), outputFile.getName(),
                             LanguageUtils.getTabulation(outputFile.getName(), maxSize),
                             positiveHash, positiveOutputSize[i]);
                outputFile = new File(iterationDirectory, key.getName() + negativeOutputExtension);
                negativeHash = outputNegativeHash[i].get(key);
                logger.trace(FILE_HASH_AND_SIZE.toString(), outputFile.getName(),
                             LanguageUtils.getTabulation(outputFile.getName(), maxSize),
                             negativeHash, negativeOutputSize[i]);
                digest.update(positiveHash.getBytes(fileEncode));
                digest.update(negativeHash.getBytes(fileEncode));
                totalHash.update(positiveHash.getBytes(fileEncode));
                totalHash.update(negativeHash.getBytes(fileEncode));
            }
            if (positiveOutputSize[i] + negativeOutputSize[i] == 0) {
                positiveRate = "-";
                negativeRate = "-";
            } else {
                positiveRate = format.format((float) positiveOutputSize[i] / (positiveOutputSize[i] +
                        negativeOutputSize[i]) * PERCENT_FACTOR);
                negativeRate = format.format((float) negativeOutputSize[i] / (positiveOutputSize[i] +
                        negativeOutputSize[i]) * PERCENT_FACTOR);
            }
            String totalExamples = numberFormat.format(positiveOutputSize[i] + negativeOutputSize[i]);
            positiveExamples = numberFormat.format(positiveOutputSize[i]);
            positiveExamples = positiveExamples + LanguageUtils.getTabulation(positiveExamples, totalExamples.length());
            negativeExamples = numberFormat.format(negativeOutputSize[i]);
            negativeExamples = negativeExamples + LanguageUtils.getTabulation(negativeExamples, totalExamples.length());

            logger.info("");
            logger.info(TOTAL_NUMBER_PREDICATES.toString(), predicates.size());
            logger.info(TOTAL_NUMBER_POSITIVES.toString(), positiveExamples, positiveRate);
            logger.info(TOTAL_NUMBER_NEGATIVES.toString(), negativeExamples, negativeRate);
            logger.info(TOTAL_NUMBER_EXAMPLES.toString(), totalExamples);
            logger.info(TOTAL_SKIPPED_EXAMPLES.toString(), numberFormat.format(previousSkippedAtoms[i]));
            logger.info(TOTAL_REMOVED_EXAMPLES.toString(), numberFormat.format(removedAtoms[i]));

            logger.info(ITERATION_TOTAL_HASH.toString(), Hex.encodeHexString(digest.digest()));
            logger.info("");
        }

        saveInputHash();

        String outputHash = Hex.encodeHexString(totalHash.digest());
        String configHash = DigestUtils.shaHex(String.valueOf(hashCode()));
        totalHash = MessageDigest.getInstance(hashAlgorithm);
        totalHash.update(outputHash.getBytes(fileEncode));
        totalHash.update(configHash.getBytes(fileEncode));
        logger.info(CONFIGURATION_TOTAL_HASH.toString(), configHash);
        logger.info(OUTPUT_TOTAL_HASH.toString(), outputHash);
        logger.info(TOTAL_HASH.toString(), Hex.encodeHexString(totalHash.digest()));
    }

    /**
     * Logs the found index.
     *
     * @param name  the name
     * @param index the index
     */
    protected void logIndexFound(String name, int index) {
        logger.info(FOUND_INDEX.toString(), name, LanguageUtils.getTabulation(name, maxNameSize), index);
    }

    /**
     * Saves the input hash.
     */
    protected void saveInputHash() {
        if (!logger.isInfoEnabled()) { return; }
        logger.info(FILE_HASH_HEADER.toString());
        String hash;
        for (int i = 0; i < nellInputFilePaths.length; i++) {
            if (inputZippedHash[i] != null) {
                hash = inputZippedHash[i];
            } else {
                hash = inputHash[i];
            }
            logger.info(FILE_HASH.toString(), hash, new File(nellInputFilePaths[i]).getName());
        }
        logger.info(FILE_HASH_FOOTER.toString());
        logger.info("");
    }

    /**
     * Gets the subject name.
     *
     * @return subject name
     */
    public String getSubjectName() {
        return subjectName;
    }

    /**
     * Sets the subject name.
     *
     * @param subjectName subject name
     */
    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName.toLowerCase();
        updateMaxNameSize(subjectName);
    }

    /**
     * Updates the maxNameSize
     *
     * @param name the name
     */
    protected void updateMaxNameSize(String name) {
        maxNameSize = Math.max(maxNameSize, name.length());
    }

    /**
     * Gets the object name.
     *
     * @return object name
     */
    public String getObjectName() {
        return objectName;
    }

    /**
     * Sets the object name.
     *
     * @param objectName object name
     */
    public void setObjectName(String objectName) {
        this.objectName = objectName.toLowerCase();
        updateMaxNameSize(objectName);
    }

    /**
     * Gets the predicate name.
     *
     * @return predicate name
     */
    public String getPredicateName() {
        return predicateName;
    }

    /**
     * Sets the predicate name.
     *
     * @param predicateName predicate name
     */
    public void setPredicateName(String predicateName) {
        this.predicateName = predicateName.toLowerCase();
        updateMaxNameSize(predicateName);
    }

    /**
     * Gets the confidence name.
     *
     * @return confidence name
     */
    public String getConfidenceName() {
        return confidenceName;
    }

    /**
     * Sets the confidence name.
     *
     * @param confidenceName confidence name
     */
    public void setConfidenceName(String confidenceName) {
        this.confidenceName = confidenceName.toLowerCase();
        updateMaxNameSize(confidenceName);
    }

    @SuppressWarnings("OverlyComplexMethod")
    @Override
    public int hashCode() {
        int result;
        long temp;
        result = commentCharacter != null ? commentCharacter.hashCode() : 0;
        result = 31 * result + (valueSeparator != null ? valueSeparator.hashCode() : 0);
        result = 31 * result + (subjectName != null ? subjectName.hashCode() : 0);
        result = 31 * result + (objectName != null ? objectName.hashCode() : 0);
        result = 31 * result + (predicateName != null ? predicateName.hashCode() : 0);
        result = 31 * result + (confidenceName != null ? confidenceName.hashCode() : 0);
        temp = Double.doubleToLongBits(confidenceThreshold);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (positiveOutputExtension != null ? positiveOutputExtension.hashCode() : 0);
        result = 31 * result + (negativeOutputExtension != null ? negativeOutputExtension.hashCode() : 0);
        result = 31 * result + (hashAlgorithm != null ? hashAlgorithm.hashCode() : 0);
        result = 31 * result + (fileEncode != null ? fileEncode.hashCode() : 0);
        result = 31 * result + (entityReplaceMap != null ? entityReplaceMap.hashCode() : 0);
        result = 31 * result + (predicateReplaceMap != null ? predicateReplaceMap.hashCode() : 0);
        result = 31 * result + (skipPredicate != null ? skipPredicate.hashCode() : 0);
        return result;
    }

    @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod"})
    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof NellBaseConverterCLI)) { return false; }

        NellBaseConverterCLI that = (NellBaseConverterCLI) o;

        if (Double.compare(that.confidenceThreshold, confidenceThreshold) != 0) { return false; }
        if (maxNameSize != that.maxNameSize) { return false; }
        if (commentCharacter != null ? !commentCharacter.equals(that.commentCharacter) : that.commentCharacter !=
                null) {
            return false;
        }
        if (valueSeparator != null ? !valueSeparator.equals(that.valueSeparator) : that.valueSeparator != null) {
            return false;
        }
        if (subjectName != null ? !subjectName.equals(that.subjectName) : that.subjectName != null) { return false; }
        if (objectName != null ? !objectName.equals(that.objectName) : that.objectName != null) { return false; }
        if (predicateName != null ? !predicateName.equals(that.predicateName) : that.predicateName != null) {
            return false;
        }
        if (confidenceName != null ? !confidenceName.equals(that.confidenceName) : that.confidenceName != null) {
            return false;
        }
        if (positiveOutputExtension != null ? !positiveOutputExtension.equals(that.positiveOutputExtension) : that
                .positiveOutputExtension != null) {
            return false;
        }
        if (negativeOutputExtension != null ? !negativeOutputExtension.equals(that.negativeOutputExtension) : that
                .negativeOutputExtension != null) {
            return false;
        }
        if (hashAlgorithm != null ? !hashAlgorithm.equals(that.hashAlgorithm) : that.hashAlgorithm != null) {
            return false;
        }
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(nellInputFilePaths, that.nellInputFilePaths)) { return false; }
        if (fileEncode != null ? !fileEncode.equals(that.fileEncode) : that.fileEncode != null) { return false; }
        if (entityReplaceMap != null ? !entityReplaceMap.equals(that.entityReplaceMap) : that.entityReplaceMap !=
                null) {
            return false;
        }
        if (predicateReplaceMap != null ? !predicateReplaceMap.equals(that.predicateReplaceMap) : that
                .predicateReplaceMap != null) {
            return false;
        }
        if (skipPredicate != null ? !skipPredicate.equals(that.skipPredicate) : that.skipPredicate != null) {
            return false;
        }
        if (iterationPrefix != null ? !iterationPrefix.equals(that.iterationPrefix) : that.iterationPrefix != null) {
            return false;
        }
        if (dataDirectoryName != null ? !dataDirectoryName.equals(that.dataDirectoryName) : that.dataDirectoryName !=
                null) {
            return false;
        }
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(inputHash, that.inputHash)) { return false; }
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(inputZippedHash, that.inputZippedHash)) { return false; }
        return outputDataDirectory != null ? outputDataDirectory.equals(that.outputDataDirectory) : that
                .outputDataDirectory == null;
    }

    @SuppressWarnings("OverlyLongMethod")
    @Override
    public String toString() {
        StringBuilder description = new StringBuilder();
        description.append("\t").append("Settings:");

        description.append("\n").append("\t").append("File encode:\t\t\t\t\"").append(fileEncode).append("\"");
        description.append("\n").append("\t").append("Comment character:\t\t\t\"").append(commentCharacter)
                .append("\"");
        description.append("\n").append("\t").append("Value separator:\t\t\t\"")
                .append(StringEscapeUtils.escapeJava(valueSeparator)).append("\"\n");
        description.append("\n").append("\t").append("Subject name:\t\t\t\t\"").append(subjectName).append("\"");
        description.append("\n").append("\t").append("Object name:\t\t\t\t\"").append(objectName).append("\"");
        description.append("\n").append("\t").append("Predicate name:\t\t\t\t\"").append(predicateName).append("\"");
        description.append("\n").append("\t").append("Confidence name:\t\t\t\"").append(confidenceName).append("\"");

        description.append("\n\n").append("\t").append("Confidence Threshold:\t\t").append(confidenceThreshold);

        description.append("\n\n").append("\t").append("Positive output extension:\t\"").append(positiveOutputExtension)
                .append("\"").append("\n").append("\t").append("Negative output extension:\t\"")
                .append(negativeOutputExtension).append("\"");

        description.append("\n\n").append("\t").append("Hash algorithm:\t\t\t\t\"").append(hashAlgorithm).append("\"");

        description.append("\n").append("\t").append("Entity Replace Map:\n");
        for (Map.Entry<String, String> entry : entityReplaceMap.entrySet()) {
            description.append("\t\t\"").append(entry.getKey());
            description.append("\"\t:\t\"").append(entry.getValue()).append("\"\n");
        }
        description.append("\tPredicate Replace Map:\n");
        for (Map.Entry<String, String> entry : predicateReplaceMap.entrySet()) {
            description.append("\t\t\"").append(entry.getKey());
            description.append("\"\t:\t\"").append(entry.getValue()).append("\"\n");
        }
        description.append("\tSkip Predicate:\n");
        for (String entry : skipPredicate) {
            description.append("\t\t\"").append(entry).append("\"\n");
        }

        description.append("\n").append("\t").append("Output directory path:\t").append(outputDirectoryPath);
        description.append("\n");
        return description.toString();
    }

}