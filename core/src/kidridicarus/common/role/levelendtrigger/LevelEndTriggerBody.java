package kidridicarus.common.role.levelendtrigger;

import java.util.Collection;
import java.util.LinkedList;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.AgentFilter;
import kidridicarus.agency.AgentFixture;
import kidridicarus.agency.PhysicsHooks;
import kidridicarus.agency.tool.FilterBitSet;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.role.optional.ScriptableRole;
import kidridicarus.common.role.player.PlayerRole;
import kidridicarus.common.tool.ABodyFactory;
import kidridicarus.story.RoleSensor;
import kidridicarus.story.rolebody.RoleBody;

class LevelEndTriggerBody extends RoleBody {
	private static final float GRAVITY_SCALE = 0f;

	private RoleSensor playerSensor;

	LevelEndTriggerBody(PhysicsHooks physHooks, Rectangle bounds) {
		super(physHooks);
		this.agentBody = ABodyFactory.makeDynamicBody(physHooks, bounds.getCenter(new Vector2()));
		this.agentBody.setGravityScale(GRAVITY_SCALE);
		AgentFilter filter = new AgentFilter(new FilterBitSet(CommonCF.ACFB.PLAYER_TAKEBIT),
				new FilterBitSet(CommonCF.ACFB.PLAYER_GIVEBIT));
		AgentFixture fixture = ABodyFactory.makeSensorBoxFixture(this.agentBody, filter, bounds.width, bounds.height);
		this.playerSensor = new RoleSensor(fixture, filter);
	}

	// returns a begin contacts list of PlayerRoles that are also ScriptableRoles
	Collection<ScriptableRole> getPlayerBeginContacts() {
		Collection<ScriptableRole> playerRoles = new LinkedList<ScriptableRole>();
		for(PlayerRole role : playerSensor.getBeginContactsByRoleClass(PlayerRole.class)) {
			if(role instanceof ScriptableRole)
				playerRoles.add((ScriptableRole) role);
		}
		return playerRoles;
	}
}
