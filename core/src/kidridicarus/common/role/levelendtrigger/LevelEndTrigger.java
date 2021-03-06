package kidridicarus.common.role.levelendtrigger;

import kidridicarus.agency.Agent.AgentUpdateListener;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.role.levelendtrigger.script.LevelEndScript;
import kidridicarus.common.role.optional.ScriptableRole;
import kidridicarus.common.role.optional.TriggerTakeRole;
import kidridicarus.story.Role;
import kidridicarus.story.RoleHooks;
import kidridicarus.story.tool.RP_Tool;

public class LevelEndTrigger extends Role implements TriggerTakeRole {
	private String nextLevelFilename;
	private RoleHooks myRoleHooks;
	private LevelEndTriggerBody body;

	public LevelEndTrigger(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);
		this.myRoleHooks = roleHooks;
		body = new LevelEndTriggerBody(myPhysHooks, RP_Tool.getBounds(properties));
		nextLevelFilename = properties.getString(CommonKV.Level.VAL_NEXTLEVEL_FILENAME, "");
		myAgentHooks.addUpdateListener(CommonInfo.UpdateOrder.PRE_MOVE_UPDATE, new AgentUpdateListener() {
			@Override
			public void update(FrameTime frameTime) { doContactUpdate(); }
		});
	}

	private void doContactUpdate() {
		for(ScriptableRole role : body.getPlayerBeginContacts()) {
			// If the role is not running a script, or override of script is allowed, then create/run the level-
			// end script.
			if(!role.scriptIsEnabled() || role.scriptIsOverrideAllowed())
				myRoleHooks.storyHooks.createRole(LevelEndScript.makeRP(role, nextLevelFilename));
		}
	}

	@Override
	public void onTakeTrigger() {
		Role targetRole = RP_Tool.getTargetRole(this, myRoleHooks);
		if(targetRole instanceof TriggerTakeRole)
			((TriggerTakeRole) targetRole).onTakeTrigger();
	}
}
