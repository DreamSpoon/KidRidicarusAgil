package kidridicarus.common.role.levelendtrigger;

import kidridicarus.agency.agent.AgentRemoveCallback;
import kidridicarus.agency.agent.AgentUpdateListener;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.role.general.CorpusRole;
import kidridicarus.common.role.optional.TriggerTakeRole;
import kidridicarus.common.role.playerrole.PlayerRole;
import kidridicarus.common.tool.RP_Tool;
import kidridicarus.story.Role;
import kidridicarus.story.RoleHooks;

public class LevelEndTrigger extends CorpusRole implements TriggerTakeRole{
	private String nextLevelFilename;
	private RoleHooks myRoleHooks;

	public LevelEndTrigger(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);
		this.myRoleHooks = roleHooks;
		body = new LevelEndTriggerBody(this, myPhysHooks, RP_Tool.getBounds(properties));
		nextLevelFilename = properties.getString(CommonKV.Level.VAL_NEXTLEVEL_FILENAME, "");
		myAgentHooks.addUpdateListener(CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(FrameTime frameTime) { doContactUpdate(); }
		});
		myAgentHooks.createAgentRemoveListener(myAgent, new AgentRemoveCallback() {
			@Override
			public void preRemoveAgent() { dispose(); }
		});
	}

	private void doContactUpdate() {
		for(PlayerRole role : ((LevelEndTriggerBody) body).getPlayerBeginContacts())
			role.getSupervisor().startScript(new LevelEndScript(this, nextLevelFilename));
	}

	@Override
	public void onTakeTrigger() {
		Role targetRole = RP_Tool.getTargetRole(this, myRoleHooks);
		if(targetRole instanceof TriggerTakeRole)
			((TriggerTakeRole) targetRole).onTakeTrigger();
	}
}
