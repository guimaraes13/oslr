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

package br.ufrj.cos.cli.nell.check;

import br.ufrj.cos.cli.CommandLineInterface;
import br.ufrj.cos.cli.CommandLineInterrogationException;
import br.ufrj.cos.cli.CommandLineOptions;
import br.ufrj.cos.cli.nell.NellBaseConverterCLI;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static br.ufrj.cos.cli.nell.NellBaseConverterCLI.*;
import static br.ufrj.cos.util.log.FileIOLog.ERROR_READING_FILE;
import static br.ufrj.cos.util.log.NellConverterLog.*;

/**
 * Class to check if two output directories from {@link NellBaseConverterCLI} contains the same data.
 * <p>
 * Created on 27/07/17.
 *
 * @author Victor Guimarães
 */
public class NellHashCheckerCLI extends CommandLineInterface {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();
    @SuppressWarnings("MismatchedReadAndWriteOfArray")
    private static final File[] FILES = new File[0];
    private static final int BUFFER_SIZE = 8192;

    protected String iterationPrefix = DEFAULT_ITERATION_PREFIX;
    protected String positiveExtension = DEFAULT_POSITIVE_EXTENSION;
    protected String negativeExtension = DEFAULT_NEGATIVE_EXTENSION;

    protected File directory1;
    protected File directory2;
    /**
     * The hash algorithm to be used
     */
    @SuppressWarnings("CanBeFinal")
    public String hashAlgorithm = DEFAULT_HASH_ALGORITHM;

    /**
     * The main method
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CommandLineInterface instance = new NellHashCheckerCLI();
        mainProgram(instance, logger, args);
    }

    @Override
    protected void initializeOptions() {
        super.initializeOptions();
        if (options == null) { options = new Options(); }
        options.addOption(CommandLineOptions.INPUT_DIRECTORIES.getOption());
        options.addOption(CommandLineOptions.ITERATION_PREFIX.getOption());
        options.addOption(CommandLineOptions.POSITIVE_EXTENSION.getOption());
        options.addOption(CommandLineOptions.NEGATIVE_EXTENSION.getOption());
    }

    @Override
    protected CommandLineInterface parseOptions(CommandLine commandLine) throws CommandLineInterrogationException {
        super.parseOptions(commandLine);
        String[] values = commandLine.getOptionValues(CommandLineOptions.INPUT_DIRECTORIES.getOptionName());
        directory1 = new File(values[0]);
        directory2 = new File(values[1]);

        iterationPrefix = commandLine.getOptionValue(CommandLineOptions.ITERATION_PREFIX.getOptionName(),
                                                     iterationPrefix);
        positiveExtension = commandLine.getOptionValue(CommandLineOptions.POSITIVE_EXTENSION.getOptionName(),
                                                       positiveExtension);
        negativeExtension = commandLine.getOptionValue(CommandLineOptions.NEGATIVE_EXTENSION.getOptionName(),
                                                       negativeExtension);
        return this;
    }

    @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod", "HardCodedStringLiteral"})
    @Override
    public void run() {
        logCommittedVersion();
        logger.info(DIRECTORY_1.toString(), directory1);
        logger.info(DIRECTORY_2.toString(), directory2);
        logger.info(ITERATION_PREFIX.toString(), iterationPrefix);
        logger.info(POSITIVE_EXTENSION.toString(), positiveExtension);
        logger.info(NEGATIVE_EXTENSION.toString(), negativeExtension);
        FilenameFilter iterationFilter = (dir, name) -> name.startsWith(iterationPrefix);
        @SuppressWarnings("OverlyLongLambda") FileFilter relationFilter = pathname -> {
            if (!pathname.isFile()) { return false; }
            if (pathname.isHidden()) { return false; }
            String name = pathname.getName();
            return name.endsWith(positiveExtension) || name.endsWith(negativeExtension);
        };
        try {
            boolean allEquals = true;
            boolean allIterations = true;
            File[] directory1Iterations = directory1.listFiles(iterationFilter);
            File[] relations;
            File iteration2;
            for (File iteration1 : directory1Iterations != null ? directory1Iterations : FILES) {
                relations = iteration1.listFiles(relationFilter);
                iteration2 = new File(directory2, iteration1.getName());
                if (!iteration2.exists()) {
                    logger.debug("Directory {} exists in {} but does not in {}",
                                 iteration1.getName(), directory1, directory2);
                    allIterations = false;
                    continue;
                }
                for (File relation1 : relations != null ? relations : FILES) {
                    allEquals &= isEquals(iteration1, relation1, iteration2);
                }
            }

            if (allIterations) {
                logger.info("All the iterations are present in both directories.");
            } else {
                logger.warn("There are different iterations in the each directory.");
            }
            if (allEquals) {
                logger.info("All the files are equals in both directories.");
            } else {
                logger.warn("There are different files in the directories.");
            }
        } catch (NoSuchAlgorithmException e) {
            logger.error(ERROR_READING_FILE.toString(), e);
        }

    }

    /**
     * Checks if the files from relations are equal, by a hash algorithm.
     *
     * @param iteration1 the iteration 1
     * @param relation1  the relation 1
     * @param iteration2 the iteration 2
     * @return {@code true} if they are equal {@code false}, otherwise
     * @throws NoSuchAlgorithmException if no Provider supports a MessageDigestSpi implementation for the specified
     *                                  algorithm.
     */
    @SuppressWarnings({"HardCodedStringLiteral", "ConstantConditions"})
    protected boolean isEquals(File iteration1, File relation1, File iteration2) throws NoSuchAlgorithmException {
        File relation2 = new File(iteration2, relation1.getName());
        if (!relation2.exists()) {
            logger.debug("File {} exists in {} but does not in {}",
                         relation1.getName(), iteration1, iteration2);
            return true;
        }
        byte[] sha1 = calculateHash(relation1, hashAlgorithm);
        byte[] sha2 = calculateHash(relation2, hashAlgorithm);
        if (Arrays.equals(sha1, sha2)) {
            logger.trace("File {} is equal in the two directories with hash {}", relation1.getName(),
                         Hex.encodeHexString(sha1));
            return true;
        } else {
            logger.debug("File {} in directory {} has hash {}", relation1.getName(), iteration1,
                         Hex.encodeHexString(sha1));
            logger.debug("File {} in directory {} has hash {}", relation2.getName(), iteration2,
                         Hex.encodeHexString(sha2));
            return false;
        }
    }

    /**
     * Calculates the hash from a file.
     *
     * @param file      the file
     * @param algorithm the hash algorithm
     * @return the hash
     * @throws NoSuchAlgorithmException if no Provider supports a MessageDigestSpi implementation for the specified
     *                                  algorithm.
     */
    protected static byte[] calculateHash(File file, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        try (InputStream fileInputStream = new FileInputStream(file)) {
            int n = 0;
            byte[] buffer = new byte[BUFFER_SIZE];
            while (n != -1) {
                n = fileInputStream.read(buffer);
                if (n > 0) {
                    digest.update(buffer, 0, n);
                }
            }
            return digest.digest();
        } catch (IOException e) {
            logger.error(ERROR_READING_FILE.toString(), e);
        }
        return null;
    }

}
