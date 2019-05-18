package kidridicarus.story;

import kidridicarus.agency.AgentHooksBundle;

public class RoleHooks {
	public final StoryHooks storyHooks;
	public final AgentHooksBundle agentHooksBundle;

	public RoleHooks(StoryHooks storyHooks, AgentHooksBundle agentHooksBundle) {
		this.storyHooks = storyHooks;
		this.agentHooksBundle = agentHooksBundle;
	}
}
