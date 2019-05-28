package kidridicarus.desktop;
/*
import java.util.LinkedList;

import kidridicarus.agency.Agent;
import kidridicarus.agency.tool.discoroad.DepNode;
import kidridicarus.agency.tool.discoroad.DepResolver;
import kidridicarus.agency.tool.discoroad.DiscoROAD;
import kidridicarus.common.tool.QQ;

public class TestFinal {
	private interface AgencyChange { public void applyChange(); }

	private DiscoROAD removalROAD;
	private LinkedList<AgencyChange> removalDependencyChangeQ;

	public TestFinal() {
		Agent testAgent = new Agent();
		removalROAD = new DiscoROAD();
		removalDependencyChangeQ = new LinkedList<AgencyChange>(); 
		addAgent(testAgent);
	}

	public void addAgent(final Agent agent) {
		QQ.pr("add agent");
		// before adding agent, create a diabolical plan to remove agent (mwa-ha-ha!)
		agent.removalNode = removalROAD.createNode(new DepResolver() {
				@Override
				public void resolve() { 
					// Queue destruction of removal dependency node instead of destroying immediately - since this
					// operation is performed during resolution of removal graph, making destruction of node
					// impossible at this time.
QQ.pr("agent.removelNode="+agent.removalNode);
					final DepNode nodeToDestroy = agent.removalNode;	// be careful with references
QQ.pr("nodeToDestroy="+nodeToDestroy);
					// mark agent as removed, to prevent removing agent multiple times
					agent.removalNode = null;
					removalDependencyChangeQ.add(new AgencyChange() {
							@Override
							public void applyChange() {
QQ.pr("removalROAD.destroyNode(nodeToDestroy);, nodeToDestroy="+nodeToDestroy);
								removalROAD.destroyNode(nodeToDestroy);
							}
						});
				}
			});
QQ.pr("finished creating removal node, agent.removalNode="+agent.removalNode);
		agent.removalNode.resolver.resolve();
QQ.pr("finished resolving node, agent.removalNode="+agent.removalNode);
	}
}
*/
/*
 * Correct result,
 * I expected nodeToDestroy to be:
 *   -final within the scope of the resolve() method
 * instead of:
 *   -final outside the scope of the resolve() method, and
 *   -final within the scope of the addAgent() method 
 * Result of running the above code:

add agent
finished creating removal node, agent.removalNode=kidridicarus.agency.tool.discoroad.DepNode@6d06d69c
agent.removelNode=kidridicarus.agency.tool.discoroad.DepNode@6d06d69c
nodeToDestroy=kidridicarus.agency.tool.discoroad.DepNode@6d06d69c
finished resolving node, agent.removalNode=null

*/