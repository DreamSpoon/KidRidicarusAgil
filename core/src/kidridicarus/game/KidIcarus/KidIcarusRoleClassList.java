package kidridicarus.game.KidIcarus;

import kidridicarus.game.KidIcarus.role.NPC.shemum.Shemum;
import kidridicarus.game.KidIcarus.role.item.angelheart.AngelHeart;
import kidridicarus.game.KidIcarus.role.other.kidicarusdoor.KidIcarusDoor;
import kidridicarus.game.KidIcarus.role.other.vanishpoof.VanishPoof;
import kidridicarus.game.KidIcarus.role.player.pit.Pit;
import kidridicarus.game.KidIcarus.role.player.pitarrow.PitArrow;
import kidridicarus.story.tool.RoleClassList;

public class KidIcarusRoleClassList {
	public static final RoleClassList KIDICARUS_ROLE_CLASSLIST = new RoleClassList(
			KidIcarusKV.RoleClassAlias.VAL_ANGEL_HEART, AngelHeart.class,
			KidIcarusKV.RoleClassAlias.VAL_DOOR, KidIcarusDoor.class,
			KidIcarusKV.RoleClassAlias.VAL_PIT, Pit.class,
			KidIcarusKV.RoleClassAlias.VAL_PIT_ARROW, PitArrow.class,
			KidIcarusKV.RoleClassAlias.VAL_SHEMUM, Shemum.class,
			KidIcarusKV.RoleClassAlias.VAL_VANISH_POOF, VanishPoof.class);
}
