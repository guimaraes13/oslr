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

package edu.cmu.ml.proppr.util.multithreading;

import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;;

import edu.cmu.ml.proppr.util.StatusLogger;


public class WritingCleanup extends Cleanup<String> {
	protected Logger log;
	protected Writer writer;
	protected StatusLogger status;
	protected int count=0;
	public WritingCleanup(Writer w, Logger l, StatusLogger s) { 
		this.writer = w;
		this.log = l;
		this.status = s;
	}
	@Override
	public Runnable cleanup(Future<String> in, int id) {
		return new RunWritingCleanup(in,id,writer);
	}
	@Override
	public Logger getLog() { return this.log; }
	public class RunWritingCleanup implements Runnable {
		protected Writer writer;
		protected int id;
		protected Future<String> input;
		public RunWritingCleanup(Future<String>in, int id, Writer w) {
			this.input = in;
			this.id = id;
			this.writer = w;
		}
		@Override
		public void run() {
			try {
				String s = this.input.get();
				if (s != null) this.writer.write(s);
				count++;
				if (status.due()) getLog().info("Finished "+count+" ...");
			} catch (IOException e) {
				throw new IllegalStateException("IO trouble while writing: ",e);
			} catch (InterruptedException e) {
				throw new IllegalStateException("Interrupted while writing: ",e);
			} catch (ExecutionException e) {
				log.error("Execution trouble with #"+this.id,e);
			}
		}
	}
}
