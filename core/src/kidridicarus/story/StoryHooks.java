package kidridicarus.story;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import kidridicarus.agency.tool.ObjectProperties;

public class StoryHooks {
	public Role createRole(ObjectProperties properties) { return myStory.externalCreateRole(properties); }

	private final Story myStory;

	public StoryHooks(Story story) {
		this.myStory = story;
	}

	public List<Role> createRoles(Collection<ObjectProperties> roleProps) {
		LinkedList<Role> newRoleList = new LinkedList<Role>();
		Iterator<ObjectProperties> propsIter = roleProps.iterator();
		while(propsIter.hasNext())
			newRoleList.add(myStory.externalCreateRole(propsIter.next()));
		return newRoleList;
	}

	public boolean isValidRoleClassAlias(String strClassAlias) {
		return myStory.allRolesClassList.get(strClassAlias) != null;
	}
}
