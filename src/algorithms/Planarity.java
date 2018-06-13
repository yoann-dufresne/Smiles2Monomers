package algorithms;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org._3pq.jgrapht.graph.SimpleGraph;
import org.openscience.cdk.ringsearch.cyclebasis.SimpleCycle;

import algorithms.utils.planarity.Biconnected;
import algorithms.utils.planarity.CompatibilityGraph;
import algorithms.utils.planarity.Segment;

/**
 * Planarity test for chemical graphs.
 * Auslander-Parter Algorithm implemented
 * Paper : Planarity testing and embedding, Maurizio Patrignani
 * @author dufresne
 */
public class Planarity {
	
	private TwoColoration tc;
	
	public Planarity ()
	{
		this.tc = new TwoColoration();
	}
	
	/**
	 * Planarity test with Auslander-Parter algorithm.
	 * @return True if planar
	 */
	public boolean isPlanar (SimpleGraph g)
	{
		List<Biconnected> biconnecteds = Biconnected.createBiconnectedComponents(g);
		
		boolean planar = true;
		for (Biconnected bc : biconnecteds)
		{
			boolean biplan = this.testCycles(bc);
			planar = planar && biplan;
		}
		
		return planar;
	}


	private boolean testCycles(Biconnected bc)
	{
		Set<SimpleCycle> cycles = bc.getSeparatedCycles();
		Iterator<SimpleCycle> i = cycles.iterator();
		
		if (!i.hasNext())
			return true;
		
		SimpleCycle mainCycle = i.next();
		List<Segment> segments = bc.getSegments(mainCycle);
		
		switch (segments.size()) {
		case 0:
			return true;
			
		case 1:
			Segment s = segments.get(0);
			if (s.isStringIn(mainCycle))
				return true;
			else {
				System.err.println("TODO : find separated cycle");
				return false;
			}
			
		default:
			SimpleGraph comp = new CompatibilityGraph(mainCycle, segments);
			if (!this.tc.isTwoColorable(comp))
				return false;
			else {
				for (Segment s2 : segments)
					if (!this.isPlanar(s2))
						return false;
				return true;
			}
		}
	}

}
