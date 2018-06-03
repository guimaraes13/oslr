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

package br.ufrj.cos.knowledge.base;

import br.ufrj.cos.logic.FunctionalSymbol;
import edu.cmu.ml.proppr.prove.wam.*;
import edu.cmu.ml.proppr.prove.wam.plugins.WamPlugin;
import edu.cmu.ml.proppr.util.APROptions;
import edu.cmu.ml.proppr.util.Dictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created on 19/05/18.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings("SpellCheckingInspection")
public class FunctionsPlugin extends WamPlugin {

    /**
     * The logger
     */
    public static final Logger logger = LogManager.getLogger();

    /**
     * The plugin name indicator.
     */
    public static final String PLUGIN_NAME = "functions";
    /**
     * The default function weight.
     */
    public static final double DEFAULT_FUNCTION_WEIGHT = 1.0;
    /**
     * The fetching log message.
     */
    public static final String LOG_FETCHING_OUT_LINKS_FOR = "Fetching out links for\t{}:\t{}";

    protected final String name;
    protected final Map<String, FunctionalSymbol> functionalSymbolMap = new HashMap<>();
    protected final Map<Feature, Double> fd = new HashMap<>();

    /**
     * Constructs a empty body {@link FunctionsPlugin}.
     *
     * @param apr  the {@link APROptions}
     * @param name the name
     */
    public FunctionsPlugin(APROptions apr, String name) {
        super(apr);
        this.name = name;
        //noinspection ThisEscapedInObjectConstruction
        this.fd.put(WamPlugin.pluginFeature(this, name), 1.0);
    }

    @SuppressWarnings({"RedundantSuppression", "InstanceMethodNamingConvention"})
    @Override
    public boolean _claim(String jumpto) {
        return functionalSymbolMap.containsKey(jumpto);
    }

    @Override
    public List<Outlink> outlinks(State state, WamInterpreter wamInterp, boolean computeFeatures)
            throws LogicProgramException {
        List<Outlink> result = new LinkedList<>();
        String jumpTo = state.getJumpTo();
        int delimiter = jumpTo.indexOf(WamInterpreter.JUMPTO_DELIMITER);
        int arity = Integer.parseInt(jumpTo.substring(delimiter + 1));
        String[] constantsArguments = new String[arity];
        for (int i = 0; i < arity; i++) { constantsArguments[i] = wamInterp.getConstantArg(arity, i + 1); }
        logger.debug(LOG_FETCHING_OUT_LINKS_FOR,
                     jumpTo, Dictionary.buildString(constantsArguments, new StringBuilder(), ", "));
        boolean fail = !functionalSymbolMap.get(jumpTo).apply(constantsArguments);
        // then iterate through what you got
        if (fail) { return result; }
        wamInterp.restoreState(state);
        wamInterp.returnp();
        wamInterp.executeWithoutBranching();
        if (computeFeatures) {
            result.add(new Outlink(scaleFD(this.fd, DEFAULT_FUNCTION_WEIGHT), wamInterp.saveState()));
        } else {
            result.add(new Outlink(null, wamInterp.saveState()));
        }
        return result;
    }

    @Override
    public String about() {
        return PLUGIN_NAME + "(" + name + ")";
    }

    /**
     * Adds the {@link FunctionalSymbol} to the {@link FunctionsPlugin}.
     *
     * @param functionalSymbol the {@link FunctionalSymbol}
     */
    public void addFunctionalSymbol(FunctionalSymbol functionalSymbol) {
        functionalSymbolMap.put(functionalSymbol.getHead().toString(), functionalSymbol);
    }

}
