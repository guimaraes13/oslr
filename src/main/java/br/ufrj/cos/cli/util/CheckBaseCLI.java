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
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.logic.Atom;
import br.ufrj.cos.logic.Clause;
import br.ufrj.cos.util.ExceptionMessages;
import br.ufrj.cos.util.FileIOUtils;
import br.ufrj.cos.util.time.TimeUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static br.ufrj.cos.util.log.GeneralLog.TOTAL_PROGRAM_TIME;
import static br.ufrj.cos.util.log.SystemLog.KNOWLEDGE_BASE_SIZE;

/**
 * Class to check if the background knowledge and examples are disjoint.
 * <p>
 * Created on 28/08/17.
 *
 * @author Victor Guimarães
 */
public class CheckBaseCLI extends CommandLineInterface {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();
    protected static final String ANSI_RESET = "\u001B[0m";
    protected static final String ANSI_GREEN = "\u001B[32m";
    protected static final String ANSI_RED = "\u001B[31m";
    protected static final String EXAMPLES_NOT_IN_THE_KNOWLEDGE_BASE = ANSI_GREEN + "Ok\t-\t{}" + ANSI_RESET;
    protected static final String KNOWLEDGE_BASE_CONTAINS_EXAMPLES = ANSI_RED +
            "Knowledge base contains\t{} example(s)." + ANSI_RESET;
    protected static final String KNOWLEDGE_BASES_WHICH_CONTAIN_EXAMPLES = "Knowledge Bases which contain examples:";
    protected static final String CONTAINED_EXAMPLES = "Contained examples:";
    protected static final String BASE_SIZE = "Base:\t{}\tSize:\t{}";
    protected static final String EXAMPLES_NOT_IN_EXAMPLES = ANSI_GREEN + "Examples\t{}\tnot in examples\t{}."
            + ANSI_RESET;
    protected static final String EXAMPLES_IN_EXAMPLES = ANSI_RED + "Examples\t{}\tin examples\t{}." + ANSI_RESET;
    private static final String[] STRINGS = new String[0];
    /**
     * If the run was ok.
     */
    public boolean success = true;
    protected String[] knowledgeBasePaths = STRINGS;
    protected String[] exampleFilePaths = STRINGS;

    /**
     * The main method
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CheckBaseCLI instance = new CheckBaseCLI();
        mainProgram(instance, logger, args);
        if (!instance.success) { System.exit(2); }
    }

    @Override
    public void run() {
        if (exampleFilePaths.length == 0) { return; }
        try {
            long begin = TimeUtils.getNanoTime();
            final List<Set<Atom>> clauses = buildKnowledgeBase();
            success &= checkExamplesWithBases(clauses);
            success &= checkExamples();
            long end = TimeUtils.getNanoTime();
            logger.warn(TOTAL_PROGRAM_TIME.toString(), TimeUtils.formatNanoDifference(begin, end));
        } catch (IOException | ReflectiveOperationException e) {
            logger.error(ExceptionMessages.GENERAL_ERROR.toString(), e);
        }
    }

    /**
     * Builds the {@link KnowledgeBase} from the input files.
     *
     * @return the bases
     * @throws FileNotFoundException        if a file does not exists
     */
    protected List<Set<Atom>> buildKnowledgeBase() throws FileNotFoundException {
        List<Set<Atom>> clauses = new ArrayList<>(knowledgeBasePaths.length);
        int total = 0;
        for (String knowledgeBasePath : knowledgeBasePaths) {
            final List<Clause> clauseList = FileIOUtils.readInputKnowledge(FileIOUtils.readPathsToFiles
                    (knowledgeBasePaths, CommandLineOptions.KNOWLEDGE_BASE.getOptionName()));
            clauses.add(clauseList.stream()
                                .filter(c -> c instanceof Atom).map(a -> (Atom) a).collect(Collectors.toSet()));
            total += clauses.size();
            logger.info(BASE_SIZE, knowledgeBasePath, clauses.size());
        }
        logger.warn(KNOWLEDGE_BASE_SIZE.toString(), total);
        return clauses;
    }

