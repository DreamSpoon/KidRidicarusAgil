package kidridicarus.agency.tool.draodgraph;

import java.util.HashSet;

import kidridicarus.agency.tool.DoIt;

// DRAOD Graph Dependency Node
public class DepNode {
	public DoIt resolver;

	DRAOD_Graph graph;
	HashSet<DepEdge> requireParentEdges;
	HashSet<DepEdge> requireChildEdges;
	HashSet<DepEdge> orderParentEdges;
	HashSet<DepEdge> orderChildEdges;
	boolean isVisitDirty;
	boolean isOrderDirty;
	private boolean isCycleVisitDirty;

	DepNode(DRAOD_Graph graph, DoIt resolver) {
		this.graph = graph;
		this.resolver = resolver;
		requireParentEdges = new HashSet<DepEdge>();
		requireChildEdges = new HashSet<DepEdge>();
		orderParentEdges = new HashSet<DepEdge>();
		orderChildEdges = new HashSet<DepEdge>();
		isVisitDirty = false;
		isCycleVisitDirty = false;
		isOrderDirty = false;
	}

	// is otherNode a parent of this node?
	boolean isMyRequireParent(DepNode otherNode) {
		// if otherNode is in this node's list of require parents then return true
		for(DepEdge parentEdge : requireParentEdges) {
			if(parentEdge.parentNode == otherNode)
				return true;
		}
		// otherNode was not found in this node's list of require parents
		return false;
	}

	// is otherNode a parent of this node?
	boolean isMyOrderParent(DepNode otherNode) {
		// if otherNode is in this node's list of order parents then return true
		for(DepEdge parentEdge : orderParentEdges) {
			if(parentEdge.parentNode == otherNode)
				return true;
		}
		// otherNode was not found in this node's list of order parents
		return false;
	}

	public boolean isMyOrderCycleParent(DepNode parentNode) {
		// If parent does not have order parents or child does not have order children then circular reference is
		// impossible.
		if(orderChildEdges.isEmpty() || parentNode.orderParentEdges.isEmpty())
			return false;
		// visit nodes recursively, ignoring nodes that have already been visited for efficiency
		HashSet<DepNode> visitedNodes = new HashSet<DepNode>();
		boolean result = recursiveIsOrderCycleParent(this, parentNode, visitedNodes);
		// "clean" the nodes that were visited, reset dirty flag
		for(DepNode visitedNode : visitedNodes)
			visitedNode.isCycleVisitDirty = false;
		return result;
	}

	/*
	 * Starting at currentNode, search down the order dependency links to see if parentNode is referenced again
	 * (i.e. if parentNode is child of currentNode).
	 */
	private boolean recursiveIsOrderCycleParent(DepNode currentNode, DepNode parentNode,
			HashSet<DepNode> visitedNodes) {
		if(currentNode == parentNode)
			return true;
		// add currentNode to visited nodes list before visiting child nodes 
		currentNode.isCycleVisitDirty = true;
		visitedNodes.add(currentNode);
		// visit child nodes recursively to check for a cycle, ignoring nodes that have already been visited
		for(DepEdge childEdge : currentNode.orderChildEdges) {
			// check each child node recursively, and return true if cycle is found
			if(!childEdge.childNode.isCycleVisitDirty &&
					recursiveIsOrderCycleParent(childEdge.childNode, parentNode, visitedNodes)) {
				return true;
			}
		}
		// no cycles found
		return false;
	}
}
