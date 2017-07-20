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
import com.esotericsoftware.yamlbeans.YamlException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
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
@SuppressWarnings("CanBeFinal")
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
     * The tabulation size.
     */
    public static final int TABULATION_SIZE = 4;
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

    protected int maxNameSize = confidenceName.length();
    protected int subjectIndex = -1;
    protected int objectIndex = -1;
    protected int predicateIndex = -1;
    protected int confidenceIndex = -1;
    protected int currentIndex = -1;

    protected Map<Predicate, Set<Atom>>[] positiveAtoms;
    protected Map<Predicate, Set<Atom>>[] negativeAtoms;
    protected String[] inputHash;
    protected String[] inputZippedHash;
    protected Map<Predicate, String>[] outputPositiveHash;
    protected Map<Predicate, String>[] outputNegativeHash;
    protected AtomFactory atomFactory = new AtomFactory();
    protected File outputDataDirectory;

    /**
     * Default constructor.
     */
    public NellBaseConverterCLI() {
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

    @Override
    public void run() {
        try {
            long begin = TimeMeasure.getNanoTime();
            initializeFields();
            readFiles();
            saveIterations();
            saveDescriptionFile();
            long end = TimeMeasure.getNanoTime();
            logger.info(HASH_DISCLAIMER);
            logger.warn(TOTAL_PROGRAM_TIME.toString(), TimeMeasure.formatNanoDifference(begin, end));
        } catch (Exception e) {
            logger.error(ExceptionMessages.GENERAL_ERROR.toString(), e);
        }
    }

    /**
     * Initializes the fields.
     */
    protected void initializeFields() {
        positiveAtoms = new HashMap[nellInputFilePaths.length];
        negativeAtoms = new HashMap[nellInputFilePaths.length];

        inputHash = new String[nellInputFilePaths.length];
        inputZippedHash = new String[nellInputFilePaths.length];

        outputPositiveHash = new HashMap[nellInputFilePaths.length];
        outputNegativeHash = new HashMap[nellInputFilePaths.length];

        outputDataDirectory = new File(outputDirectory, dataDirectoryName);
        deleteDataDirectory(outputDataDirectory);
    }

    /**
     * Reads the files to memory.
     *
     * @throws NoSuchAlgorithmException if no Provider supports a MessageDigestSpi implementation for the specified
     *                                  algorithm.
     * @throws IOException              if an I/O error has occurred
     */
    protected void readFiles() throws NoSuchAlgorithmException, IOException {
        for (int i = 0; i < nellInputFilePaths.length; i++) {
            currentIndex = i;
            positiveAtoms[i] = new HashMap<>();
            negativeAtoms[i] = new HashMap<>();
            readFile();
        }
    }

    /**
     * Saves the iterations to files.
     *
     * @throws NoSuchAlgorithmException if no Provider supports a MessageDigestSpi implementation for the specified
     *                                  algorithm.
     * @throws IOException              if an I/O error has occurred
     */
    protected void saveIterations() throws IOException, NoSuchAlgorithmException {
        for (int i = 0; i < nellInputFilePaths.length; i++) {
            currentIndex = i;
            outputPositiveHash[currentIndex] = new HashMap<>();
            outputNegativeHash[currentIndex] = new HashMap<>();
            saveIteration();
        }
    }

    /**
     * Saves the iteration to file.
     *
     * @throws NoSuchAlgorithmException if no Provider supports a MessageDigestSpi implementation for the specified
     *                                  algorithm.
     * @throws IOException              if an I/O error has occurred
     */
    protected void saveIteration() throws IOException, NoSuchAlgorithmException {
        File iterationDirectory = getIterationDirectory(currentIndex);
        if (!iterationDirectory.exists()) {
            if (!iterationDirectory.mkdirs()) {
                throw new IOException(
                        LanguageUtils.formatLogMessage(ExceptionMessages.ERROR_CREATING_DIRECTORY.toString(),
                                                       iterationDirectory));
            }
        }

        Set<Predicate> predicates = new HashSet<>();
        Map<Predicate, Set<Atom>> positiveMap = positiveAtoms[currentIndex];
        predicates.addAll(positiveMap.keySet());
        Map<Predicate, Set<Atom>> negativeMap = negativeAtoms[currentIndex];
        predicates.addAll(negativeMap.keySet());

        File outputFile;
        String hash;
        for (Predicate predicate : predicates) {
            outputFile = new File(iterationDirectory, predicate.getName() + positiveOutputExtension);
            hash = saveIterationPredicate(positiveMap.computeIfAbsent(predicate, k -> new HashSet<>()), outputFile);
            outputPositiveHash[currentIndex].put(predicate, hash);
            outputFile = new File(iterationDirectory, predicate.getName() + negativeOutputExtension);
            hash = saveIterationPredicate(negativeMap.computeIfAbsent(predicate, k -> new HashSet<>()), outputFile);
            outputNegativeHash[currentIndex].put(predicate, hash);
        }
    }

    private File getIterationDirectory(int index) {
        return new File(outputDataDirectory, iterationPrefix + index);
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
     * Reads the file to the memory, also calculating the hash of the file.
     *
     * @throws NoSuchAlgorithmException if no Provider supports a MessageDigestSpi implementation for the specified
     *                                  algorithm.
     * @throws IOException              if an I/O error has occurred
     */
    @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod"})
    protected void readFile() throws NoSuchAlgorithmException, IOException {
        String filePath = nellInputFilePaths[currentIndex];
        InputStream stream = new FileInputStream(filePath);
        MessageDigest zippedDigest = null;
        if (filePath.endsWith(GZIP_SHORT_SUFFIX) || filePath.endsWith(GZIP_LONG_SUFFIX)) {
            zippedDigest = MessageDigest.getInstance(hashAlgorithm);
            stream = new DigestInputStream(stream, zippedDigest);
            stream = new GZIPInputStream(stream);
        } else if (filePath.endsWith(ZIP_SUFFIX)) {
            zippedDigest = MessageDigest.getInstance(hashAlgorithm);
            stream = new DigestInputStream(stream, zippedDigest);
            @SuppressWarnings({"resource", "IOResourceOpenedButNotSafelyClosed"})
            ZipInputStream zipInputStream = new ZipInputStream(stream);
            zipInputStream.getNextEntry();
            stream = zipInputStream;
        }
        MessageDigest digest = MessageDigest.getInstance(hashAlgorithm);
        stream = new DigestInputStream(stream, digest);
        InputStreamReader inputStreamReader = new InputStreamReader(stream, fileEncode);

        try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            String line;
            line = bufferedReader.readLine();
            int count = 1;
            readHeader(line, filePath);
            line = bufferedReader.readLine();
            while (line != null) {
                count++;
                if (line.isEmpty() || line.startsWith(commentCharacter)) {
                    continue;
                }
                readLine(line);
                line = bufferedReader.readLine();
            }

            logger.debug(FILE_CONTAINS_LINES.toString(), filePath, count);
            String hash = Hex.encodeHexString(digest.digest());
            inputHash[currentIndex] = hash;
            logger.debug(FILE_NORMAL_HASH.toString(), hashAlgorithm, hash);
            if (zippedDigest != null) {
                String zippedHash = Hex.encodeHexString(zippedDigest.digest());
                inputZippedHash[currentIndex] = zippedHash;
                logger.debug(FILE_ZIPPED_HASH.toString(), hashAlgorithm, zippedHash);
            }
        } catch (IOException e) {
            logger.error(ERROR_READING_FILE.toString(), e);
        }
    }

    /**
     * Reads the header from the file and load the fields' indexes
     *
     * @param header   the header
     * @param filePath the file path
     * @throws IOException if one or more indexes can not be found
     */
    @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod"})
    protected void readHeader(String header, String filePath) throws IOException {
        String[] fields = header.toLowerCase().split(valueSeparator);
        for (int i = 0; i < fields.length; i++) {
            if (subjectIndex < 0 && fields[i].contains(subjectName)) {
                subjectIndex = i;
                logIndexFound(subjectName, subjectIndex);
                continue;
            }
            if (objectIndex < 0 && fields[i].contains(objectName)) {
                objectIndex = i;
                logIndexFound(objectName, objectIndex);
                continue;
            }
            if (predicateIndex < 0 && fields[i].contains(predicateName)) {
                predicateIndex = i;
                logIndexFound(predicateName, predicateIndex);
                continue;
            }
            if (confidenceIndex < 0 && fields[i].contains(confidenceName)) {
                confidenceIndex = i;
                logIndexFound(confidenceName, confidenceIndex);
            }
        }

        List<String> list = new ArrayList<>();
        if (subjectIndex < 0) { list.add(subjectName); }
        if (objectIndex < 0) { list.add(objectName); }
        if (predicateIndex < 0) { list.add(predicateName); }
        if (confidenceIndex < 0) { list.add(confidenceName); }

        if (!list.isEmpty()) {
            throw new IOException(LanguageUtils.formatLogMessage(ExceptionMessages.INDEXES_NOT_FOUND.toString(),
                                                                 ExceptionMessages.formatList(list), filePath));
        }
    }

    /**
     * Saves the description files with all the information to reproduce the data.
     *
     * @throws UnsupportedEncodingException if the encoding is not supported
     * @throws NoSuchAlgorithmException     if no Provider supports a MessageDigestSpi implementation for the specified
     *                                      algorithm.
     */
    @SuppressWarnings("OverlyLongMethod")
    protected void saveDescriptionFile() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //TODO: implement method
        MessageDigest totalHash = MessageDigest.getInstance(hashAlgorithm);
        String hash;
        File iterationDirectory;
        List<Predicate> predicates;
        File outputFile;
        int maxSize = 1;
        int positiveSize;
        int negativeSize;
        DecimalFormat format = new DecimalFormat("0.00");
        MessageDigest digest;
        String positiveHash;
        String negativeHash;
        String positiveRate;
        String negativeRate;
        for (int i = 0; i < nellInputFilePaths.length; i++) {
            digest = MessageDigest.getInstance(hashAlgorithm);
            int positiveTotal = 0;
            int negativeTotal = 0;
            iterationDirectory = getIterationDirectory(i);
            logger.info("");
            logger.info(FILE_SAVE.toString(), nellInputFilePaths[i], iterationDirectory.getAbsolutePath());
            hash = inputHash[i];
            logger.info(FILE_NORMAL_HASH.toString(), hashAlgorithm, hash);
            hash = inputZippedHash[i];
            if (hash != null) { logger.debug(FILE_ZIPPED_HASH.toString(), hashAlgorithm, hash); }
            predicates = new ArrayList<>(outputPositiveHash[i].keySet());
            predicates.sort(Comparator.comparing(Predicate::getName));
            if (logger.isTraceEnabled()) {
                OptionalInt optionalMaxSize = predicates.stream().mapToInt(p -> p.getName().length()).max();
                maxSize = (optionalMaxSize.isPresent() ? optionalMaxSize.getAsInt() : 1);
                maxSize += Math.max(positiveOutputExtension.length(), negativeOutputExtension.length());
            }
            for (Predicate key : predicates) {
                outputFile = new File(iterationDirectory, key.getName() + positiveOutputExtension);
                positiveSize = positiveAtoms[i].get(key).size();
                positiveHash = outputPositiveHash[i].get(key);
                logger.trace(FILE_HASH_AND_SIZE.toString(), outputFile.getName(),
                             getTab(outputFile.getName(), maxSize),
                             positiveHash, positiveSize);
                outputFile = new File(iterationDirectory, key.getName() + negativeOutputExtension);
                negativeSize = negativeAtoms[i].get(key).size();
                negativeHash = outputNegativeHash[i].get(key);
                logger.trace(FILE_HASH_AND_SIZE.toString(), outputFile.getName(),
                             getTab(outputFile.getName(), maxSize),
                             negativeHash, negativeSize);
                positiveTotal += positiveSize;
                negativeTotal += negativeSize;
                digest.update(positiveHash.getBytes(fileEncode));
                digest.update(negativeHash.getBytes(fileEncode));
                totalHash.update(positiveHash.getBytes(fileEncode));
                totalHash.update(negativeHash.getBytes(fileEncode));
            }
            if (positiveTotal + negativeTotal == 0) {
                positiveRate = "-";
                negativeRate = "-";
            } else {
                positiveRate = format.format((float) positiveTotal / (positiveTotal + negativeTotal) * PERCENT_FACTOR);
                negativeRate = format.format((float) negativeTotal / (positiveTotal + negativeTotal) * PERCENT_FACTOR);
            }
            logger.info("");
            logger.debug(TOTAL_NUMBER_PREDICATES.toString(), predicates.size());
            logger.info(TOTAL_NUMBER_POSITIVES.toString(), positiveTotal, positiveRate);
            logger.info(TOTAL_NUMBER_NEGATIVES.toString(), negativeTotal, negativeRate);
            logger.info(TOTAL_NUMBER_EXAMPLES.toString(), positiveTotal + negativeTotal);

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
            logger.info(FILE_HASH.toString(), hash, nellInputFilePaths[i]);
        }
        logger.info(FILE_HASH_FOOTER.toString());
        logger.info("");
    }

    /**
     * Reads the line into a literal.
     *
     * @param line the line
     * @return the literal
     */
    @SuppressWarnings({"BooleanMethodNameMustStartWithQuestion", "OverlyLongMethod"})
    protected boolean readLine(String line) {
        String[] fields = line.split(valueSeparator);
        String[] confidences;
        String predicate = fields[predicateIndex].trim();
        if (skipPredicate.contains(predicate)) { return false; }
        predicate = formatName(predicate, predicateReplaceMap);
        String confidenceString = fields[confidenceIndex].trim();
        double confidence;
        if (confidenceString.isEmpty()) {
            return false;
        }
        if (confidenceString.startsWith(CONFIDENCE_OPEN_ARRAY)) {
            try {
                confidenceString = confidenceString.substring(CONFIDENCE_OPEN_ARRAY.length(),
                                                              confidenceString.length() -
                                                                      CONFIDENCE_OPEN_ARRAY.length());
                confidences = confidenceString.split(CONFIDENCE_VALUE_SEPARATOR);
                confidence = averageProbability(confidences);
            } catch (NumberFormatException ignore) {
                return false;
            }
        } else {
            confidence = Double.parseDouble(confidenceString);
        }
        String subject = formatName(fields[subjectIndex].trim(), entityReplaceMap);
        String object = formatName(fields[objectIndex].trim(), entityReplaceMap);
        Atom atom = atomFactory.createAtom(predicate, subject, object);

        return addAtom(atom, confidence >= confidenceThreshold);
    }

    /**
     * Logs the found index.
     *
     * @param name  the name
     * @param index the index
     */
    protected void logIndexFound(String name, int index) {
        logger.debug(FOUND_INDEX.toString(), name, getTab(name, maxNameSize), index);
    }

    private static String getTab(String name, int maxNameSize) {
        return StringUtils.repeat("\t", Math.round((float) (maxNameSize - name.length()) / TABULATION_SIZE) + 1);
    }

    /**
     * Adds the atom to the set of the current iteration.
     * <p>
     * If the atom already appears in this iteration, ignores it.
     * <p>
     * If the atom appears if same label in a previous iteration, ignores it.
     * <p>
     * if the atom appears if the opposite label on the iteration immediately before that, removes it from the
     * previous iteration.
     *
     * @param atom     the atom
     * @param positive if the atom is positive
     * @return {@code true} if the atom was added, {@code false} otherwise.
     */
    @SuppressWarnings("OverlyComplexMethod")
    protected boolean addAtom(Atom atom, boolean positive) {
        Map<Predicate, Set<Atom>>[] thisList = positive ? positiveAtoms : negativeAtoms;
        Map<Predicate, Set<Atom>>[] thatList = positive ? negativeAtoms : positiveAtoms;

        Set<Atom> atomSet;
        // if atom appears with another label in this iteration, ignore it.
        atomSet = thatList[currentIndex].get(atom.getPredicate());
        if (atomSet != null && atomSet.contains(atom)) { return false; }

        // looking at the older generations
        for (int i = 0; i < currentIndex; i++) {
            atomSet = thisList[i].get(atom.getPredicate());
            if (atomSet != null && atomSet.contains(atom)) { return false; }
        }

        // look at the previous generation
        if (currentIndex > 0) {
            atomSet = thatList[currentIndex - 1].get(atom.getPredicate());
            // if atom appears if another label on previous iteration, removes it.
            if (atomSet != null) { atomSet.remove(atom); }
        }

        // adding the atom to this list
        return thisList[currentIndex].computeIfAbsent(atom.getPredicate(), p -> new LinkedHashSet<>()).add(atom);
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

    @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod"})
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
//        result = 31 * result + Arrays.hashCode(nellInputFilePaths);
        result = 31 * result + (fileEncode != null ? fileEncode.hashCode() : 0);
        result = 31 * result + (entityReplaceMap != null ? entityReplaceMap.hashCode() : 0);
        result = 31 * result + (predicateReplaceMap != null ? predicateReplaceMap.hashCode() : 0);
        result = 31 * result + (skipPredicate != null ? skipPredicate.hashCode() : 0);
//        result = 31 * result + (iterationPrefix != null ? iterationPrefix.hashCode() : 0);
//        result = 31 * result + (dataDirectoryName != null ? dataDirectoryName.hashCode() : 0);
//        result = 31 * result + maxNameSize;
//        result = 31 * result + subjectIndex;
//        result = 31 * result + objectIndex;
//        result = 31 * result + predicateIndex;
//        result = 31 * result + confidenceIndex;
//        result = 31 * result + currentIndex;
//        result = 31 * result + Arrays.hashCode(positiveAtoms);
//        result = 31 * result + Arrays.hashCode(negativeAtoms);
//        result = 31 * result + Arrays.hashCode(inputHash);
//        result = 31 * result + Arrays.hashCode(inputZippedHash);
//        result = 31 * result + Arrays.hashCode(outputPositiveHash);
//        result = 31 * result + Arrays.hashCode(outputNegativeHash);
//        result = 31 * result + (atomFactory != null ? atomFactory.hashCode() : 0);
//        result = 31 * result + (outputDataDirectory != null ? outputDataDirectory.hashCode() : 0);
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
        if (subjectIndex != that.subjectIndex) { return false; }
        if (objectIndex != that.objectIndex) { return false; }
        if (predicateIndex != that.predicateIndex) { return false; }
        if (confidenceIndex != that.confidenceIndex) { return false; }
        if (currentIndex != that.currentIndex) { return false; }
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
        if (!Arrays.equals(positiveAtoms, that.positiveAtoms)) { return false; }
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(negativeAtoms, that.negativeAtoms)) { return false; }
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(inputHash, that.inputHash)) { return false; }
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(inputZippedHash, that.inputZippedHash)) { return false; }
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(outputPositiveHash, that.outputPositiveHash)) { return false; }
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(outputNegativeHash, that.outputNegativeHash)) { return false; }
        if (atomFactory != null ? !atomFactory.equals(that.atomFactory) : that.atomFactory != null) { return false; }
        return outputDataDirectory != null ? outputDataDirectory.equals(that.outputDataDirectory) : that
                .outputDataDirectory == null;
    }

    @Override
    public String toString() {
        StringBuilder description = new StringBuilder();
        description.append("\t").append("Settings:");
        description.append("\n").append("\t").append("Output directory path:\t").append(outputDirectoryPath);
//        description.append("\n").append("\t").append("Config Hash:\t\t\t").append(configHash());
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

        return description.toString().trim();
    }
}
