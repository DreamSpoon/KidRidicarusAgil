package kidridicarus.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import kidridicarus.common.info.CommonRoleClassList;
import kidridicarus.common.info.CommonInfo;
import kidridicarus.common.screen.InstructionsScreen;
import kidridicarus.game.KidIcarus.KidIcarusRoleClassList;
import kidridicarus.game.KidIcarus.KidIcarusAudio;
import kidridicarus.story.Story;
import kidridicarus.story.tool.RoleClassList;

/*
 * Main game asset loader class.
 */
public class MyKidRidicarus extends Game {
	public SpriteBatch batch;
	private TextureAtlas atlas;
	public AssetManager manager;
	public Story story;

	@Override
	public void create () {
		batch = new SpriteBatch();
		atlas = new TextureAtlas(CommonInfo.TA_MAIN_FILENAME);
		manager = new AssetManager();
		// other music files may be loaded later when a space is loaded
		manager.load(KidIcarusAudio.Music.PIT_DIE, Music.class);
		manager.load(KidIcarusAudio.Sound.Pit.HURT, Sound.class);
		manager.load(KidIcarusAudio.Sound.Pit.JUMP, Sound.class);
		manager.load(KidIcarusAudio.Sound.Pit.SHOOT, Sound.class);
		manager.load(KidIcarusAudio.Sound.General.HEART_PICKUP, Sound.class);
		manager.load(KidIcarusAudio.Sound.General.SMALL_POOF, Sound.class);
		manager.finishLoading();
		story = new Story(new RoleClassList(CommonRoleClassList.CORE_ROLE_CLASS_LIST,
				KidIcarusRoleClassList.KIDICARUS_ROLE_CLASSLIST), atlas);
		// show intro/instructions screen
		setScreen(new InstructionsScreen(this, CommonInfo.GAMEMAP_FILENAME2));
	}

	@Override
	public void dispose () {
		super.dispose();
		if(getScreen() != null)
			getScreen().dispose();
		story.dispose();
		batch.dispose();
		atlas.dispose();
		manager.dispose();
	}
}
