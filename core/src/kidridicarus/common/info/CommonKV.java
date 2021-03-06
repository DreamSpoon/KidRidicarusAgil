package kidridicarus.common.info;

public class CommonKV {
	public static final String VAL_TRUE = "true";
	public static final String VAL_FALSE = "false";

	public static final String KEY_DIRECTION = "direction";
	public static final String VAL_RIGHT = "right";
	public static final String VAL_UP_RIGHT = "up_right";
	public static final String VAL_UP = "up";
	public static final String VAL_UP_LEFT = "up_left";
	public static final String VAL_LEFT = "left";
	public static final String VAL_DOWN_LEFT = "down_left";
	public static final String VAL_DOWN = "down";
	public static final String VAL_DOWN_RIGHT = "down_right";

	public static final String KEY_PARENT_ROLE = "parent_role";
	public static final String KEY_CHILD_ROLE = "child_role";
	public static final String KEY_POSITION = "position";
	public static final String KEY_BOUNDS = "bounds";
	public static final String KEY_VELOCITY = "velocity";
	// used by tile Role constructors (e.g. breakable brick tile blocks)
	public static final String KEY_TEXREGION = "tex_region";
	public static final String KEY_CURRENT_ROOM = "current_room";
	public static final String KEY_GAME_OVER = "game_over";
	public static final String KEY_NON_CHAR_POWERUPLIST = "non_char_poweruplist";
	public static final String KEY_NEXTLEVEL_FILENAME = "nextlevel_filename";
	public static final String KEY_EXIT_SPAWNER = "exit_spawner";

	public class Player {
		public static final String KEY_ROLE_PROPERTIES = "role_properties";
	}

	public class RoleClassAlias {
		public static final String VAL_ROLESPAWNER = "role_spawner";
		public static final String VAL_ROLESPAWN_TRIGGER = "role_spawn_trigger";
		public static final String VAL_DESPAWN = "despawn";
		public static final String VAL_DRAWABLE_TILEMAP = "drawable_tiled_map";
		public static final String VAL_KEEPALIVE_BOX = "keep_alive_box";
		public static final String VAL_LEVELEND_TRIGGER = "level_end_trigger";
		public static final String VAL_LEVELEND_SCRIPT = "levelend_script";
		public static final String VAL_META_TILEDMAP = "meta_tiled_map";
		public static final String VAL_PIPEWARP = "pipe_warp";
		public static final String VAL_PLAYER_SPAWNER = "player_spawner";
		public static final String VAL_ROOM = "room";
		public static final String VAL_SCROLL_PUSHBOX = "scroll_push_box";
		public static final String VAL_SCROLL_KILLBOX = "scroll_kill_box";
		public static final String VAL_SEMISOLID_FLOOR = "semi_solid_floor";
		public static final String VAL_SOLID_TILEDMAP = "solid_tiled_map";
		public static final String VAL_PLAYER_CONTROLLER = "player_wrapper";
	}

	public class Spawn {
		public static final String KEY_SPAWNER_TYPE = "spawner_type";
		public static final String VAL_SPAWNER_TYPE_RESPAWN = "spawner_type_respawn";
		public static final String VAL_SPAWNER_TYPE_MULTI = "spawner_type_multi";
		public static final String KEY_SPAWN_MULTI_COUNT = "spawn_multi_count";
		public static final String KEY_SPAWN_MULTI_GRP_COUNT = "spawn_multi_grp_count";
		public static final String KEY_SPAWN_MULTI_RATE = "spawn_multi_rate";
		public static final String KEY_SPAWN_SCROLL_DIR = "spawn_scroll_direction";
		public static final String KEY_SPAWN_MAIN = "spawn_main";
		public static final String KEY_SPAWN_ROLECLASS = "spawn_role_class";
		public static final String KEY_PLAYER_ROLECLASS = "player_role_class";
		// something that needs to expire immediately
		public static final String KEY_EXPIRE = "expire";
		public static final String KEY_SPAWN_TYPE = "spawn_script";
		public static final String VAL_SPAWN_TYPE_PIPEWARP = "spawn_script_pipe_warp";
		public static final String VAL_SPAWN_TYPE_IMMEDIATE = "spawn_script_immediate";
		public static final String KEY_SPAWN_RAND_POS = "spawn_rand_pos";
	}

	public class Layer {
		public static final String KEY_LAYER_SOLID = "layer_solid";
		public static final String KEY_LAYER_DRAWORDER = "layer_draworder";
	}

	public class Script {
		public static final String KEY_SPRITE_STATE = "sprite_state";
		public static final String KEY_SPRITE_SIZE = "body_size";
		// name of Role object, so Role object can be targeted (e.g. pipe-warp entrance targets pipe-warp exit)
		public static final String KEY_NAME = "name";
		// name of targeted Role
		public static final String KEY_TARGET_NAME = "target_name";
		public static final String KEY_TARGET_LEFT = "target_left";
		public static final String KEY_TARGET_RIGHT = "target_right";
	}

	public class Level {
		public static final String VAL_NEXTLEVEL_FILENAME = "next_level_filename";
	}

	public class Sprite {
		public static final String KEY_START_FRAME = "start_frame";
	}

	public class RoleMapParams {
		public static final String KEY_TILEDMAP = "tiled_map";
		public static final String KEY_TILEDMAP_TILELAYER = "tiled_map_tile_layer";
		public static final String KEY_TILEDMAP_TILELAYER_LIST = "tiled_map_tile_layer_list";
	}

	public class TiledMap {
		public static final String KEY_WIDTH = "width";
		public static final String KEY_HEIGHT = "height";
	}

	public class Room {
		public static final String KEY_TYPE = "room_type";
		public static final String VAL_TYPE_CENTER = "center";
		public static final String VAL_TYPE_SCROLL_X = "scroll_x";
		public static final String VAL_TYPE_SCROLL_Y = "scroll_y";

		public static final String KEY_MUSIC = "room_music";
		public static final String KEY_VIEWOFFSET_Y = "room_view_offset_y";
		public static final String KEY_SCROLL_DIR = "room_scroll_direction";
		public static final String KEY_SCROLL_VEL = "room_scroll_velocity";
		public static final String KEY_SCROLL_BOUND_X = "room_scroll_bound_x";
		public static final String KEY_SCROLL_BOUND_Y = "room_scroll_bound_y";
		public static final String KEY_SCROLL_PUSHBOX = "room_scroll_push_box";
		public static final String KEY_SCROLL_KILLBOX = "room_scroll_kill_box";
		public static final String KEY_SPACEWRAP_X = "room_space_wrap_x";
	}

	public class Powerup {
		public static final String KEY_POWERUP_LIST = "powerup_list";
	}
}
