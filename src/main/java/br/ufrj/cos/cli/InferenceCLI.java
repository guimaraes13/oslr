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

package br.ufrj.cos.cli;

import br.ufrj.cos.engine.proppr.ProPprEngineSystemTranslator;
import br.ufrj.cos.knowledge.Knowledge;
import br.ufrj.cos.knowledge.base.KnowledgeBase;
import br.ufrj.cos.knowledge.example.Example;
import br.ufrj.cos.knowledge.example.Examples;
import br.ufrj.cos.knowledge.filter.ClausePredicate;
import br.ufrj.cos.knowledge.filter.GroundedFactPredicate;
import br.ufrj.cos.knowledge.theory.Theory;
import br.ufrj.cos.logic.*;
import br.ufrj.cos.util.FileIOUtils;
import edu.cmu.ml.proppr.util.APROptions;
import org.apache.commons.cli.CommandLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static br.ufrj.cos.cli.CommandLineOptions.*;
import static br.ufrj.cos.util.log.EngineSystemLog.ADDING_NON_GROUND_FACT;

/**
 * Created on 19/05/18.
 *
 * @author Victor Guimarães
 */
public class InferenceCLI extends CommandLineInterface {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    /**
     * The default input files.
     */
    public static final String[] DEFAULT_VALUE = new String[]{};

    protected String[] knowledgeBaseFilePaths;
    protected String[] queryFilePaths;

    protected KnowledgeBase knowledgeBase;
    protected Knowledge<FunctionalSymbol> functionBase;
    protected Theory theory;

    protected Examples queries;

    protected File workingDirectory;

    protected ProPprEngineSystemTranslator engineSystemTranslator;
    protected double alpha = APROptions.MINALPH_DEFAULT;
    protected double epsilon = APROptions.EPS_DEFAULT;
    protected int maxDepth = APROptions.MAXDEPTH_DEFAULT;
    protected boolean individually;

    /**
     * The main method
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CommandLineInterface instance = new InferenceCLI();
        mainProgram(instance, logger, args);
    }

    @Override
    protected void initializeOptions() {
        super.initializeOptions();
        options.addOption(CommandLineOptions.KNOWLEDGE_BASE.getOption());
        options.addOption(CommandLineOptions.QUERIES.getOption());
        options.addOption(CommandLineOptions.WORKING_DIRECTORY.getOption());
        options.addOption(PROPPR_ALPHA_PARAMETER.getOption());
        options.addOption(CommandLineOptions.PROPPR_EPSILON_PARAMETER.getOption());
        options.addOption(CommandLineOptions.PROPPR_MAX_DEPTH_PARAMETER.getOption());
        options.addOption(CommandLineOptions.INDIVIDUALLY_INFERENCE.getOption());
    }

    @Override
    public CommandLineInterface parseOptions(CommandLine commandLine) throws CommandLineInterrogationException {
        super.parseOptions(commandLine);
        knowledgeBaseFilePaths = getFilesFromOption(commandLine, CommandLineOptions.KNOWLEDGE_BASE.getOptionName(),
                                                    DEFAULT_VALUE);
        queryFilePaths = getFilesFromOption(commandLine, CommandLineOptions.QUERIES.getOptionName(), DEFAULT_VALUE);
        final String workingDirectoryOption = CommandLineOptions.WORKING_DIRECTORY.getOptionName();
        if (commandLine.hasOption(workingDirectoryOption)) {
            workingDirectory = new File(commandLine.getOptionValue(workingDirectoryOption));
        }
        if (commandLine.hasOption(PROPPR_ALPHA_PARAMETER.getOptionName())) {
            alpha = Double.parseDouble(commandLine.getOptionValue(PROPPR_ALPHA_PARAMETER.getOptionName()));
        }
        if (commandLine.hasOption(PROPPR_EPSILON_PARAMETER.getOptionName())) {
            epsilon = Double.parseDouble(commandLine.getOptionValue(PROPPR_EPSILON_PARAMETER.getOptionName()));
        }
        if (commandLine.hasOption(PROPPR_MAX_DEPTH_PARAMETER.getOptionName())) {
            maxDepth = Integer.parseInt(commandLine.getOptionValue(PROPPR_MAX_DEPTH_PARAMETER.getOptionName()));
        }
        individually = commandLine.hasOption(INDIVIDUALLY_INFERENCE.getOptionName());
        return this;
    }

    @Override
    public void run() {
        try {
            buildKnowledgeBase();
            queries = FileIOUtils.buildExampleSet(queryFilePaths);
            logKnowledge();
            buildEngineSystemTranslator();
            if (individually) { inferQueriesIndividually(); } else { inferQueries(); }
        } catch (FileNotFoundException | IllegalAccessException | InstantiationException e) {
            logger.error(e);
        }
    }

    /**
     * Builds the {@link KnowledgeBase} from the input files.
     *
     * @throws FileNotFoundException if a file does not exists
     */
    @SuppressWarnings("OverlyCoupledMethod")
    protected void buildKnowledgeBase() throws FileNotFoundException {
        List<Clause> clauses = FileIOUtils.readInputKnowledge(
                FileIOUtils.readPathsToFiles(knowledgeBaseFilePaths,
                                             CommandLineOptions.KNOWLEDGE_BASE.getOptionName()));

        ClausePredicate predicate = new GroundedFactPredicate();
        knowledgeBase = new KnowledgeBase(new ArrayList<>(), predicate);
        functionBase = new Knowledge<>(new ArrayList<>());
        theory = new Theory(new ArrayList<>());

        List<HornClause> nonGroundFacts = new ArrayList<>();
        for (Clause clause : clauses) {
            if (clause.isFact() && !clause.isGrounded()) {
                logger.trace(ADDING_NON_GROUND_FACT.toString(), clause);
                final Atom head = (Atom) clause;
                nonGroundFacts.add(new HornClause(head, new Conjunction(Literal.TRUE_LITERAL)));
            }
        }

        knowledgeBase.addAll(clauses, Atom.class);
        functionBase.addAll(clauses, FunctionalSymbol.class);
        theory.addAll(clauses, HornClause.class);
        theory.addAll(nonGroundFacts, HornClause.class);
    }

