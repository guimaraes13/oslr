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

package edu.cmu.ml.proppr.prove.wam;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;

import org.junit.Test;

import edu.cmu.ml.proppr.prove.wam.Argument;
import edu.cmu.ml.proppr.prove.wam.Goal;
import edu.cmu.ml.proppr.prove.wam.Query;
import edu.cmu.ml.proppr.util.SimpleSymbolTable;

public class GoalTest {

    @Test
    public void testEquals() {
        Goal f = new Goal("milk"), g = new Goal("milk");
        assertTrue(f.equals(g));
        assertTrue(g.equals(f));
        
    }
    @Test
    public void testHashMembership() {
        Goal f = new Goal("eggs"), g = new Goal("eggs");
        
//        System.err.println("f"); 
//        f.hashCode();
//        System.err.println("g"); 
//        g.hashCode();
//        assertEquals("hashcode",f.hashCode(),g.hashCode()); // if this passes
        assertTrue("equals",g.equals(f)); // and this passes
        
        HashSet<Goal> set = new HashSet<Goal>();
        set.add(f);
        
//        boolean has = false;
//        for (Goal q : set) has = has || g.equals(q);
//        assertTrue("set membership by hand",has); // and this passes

        assertTrue("set membership",set.contains(g)); // shouldn't this pass?
    }
}
