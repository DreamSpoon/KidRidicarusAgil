package kidridicarus.agency.tool.draodgraph;

/*
 * The childNode must be resolved before the parentNode.
 * Like a tree, leaf nodes are removed (pruned), which changes parent nodes into leaf nodes, which are pruned, etc.
 */
class DepEdge {
	DepNode childNode;
	DepNode parentNode;

	DepEdge(DepNode childNode, DepNode parentNode) {
		this.childNode = childNode;
		this.parentNode = parentNode;
	}
}