    /**
     * For each examples path in {@link #exampleFilePaths}, check if there is a intersection with the knowledge base.
     *
     * @param clauses the knowledge base
     * @return {@code true} if every thing was ok, {@code false} otherwise
     * @throws ReflectiveOperationException if an error occurs when instantiating a new object by reflection
     * @throws FileNotFoundException        if a file does not exists
     */
    protected boolean checkExamplesWithBases(List<Set<Atom>> clauses)
            throws ReflectiveOperationException, FileNotFoundException {
        Set<Atom> examples;
        boolean checkOk = true;
        for (String examplesPath : exampleFilePaths) {
            List<Integer> joints = new ArrayList<>();
            examples = FileIOUtils.buildExampleSet(new String[]{examplesPath}).stream()
                    .flatMap(e -> e.getGroundedQuery().stream()).map(Example::getAtom).collect(Collectors.toSet());

            for (int i = 0; i < clauses.size(); i++) {
                if (Collections.disjoint(examples, clauses.get(i))) { continue; }
                joints.add(i);
            }

            if (joints.isEmpty()) {
                logger.info(EXAMPLES_NOT_IN_THE_KNOWLEDGE_BASE, examplesPath);
            } else {
                checkOk = false;
                examples.retainAll(clauses.stream().flatMap(Collection::stream).collect(Collectors.toSet()));
                logger.warn(KNOWLEDGE_BASE_CONTAINS_EXAMPLES, examples.size());
                logJointKnowledgeBases(joints);
                logJointExamples(examples);
            }
        }
        return checkOk;
    }

    /**
     * Verifies if there are intersections between the examples.
     *
     * @return {@code true} if every thing was ok, {@code false} otherwise
     * @throws ReflectiveOperationException if an error occurs when instantiating a new object by reflection
     * @throws FileNotFoundException        if a file does not exists
     */
    protected boolean checkExamples() throws ReflectiveOperationException, FileNotFoundException {
        Set<Atom> examples1;
        Set<Atom> examples2;
        boolean checkOk = true;
        for (int i = 0; i < exampleFilePaths.length - 1; i++) {
            examples1 = FileIOUtils.buildExampleSet(new String[]{exampleFilePaths[i]}).stream()
                    .flatMap(e -> e.getGroundedQuery().stream()).map(Example::getAtom).collect(Collectors.toSet());
            for (int j = i + 1; j < exampleFilePaths.length; j++) {
                examples2 = FileIOUtils.buildExampleSet(new String[]{exampleFilePaths[j]}).stream()
                        .flatMap(e -> e.getGroundedQuery().stream()).map(Example::getAtom).collect(Collectors.toSet());
                examples2.retainAll(examples1);
                if (examples2.isEmpty()) {
                    logger.info(EXAMPLES_NOT_IN_EXAMPLES, exampleFilePaths[i], exampleFilePaths[j]);
                } else {
                    checkOk = false;
                    logger.warn(EXAMPLES_IN_EXAMPLES, exampleFilePaths[i], exampleFilePaths[j]);
                    logJointExamples(examples2);
                }
            }
        }
        return checkOk;
    }

    /**
     * Logs the knowledge bases which contains examples.
     *
     * @param joints the indexes of the bases
     */
    protected void logJointKnowledgeBases(List<Integer> joints) {
        if (logger.isDebugEnabled()) {
            logger.debug(KNOWLEDGE_BASES_WHICH_CONTAIN_EXAMPLES);
            for (Integer index : joints) {
                logger.debug(knowledgeBasePaths[index]);
            }
        }
    }

    /**
     * Logs the examples that appears in the knowledge base.
     *
     * @param examples the examples
     */
    protected static void logJointExamples(Set<Atom> examples) {
        if (logger.isTraceEnabled()) {
            logger.trace(CONTAINED_EXAMPLES);
            for (Atom example : examples) {
                logger.trace(example);
            }
        }
    }

    @Override
    protected void initializeOptions() {
        super.initializeOptions();
        if (options == null) { options = new Options(); }
        options.addOption(CommandLineOptions.KNOWLEDGE_BASE.getOption());
        options.addOption(CommandLineOptions.EXAMPLES.getOption());
    }

    @Override
    protected CommandLineInterface parseOptions(CommandLine commandLine) throws CommandLineInterrogationException {
        super.parseOptions(commandLine);
        knowledgeBasePaths = getFilesFromOption(commandLine, CommandLineOptions.KNOWLEDGE_BASE.getOptionName(),
                                                knowledgeBasePaths);
        exampleFilePaths = getFilesFromOption(commandLine, CommandLineOptions.EXAMPLES.getOptionName(),
                                              exampleFilePaths);
        return this;
    }

    @Override
    public String toString() {
        String description = "\t" +
                "Settings:" +
                "\n" +
                "\t" +
                "Knowledge base files:\t" +
                Arrays.deepToString(knowledgeBasePaths) +
                "\n" +
                "\t" +
                "Example files:\t\t\t" +
                Arrays.deepToString(exampleFilePaths);

        return description.trim();
    }

}
