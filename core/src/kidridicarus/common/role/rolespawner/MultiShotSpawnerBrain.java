package kidridicarus.common.role.rolespawner;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import kidridicarus.agency.AgentRemovalListener.AgentRemovalCallback;
import kidridicarus.agency.tool.FrameTime;
import kidridicarus.agency.tool.ObjectProperties;
import kidridicarus.common.info.CommonKV;
import kidridicarus.common.info.QCC;
import kidridicarus.common.info.UInfo;
import kidridicarus.common.role.rolespawntrigger.RoleSpawnTrigger;
import kidridicarus.common.role.tiledmap.solidlayer.SolidTiledMapRole;
import kidridicarus.common.tool.Direction4;
import kidridicarus.story.Role;
import kidridicarus.story.RoleHooks;
import kidridicarus.story.tool.RP_Tool;

class MultiShotSpawnerBrain extends SpawnerBrain {
	private RoleSpawnerBody body;
	private Vector2 myBoundSize;
	private int multiCount;
	private int multiGrpCount;
	private float spawnRate;
	private Direction4 scrollDir;

	private int numSpawns;
	private int numSpawnsDisposed;
	private float spawnTimer;

	MultiShotSpawnerBrain(Role parentRole, RoleHooks parentRoleHooks, ObjectProperties properties,
			RoleSpawnerBody body) {
		super(parentRole, parentRoleHooks, properties);
		this.body = body;
		this.myBoundSize = RP_Tool.getBounds(properties).getSize(new Vector2());
		this.multiCount = properties.getInteger(CommonKV.Spawn.KEY_SPAWN_MULTI_COUNT, 1);
		this.multiGrpCount = properties.getInteger(CommonKV.Spawn.KEY_SPAWN_MULTI_GRP_COUNT, 1);
		this.spawnRate = properties.getFloat(CommonKV.Spawn.KEY_SPAWN_MULTI_RATE, 0f);
		this.scrollDir = properties.getDirection4(CommonKV.Spawn.KEY_SPAWN_SCROLL_DIR, Direction4.NONE);
		this.numSpawns = 0;
		this.numSpawnsDisposed = 0;
		this.spawnTimer = 0f;
	}

	@Override
	void update(FrameTime frameTime, boolean isEnabled) {
		// DEBUG: error state check
		if(numSpawnsDisposed > numSpawns)
			throw new IllegalStateException("numSpawnsDisposed ("+numSpawnsDisposed+" > numSpawns ("+numSpawns+")");
		// if not enabled or if all groups have been spawned then exit
		if(!isEnabled || numSpawns == multiCount * multiGrpCount)
			return;

		// if a spawn position exists and spawn is allowed then do spawn
		if(isSpawnAllowed()) {
			// if this spawner has a scroll direction property then get scroll spawn position
			Vector2 scrollSpawnPos = null;
			if(scrollDir != Direction4.NONE)
				scrollSpawnPos = getScrollSpawnPos();
			// if not scrolling, or if scrolling and spawn position is available, then do spawn
			if(scrollDir == Direction4.NONE || scrollSpawnPos != null) {
				numSpawns++;
				spawnTimer = 0f;
				if(scrollDir == Direction4.NONE)
					doSpawn(null);
				else {
					doSpawn(scrollSpawnPos, new AgentRemovalCallback() {
						@Override
						public void preAgentRemoval() { numSpawnsDisposed++; }
						@Override
						public void postAgentRemoval() {}	// this method is purposely empty
					});
				}
			}
		}
		spawnTimer += frameTime.timeDelta;
	}

	private Vector2 getScrollSpawnPos() {
		// get spawn trigger for scroll spawn position calculation, and exit if spawn trigger not found
		RoleSpawnTrigger spawnTrigger = body.getRoleSpawnTrigger();
		if(spawnTrigger == null)
			return null;
		Rectangle spawnTriggerBounds = RP_Tool.getBounds(spawnTrigger);
		if(spawnTriggerBounds == null)
			return null;

		// that's all folks!
		if(scrollDir != Direction4.UP)
			throw new IllegalStateException("do more code");

		// RoleSpawnTrigger is contacting, so X value is okay...
		// Check only Y bounds for overlap and where empty tiles available...
		Rectangle spawnerTiles = UInfo.RectangleM2T(QCC.rectSizePos(myBoundSize, body.getPosition()));
		Rectangle triggerTiles = UInfo.RectangleM2T(spawnTriggerBounds);
		// if top of RoleSpawnTrigger is at least as high as top of RoleSpawner then disallow spawn
		if(triggerTiles.y+triggerTiles.height >= spawnerTiles.y+spawnerTiles.height)
			return null;

		int topY = (int) (triggerTiles.y + triggerTiles.height-1);
		int bottomY = (int) spawnerTiles.y;
		Integer topNonSolidY = null;
		// tileX = tile X coordinate of middle of RoleSpawner
		int tileX = (int) (spawnerTiles.x + (spawnerTiles.width-1)/2);
		for(int tileY=topY; tileY >= bottomY;tileY--) {
			if(!isMapTileSolid(new Vector2(tileX, tileY))) {
				topNonSolidY = tileY;
				break;
			}
		}

		// if a "top" tile to use for spawning was not available, then return null to indicate no spawn pos found
		if(topNonSolidY == null)
			return null;
		// otherwise return spawn position
		return UInfo.VectorT2M(tileX, topNonSolidY);
	}

	private boolean isSpawnAllowed() {
		// if doing first spawn then don't wait
		if(numSpawns == 0)
			return true;
		// If doing second, third, fourth, etc. spawns then wait between spawns - 
		// If wait time has elapsed...
		else if(spawnTimer > spawnRate) {
			// if doing multiple spawn groups...
			if(multiGrpCount > 0) {
				// how many individuals have been spawned within current group?
				int numSpawnsCurrentGrp = Math.floorMod(numSpawns, multiCount);
				// If full group has been spawned then wait for last member of group to be disposed before
				// spawning next Role.
				if(numSpawnsCurrentGrp == 0) {
					if(numSpawns == numSpawnsDisposed)
						return true;
				}
				// if less than the full group has been spawned, then spawn another individual Role
				else if(numSpawnsCurrentGrp < multiCount)
					return true;
			}
			// Not doing multiple spawn groups; if less than the full group has been spawned, then spawn
			// another individual Role.
			else if(numSpawns < multiCount)
				return true;
		}
		return false;
	}

	private boolean isMapTileSolid(Vector2 tileCoords) {
		SolidTiledMapRole ctMap = body.getSolidTiledMap();
		return ctMap == null ? false : ctMap.isMapTileSolid(tileCoords); 
	}
}
