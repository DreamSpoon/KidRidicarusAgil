package kidridicarus.common.role.levelendtrigger.script;

import kidridicarus.agency.Agent.AgentUpdateListener;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.role.optional.ScriptableRole;
import kidridicarus.common.role.player.PlayerRole;
import kidridicarus.story.Role;
import kidridicarus.story.RoleHooks;
import kidridicarus.story.rolescript.ScriptedBodyState;
import kidridicarus.story.rolescript.ScriptedRoleState;
import kidridicarus.story.rolescript.ScriptedSpriteState;
import kidridicarus.story.tool.RP_Tool;

public class LevelEndScript extends Role {
	// 2 second delay before change to next level
	private static final float DELAY_TIME = 2f;

	private String nextLevelFilename;
	private ScriptableRole childRole;
	private float startTime;

	public LevelEndScript(RoleHooks roleHooks, ObjectProperties properties) {
		super(roleHooks, properties);
		this.nextLevelFilename = properties.getString(CommonKV.KEY_NEXTLEVEL_FILENAME, null);
		if(this.nextLevelFilename == null)
			throw new IllegalStateException("Cannot create level end script with nextLevelFilename = null.");
		this.childRole = properties.get(CommonKV.KEY_CHILD_ROLE, null, ScriptableRole.class);
		this.childRole.scriptSetEnabled(true);
		this.childRole.scriptSetFrameState(getInitChildRoleState());
		this.startTime = myAgentHooks.getAbsTime();
		myAgentHooks.addUpdateListener(CommonInfo.UpdateOrder.SCRIPT_UPDATE, new AgentUpdateListener() {
					@Override
					public void update(FrameTime frameTime) { updateScript(frameTime); }
				});
	}

	private ScriptedRoleState getInitChildRoleState() {
		ScriptedRoleState initState = new ScriptedRoleState();
		// player is not visible during entirety of script
		initState.scriptedSpriteState = new ScriptedSpriteState();
		// position remains the same during entirety of script
		initState.scriptedBodyState = new ScriptedBodyState();
		initState.scriptedBodyState.position.set(RP_Tool.getCenter((Role) childRole));
		return initState;
	}

	private void updateScript(FrameTime frameTime) {
		// inform player of level end and remove this role when delay period finishes
		if(frameTime.timeAbs >= startTime + DELAY_TIME) {
			((PlayerRole) childRole).setLevelEnded(nextLevelFilename);
			childRole.scriptSetEnabled(false);
			myAgentHooks.removeThisAgent();
		}
	}

	public static ObjectProperties makeRP(ScriptableRole childRole, String nextLevelFilename) {
		if(!(childRole instanceof PlayerRole)) {
			throw new IllegalStateException(
					"Cannot create level end script with childRole that is not instance of a PlayerRole.");
		}
		ObjectProperties rp = RP_Tool.createRP(CommonKV.RoleClassAlias.VAL_LEVELEND_SCRIPT);
		rp.put(CommonKV.KEY_CHILD_ROLE, childRole);
		rp.put(CommonKV.KEY_NEXTLEVEL_FILENAME, nextLevelFilename);
		return rp;
	}
}
