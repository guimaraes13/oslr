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
import br.ufrj.cos.cli.CommandLineOptions;
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.Clause;
import br.ufrj.cos.logic.Term;
import br.ufrj.cos.util.FileIOUtils;
import br.ufrj.cos.util.LanguageUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static br.ufrj.cos.util.FileIOUtils.DEFAULT_INPUT_ENCODE;

/**
 * Converts a file of facts to ProPPR native input format.
 * <p>
 * Created on 03/07/18.
 *
 * @author Victor Guimarães
 */
public class FactsToProPprConverter extends CommandLineInterface {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();
    protected static final String ERROR_READING_INPUT_FILES = "Error reading input files, reason:";
    protected static final String ERROR_WRITING_OUTPUT_FILE = "Error writing output file, reason:";

    protected String[] knowledgeBaseFilePaths;

    /**
     * The main method
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CommandLineInterface instance = new FactsToProPprConverter();
        mainProgram(instance, logger, args);
    }

    @Override
    protected void initializeOptions() {
        super.initializeOptions();
        if (options == null) { options = new Options(); }

        options.addOption(CommandLineOptions.KNOWLEDGE_BASE.getOption());
        options.addOption(CommandLineOptions.OUTPUT_DIRECTORY.getOption());
    }

    @Override
    public CommandLineInterface parseOptions(CommandLine commandLine) throws CommandLineInterrogationException {
        super.parseOptions(commandLine);
        knowledgeBaseFilePaths = getFilesFromOption(commandLine,
                                                    CommandLineOptions.KNOWLEDGE_BASE.getOptionName(),
                                                    knowledgeBaseFilePaths);
        outputDirectoryPath = commandLine.getOptionValue(CommandLineOptions.OUTPUT_DIRECTORY.getOptionName(),
                                                         outputDirectoryPath);
        return this;
    }

    @Override
    public void run() {
        try {
            List<Clause> clauses = FileIOUtils.readInputKnowledge(
                    FileIOUtils.readPathsToFiles(knowledgeBaseFilePaths,
                                                 CommandLineOptions.KNOWLEDGE_BASE.getOptionName()));
            writeFactsToFile(clauses);
        } catch (FileNotFoundException e) {
            logger.error(ERROR_READING_INPUT_FILES, e);
        }
    }

    /**
     * Writes the facts in clauses to output file.
     *
     * @param clauses the clauses
     */
    protected void writeFactsToFile(List<Clause> clauses) {
        final File outputFile;
        if (outputDirectoryPath == null) {
            outputFile = new File("output.txt");
        } else {
            outputFile = new File(outputDirectoryPath);
        }
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile),
                                                                               DEFAULT_INPUT_ENCODE))) {
            for (Clause clause : clauses) {
                if (clause instanceof Atom && clause.isGrounded()) {
                    writeFactToFile((Atom) clause, writer);
                }
            }
        } catch (Exception e) {
            logger.error(ERROR_WRITING_OUTPUT_FILE, e);
        }
    }

    /**
     * Writes the fact to the writer in the ProPPR's format.
     *
     * @param fact   the fact
     * @param writer the writer
     * @throws IOException if something goes wrong during writing
     */
    protected static void writeFactToFile(Atom fact, BufferedWriter writer) throws IOException {
        writer.write(fact.getName());
        for (Term term : fact.getTerms()) {
            writer.write("\t");
            String name = term.getName();
            if (LanguageUtils.doesNameContainsSpecialCharacters(name)) {
                name = LanguageUtils.surroundConstant(name);
            }
            writer.write(name);
        }
        writer.write("\n");
    }

    @Override
    public String toString() {
        String description = "\t" +
                "Settings:" +
                "\n" +
                "\t" +
                "Knowledge base files:\t" +
                Arrays.deepToString(knowledgeBaseFilePaths) +
                "\n" +
                "\t" +
                "Output directory:\t" +
                outputDirectoryPath;

        return description.trim();
    }
}
