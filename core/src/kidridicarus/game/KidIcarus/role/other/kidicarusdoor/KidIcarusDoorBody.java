package kidridicarus.game.KidIcarus.role.other.kidicarusdoor;

import java.util.Collection;
import java.util.LinkedList;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.AgentFilter;
import kidridicarus.agency.AgentFixture;
import kidridicarus.agency.PhysicsHooks;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.role.optional.ScriptableRole;
import kidridicarus.common.role.player.PlayerRole;
import kidridicarus.common.tool.ABodyFactory;
import kidridicarus.story.RoleSensor;
import kidridicarus.story.rolebody.RoleBody;

class KidIcarusDoorBody extends RoleBody {
	private static final Vector2 DOORWAY_SIZE = UInfo.VectorP2M(14f, 27f);
	private static final Vector2 BODY_OFFSET = UInfo.VectorP2M(0f, -2.5f);
	private static final Vector2 ROOF_SIZE = UInfo.VectorP2M(13f, 4f);
	private static final float GRAVITY_SCALE = 0f;

	private RoleSensor playerSensor;
	private AgentFixture doorwaySolidFixture;
	private boolean isOpened;

	KidIcarusDoorBody(PhysicsHooks physHooks, Vector2 position, boolean isOpened) {
		super(physHooks);

		this.isOpened = isOpened;

		this.agentBody = ABodyFactory.makeDynamicBody(physHooks, position.cpy().add(BODY_OFFSET));
		this.agentBody.setGravityScale(GRAVITY_SCALE);

		// create the player sensor fixture
		AgentFilter playerSensorFilter = new AgentFilter(CommonCF.ACFB.PLAYER_TAKEBIT, CommonCF.ACFB.PLAYER_GIVEBIT);
		AgentFixture playerSensorFixture = ABodyFactory.makeSensorBoxFixture(this.agentBody, playerSensorFilter,
				DOORWAY_SIZE);
		this.playerSensor = new RoleSensor(playerSensorFixture, playerSensorFilter);

		// door fixture that can be solid if door is closed, or non-solid if door is open
		this.doorwaySolidFixture = ABodyFactory.makeBoxFixture(this.agentBody,
				isOpened ? CommonCF.NO_CONTACT_FILTER : CommonCF.FULL_SOLID_FILTER, DOORWAY_SIZE);

		// solid roof that things can stand on
		ABodyFactory.makeBoxFixture(this.agentBody, CommonCF.FULL_SOLID_FILTER, ROOF_SIZE,
				new Vector2(0f, (DOORWAY_SIZE.y+ROOF_SIZE.y)/2f));
	}

	void setOpened(boolean isOpened) {
		// if open state is not changing then exit
		if(this.isOpened == isOpened)
			return;
		doorwaySolidFixture.setFilterData(isOpened ? CommonCF.NO_CONTACT_FILTER : CommonCF.FULL_SOLID_FILTER);
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
