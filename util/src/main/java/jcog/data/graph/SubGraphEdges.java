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

package jcog.data.graph;

import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;

import java.util.BitSet;

/**
 * This class is an adaptor for representing subgraphs of any graph.
 * The subgraph is defined the following way.
 * The subgraph always contains all the nodes of the original underlying
 * graph. However, it is possible to remove edges by flagging nodes as
 * removed, in which case
 * the edges that have at least one end on those nodes are removed.
 * If the underlying graph changes after initialization, this class follows
 * the change.
 */
public class SubGraphEdges implements Graph {


// ====================== private fields ========================
// ==============================================================


    private final Graph g;

    private final BitSet nodes;


// ====================== public constructors ===================
// ==============================================================


    /**
     * Constructs an initially empty subgraph of g. That is, the subgraph will
     * contain no nodes.
     */
    public SubGraphEdges(Graph g) {

        this.g = g;
        nodes = new BitSet(g.size());
    }


// ======================= Graph implementations ================
// ==============================================================


    @Override
    public boolean isEdge(int i, int j) {

        return nodes.get(i) && nodes.get(j) && g.isEdge(i, j);
    }

// ---------------------------------------------------------------

    @Override
    public IntHashSet neighbors(int i) {

        IntHashSet result = new IntHashSet();
        if (nodes.get(i)) {
            g.neighbors(i).forEach(j -> {
                if (nodes.get(j)) result.add(j);
            });
        }

        return result;
    }

// ---------------------------------------------------------------

    @Override
    public Object vertex(int v) {
        return g.vertex(v);
    }

// ---------------------------------------------------------------

    /**
     * If both i and j are within the node set of the subgraph and the original
     * graph has an (i,j) edge, returns that edge.
     */
    @Override
    public Object edge(int i, int j) {

        if (isEdge(i, j)) return g.edge(i, j);
        return null;
    }

// --------------------------------------------------------------------

    @Override
    public int size() {
        return g.size();
    }

// --------------------------------------------------------------------

    @Override
    public boolean directed() {
        return g.directed();
    }

// --------------------------------------------------------------------

    /**
     * not supported
     */
    @Override
    public boolean setEdge(int i, int j) {

        throw new UnsupportedOperationException();
    }

// ---------------------------------------------------------------

    /**
     * not supported
     */
    @Override
    public boolean removeEdge(int i, int j) {

        throw new UnsupportedOperationException();
    }

// ---------------------------------------------------------------

    @Override
    public int degree(int i) {

        final int[] degree = {0};
        if (nodes.get(i)) {
            g.neighbors(i).forEach(j -> {
                if (nodes.get(j)) degree[0]++;
            });
        }
        return degree[0];
    }


// ================= public functions =================================
// ====================================================================


    /**
     * This function returns the size of the subgraph, i.e. the number of nodes
     * in the subgraph.
     */
    public int subGraphSize() {
        return nodes.cardinality();
    }

// --------------------------------------------------------------------

    /**
     * Removes given node from subgraph.
     *
     * @return true if the node was already in the subgraph otherwise false.
     */
    public boolean removeNode(int i) {

        boolean was = nodes.get(i);
        nodes.clear(i);
        return was;
    }

// --------------------------------------------------------------------

    /**
     * Adds given node to subgraph.
     *
     * @return true if the node was already in the subgraph otherwise false.
     */
    public boolean addNode(int i) {

        boolean was = nodes.get(i);
        nodes.set(i);
        return was;
    }
}

