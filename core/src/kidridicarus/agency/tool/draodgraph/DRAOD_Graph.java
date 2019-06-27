package kidridicarus.agency.tool.draodgraph;

import java.util.HashSet;
import java.util.LinkedList;

import kidridicarus.agency.tool.DoIt;

/*
 * Disconnected Requirement And Order Dependency Graph (DRAOD Graph)
 * ? Disconnected Requirement Order And Dependency Graph ?
 * ? Disconnected Requirement Order Digraph ?
 * Two dependency graphs that intersect only at the nodes.
 * Edges in one graph are independent of edges in the other graph.
 * The dependency graphs must be resolved in the correct order - dependency recursion for the win!
 * Step (1) Solve the "requirement dependency" graph:
 *    -if node X has a child node then node X is a parent node
 *    -if node X is resolved then all child nodes of parent node X must be resolved (dependency chaining)
 *    -however, if the child is resolved then the parent is NOT resolved (only a one-way relationship)
 *    -cycles (two-way relationships) are allowed in this graph, since "time sequence" is not important - it's as if
 *     the nodes are resolved concurrently
 *        -e.g. it is permissible that node X is parent of node Y, and node Y is parent of node X,
 *              so that resolution of one always causes resolution of the other
 *    -solving the requirement graph produces a "dirty list" of nodes that need to be resolved in (2)
 * Step (2) Solve the "order dependency" graph:
 *    -parent-child relationships (edges) between the nodes in this "order dependency" graph are independent of
 *     parent-child relationships (edges) in (1)
 *    -solve this graph as usual, visiting nodes starting at a root node and eventually visiting all child,
 *     grandchild, etc. nodes
 *        -instead of resolving all nodes, only resolve the nodes in the "dirty list" from (1)
 * Performing steps (1) and (2) will resolve, in correct order, all nodes that require resolution.
 * Note:
 *   If resolving the entire graph, then step (1) can be skipped because all nodes will be resolved, irrespective
 *   of the "requirement dependency" graph.
 */
public class DRAOD_Graph {
	private HashSet<DepNode> requireRootNodes;
	private HashSet<DepNode> orderRootNodes;

	public DRAOD_Graph() {
		requireRootNodes = new HashSet<DepNode>();
		orderRootNodes = new HashSet<DepNode>();
	}

	// all nodes created by this method are root nodes
	public DepNode createNode(DoIt resolver) {
		// TODO is it reasonable/necessary to create node with null resolver?
		if(resolver == null)
			throw new IllegalArgumentException("Cannot create dependency node with resolver equal to null.");
		// A ref to this graph is given to the node, to efficiently detect errors re: references to nodes not
		// contained in this graph. E.g. Error caused by creation of dependency between a node in this graph and
		// a node in a separate graph object.
		DepNode newNode = new DepNode(this, resolver);
		requireRootNodes.add(newNode);
		orderRootNodes.add(newNode);
		return newNode;
	}

	/*
	 * childNode must be resolved if parentNode is resolved.
	 * childNode cannot already be a child node of parentNode, to prevent duplicate dependencies.
	 *
	 * This method creates a one-way relationship; parentNode does not need to be resolved if childNode is resolved.
	 * A two-way relationship can be created by calling this method again with the child and parent variables
	 * reversed.
	 * Cycles are allowed, i.e. parent-child child-parent cycles are permissible.
	 * If a cycle exists between two nodes, then resolution of one requires resolution of the other - either one can
	 * be "resolved first" to cause resolution of the other.
	 */
	public void createRequireEdge(DepNode childNode, DepNode parentNode) {
		// if either of the given nodes is not in this graph then throw error
		if(childNode.graph != this && parentNode.graph != this) {
			throw new IllegalArgumentException(
					"Cannot create dependency where parentNode and childNode are not in this graph.");
		}
		else if(childNode.graph != this)
			throw new IllegalArgumentException("Cannot create dependency where childNode is not in this graph.");
		else if(parentNode.graph != this)
			throw new IllegalArgumentException("Cannot create dependency where parentNode is not in this graph.");
		// if trying to create duplicate dependency then throw error
		else if(childNode.isMyRequireParent(parentNode))
			throw new IllegalArgumentException("Cannot create duplicate dependency.");

		// if childNode was a root node then remove it from list of root nodes because it has a parent now
		if(childNode.requireParentEdges.isEmpty())
			requireRootNodes.remove(childNode);
		// link the parent and child nodes with a dependency
		DepEdge newEdge = new DepEdge(childNode, parentNode);
		childNode.requireParentEdges.add(newEdge);
		parentNode.requireChildEdges.add(newEdge);
	}

