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
import br.ufrj.cos.knowledge.base.KnowledgeBase;
import br.ufrj.cos.knowledge.example.Examples;
import br.ufrj.cos.knowledge.filter.ClausePredicate;
import br.ufrj.cos.knowledge.filter.GroundedFactPredicate;
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.Clause;
import br.ufrj.cos.logic.Term;
import br.ufrj.cos.util.FileIOUtils;
import br.ufrj.cos.util.LanguageUtils;
import br.ufrj.cos.util.time.TimeUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static br.ufrj.cos.cli.CommandLineOptions.*;
import static br.ufrj.cos.util.log.GeneralLog.ERROR_MAIN_PROGRAM;
import static br.ufrj.cos.util.log.GeneralLog.TOTAL_PROGRAM_TIME;

/**
 * Filter the base to remove the examples that are further than a specified relevantsDepth on the knowledge graph.
 * <p>
 * Created on 19/03/2018.
 *
 * @author Victor Guimarães
 */
public class BaseFilterCLI extends CommandLineInterface {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();
    /**
     * The default relevants depth value.
     */
    public static final int DEFAULT_DEPTH_VALUE = 0;
    /**
     * The input files.
     */
    public String[] inputFilePaths;
    /**
     * The example files.
     */
    public String[] exampleFilePaths;
    /**
     * The relevants depth value.
     */
    public int relevantsDepth = DEFAULT_DEPTH_VALUE;
    /**
     * The output file.
     */
    public String outputFilePath;

    /**
     * The main method
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CommandLineInterface instance = new BaseFilterCLI();
        mainProgram(instance, logger, args);
    }

    @Override
    public void run() {
        long begin = TimeUtils.getNanoTime();
        try {
            processFiles();
        } catch (IllegalAccessException | IOException | InstantiationException e) {
            logger.error(ERROR_MAIN_PROGRAM, e);
        }
        long end = TimeUtils.getNanoTime();
        logger.warn(TOTAL_PROGRAM_TIME.toString(), TimeUtils.formatNanoDifference(begin, end));
    }

    private void processFiles() throws IllegalAccessException, IOException, InstantiationException {
        KnowledgeBase knowledgeBase = buildKnowledgeBase();
        final Examples examples = FileIOUtils.buildExampleSet(exampleFilePaths);
        Set<Term> constantSet = examples.parallelStream().flatMap(e -> e.getGroundedQuery().stream())
                .flatMap(e -> e.getTerms().stream()).collect(Collectors.toSet());
        final Set<Atom> atoms = knowledgeBase.baseBreadthFirstSearch(constantSet, relevantsDepth);
        List<Atom> sorted = new ArrayList<>(atoms);
        sorted.sort(Comparator.comparing(Atom::toString));
        FileIOUtils.writeIterableToFile(new File(outputFilePath), sorted.stream().map(LanguageUtils::formatFactToString)
                .collect(Collectors.toList()));
    }

    /**
     * Builds the {@link KnowledgeBase} from the input files.
     *
     * @return the knowledge base
     * @throws FileNotFoundException if a file does not exists
     */
    protected KnowledgeBase buildKnowledgeBase() throws FileNotFoundException {
        List<Clause> clauses =
                FileIOUtils.readInputKnowledge(
                        FileIOUtils.readPathsToFiles(inputFilePaths,
                                                     CommandLineOptions.KNOWLEDGE_BASE.getOptionName()));

        ClausePredicate predicate = new GroundedFactPredicate();
        KnowledgeBase knowledgeBase = new KnowledgeBase(new HashSet<>(), predicate);
        knowledgeBase.addAll(clauses, Atom.class);
        return knowledgeBase;
    }

    @Override
    protected void initializeOptions() {
        super.initializeOptions();
        options.addOption(INPUT_FILES.getOption());
        options.addOption(EXAMPLES.getOption());
        options.addOption(DEPTH.getOption());
        options.addOption(OUTPUT_FILE.getOption());
    }

    @Override
    public CommandLineInterface parseOptions(CommandLine commandLine) throws CommandLineInterrogationException {
        super.parseOptions(commandLine);
        exampleFilePaths = commandLine.getOptionValues(EXAMPLES.getOptionName());
        inputFilePaths = commandLine.getOptionValues(INPUT_FILES.getOptionName());
        outputFilePath = commandLine.getOptionValue(OUTPUT_FILE.getOptionName());
        if (commandLine.hasOption(DEPTH.getOptionName())) {
            relevantsDepth = Integer.parseInt(commandLine.getOptionValue(DEPTH.getOptionName()));
        }
        return this;
    }

    @Override
    public String toString() {
        StringBuilder description = new StringBuilder();
        description.append("\t").append("Settings:").append("\n");
        description.append("\t").append("Input Files:\t").append("\n");
        for (String inputFile : inputFilePaths) {
            description.append("\t\t").append(inputFile).append("\n");
        }
        description.append("\t").append("Example Files:\t").append("\n");
        for (String exampleFile : exampleFilePaths) {
            description.append("\t\t").append(exampleFile).append("\n");
        }
        description.append("\t").append("Relevants Depth:\t").append(relevantsDepth).append("\n");
        description.append("\t").append("Output File:\t").append(outputFilePath).append("\n");
        return description.toString();
    }

}
