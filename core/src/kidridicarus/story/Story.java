package kidridicarus.story;

import java.lang.reflect.Constructor;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;

import kidridicarus.agency.Agency;
import kidridicarus.agency.tool.Ear;
import kidridicarus.agency.tool.Eye;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.story.info.StoryKV;
import kidridicarus.story.tool.RoleClassList;

public class Story implements Disposable {
	RoleClassList allRolesClassList;
	private Agency agency;
	private StoryHooks panStoryHooks;

	public Story(RoleClassList allRolesClassList, TextureAtlas atlas) {
		this.allRolesClassList = allRolesClassList;
		this.agency = new Agency(atlas);
		this.panStoryHooks = new StoryHooks(this);
	}

	/*
	 * Create a Role object from the given properties.
	 * See website:
	 * http://www.avajava.com/tutorials/lessons/how-do-i-create-an-object-via-its-multiparameter-constructor-using-reflection.html
	 */
	public Role externalCreateRole(ObjectProperties properties) {
		String roleClassAlias = properties.getString(StoryKV.KEY_ROLE_CLASS, null);
		if(roleClassAlias == null)
			throw new IllegalArgumentException(StoryKV.KEY_ROLE_CLASS + " key not found in role definition, properties="+properties);

		Class<?> roleClass = allRolesClassList.get(roleClassAlias);
		if(roleClass == null)
			return null;
		RoleHooks newRoleHooks = new RoleHooks(panStoryHooks, agency.createAgentHooksBundle());
		Role newlyCreatedRole = null;
		try {
			Constructor<?> constructor = roleClass.getConstructor(
					new Class[] { RoleHooks.class, ObjectProperties.class });
			newlyCreatedRole = (Role) constructor.newInstance(new Object[] { newRoleHooks, properties });
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new IllegalStateException("Unable to create Role with roleClassAlias=" + roleClassAlias);
		}
		return newlyCreatedRole;
	}

	public void update(final float timeDelta) {
		agency.update(timeDelta);
	}

	public void setEar(Ear ear) {
		agency.setEar(ear);
	}

	public void setEye(Eye eye) {
		agency.setEye(eye);
	}

	public void draw() {
		agency.draw();
	}

	/*
	 * Roles are directly tied to Agents, so if the Agents are removed then the Roles will be removed as well
	 * (since Story does not keep refs to Role objects after creation). If a role needs disposal functionality,
	 * it must add an AgentRemoveListener to Agency to call Role disposal method(s).
	 */
	public void removeAllRoles() {
		agency.removeAllAgents();
	}

	// for Box2D debug renderer
	public World externalGetWorld() {
		return agency.externalGetWorld();
	}

	@Override
	public void dispose() {
		agency.dispose();
	}
}
