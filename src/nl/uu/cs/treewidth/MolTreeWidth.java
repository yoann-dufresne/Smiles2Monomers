/*
 * Copyright (C) 2006 
 * Thomas van Dijk
 * Jan-Pieter van den Heuvel
 * Wouter Slob
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package nl.uu.cs.treewidth;

import nl.uu.cs.treewidth.algorithm.LexBFS;
import nl.uu.cs.treewidth.algorithm.Permutation;
import nl.uu.cs.treewidth.algorithm.PermutationToTreeDecomposition;
import nl.uu.cs.treewidth.input.GraphInput.InputData;
import nl.uu.cs.treewidth.ngraph.NGraph;
import nl.uu.cs.treewidth.timing.JavaNanoTime;
import nl.uu.cs.treewidth.timing.Stopwatch;

import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IMoleculeSet;

public class MolTreeWidth {

    public int calculateTreeWidth (IMolecule m) {
        NGraph<InputData> g;
        AtomContainerToNgraph converter = new AtomContainerToNgraph();

        IAtomContainer mol = m;
        
        if (!ConnectivityChecker.isConnected(mol)) {
            IMoleculeSet comps = ConnectivityChecker.partitionIntoMolecules(mol);
            int maxIndex = 0;
            int maxCount = -9999;
            for (int i = 0; i < comps.getAtomContainerCount(); i++) {
                if (comps.getAtomContainer(i).getAtomCount() > maxCount) {
                    maxCount = comps.getAtomContainer(i).getAtomCount();
                    maxIndex = i;
                }
            }
            mol = comps.getAtomContainer(maxIndex);
        }
        g = converter.convert(mol);

        Stopwatch stopwatch = new Stopwatch(new JavaNanoTime());

        Permutation<InputData> p = new LexBFS<InputData>();
        PermutationToTreeDecomposition<InputData> pttd = new PermutationToTreeDecomposition<InputData>(p);
        stopwatch.reset();
        stopwatch.start();
        pttd.setInput(g);
        pttd.run();
        stopwatch.stop();

        return pttd.getUpperBound();
    }

}