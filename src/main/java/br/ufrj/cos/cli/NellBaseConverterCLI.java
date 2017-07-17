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

import br.ufrj.cos.util.InitializationException;
import br.ufrj.cos.util.LanguageUtils;
import br.ufrj.cos.util.LogMessages;
import com.esotericsoftware.yamlbeans.YamlException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
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
     * Default hash algorithm.
     */
    public static final String DEFAULT_HASH_ALGORITHM = "SHA-1";
    /**
     * Comment character for the input nell file
     */
    private static final String COMMENT_CHARACTER = "#";
    /**
     * Hash algorithm.
     */
    public String hashAlgorithm = DEFAULT_HASH_ALGORITHM;
    /**
     * The array of nell's input files, sorted by iteration.
     */
    public String[] nellInputFilePaths;
    /**
     * The file encode
     */
    public String fileEncode = LanguageUtils.DEFAULT_INPUT_ENCODE;

    protected Map<String, MessageDigest> defaultDigestMap;
    protected Map<String, MessageDigest> zippedDigestMap;

    /**
     * Default constructor.
     */
    public NellBaseConverterCLI() {
        defaultDigestMap = new HashMap<>();
        zippedDigestMap = new HashMap<>();
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
            if (nellInputFilePaths.length == 1) {
                convertSingleFile(nellInputFilePaths[0]);
            } else if (nellInputFilePaths.length == 2) {

            } else if (nellInputFilePaths.length > 2) {

            }
//            for (String filePath : nellInputFilePaths) {
//                readFile(filePath);
//            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    protected void convertSingleFile(String filePath) {
        try (BufferedReader bufferedReader = buildBufferedReader(filePath)) {
            String line;
            int count = 0;
            //noinspection AssignmentUsedAsCondition,NestedAssignment
            while ((line = bufferedReader.readLine()) != null) {
                count++;
                if (line.isEmpty() || line.startsWith(COMMENT_CHARACTER)) {
                    continue;
                }
            }

            logger.info("File:\t{}\tContains {} lines", filePath, count);
            MessageDigest digest = defaultDigestMap.get(filePath);
            String hash = (new HexBinaryAdapter()).marshal(digest.digest()).toLowerCase();
            logger.info("{} Hash (Normal):\t{}", hashAlgorithm, hash);
            MessageDigest zippedDigest = zippedDigestMap.get(filePath);
            if (zippedDigest != null) {
                String zippedHash = (new HexBinaryAdapter()).marshal(zippedDigest.digest()).toLowerCase();
                logger.info("{} Hash (Zipped):\t{}", hashAlgorithm, zippedHash);
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            logger.error(LogMessages.ERROR_READING_FILE.toString(), e);
        }
    }

    protected BufferedReader buildBufferedReader(String filePath) throws NoSuchAlgorithmException, IOException {
        InputStream stream = new FileInputStream(filePath);
        MessageDigest zippedDigest;
        if (filePath.endsWith(GZIP_SHORT_SUFFIX) || filePath.endsWith(GZIP_LONG_SUFFIX)) {
            zippedDigest = MessageDigest.getInstance(hashAlgorithm);
            stream = new DigestInputStream(stream, zippedDigest);
            zippedDigestMap.put(filePath, zippedDigest);
            stream = new GZIPInputStream(stream);
        } else if (filePath.endsWith(ZIP_SUFFIX)) {
            zippedDigest = MessageDigest.getInstance(hashAlgorithm);
            stream = new DigestInputStream(stream, zippedDigest);
            zippedDigestMap.put(filePath, zippedDigest);
            ZipInputStream zipInputStream = new ZipInputStream(stream);
            zipInputStream.getNextEntry();
            stream = zipInputStream;
        }
        MessageDigest digest = MessageDigest.getInstance(hashAlgorithm);
        stream = new DigestInputStream(stream, digest);
        defaultDigestMap.put(filePath, digest);
        InputStreamReader inputStreamReader = new InputStreamReader(stream, fileEncode);
        return new BufferedReader(inputStreamReader);
    }

    @Override
    public String toString() {
        String description = "\t" +
                "Settings:" +
                "\n" +
                "\t" +
                "Output directory path:\t" + outputDirectoryPath;
        return description.trim();
    }
}
