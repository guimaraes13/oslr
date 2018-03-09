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

public class StatusLogger {
	private static final int DEFAULT_PERIOD_MS = 3000;
	private int period_ms;
	private long start,last;
	public StatusLogger() {
		this(DEFAULT_PERIOD_MS);
		this.start();
	}
	public StatusLogger(int p) {
		this.period_ms = p;
	}
	public void start() {
		this.start = this.last = System.currentTimeMillis();
	}
	public boolean due() { return due(0); }
	public boolean due(int level) {
		long now = System.currentTimeMillis();
		boolean ret = now-last > Math.exp(level)*period_ms;
		if (ret) last = now;
		return ret;
	}
	public long sinceLast() {
		return since(last);
	}
	public long sinceStart() {
		return since(start);
	}
	public long since(long t) {
		return System.currentTimeMillis() - t;
	}
	public long tick() { return last = System.currentTimeMillis(); }
}
