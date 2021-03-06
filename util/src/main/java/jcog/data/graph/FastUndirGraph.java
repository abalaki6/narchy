/*
 * Copyright (c) 2003-2005 The BISON Project
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 2 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

/*
 * Created on Jan 30, 2005 by Spyros Voulgaris
 *
 */
package jcog.data.graph;

import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;

import java.util.BitSet;

/**
 * Speeds up {@link ConstUndirGraph#isEdge} by storing the links in an
 * adjacency matrix (in fact in a triangle).
 * Its memory consumption is huge but it's much faster if the isEdge method
 * of the original underlying graph is slow.
 */
public class FastUndirGraph extends ConstUndirGraph {

    private BitSet[] triangle;


// ======================= initializarion ==========================
// =================================================================


    /**
     * Calls super constructor
     */
    public FastUndirGraph(Graph graph) {
        super(graph);
    }

// -----------------------------------------------------------------

    @Override
    protected void initGraph() {
        final int max = g.size();
        triangle = new BitSet[max];
        for (int i = 0; i < max; ++i) {
            in[i] = new IntArrayList();
            triangle[i] = new BitSet(i);
        }

        for (int i = 0; i < max; ++i) {
            int ii = i;
            g.neighbors(i).forEach(out -> {
                int j = out;
                if (!g.isEdge(j, ii))
                    in[j].add(ii);
                // But always add the link to the triangle
                if (ii > j) // make sure i>j
                    triangle[ii].set(j);
                else
                    triangle[j].set(ii);
            });
        }
    }


// ============================ Graph functions ====================
// =================================================================


    @Override
    public boolean isEdge(int i, int j) {
        if (i < j) {
            //sorted order
            int ii = i;
            i = j;
            j = ii;
        }
        return triangle[i].get(j);
    }
}

