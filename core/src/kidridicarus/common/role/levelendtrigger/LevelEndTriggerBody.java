package kidridicarus.common.role.levelendtrigger;

import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.PhysicsHooks;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.role.playerrole.PlayerRole;
import kidridicarus.common.rolesensor.OneWayContactSensor;
import kidridicarus.common.tool.B2DFactory;
import kidridicarus.story.Role;

class LevelEndTriggerBody extends AgentBody {
	private OneWayContactSensor playerSensor;

	LevelEndTriggerBody(Role parentRole, PhysicsHooks physHooks, Rectangle bounds) {
		super(parentRole.getAgent(), physHooks);
		setBoundsSize(bounds.width, bounds.height);
		b2body = B2DFactory.makeStaticBody(physHooks, bounds.getCenter(new Vector2()));
		playerSensor = new OneWayContactSensor(parentRole, true);
		B2DFactory.makeBoxFixture(b2body, CommonCF.ROLE_SENSOR_CFCAT, CommonCF.ROLE_SENSOR_CFMASK, playerSensor,
				getBounds().width, getBounds().height);
	}

	public List<PlayerRole> getPlayerBeginContacts() {
		return playerSensor.getOnlyAndResetContacts(PlayerRole.class);
	}
}
