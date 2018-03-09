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

package br.ufrj.cos.cli.util;

import br.ufrj.cos.cli.CommandLineInterface;
import br.ufrj.cos.cli.CommandLineInterrogationException;
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.parser.knowledge.ParseException;
import br.ufrj.cos.util.AtomFactory;
import br.ufrj.cos.util.FileIOUtils;
import br.ufrj.cos.util.InitializationException;
import br.ufrj.cos.util.LanguageUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static br.ufrj.cos.cli.CommandLineOptions.*;
import static br.ufrj.cos.util.log.FileIOLog.ERROR_READING_WRITING_FILE;

/**
 * Created on 05/10/2017.
 *
 * @author Victor Guimarães
 */
public class KnowledgeConcatenator extends CommandLineInterface {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();
    /**
     * The default output file path.
     */
    public static final String DEFAULT_OUTPUT_FILE_PATH = "output.pl";

    protected AtomFactory atomFactory;
    protected List<File> inputFiles;

    /**
     * The main method
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CommandLineInterface instance = new KnowledgeConcatenator();
        mainProgram(instance, logger, args);
    }

    @Override
    public void initialize() throws InitializationException {
        atomFactory = new AtomFactory();
        outputDirectory = new File(outputDirectoryPath);
    }

    @Override
    protected void initializeOptions() {
        super.initializeOptions();
        if (options == null) { options = new Options(); }

        options.addOption(INPUT_FILES.getOption());
        options.addOption(OUTPUT_FILES.getOption());
    }

    @Override
    public CommandLineInterface parseOptions(CommandLine commandLine) throws CommandLineInterrogationException {
        super.parseOptions(commandLine);
        if (commandLine.hasOption(INPUT_FILES.getOptionName())) {
            inputFiles = Arrays.stream(commandLine.getOptionValues(INPUT_FILES.getOptionName()))
                    .map(File::new).collect(Collectors.toList());
        } else {
            inputFiles = Arrays.stream(commandLine.getArgs()).map(File::new).collect(Collectors.toList());
        }
        if (commandLine.hasOption(OUTPUT_FILES.getOptionName())) {
            outputDirectoryPath = commandLine.getOptionValue(OUTPUT_DIRECTORY.getOptionName(),
                                                             DEFAULT_OUTPUT_FILE_PATH);
        } else {
            final int lastInputFileIndex = inputFiles.size() - 1;
            outputDirectoryPath = inputFiles.get(lastInputFileIndex).getAbsolutePath();
            inputFiles.remove(lastInputFileIndex);
        }

        return this;
    }

    @Override
    public void run() {
        Set<Atom> atoms = new HashSet<>();
        try {
            for (File inputFile : inputFiles) {
                FileIOUtils.readAtomKnowledgeFromFile(inputFile, atoms, atomFactory);
            }
            List<Atom> sorted = new ArrayList<>(atoms);
            sorted.sort(Comparator.comparing(Atom::toString));
            FileIOUtils.writeIterableToFile(outputDirectory, sorted.stream().map(LanguageUtils::formatFactToString)
                    .collect(Collectors.toList()));
        } catch (IOException | ParseException e) {
            logger.error(ERROR_READING_WRITING_FILE.toString(), e);
        }
    }

    @Override
    public String toString() {
        StringBuilder description = new StringBuilder();
        description.append("\t").append("Settings:").append("\n");
        description.append("\t").append("Input Files:\n");
        for (File inputFile : inputFiles) {
            description.append("\t\t - ").append(inputFile.getAbsolutePath()).append("\n");
        }
        description.append("\t").append("Output File:\t").append(outputDirectory.getAbsolutePath()).append("\n");
        return description.toString();
    }

}
