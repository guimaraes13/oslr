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
import br.ufrj.cos.logic.Constant;
import br.ufrj.cos.logic.Predicate;
import br.ufrj.cos.logic.Term;
import br.ufrj.cos.util.ExceptionMessages;
import br.ufrj.cos.util.InitializationException;
import br.ufrj.cos.util.LanguageUtils;
import br.ufrj.cos.util.LogMessages;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

/**
 * Class to convert a Knowledge base from Nell's csv files to a set of logic files.
 * <p>
 * Created on 16/07/17.
 *
 * @author Victor Guimarães
 */
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
    public static final int TABULATION_SIZE = 3;
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
     * The default arity.
     */
    public static final int DEFAULT_ARITY = 2;
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
     * The array of nell's input files, sorted by iteration.
     */
    public String[] nellInputFilePaths = null;
    /**
     * The file encode
     */
    public String fileEncode = LanguageUtils.DEFAULT_INPUT_ENCODE;
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
     * The set of predicate to skip. This checks occurs before the predicateReplaceMap be applied.
     */
    public Set<String> skipPredicate;

    protected Map<Predicate, List<Atom>> positiveAtoms;

    protected Map<Predicate, List<Atom>> negativeAtoms;
    protected int maxNameSize = confidenceName.length() + 1;
    protected int subjectIndex = -1;
    protected int objectIndex = -1;
    protected int predicateIndex = -1;
    protected int confidenceIndex = -1;

    /**
     * Default constructor.
     */
    public NellBaseConverterCLI() {
        positiveAtoms = new HashMap<>();
        negativeAtoms = new HashMap<>();

        entityReplaceMap = new HashMap<>();
        entityReplaceMap.put("\"", "");
        entityReplaceMap.put("\'", "");
        entityReplaceMap.put(":", "_");

        predicateReplaceMap = new HashMap<>();
        //noinspection HardCodedStringLiteral
        predicateReplaceMap.put("concept_", "");
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
            logger.error(LogMessages.ERROR_MAIN_PROGRAM, e);
        } finally {
            logger.fatal(LogMessages.PROGRAM_END);
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

    @Override
    public void run() {
        try {
            for (int i = 0; i < nellInputFilePaths.length; i++) {
                readFile(i);
                saveIterations();
                saveDescriptionFile();
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    /**
     * Reads the file to the memory, also calculating the hash of the file.
     *
     * @param index the index of the file
     * @throws NoSuchAlgorithmException if no Provider supports a MessageDigestSpi implementation for the specified
     *                                  algorithm.
     * @throws IOException              if an I/O error has occurred
     */
    @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod"})
    protected void readFile(int index) throws NoSuchAlgorithmException, IOException {
        String filePath = nellInputFilePaths[index];
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
            //noinspection AssignmentUsedAsCondition,NestedAssignment
            line = bufferedReader.readLine();
            int count = 1;
            readHeader(line, filePath);
            line = bufferedReader.readLine();
            while (line != null) {
                count++;
                if (line.isEmpty() || line.startsWith(commentCharacter)) {
                    continue;
                }
                line = bufferedReader.readLine();
            }

            logger.debug(LogMessages.FILE_CONTAINS_LINES.toString(), filePath, count);
            String hash = Hex.encodeHexString(digest.digest());
            logger.debug(LogMessages.FILE_NORMAL_HASH.toString(), hashAlgorithm, hash);
            if (zippedDigest != null) {
                String zippedHash = Hex.encodeHexString(zippedDigest.digest());
                logger.debug(LogMessages.FILE_ZIPPED_HASH.toString(), hashAlgorithm, zippedHash);
            }
        } catch (IOException e) {
            logger.error(LogMessages.ERROR_READING_FILE.toString(), e);
        }
    }

    /**
     * Saves the iteration to files.
     */
    protected void saveIterations() {
        //TODO: implement method
    }

    /**
     * Saves the description files with all the information to reproduce the data.
     */
    protected void saveDescriptionFile() {
        //TODO: implement method
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
     * Logs the found index.
     *
     * @param name  the name
     * @param index the index
     */
    protected void logIndexFound(String name, int index) {
        logger.debug(LogMessages.FOUND_INDEX.toString(),
                     name, StringUtils.repeat("\t", (maxNameSize - name.length()) / TABULATION_SIZE + 1), index);
    }

    public void readLine(String line, Set<Atom> positiveSet, Set<Atom> negativeSet) {
        String[] fields = line.split(valueSeparator);
        String predicate = fields[predicateIndex].trim();
        if (skipPredicate.contains(predicate)) { return; }
        predicate = formatName(predicate, predicateReplaceMap);
        String confidenceString = fields[confidenceIndex].trim();
        double confidence;
        if (confidenceString.isEmpty()) {
            return;
        }
        if (confidenceString.startsWith(CONFIDENCE_OPEN_ARRAY)) {
            confidence = averageProbability(
                    confidenceString.substring(CONFIDENCE_OPEN_ARRAY.length(),
                                               confidenceString.length() - CONFIDENCE_OPEN_ARRAY.length()));
        } else {
            confidence = Double.parseDouble(confidenceString);
        }
        String subject = formatName(fields[subjectIndex].trim(), entityReplaceMap);
        String object = formatName(fields[objectIndex].trim(), entityReplaceMap);

        //TODO: use a factory to create the atom to make the same predicate and constants point to the same object
        List<Term> terms = new ArrayList<>();
        terms.add(new Constant(subject));
        terms.add(new Constant(object));
        Atom atom = new Atom(new Predicate(predicate, DEFAULT_ARITY), terms);

        if (confidence >= confidenceThreshold) {
            positiveSet.add(atom);
        } else {
            negativeSet.add(atom);
        }
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
            entity = name.replaceAll(entry.getKey(), entry.getValue());
        }
        return entity;
    }

    /**
     * Calculates the average probability from a string representation of a array of doubles.
     *
     * @param confidenceString the array of doubles
     * @return the average probability
     */
    protected static double averageProbability(String confidenceString) {
        //TODO: implement
        OptionalDouble average = Arrays.stream(confidenceString.split(CONFIDENCE_VALUE_SEPARATOR))
                .mapToDouble(s -> Double.parseDouble(s.trim()))
                .average();
        return average.isPresent() ? average.getAsDouble() : 0.0;
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
        maxNameSize = Math.max(maxNameSize, name.length() + 1);
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

    @Override
    public String toString() {
        StringBuilder description = new StringBuilder();
        description.append("\t").append("Settings:");
        description.append("\n").append("\t").append("Output directory path:\t").append(outputDirectoryPath);
        description.append("\n").append("\t").append("Config Hash:\t\t\t").append(configHash());
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

    /**
     * Calculates the config SHA-1 hash.
     *
     * @return the config SHA-1 hash
     */
    protected String configHash() {
        String config = commentCharacter + valueSeparator + subjectName + objectName + predicateName + confidenceName
                + confidenceThreshold + positiveOutputExtension + negativeOutputExtension + hashAlgorithm +
                Arrays.toString(nellInputFilePaths) + fileEncode;
        return DigestUtils.shaHex(config);
    }

}
