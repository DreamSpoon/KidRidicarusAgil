package kidridicarus.story;

import kidridicarus.agency.Agent;
import kidridicarus.agency.AgentHooks;
import kidridicarus.agency.AudioHooks;
import kidridicarus.agency.GfxHooks;
import kidridicarus.agency.PhysicsHooks;
import kidridicarus.agency.agent.AgentPropertyListener;
import kidridicarus.agency.agentbody.AgentBodyFilter;
import kidridicarus.agency.agentbody.AgentContactSensor;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.story.info.StoryKV;

public abstract class Role {
	protected final StoryHooks myStoryHooks;
	protected final Agent myAgent;
	protected final AgentHooks myAgentHooks;
	protected final PhysicsHooks myPhysHooks;
	protected final GfxHooks myGfxHooks;
	protected final AudioHooks myAudioHooks;

	public Role(RoleHooks roleHooks, ObjectProperties properties) {
		this.myStoryHooks = roleHooks.storyHooks;
		this.myAgent = roleHooks.agentHooksBundle.agent;
		this.myAgentHooks = roleHooks.agentHooksBundle.agentHooks;
		this.myPhysHooks = roleHooks.agentHooksBundle.physHooks;
		this.myGfxHooks = roleHooks.agentHooksBundle.gfxHooks;
		this.myAudioHooks = roleHooks.agentHooksBundle.audioHooks;
		// TODO insert explanation here
		this.myAgentHooks.setUserData(this);
		// Role class is set at constructor time and never changes
		final String myRoleClass = properties.getString(StoryKV.KEY_ROLE_CLASS, null);
		myAgentHooks.addPropertyListener(false, StoryKV.KEY_ROLE_CLASS, new AgentPropertyListener<String>(String.class) {
				@Override
				public String getValue() { return myRoleClass; }
			});
	}

	public Agent getAgent() {
		return myAgent;
	}

	public static Role getRoleFromABF(AgentBodyFilter abf) {
		if(abf.userData instanceof Role)
			return (Role) abf.userData;
		else if(abf.userData instanceof AgentContactSensor) {
			Agent agent = ((AgentContactSensor) abf.userData).getParent();
			if(agent != null && agent.getUserData() instanceof Role)
				return (Role) agent.getUserData();
		}
		return null;
	}
}