	public void createOrderEdge(DepNode childNode, DepNode parentNode) {
		// if either of the given nodes is not in this graph then throw error
		if(childNode.graph != this && parentNode.graph != this) {
			throw new IllegalArgumentException(
					"Cannot create order dependency where parentNode and childNode are not in this graph.");
		}
		else if(childNode.graph != this) {
			throw new IllegalArgumentException(
					"Cannot create order dependency where childNode is not in this graph.");
		}
		else if(parentNode.graph != this) {
			throw new IllegalArgumentException(
					"Cannot create order dependency where parentNode is not in this graph.");
		}
		// if trying to create duplicate order dependency then throw error
		else if(childNode.isMyOrderParent(parentNode))
			throw new IllegalArgumentException("Cannot create duplicate order dependency.");
		// if creating the order dependency would cause a circular reference (a cycle) then throw error
		else if(childNode.isMyOrderCycleParent(parentNode))
			throw new IllegalArgumentException("Cannot create dependency, circular reference error.");

		// if childNode was an order root node then remove it from list because it now has an order parent
		if(childNode.orderParentEdges.isEmpty())
			orderRootNodes.remove(childNode);
		// link the parent and child nodes with an order dependency
		DepEdge newEdge = new DepEdge(childNode, parentNode);
		childNode.orderParentEdges.add(newEdge);
		parentNode.orderChildEdges.add(newEdge);
	}

	// resolve child requirement dependencies of node, and resolve node
	private void resolveRequire(DepNode node, HashSet<DepNode> orderDirtyNodes) {
		HashSet<DepNode> visitedNodes = new HashSet<DepNode>();
		recursiveResolveRequire(node, visitedNodes, orderDirtyNodes);
		for(DepNode visitedNode : visitedNodes)
			visitedNode.isVisitDirty = false;
	}

	// visit nodes recursively, ignoring nodes that have already been visited
	private void recursiveResolveRequire(DepNode node, HashSet<DepNode> visitedNodes,
			HashSet<DepNode> orderDirtyNodes) {
		// add node to visited nodes list before visiting child nodes 
		node.isVisitDirty = true;
		visitedNodes.add(node);
		// resolve child nodes by visiting them recursively, ignoring nodes that have already been visited
		for(DepEdge childEdge : node.requireChildEdges) {
			if(!childEdge.childNode.isVisitDirty)
				recursiveResolveRequire(childEdge.childNode, visitedNodes, orderDirtyNodes);
		}
		// inner resolve this node
		node.isOrderDirty = true;
		orderDirtyNodes.add(node);
	}

	public void resolveOrder() {
		HashSet<DepNode> visitedNodes = new HashSet<DepNode>();
		for(DepNode orderRootNode : orderRootNodes)
			recursiveResolveOrder(orderRootNode, visitedNodes, true);
		for(DepNode visitedNode : visitedNodes)
			visitedNode.isVisitDirty = false;
	}

	public void resolveOrder(DepNode node) {
		// resolving the requirement dependency graph will mark nodes dirty for the next step
		HashSet<DepNode> orderDirtyNodes = new HashSet<DepNode>();
		resolveRequire(node, orderDirtyNodes);
		// resolve the order dependency graph, issuing "resolve" callbacks only for nodes marked dirty
		HashSet<DepNode> visitedNodes = new HashSet<DepNode>();

		// A full resolve of the order graph is necessary, because resolving the "requirement dependency" graph may
		// have marked the parents nodes of node as "dirty" (to be resolved).
		for(DepNode orderRootNode : orderRootNodes)
			recursiveResolveOrder(orderRootNode, visitedNodes, false);

		// "clean" the dirty nodes by resetting their dirty flags
		for(DepNode visitedNode : visitedNodes)
			visitedNode.isVisitDirty = false;
		for(DepNode orderNode : orderDirtyNodes)
			orderNode.isOrderDirty = false;
	}

	// visit nodes recursively, ignoring nodes that have already been visited
	private void recursiveResolveOrder(DepNode node, HashSet<DepNode> visitedNodes, boolean isFullResolve) {
		// add node to visited nodes list before visiting child nodes 
		node.isVisitDirty = true;
		visitedNodes.add(node);
		// resolve child nodes by visiting them recursively, ignoring nodes that have already been visited
		for(DepEdge childEdge : node.orderChildEdges) {
			if(!childEdge.childNode.isVisitDirty)
				recursiveResolveOrder(childEdge.childNode, visitedNodes, isFullResolve);
		}
		// outer resolve this node if it is in the set of nodes to be resolved
		if(isFullResolve || node.isOrderDirty)
			node.resolver.doIt();
	}

