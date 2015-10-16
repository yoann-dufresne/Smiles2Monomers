package model.graph;

import org._3pq.jgrapht.edge.UndirectedEdge;

@SuppressWarnings("serial")
public class LabeledEdge extends UndirectedEdge {

	protected String sourceLabel;
	protected String targetLabel;
	
	public LabeledEdge(Object arg0, Object arg1) {
		super(arg0, arg1);
	}
	
	public void setEndEdgeLabel(Object verticeEnd, String label) {
		if (this.getSource().equals(verticeEnd))
			this.sourceLabel = label;
		else
			this.targetLabel = label;
	}
	
	public String getSourceLabel() {
		return sourceLabel;
	}
	
	public String getTargetLabel() {
		return targetLabel;
	}

}
