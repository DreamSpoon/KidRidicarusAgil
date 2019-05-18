package kidridicarus.common.tool;

import java.util.Iterator;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.Agent;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonKV;
import kidridicarus.story.Role;
import kidridicarus.story.RoleHooks;
import kidridicarus.story.info.StoryKV;

/*
 * Title: Agent Properties tool
 * Desc: Includes convenience methods for:
 *   -creating Agent properties
 *   -retrieving individual Agent properties
 */
public class RP_Tool {
	public static ObjectProperties createAP(String roleClassAlias) {
		ObjectProperties ret = new ObjectProperties();
		ret.put(StoryKV.KEY_ROLE_CLASS, roleClassAlias);
		return ret;
	}

	public static ObjectProperties createPointAP(String roleClassAlias, Vector2 position) {
		ObjectProperties ret = new ObjectProperties();
		ret.put(StoryKV.KEY_ROLE_CLASS, roleClassAlias);
		ret.put(CommonKV.KEY_POSITION, position);
		return ret;
	}

	public static ObjectProperties createPointAP(String roleClassAlias, Vector2 position, Vector2 velocity) {
		ObjectProperties ret = new ObjectProperties();
		ret.put(StoryKV.KEY_ROLE_CLASS, roleClassAlias);
		ret.put(CommonKV.KEY_POSITION, position);
		ret.put(CommonKV.KEY_VELOCITY, velocity);
		return ret;
	}

	public static ObjectProperties createRectangleRP(String roleClassAlias, Rectangle bounds) {
		ObjectProperties ret = new ObjectProperties();
		ret.put(StoryKV.KEY_ROLE_CLASS, roleClassAlias);
		ret.put(CommonKV.KEY_BOUNDS, bounds);
		return ret;
	}

	public static ObjectProperties createTileRP(MapProperties mapProps, Rectangle bounds,
			TextureRegion tileTexRegion) {
		ObjectProperties agentProps = createRectangleRP(mapProps, bounds);
		// add a reference to the start tile texture region if non-null is given 
		if(tileTexRegion != null)
			agentProps.put(CommonKV.KEY_TEXREGION, tileTexRegion);
		return agentProps;
	}

	public static ObjectProperties createRectangleRP(MapProperties mapProps, Rectangle bounds) {
		ObjectProperties agentProps = createMapAP(mapProps);
		// copy the bounds rectangle to the agent properties
		agentProps.put(CommonKV.KEY_BOUNDS, bounds);
		return agentProps;
	}

	private static ObjectProperties createMapAP(MapProperties mapProps) {
		// copy the map properties to the agent properties
		ObjectProperties agentProps = new ObjectProperties();
		Iterator<String> keyIter = mapProps.getKeys();
		while(keyIter.hasNext()) {
			String key = keyIter.next();
			// add the map property to the agent properties map
			agentProps.put(key, mapProps.get(key));
		}
		return agentProps;
	}

	public static Vector2 getCenter(ObjectProperties agentProps) {
		Vector2 point = agentProps.get(CommonKV.KEY_POSITION, null, Vector2.class);
		if(point != null)
			return point;
		Rectangle bounds = agentProps.get(CommonKV.KEY_BOUNDS, null, Rectangle.class);
		if(bounds != null)
			return bounds.getCenter(new Vector2());
		return null;
	}

	public static Vector2 getCenter(Role role) {
		Vector2 point = role.getAgent().getProperty(CommonKV.KEY_POSITION, null, Vector2.class);
		if(point != null)
			return point;
		Rectangle bounds = role.getAgent().getProperty(CommonKV.KEY_BOUNDS, null, Rectangle.class);
		if(bounds != null)
			return bounds.getCenter(new Vector2());
		return null;
	}

	public static Rectangle getBounds(ObjectProperties agentProps) {
		return agentProps.get(CommonKV.KEY_BOUNDS, null, Rectangle.class);
	}

	public static Rectangle getBounds(Role role) {
		return role.getAgent().getProperty(CommonKV.KEY_BOUNDS, null, Rectangle.class);
	}

	public static Vector2 safeGetVelocity(ObjectProperties agentProps) {
		return agentProps.get(CommonKV.KEY_VELOCITY, new Vector2(0f, 0f), Vector2.class);
	}

	public static Vector2 getVelocity(Role role) {
		return role.getAgent().getProperty(CommonKV.KEY_VELOCITY, null, Vector2.class);
	}

	public static TextureRegion getTexRegion(ObjectProperties agentProps) {
		return agentProps.get(CommonKV.KEY_TEXREGION, null, TextureRegion.class);
	}

	public static Direction4 safeGetDirection4(Role role) {
		return role.getAgent().getProperty(CommonKV.KEY_DIRECTION, Direction4.NONE, Direction4.class);
	}

	public static Direction8 safeGetDirection8(ObjectProperties agentProps) {
		return agentProps.get(CommonKV.KEY_DIRECTION, Direction8.NONE, Direction8.class);
	}

	// search Agency by the global property by "name", and return a list of Roles associated with "name"
	public static Role getNamedRole(String strName, RoleHooks roleHooks) {
		Agent agentByName = roleHooks.agentHooksBundle.agentHooks.getFirstAgentByProperty(
				CommonKV.Script.KEY_NAME, strName);
		if(agentByName == null || !(agentByName.getUserData() instanceof Role))
			return null;
		return (Role) agentByName.getUserData();
	}

	public static Role getTargetRole(Role role, RoleHooks roleHooks) {
		String targetNameStr = role.getAgent().getProperty(CommonKV.Script.KEY_TARGET_NAME, null, String.class);
		if(targetNameStr == null)
			return null;
		return getNamedRole(targetNameStr, roleHooks);
	}

	public static String getTargetName(ObjectProperties properties) {
		return properties.getString(CommonKV.Script.KEY_TARGET_NAME, "");
	}
}