	/*
	 * Order of input nodes does not matter. The only requirement is that firstNode is a "requirement dependency"
	 * parent of secondNode or secondNode is a "requirement dependency" parent of firstNode.
	 */
	public void destroyRequireEdge(DepNode firstNode, DepNode secondNode) {
		if(firstNode.graph != this && secondNode.graph != this)
			throw new IllegalArgumentException("Cannot destroy edge where both nodes are not in this graph.");
		else if(firstNode.graph != this)
			throw new IllegalArgumentException("Cannot destroy edge where first node is not in this graph.");
		else if(secondNode.graph != this)
			throw new IllegalArgumentException("Cannot destroy edge where second node is not in this graph.");

		LinkedList<DepEdge> edgesToRemove = new LinkedList<DepEdge>();
		// in firstNode, remove any links to second node by way of child edges
		for(DepEdge childEdge : firstNode.requireChildEdges) {
			if(childEdge.childNode == secondNode)
				edgesToRemove.add(childEdge);
		}
		firstNode.requireChildEdges.removeAll(edgesToRemove);
		edgesToRemove.clear();
		// in firstNode, remove any links to second node by way of parent edges
		for(DepEdge parentEdge : firstNode.requireParentEdges) {
			if(parentEdge.parentNode == secondNode)
				edgesToRemove.add(parentEdge);
		}
		firstNode.requireParentEdges.removeAll(edgesToRemove);
		edgesToRemove.clear();

		// in secondNode, remove any links to first node by way of child edges
		for(DepEdge childEdge : secondNode.requireChildEdges) {
			if(childEdge.childNode == firstNode)
				edgesToRemove.add(childEdge);
		}
		secondNode.requireChildEdges.removeAll(edgesToRemove);
		edgesToRemove.clear();
		// in secondNode, remove any links to first node by way of parent edges
		for(DepEdge parentEdge : secondNode.requireParentEdges) {
			if(parentEdge.parentNode == firstNode)
				edgesToRemove.add(parentEdge);
		}
		secondNode.requireParentEdges.removeAll(edgesToRemove);
	}

	/*
	 * Order of input nodes does not matter. The only requirement is that firstNode is an "order dependency"
	 * parent of secondNode or secondNode is an "order dependency" parent of firstNode.
	 */
	public void destroyOrderEdge(DepNode firstNode, DepNode secondNode) {
		if(firstNode.graph != this && secondNode.graph != this)
			throw new IllegalArgumentException("Cannot destroy order edge where both nodes are not in this graph.");
		else if(firstNode.graph != this)
			throw new IllegalArgumentException("Cannot destroy order edge where first node is not in this graph.");
		else if(secondNode.graph != this)
			throw new IllegalArgumentException("Cannot destroy order edge where second node is not in this graph.");

		LinkedList<DepEdge> edgesToRemove = new LinkedList<DepEdge>();
		// in firstNode, remove any order links to second node by way of child edges
		for(DepEdge childEdge : firstNode.orderChildEdges) {
			if(childEdge.childNode == secondNode)
				edgesToRemove.add(childEdge);
		}
		firstNode.orderChildEdges.removeAll(edgesToRemove);
		edgesToRemove.clear();
		// in firstNode, remove any order links to second node by way of parent edges
		for(DepEdge parentEdge : firstNode.orderParentEdges) {
			if(parentEdge.parentNode == secondNode)
				edgesToRemove.add(parentEdge);
		}
		firstNode.orderParentEdges.removeAll(edgesToRemove);
		edgesToRemove.clear();

		// in secondNode, remove any order links to first node by way of child edges
		for(DepEdge childEdge : secondNode.orderChildEdges) {
			if(childEdge.childNode == firstNode)
				edgesToRemove.add(childEdge);
		}
		secondNode.orderChildEdges.removeAll(edgesToRemove);
		edgesToRemove.clear();
		// in secondNode, remove any order links to first node by way of parent edges
		for(DepEdge parentEdge : secondNode.orderParentEdges) {
			if(parentEdge.parentNode == firstNode)
				edgesToRemove.add(parentEdge);
		}
		secondNode.orderParentEdges.removeAll(edgesToRemove);
	}

	// destroy (without resolve) the given node; do not resolve it, or any other nodes
	public void destroyNode(DepNode node) {
		if(node.graph != this)
			throw new IllegalArgumentException("Cannot destroy node that is not in this graph.");

		// if node is root node then remove from list of root nodes
		if(node.requireParentEdges.isEmpty())
			requireRootNodes.remove(node);
		// remove any child node links to this node
		for(DepEdge childEdge : node.requireChildEdges) {
			childEdge.childNode.requireParentEdges.remove(childEdge);
			// if child node now has zero parent nodes then child is a root node
			if(childEdge.childNode.requireParentEdges.isEmpty())
				requireRootNodes.add(childEdge.childNode);
		}
		node.requireChildEdges.clear();
		// remove any parent node links to this node
		for(DepEdge parentEdge : node.requireParentEdges)
			parentEdge.parentNode.requireChildEdges.remove(parentEdge);
		node.requireParentEdges.clear();

		// if node is order root node then remove from list of order root nodes
		if(node.orderParentEdges.isEmpty())
			orderRootNodes.remove(node);
		// remove any order child node links to this node
		for(DepEdge childEdge : node.orderChildEdges) {
			childEdge.childNode.orderParentEdges.remove(childEdge);
			// if child node now has zero order parent nodes then child is an order root node
			if(childEdge.childNode.orderParentEdges.isEmpty())
				orderRootNodes.add(childEdge.childNode);
		}
		node.orderChildEdges.clear();
		// remove any order parent node links to this node
		for(DepEdge parentEdge : node.orderParentEdges)
			parentEdge.parentNode.orderChildEdges.remove(parentEdge);
		node.orderParentEdges.clear();
	}

	// destroy (without resolve) all nodes in this graph
	public void destroyGraph() {
		requireRootNodes.clear();
		orderRootNodes.clear();
		// TODO clean up refs to help garbage collection?
	}
}
