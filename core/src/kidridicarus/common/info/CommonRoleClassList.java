package kidridicarus.common.info;

import kidridicarus.common.metarole.playercontrollerrole.PlayerControllerRole;
import kidridicarus.common.metarole.tiledmap.TiledMapMetaRole;
import kidridicarus.common.metarole.tiledmap.drawlayer.DrawLayerRole;
import kidridicarus.common.metarole.tiledmap.solidlayer.SolidTiledMapRole;
import kidridicarus.common.role.despawnbox.DespawnBox;
import kidridicarus.common.role.keepalivebox.KeepAliveBox;
import kidridicarus.common.role.levelendtrigger.LevelEndTrigger;
import kidridicarus.common.role.playerspawner.PlayerSpawner;
import kidridicarus.common.role.rolespawner.RoleSpawner;
import kidridicarus.common.role.rolespawntrigger.RoleSpawnTrigger;
import kidridicarus.common.role.roombox.RoomBox;
import kidridicarus.common.role.scrollkillbox.ScrollKillBox;
import kidridicarus.common.role.scrollpushbox.ScrollPushBox;
import kidridicarus.common.role.semisolidfloor.SemiSolidFloor;
import kidridicarus.story.tool.RoleClassList;

public class CommonRoleClassList {
	public static final RoleClassList CORE_ROLE_CLASS_LIST = new RoleClassList(
			CommonKV.RoleClassAlias.VAL_ROLESPAWNER, RoleSpawner.class,
			CommonKV.RoleClassAlias.VAL_ROLESPAWN_TRIGGER, RoleSpawnTrigger.class,
			CommonKV.RoleClassAlias.VAL_DESPAWN, DespawnBox.class,
			CommonKV.RoleClassAlias.VAL_DRAWABLE_TILEMAP, DrawLayerRole.class,
			CommonKV.RoleClassAlias.VAL_KEEPALIVE_BOX, KeepAliveBox.class,
			CommonKV.RoleClassAlias.VAL_LEVELEND_TRIGGER, LevelEndTrigger.class,
			CommonKV.RoleClassAlias.VAL_PLAYER_CONTROLLER, PlayerControllerRole.class,
			CommonKV.RoleClassAlias.VAL_SOLID_TILEDMAP, SolidTiledMapRole.class,
			CommonKV.RoleClassAlias.VAL_PLAYER_SPAWNER, PlayerSpawner.class,
			CommonKV.RoleClassAlias.VAL_ROOM, RoomBox.class,
			CommonKV.RoleClassAlias.VAL_SCROLL_KILLBOX, ScrollKillBox.class,
			CommonKV.RoleClassAlias.VAL_SCROLL_PUSHBOX, ScrollPushBox.class,
			CommonKV.RoleClassAlias.VAL_SEMISOLID_FLOOR, SemiSolidFloor.class,
			CommonKV.RoleClassAlias.VAL_META_TILEDMAP, TiledMapMetaRole.class);
}
