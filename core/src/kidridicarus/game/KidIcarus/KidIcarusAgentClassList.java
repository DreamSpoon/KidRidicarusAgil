package kidridicarus.game.KidIcarus;

import kidridicarus.agency.tool.AgentClassList;
import kidridicarus.game.KidIcarus.agent.NPC.shemum.Shemum;
import kidridicarus.game.KidIcarus.agent.item.angelheart.AngelHeart;
import kidridicarus.game.KidIcarus.agent.other.kidicarusdoor.KidIcarusDoor;
import kidridicarus.game.KidIcarus.agent.other.vanishpoof.VanishPoof;
import kidridicarus.game.KidIcarus.agent.player.pit.Pit;
import kidridicarus.game.KidIcarus.agent.player.pitarrow.PitArrow;

public class KidIcarusAgentClassList {
	public static final AgentClassList KIDICARUS_AGENT_CLASSLIST = new AgentClassList( 
			KidIcarusKV.AgentClassAlias.VAL_ANGEL_HEART, AngelHeart.class,
			KidIcarusKV.AgentClassAlias.VAL_DOOR, KidIcarusDoor.class,
			KidIcarusKV.AgentClassAlias.VAL_PIT, Pit.class,
			KidIcarusKV.AgentClassAlias.VAL_PIT_ARROW, PitArrow.class,
			KidIcarusKV.AgentClassAlias.VAL_SHEMUM, Shemum.class,
			KidIcarusKV.AgentClassAlias.VAL_VANISH_POOF, VanishPoof.class);
}
