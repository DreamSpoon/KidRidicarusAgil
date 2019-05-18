package kidridicarus.common.info;

import kidridicarus.common.tool.DrawOrderAlias;

public class CommonInfo {
	public static final int V_WIDTH = 256;
	public static final int V_HEIGHT = 240;
	// DEBUG: used to quickly change size of screen on desktop without affecting aspect ratio
	public static final int DESKTOP_SCALE = 2;

	public static final String INSTRO_FILENAME = "Instro/map/Instro.tmx";
	public static final String GAMEMAP_FILENAME1 = "SMB1/map/SMB1-1.tmx";
	public static final String GAMEMAP_FILENAME2 = "KidIcarus/map/KidIcarus1-1.tmx";
	public static final String GAMEMAP_FILENAME3 = "Metroid/map/Metroid1-1.tmx";

	public static final String TA_MAIN_FILENAME = "sprite/KidRidicarusSprites.pack";

	public static class DrawOrder {
		public static final float UPDATE_CAMERA = -9000f;
		public static final float MAP_BACKGROUND = 0f;
		public static final float MAP_BOTTOM = 2f;
		public static final float MAP_MIDDLE = 4f;
		public static final float MAP_TOP = 6f;
		public static final float SPRITE_BOTTOM = 1f;
		public static final float SPRITE_MIDDLE = 3f;
		public static final float SPRITE_TOP = 5f;
		public static final float SPRITE_TOPFRONT = 7f;
		// it's not over 9000
		public static final float PLAYER_HUD = 9000f;
	}

	public static final DrawOrderAlias[] KIDRID_DRAWORDER_ALIAS = new DrawOrderAlias[] {
			new DrawOrderAlias("map_background", DrawOrder.MAP_BACKGROUND),
			new DrawOrderAlias("map_bottom", DrawOrder.MAP_BOTTOM),
			new DrawOrderAlias("map_middle", DrawOrder.MAP_MIDDLE),
			new DrawOrderAlias("map_top", DrawOrder.MAP_TOP),
			new DrawOrderAlias("sprite_bottom", DrawOrder.SPRITE_BOTTOM),
			new DrawOrderAlias("sprite_middle", DrawOrder.SPRITE_MIDDLE),
			new DrawOrderAlias("sprite_top", DrawOrder.SPRITE_TOP)
		};

	public static class UpdateOrder {
		// update first
		public static final float PRE_AGENCY_UPDATE = -9000f;
		public static final float PRE_MOVE_UPDATE = 0f;
		public static final float MOVE_UPDATE = 1f;
		public static final float POST_MOVE_UPDATE = 2f;
		public static final float POST_AGENCY_UPDATE = 9000f;
		// update last
	}

	/*
	 * Returns 0 or a positive value.
	 * Used to check that the time passed to animation's getKeyFrame is positive, even when the time is
	 * running backwards.
	 */
	public static float ensurePositive(float original, float delta) {
		if(original >= 0f)
			return original;

		if(delta == 0f)
			return 0f;
		return (float) (original + (-Math.floor(original / delta))*delta);
	}
}
