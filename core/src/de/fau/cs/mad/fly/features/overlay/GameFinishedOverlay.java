package de.fau.cs.mad.fly.features.overlay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

import de.fau.cs.mad.fly.Fly;
import de.fau.cs.mad.fly.I18n;
import de.fau.cs.mad.fly.Loader;
import de.fau.cs.mad.fly.features.IFeatureFinish;
import de.fau.cs.mad.fly.features.IFeatureInit;
import de.fau.cs.mad.fly.game.GameController;
import de.fau.cs.mad.fly.profile.PlayerProfileManager;
import de.fau.cs.mad.fly.profile.Score;
import de.fau.cs.mad.fly.profile.ScoreDetail;
import de.fau.cs.mad.fly.profile.ScoreManager;
import de.fau.cs.mad.fly.res.Assets;
import de.fau.cs.mad.fly.res.Level;
import de.fau.cs.mad.fly.ui.UI;

/**
 * Optional Feature to display a start and a finish message to the player.
 * 
 * @author Tobias Zangl
 */
public class GameFinishedOverlay implements IFeatureInit, IFeatureFinish {
    private GameController gameController;
    private final Skin skin;
    private final Stage stage;
    private Score newScore;
    
    public GameFinishedOverlay(final Skin skin, final Stage stage) {
        this.stage = stage;
        this.skin = skin;
    }
    
    @Override
    public void init(final GameController gameController) {
        this.gameController = gameController;
        
    }
    
    /**
     * When the game is finished, 3 states are possible:
     * <p>
     * 1) finished in time, not dead: message, score and back button are shown
     * <p>
     * 2) time is up, no score: only message and back button are shown
     * <p>
     * 3) spaceship broken: only message and back button are shown
     */
    @Override
    public void finish() {
    	Level.Head lastLevel = PlayerProfileManager.getInstance().getCurrentPlayerProfile().getCurrentLevel();
    	PlayerProfileManager.getInstance().getCurrentPlayerProfile().setLastLevel(lastLevel);
    	
        Table outerTable = new Table();
        outerTable.setFillParent(true);
        
        TextButtonStyle textButtonStyle = skin.get(UI.Buttons.DEFAULT_STYLE, TextButtonStyle.class);
        TextButton backToMainMenuButton = new TextButton(I18n.t("back.to.menu"), textButtonStyle);
        backToMainMenuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ((Fly) Gdx.app.getApplicationListener()).setMainMenuScreen();
            }
        });
        
        Label infoLabel;
        
        final Table messageTable = new Table();
        NinePatchDrawable background = new NinePatchDrawable(skin.get("grey-progress-bar", NinePatch.class));
        messageTable.setBackground(background);
        
        if (gameController.getLevel().getGateCircuit().isReachedLastGate()) {
            infoLabel = new Label(I18n.t("level.congratulations"), skin);
            messageTable.add(infoLabel);
            //Score newScore = gameController.getLevel().getScore();
            newScore = gameController.getScoreController().getEndScore(gameController);
            String scoreString = I18n.t("newScore") + newScore.getTotalScore();
            final Label scoreLabel = new Label(scoreString, skin);
            messageTable.add(scoreLabel).pad(15f);
            messageTable.row().expand();
            for (ScoreDetail detail : newScore.getScoreDetails()) {
                messageTable.row().expand();
                messageTable.add(new Label(I18n.t(detail.getDetailName()), skin)).pad(6f).uniform();
                messageTable.add(new Label(detail.getValue(), skin)).pad(6f).uniform();
            }
            Score tmpScore = ScoreManager.getInstance().getCurrentLevelBestScore();
            if ( tmpScore == null || newScore.getTotalScore() > tmpScore.getTotalScore()) {
            	new Thread(new Runnable() {
        			@Override
        			public void run() {
        				ScoreManager.getInstance().saveBestScore(newScore);        				
        			}
        		}).start();

                messageTable.row().expand();
                messageTable.add(new Label(I18n.t("newRecord"), skin)).pad(6f).uniform();
            }
            messageTable.row().expand();
            TextButton nextLevelButton = new TextButton(I18n.t("nextLevel"), textButtonStyle);
            nextLevelButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    // unload old level if it is not the same as the last level
                    if(!PlayerProfileManager.getInstance().getCurrentPlayerProfile().nextLevel()) {
                    	String levelPath = PlayerProfileManager.getInstance().getCurrentPlayerProfile().getLastLevel().file.path();
                        Assets.unload(levelPath);
                    }

                    // set and load new level
                    Level.Head levelHead = PlayerProfileManager.getInstance().getCurrentPlayerProfile().getCurrentLevel();
                    Loader.loadLevel(levelHead);
                }
            });
            messageTable.add(nextLevelButton).pad(UI.Buttons.SPACE_WIDTH);
            messageTable.add(backToMainMenuButton).pad(UI.Buttons.SPACE_WIDTH);
        } else if (gameController.getPlayer().isDead()) {
            infoLabel = new Label(I18n.t("ship.destroyed"), skin);
            messageTable.add(infoLabel).colspan(2);
            
            TextButton restartButton = new TextButton(I18n.t("restart"), textButtonStyle);
            restartButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                	// unload old level
                	String levelPath = PlayerProfileManager.getInstance().getCurrentPlayerProfile().getLastLevel().file.path();
                	Assets.unload(levelPath);
                	
                	// reload the level
                    Level.Head levelHead = PlayerProfileManager.getInstance().getCurrentPlayerProfile().getCurrentLevel();
                    Loader.loadLevel(levelHead);
                }
            });
            messageTable.row().expand();
            messageTable.add(restartButton).pad(UI.Buttons.SPACE_WIDTH);
            messageTable.add(backToMainMenuButton).pad(UI.Buttons.SPACE_WIDTH);
            messageTable.row().expand();
        }
        
        outerTable.add(messageTable).center();
        stage.addActor(outerTable);
    }
}
