package kidridicarus.common.role.levelendtrigger;

import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.PhysicsHooks;
import kidridicarus.common.info.CommonCF;
import kidridicarus.common.role.playerrole.PlayerRole;
import kidridicarus.common.rolesensor.OneWayContactSensor;
import kidridicarus.common.tool.ABodyFactory;
import kidridicarus.story.Role;
import kidridicarus.story.rolebody.RoleBody;

class LevelEndTriggerBody extends RoleBody {
	private OneWayContactSensor playerSensor;

	LevelEndTriggerBody(Role parentRole, PhysicsHooks physHooks, Rectangle bounds) {
		super(physHooks);
		setBoundsSize(bounds.width, bounds.height);
		agentBody = ABodyFactory.makeStaticBody(physHooks, bounds.getCenter(new Vector2()));
		playerSensor = new OneWayContactSensor(parentRole, true);
		ABodyFactory.makeBoxFixture(agentBody, CommonCF.ROLE_SENSOR_CFCAT, CommonCF.ROLE_SENSOR_CFMASK, playerSensor,
				getBounds().width, getBounds().height);
	}

	public List<PlayerRole> getPlayerBeginContacts() {
		return playerSensor.getOnlyAndResetContacts(PlayerRole.class);
	}
}
