package kidridicarus.common.info;

import kidridicarus.common.role.despawntrigger.DespawnTrigger;
import kidridicarus.common.role.keepalivetrigger.KeepAliveTrigger;
import kidridicarus.common.role.levelendtrigger.LevelEndTrigger;
import kidridicarus.common.role.onewayfloor.OneWayFloor;
import kidridicarus.common.role.player.PlayerControllerRole;
import kidridicarus.common.role.playerspawner.PlayerSpawner;
import kidridicarus.common.role.rolespawner.RoleSpawner;
import kidridicarus.common.role.rolespawntrigger.RoleSpawnTrigger;
import kidridicarus.common.role.roombox.RoomBox;
import kidridicarus.common.role.scrollkillbox.ScrollKillBox;
import kidridicarus.common.role.scrollpushbox.ScrollPushBox;
import kidridicarus.common.role.tiledmap.TiledMapMetaRole;
import kidridicarus.common.role.tiledmap.drawlayer.DrawLayerRole;
import kidridicarus.common.role.tiledmap.solidlayer.SolidTiledMapRole;
import kidridicarus.story.tool.RoleClassList;

public class CommonRoleClassList {
	public static final RoleClassList CORE_ROLE_CLASS_LIST = new RoleClassList(
			CommonKV.RoleClassAlias.VAL_ROLESPAWNER, RoleSpawner.class,
			CommonKV.RoleClassAlias.VAL_ROLESPAWN_TRIGGER, RoleSpawnTrigger.class,
			CommonKV.RoleClassAlias.VAL_DESPAWN, DespawnTrigger.class,
			CommonKV.RoleClassAlias.VAL_DRAWABLE_TILEMAP, DrawLayerRole.class,
			CommonKV.RoleClassAlias.VAL_KEEPALIVE_BOX, KeepAliveTrigger.class,
			CommonKV.RoleClassAlias.VAL_LEVELEND_TRIGGER, LevelEndTrigger.class,
			CommonKV.RoleClassAlias.VAL_PLAYER_CONTROLLER, PlayerControllerRole.class,
			CommonKV.RoleClassAlias.VAL_SOLID_TILEDMAP, SolidTiledMapRole.class,
			CommonKV.RoleClassAlias.VAL_PLAYER_SPAWNER, PlayerSpawner.class,
			CommonKV.RoleClassAlias.VAL_ROOM, RoomBox.class,
			CommonKV.RoleClassAlias.VAL_SCROLL_KILLBOX, ScrollKillBox.class,
			CommonKV.RoleClassAlias.VAL_SCROLL_PUSHBOX, ScrollPushBox.class,
			CommonKV.RoleClassAlias.VAL_SEMISOLID_FLOOR, OneWayFloor.class,
			CommonKV.RoleClassAlias.VAL_META_TILEDMAP, TiledMapMetaRole.class);
}
