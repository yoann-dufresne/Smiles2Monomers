package model.graph;

import org._3pq.jgrapht.edge.UndirectedEdge;

@SuppressWarnings("serial")
public class LabeledEdge extends UndirectedEdge {

	protected String sourceLabel;
	protected String targetLabel;
	
	public LabeledEdge(Object arg0, Object arg1, String l0, String l1) {
		super(arg0, arg1);
		this.sourceLabel = l0;
		this.targetLabel = l1;
	}
	
	public String getSourceLabel() {
		return sourceLabel;
	}
	
	public String getTargetLabel() {
		return targetLabel;
	}

}
