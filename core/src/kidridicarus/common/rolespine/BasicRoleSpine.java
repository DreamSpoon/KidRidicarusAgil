package kidridicarus.common.rolespine;

import java.util.List;

import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agent;
import kidridicarus.agency.AgentHooks;
import kidridicarus.agency.agentbody.AgentBody;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.metarole.tiledmap.solidlayer.SolidTiledMapRole;
import kidridicarus.common.role.despawnbox.DespawnBox;
import kidridicarus.common.role.keepalivebox.KeepAliveBox;
import kidridicarus.common.role.optional.ContactDmgTakeRole;
import kidridicarus.common.role.optional.PowerupTakeRole;
import kidridicarus.common.role.playerrole.PlayerRole;
import kidridicarus.common.role.roombox.RoomBox;
import kidridicarus.common.rolesensor.RoleContactHoldSensor;
import kidridicarus.common.tool.Direction4;
import kidridicarus.common.tool.RP_Tool;
import kidridicarus.story.Role;

/*
 * Complement of the AgentBody, the Agent spine allows for better organization/coordination of movement than
 * simply using AgentBody.
 */
public class BasicRoleSpine {
	protected Role parentRole;
	protected AgentBody body;
	protected RoleContactHoldSensor roleSensor;

	public BasicRoleSpine(Role parentRole) {
		this.parentRole = parentRole;
		body = null;
		roleSensor = null;
	}

	public RoleContactHoldSensor createRoleSensor() {
		roleSensor = new RoleContactHoldSensor(parentRole);
		return roleSensor;
	}

	public void setBody(AgentBody body) {
		this.body = body;
	}

	public PowerupTakeRole getTouchingPowerupTaker() {
		return roleSensor.getFirstContactByUserDataClass(PowerupTakeRole.class);
	}

	public boolean isContactDespawn() {
		return roleSensor.getFirstContactByUserDataClass(DespawnBox.class) != null;
	}

	public boolean isContactKeepAlive() {
		return roleSensor.getFirstContactByUserDataClass(KeepAliveBox.class) != null;
	}

	public List<ContactDmgTakeRole> getContactDmgTakeRoles() {
		return roleSensor.getContactsByUserDataClass(ContactDmgTakeRole.class);
	}

	public List<PlayerRole> getPlayerContacts() {
		return roleSensor.getContactsByUserDataClass(PlayerRole.class);
	}

	public RoomBox getCurrentRoom() {
		return roleSensor.getFirstContactByUserDataClass(RoomBox.class);
	}

	public SolidTiledMapRole getSolidTileMap() {
		if(roleSensor == null)
			return null;
		return roleSensor.getFirstContactByUserDataClass(SolidTiledMapRole.class);
	}

/*	public void checkDoSpaceWrap(RoomBox curRoom) {
		// if no room, or no bounds, or no space wrap flag, then exit
		if(curRoom == null)
			return;
		Rectangle roomBounds = AP_Tool.getBounds(curRoom);
		if(roomBounds == null)
			return;
		if(!curRoom.getProperty(CommonKV.Room.KEY_SPACEWRAP_X, false, Boolean.class))
			return;
		// if body position is outside room on left...
		if(body.getPosition().x < roomBounds.x)
			body.checkDoDefineBody(new Vector2(roomBounds.x+roomBounds.width, body.getPosition().y), true);
		// if body position is outside room on right...
		else if(body.getPosition().x > roomBounds.x+roomBounds.width)
			body.checkDoDefineBody(new Vector2(roomBounds.x, body.getPosition().y), true);
	}
*/
	public boolean isMovingInDir(Direction4 dir) {
		if(dir == null)
			return false;
		switch(dir) {
			case RIGHT:
				if(body.getVelocity().x > UInfo.VEL_EPSILON)
					return true;
				break;
			case LEFT:
				if(body.getVelocity().x < UInfo.VEL_EPSILON)
					return true;
				break;
			case UP:
				if(body.getVelocity().y > UInfo.VEL_EPSILON)
					return true;
				break;
			case DOWN:
				if(body.getVelocity().y < UInfo.VEL_EPSILON)
					return true;
				break;
			default:
		}
		return false;
	}

	// if target Agent is on side given by isOnRight then return true, otherwise return false
	public boolean isTargetOnSide(AgentHooks hooks, Agent target, boolean isOnRight) {
		// return false if target is null or target doesn't have position
		if(target == null || !(target.getUserData() instanceof Role))
			return false;
		Vector2 otherPos = RP_Tool.getCenter((Role) target.getUserData());
		if(otherPos == null)
			return false;
		// do check based on side given by isOnRight
		if(isOnRight)
			// is other on right side of this?
			return UInfo.M2Tx(otherPos.x) > UInfo.M2Tx(body.getPosition().x);
		else
			// is other on left side of this?
			return UInfo.M2Tx(otherPos.x) < UInfo.M2Tx(body.getPosition().x);
	}

	public Vector2 getPosition() {
		return body.getPosition();
	}

	public void zeroVelocity(boolean zeroX, boolean zeroY) {
		body.zeroVelocity(zeroX, zeroY);
	}
}