    /**
     * Logs the read knowledge.
     */
    @SuppressWarnings("HardCodedStringLiteral")
    protected void logKnowledge() {
        if (!logger.isTraceEnabled()) { return; }
        logger.trace("Knowledge Base:");
        for (Atom atom : knowledgeBase) {
            logger.trace("\t{}", atom);
        }
        logger.trace("");
        logger.trace("Functional Symbols:");
        for (FunctionalSymbol functionalSymbol : functionBase) {
            logger.trace("\t{}", functionalSymbol);
        }
        logger.trace("");
        logger.trace("Horn Clauses:");
        for (HornClause clause : theory) {
            logger.trace("\t{}", clause);
        }
        logger.trace("");
        logger.trace("Queries:");
        for (Example example : queries) {
            logger.trace("\t{}", example.getGoalQuery());
        }
    }

    /**
     * Builds the engine system translator
     */
    protected void buildEngineSystemTranslator() {
        engineSystemTranslator = new ProPprEngineSystemTranslator();
        engineSystemTranslator.aprOptions.alpha = alpha;
        engineSystemTranslator.aprOptions.epsilon = epsilon;
        engineSystemTranslator.aprOptions.maxDepth = maxDepth;
        engineSystemTranslator.setKnowledgeBase(knowledgeBase);
        engineSystemTranslator.setFunctionBase(functionBase);
        engineSystemTranslator.setTheory(theory);
        engineSystemTranslator.initialize();
        if (workingDirectory != null) { engineSystemTranslator.loadParameters(workingDirectory); }
    }

    /**
     * Infer the queries
     */
    protected void inferQueriesIndividually() {
        for (Example example : queries) {
            final Map<Example, Map<Atom, Double>> inferExamples = engineSystemTranslator.inferExamples(example);
            final Map<Atom, Double> atomDoubleMap = inferExamples.get(example);
            if (atomDoubleMap == null) { continue; }
            logger.info(example.getGoalQuery());
            for (Map.Entry<Atom, Double> entry : atomDoubleMap.entrySet()) {
                logger.info("\t{}:\t{}", entry.getValue(), entry.getKey());
            }
        }
    }

    /**
     * Infer the queries
     */
    protected void inferQueries() {
        final Map<Example, Map<Atom, Double>> inferExamples = engineSystemTranslator.inferExamples(queries);
        for (Example example : queries) {
            final Map<Atom, Double> atomDoubleMap = inferExamples.get(example);
            if (atomDoubleMap == null) { continue; }
            logger.info(example.getGoalQuery());
            for (Map.Entry<Atom, Double> entry : atomDoubleMap.entrySet()) {
                logger.info("\t{}:\t{}", entry.getValue(), entry.getKey());
            }
        }
    }

    @Override
    public String toString() {
        return "\t" +
                "Settings:" +
                "\n" +
                "\t" +
                "Knowledge base files:\t" +
                Arrays.deepToString(knowledgeBaseFilePaths) +
                "\n" +
                "\t" +
                "Query files:\t\t\t" +
                Arrays.deepToString(queryFilePaths) +
                "\n" +
                "\t" +
                "Working Directory:\t\t" + workingDirectory +
                "\n" +
                "\t" +
                "ProPPR Alpha:\t\t\t" + alpha +
                "\n" +
                "\t" +
                "ProPPR Epsilon:\t\t\t" + epsilon +
                "\n" +
                "\t" +
                "ProPPR Max depth:\t\t" + maxDepth;
    }
}
