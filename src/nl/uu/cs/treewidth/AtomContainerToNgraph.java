package nl.uu.cs.treewidth;

import java.util.Hashtable;

import nl.uu.cs.treewidth.input.GraphInput;
import nl.uu.cs.treewidth.ngraph.ListGraph;
import nl.uu.cs.treewidth.ngraph.ListVertex;
import nl.uu.cs.treewidth.ngraph.NGraph;
import nl.uu.cs.treewidth.ngraph.NVertex;
import nl.uu.cs.treewidth.output.Output;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.smiles.SmilesParser;

/**
 * Convert molecule to NGraph object.
 *
 * @author Rajarshi Guha
 */
public class AtomContainerToNgraph {
    SmilesParser sp;

    public AtomContainerToNgraph() {
        sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
    }

    public NGraph<GraphInput.InputData> convert(IAtomContainer mol) {
        NGraph<GraphInput.InputData> g = new ListGraph<GraphInput.InputData>();

        int numVertices = mol.getAtomCount();
        Hashtable<String, NVertex<GraphInput.InputData>> vertices = new Hashtable<String, NVertex<GraphInput.InputData>>();
        boolean directGraph = false;
        NVertex<GraphInput.InputData> vertexPrototype = new ListVertex<GraphInput.InputData>();

        for (IBond bond : mol.bonds()) {
            String atom1 = String.valueOf(mol.getAtomNumber(bond.getAtom(0)));
            String atom2 = String.valueOf(mol.getAtomNumber(bond.getAtom(1)));


            NVertex<GraphInput.InputData> v1, v2;
            boolean newNode = false;
            //The first vertex
            if (!vertices.containsKey(atom1)) {
                v1 = vertexPrototype.newOfSameType(new GraphInput.InputData(vertices.size(), atom1));
                vertices.put(v1.data.name, v1);
                g.addVertex(v1);
                newNode = true;
            } else {
                v1 = vertices.get(atom1);
            }

            //The second vertex
            if (!vertices.containsKey(atom2)) {
                v2 = vertexPrototype.newOfSameType(new GraphInput.InputData(vertices.size(), atom2));
                vertices.put(v2.data.name, v2);
                g.addVertex(v2);
                newNode = true;
            } else {
                v2 = vertices.get(atom2);
            }

            boolean edgeExists = false;
            if (!newNode) edgeExists = v1.isNeighbor(v2);
            if (!edgeExists)
                g.addEdge(v1, v2);
            else
                directGraph = true;
        }

        if (directGraph)
            System.err.println("You have loaded a  multigraph. Duplicate edges have been removed!");

        int edgelessVertices = numVertices - vertices.size();
        if (edgelessVertices > 0) {
            System.err.println("There are " + edgelessVertices + " vertices which are not connected to other vertices");
        }
        while (numVertices > vertices.size()) {
            NVertex<GraphInput.InputData> v = vertexPrototype.newOfSameType(new GraphInput.InputData(vertices.size(), "New_Vertex_" + vertices.size()));
            vertices.put(v.data.name, v);
            g.addVertex(v);

        }

        confirmProperIDs(g);

        return g;
    }

    public NGraph<GraphInput.InputData> convert(String smiles) throws InvalidSmilesException {
        return convert(sp.parseSmiles(smiles));
    }

    private void confirmProperIDs(NGraph<GraphInput.InputData> g) {
        boolean bugged = false;

        // see if the IDs are 0..size-1
        int size = g.getNumberOfVertices();
        boolean[] idUsed = new boolean[size];
        for (NVertex<GraphInput.InputData> v : g) {
            int id = v.data.id;
            if (id < 0 || id >= size) bugged = true;
            else idUsed[id] = true;
        }
        for (boolean b : idUsed) if (!b) bugged = true;

        if (bugged) {
            Output.bugreport("The IDs that DgfReader generates for a graph should be 0..size-1, but they were not when loading molecule");
        }
    }
}
