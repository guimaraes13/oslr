/*
 * Probabilist Logic Learner is a system to learn probabilistic logic
 * programs from data and use its learned programs to make inference
 * and answer queries.
 *
 * Copyright (C) 2018 Victor Guimarães
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

package edu.cmu.ml.proppr.util;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import edu.cmu.ml.proppr.prove.Prover;

public abstract class CustomConfiguration extends ModuleConfiguration {

	public CustomConfiguration(String[] args, int inputFiles, int outputFiles, int constants, int modules) {
		super(args, inputFiles, outputFiles, constants, modules);
	}

	@Override
	protected void addOptions(Options options, int[] flags) {
		super.addOptions(options,flags);
		this.addCustomOptions(options,flags);
	}

	protected abstract void addCustomOptions(Options options, int[] flags);
	
	@Override
	protected void retrieveSettings(CommandLine line, int[] flags, Options options) throws IOException {
		super.retrieveSettings(line, flags, options);
		this.retrieveCustomSettings(line,flags,options);
	}
	
	protected abstract void retrieveCustomSettings(CommandLine line, int[] flags, Options options);

	public abstract Object getCustomSetting(String name);
}
